package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import net.kkangsworld.lvmexec.pipeWithLVM;

public class LVSizeObserver extends Thread {
	
	public static final String LOGTAG = "LVSizeOb";
	public static final int STEP_SEC = 10 * 1000;	// 몇 초마다 check할 것인지 설정

	private static final int ONLY_NUMBER_OF_LV = 3;	//테스트로 tempLV도 넣기 위해 4개로 잡음
	private static boolean RESIZE_SH_GENERATE_COMPLETED = false;	//resize용 shellscript 생성이 완료 되었으면 complete 한다.
	
	/* df파싱 데이터 */
	private static final int VOLUME_NAME = 0;
	private static final int SIZE_TOTAL = 1;
	private static final int SIZE_USED = 2;
	private static final int SIZE_FREE = 3;
	private static final int SIZE_BLOCK = 4;
	private static final int SIZE_PERCENT = 5; //splite에서 string array를 재정의하기에 쓸 수 없음
	
	public static final float ExecuteTHRESHOLD = 0.8f;		//extend 실행한 임계치
	public static final float ExpandSizePercent = 1.15f;		//얼만큼 확장할 것인지 -- 15%확장
	public static final float ReduceSizePercent = 0.85f;	//줄일 것인지 -- 15% 축소
	public static final float MyFullSizePercent = 0.95f;	//내 최대 용량도 전체 12G의 0.95%까지로 한정
	public static final float MyFullSizeVolume = 8192.0f;
	
	private static final String LV_DEV_PATH = "/dev/vg";
	private static final String LV_NAME_USERDATA = "/dev/vg/userdata";
	private static final String LV_NAME_USERSDCARD = "/dev/vg/usersdcard";
	private static final String DF_NAME_DATA = "/data/userdata";
	private static final String DF_NAME_DATA_MEDIA = "/data/usersdcard";
	
	private String targetVolumeName = "";
	private float targetExtendSize = 0.0f;
	private String[][] parsedVolume = new String[ONLY_NUMBER_OF_LV][5]; 
	
	private String[] opposeVolume;
	private String[] targetVolume;
	
	private pipeWithLVM m_pipeWithLVM;
	//private Handler rh; 						//LVM과 통신할 Handler
	private Handler observHandler; 					//디스크 체크 현황을 받게될 Handler
	private java.util.Timer checkTimer;				//STEP_SEC마다 checkTimer로 체크한다. (repeat)
	private Handler 	/* pipe readHandler */
	rh = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				Log.i(LOGTAG, ("rawMsg : "+(String)msg.obj));
				String readit = (String)msg.obj;
				//리턴값을 받아서 Service에서 호출한 observHandler에 결과를 던진다.
				//what =1 성공 =0은 실패 -1은 기타오류
					if(readit.contains("successfully")) {
						Log.d(LOGTAG, "return success");
						
						//성공했기에 리붓후 resize.sh을 실행하기 위해 Generate한다.
						Log.i(LOGTAG, "SSG target :"+targetVolumeName+"/size:"+targetExtendSize);
						ShellScriptGenerator ssg = 
								new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
								"/dev/vg"+targetVolumeName, targetExtendSize);
						
						
						msg.what = 1;
					}
					
					/**
					 * LV확장이 안되었으므로 VG가 꽉찬 것이다.
					 * 따라서 다른 것을 줄이기 위해 줄이려는 타겟이,
					 * userdata/sdcard 확인을 하고 반대것 용량 계산을 한다.
					 */
					else if(readit.contains("Insufficient")){
						Log.d(LOGTAG, "return insufficient");
						
						//반대(oppose)타겟이 opposeVolume에 들어가 있음
						//용량 계산 및 줄이기 실행
						ReduceSizeOfLV(opposeVolume);

						msg.what = 0;
						//sdcard의 용량증가 요청이면 userdata의 임계치 다시 확인하고 증가
						//반대의 경우면 반대로
						/* 용량 줄이는 메소드 실행 */
						/* 용량 증가 메소드 실행 */
					}
					else if(readit.contains("Rounding size")) {
						Log.d(LOGTAG, "equally size. no anything work.");
						/*Log.i(LOGTAG, "SSG target :"+targetVolumeName+"/size:"+targetExtendSize);
						ShellScriptGenerator ssg = 
								new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
								"/dev/vg"+targetVolumeName, targetExtendSize);*/
					}
					else if(readit.contains("[in run_command]")) {
						Log.d(LOGTAG, ""+readit);
					}
					else {
						msg.what = -1;
						Log.e(LOGTAG, "no recognized error\n"+readit);
					}
					
					/* 모든 스크립트 생성 완료 후 copytoinitvol을 한다. */
					Log.i(LOGTAG, "in execute copystart");
					m_pipeWithLVM.ActionCopystartPipe();
					
				/**
				 * 파싱한 데이터에서 현재 용량과 Data Usage를 파싱하고 우리의 THRESHOLD기준하여 계산된 값을 넘을경우 EXTEND
				 * THRESHOLD는 두가지. 
				 * 일단 용량은 총용량의 95%를 기준 (10G * 0.95) -- MyFullSize
				 * 
				 * ExecuteTHRESHOLD = 80% (10G * 0.95 * 0.8)
				 * 
				 * ExtendSize = 15% ((10G * 0.95 * 0.8) * 1.15) 
				 * --> ExtendSize만큼 증가시켰는데 전체용량을 넘을 경우,
				 * 사용자의 옵션 설정에 따라 경고를 띄우거나 기존 백업된 SnapShot을 지우거나 할 수 있도록 한다.
				 * 
				 * >>Extend는 해당 공간 확장이 필요한 Threshold때 일어난다.
				 * >>Extend시 다른 것 reduce 우선순위
				 * 1) snapshot제거 2) /usersdcard 3) /userdata --> /usersystem은 고려하지 않음
				 * */
				
				
				//클래스를 호출한 서비스로 msg전달
				msg.obtain();
				msg.obj = readit;
				msg.setTarget(observHandler);
				break;
				
			default:
				Log.e(LOGTAG, "readHandler error");
				break;
					
			}
		}
	};

	
	
	//constructor
	public LVSizeObserver() {
		m_pipeWithLVM = new pipeWithLVM(rh);
		DeleteTheExistResizeSH();
	}
	
	public LVSizeObserver(Handler observHandler) {
		m_pipeWithLVM = new pipeWithLVM(rh);
		this.observHandler = observHandler;
		DeleteTheExistResizeSH();
		
	}
	
	public LVSizeObserver(Handler observHandler, Handler rh) {
		m_pipeWithLVM = new pipeWithLVM(rh);
		this.observHandler = observHandler;
		this.rh = rh;
		DeleteTheExistResizeSH();
	}
	
	private void DeleteTheExistResizeSH() {
		File resize_sh  = new File("/data/data/com.example.timetraveler/resize.sh");
		if(resize_sh.exists()) {
			resize_sh.delete();
			Log.w(LOGTAG, "deleted the already exist resiz.sh");
		}
	}
	
	/* df를 체크하고 리턴되는 데이터를, StringBuilder로 묶으면서 파싱이 용이 하도록 하여 저장 */
	private void CommandCheckSize() {
		String cmdOfdf = "df";
		StringBuilder cmdReturn = new StringBuilder();
		
		  try {
		   ProcessBuilder processBuilder = new ProcessBuilder(cmdOfdf);
		   Process process = processBuilder.start();
		   
		   InputStream inputStream = process.getInputStream();
		   int c = 0;
		   char before_c = (char)c;
			   while ((c = inputStream.read()) != -1) {
				   //직전의 문자가 공백이 아니고 현재의 문자가 공백이면 $입력 
				   if(before_c != ' ' && ((char)c) == ' ') {
					   cmdReturn.append(":");
				   }
				   //직전의 문자와 현재 문자 둘 다 공백일 때는 pass
				   else if(before_c == ' ' && ((char)c) == ' ') {
					   continue;
				   }
				   else
					   cmdReturn.append((char) c);
				   //이전 문자 저장
				   before_c = (char)c;
			   }
			 //call on parseDfSize(cmdReturn);
			   CheckWithUsagePercent(parseDFSize(cmdReturn)); //String[][] 반환된 parseDFSize데이터를 다시 Threshold체크한다.
			 
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		   }
		 
	}
	
	/* 리턴된 df데이터를 파싱하여 String[][]로 반환 */
	private String[][] parseDFSize(StringBuilder cmdReturn) {
		String[] BeforeparsedString;
		//String[] MiddleparsedString = new String[ONLY_NUMBER_OF_LV];
		String[][] AfterparsedString = new String[ONLY_NUMBER_OF_LV][5]; //[Name][Total][Used][Free][Block][PERCENT]
		
		BeforeparsedString = cmdReturn.toString().split("\n");
		//Log.d(LOGTAG, "parsedString : "+cmdReturn.toString());
		
		int index = 0;
		/* 해당 path로 Parsing하기 /data 부터 시작하면 계속해서 걸리기 때문에 제일 나중에 찾는다. */
		for(int i=0;i<BeforeparsedString.length;i++) {

			if(BeforeparsedString[i].contains("/data:")) {
				Log.d(LOGTAG, "is /data? "+BeforeparsedString[i]);
				//MiddleparsedString[index] = BeforeparsedString[i];
				AfterparsedString[index] = BeforeparsedString[i].split(":");
				index++;
			}
			
			else if(BeforeparsedString[i].contains("/data/media:")) {
				Log.d(LOGTAG, "is /data/media? "+BeforeparsedString[i]);
				//MiddleparsedString[index] = BeforeparsedString[i];
				AfterparsedString[index] = BeforeparsedString[i].split(":");
				index++;
			}
			
			else if(BeforeparsedString[i].contains("/data/usersystem:")) {
				Log.d(LOGTAG, "is /data/usersystem? "+BeforeparsedString[i]);
				//MiddleparsedString[index] = BeforeparsedString[i];
				AfterparsedString[index] = BeforeparsedString[i].split(":");
				index++;
			}
			
			/*else if(BeforeparsedString[i].contains("/data/tempLV:")) {
				Log.d(LOGTAG, "is /data/tempLV? "+BeforeparsedString[i]);
				AfterparsedString[index] = BeforeparsedString[i].split(":");
				index++;
			}*/
			//init PERCENT
			//AfterparsedString[index][SIZE_PERCENT] = "0";
			
			//우리가 필요로 하는 것은 3개, 3개가 다 차게 되는 경우는 맨마지막 배열의 맨 마지막 열까지 차게되면 다 가득 찬 것이다.
			//따라서 for문 더 돌릴 필요가 없으므로 break;
			if(AfterparsedString[ONLY_NUMBER_OF_LV-1][SIZE_BLOCK] != null)
				break;
		}
		
		/* 안에 문자열들은 제거하고 숫자만 남긴다. GB->MB단위로 만들어 준다 */
		int i=0;
		for(i=0;i < AfterparsedString.length;i++) {
			int j=0;
			for(j=0;j < AfterparsedString[i].length;j++) {
				
				//Giga 단위면 * 1024함
				if(AfterparsedString[i][j].contains("G")) {
					float toMega = Float.parseFloat(
										AfterparsedString[i][j].
										substring(0, AfterparsedString[i][j].length()-1)
										) * 1024;
					AfterparsedString[i][j] = String.valueOf(toMega);
				}
				
				//Mega 단위이면 파싱만
				else if(AfterparsedString[i][j].contains("M")) {
					AfterparsedString[i][j] = AfterparsedString[i][j].
										substring(0, AfterparsedString[i][j].length()-1);
				}
				
				// mount /data -> /data/userdata, /data/media -> /data/usersdcard
				if(AfterparsedString[i][j].contentEquals("/data"))
					AfterparsedString[i][j] = "/data/userdata";
				else if(AfterparsedString[i][j].contentEquals("/data/media"))
					AfterparsedString[i][j] = "/data/usersdcard";
			}
			
			//Log.d(LOGTAG, "now i = "+i);
			
			/* Percent 계산 */
			/*String PercentOfUsage = String.valueOf(
						Float.parseFloat(AfterparsedString[i][SIZE_USED]) / 
						Float.parseFloat(AfterparsedString[i][SIZE_TOTAL])
						);
			Log.d(LOGTAG, "temp : "+PercentOfUsage);*/
			
			//split하면서 string array가 split사이즈 만큼으로 변형된다. 그래서 추가안됨
			//AfterparsedString[i][SIZE_PERCENT] = temp;
		}
		this.parsedVolume = AfterparsedString;
		return AfterparsedString;
	}
	
	/* Only Use My Splited :: input_data[0] = USED, input_data[1] = TOTAL */
	private float CheckWithUsagePercent(String[][] input_data) {
		float PercentOfUsage = 0;
		/* Percent 계산 */
		
		for(int i=0;i<input_data.length;i++) {
			PercentOfUsage = Float.parseFloat(input_data[i][SIZE_USED]) / 
						Float.parseFloat(input_data[i][SIZE_TOTAL]);
			Log.d(LOGTAG, input_data[i][VOLUME_NAME]+"_PercentOfUsage : "+PercentOfUsage);
			
			/* 체크한 사용량이 임계치보다 클 경우에만 작동!!!! */
			if(PercentOfUsage >= ExecuteTHRESHOLD) {
				/* sdcard가 타겟이면 userdata를, userdata가 타겟이면 sdcard를 반대것 -- opposeVolume으로 추가 */
				if(input_data[i][VOLUME_NAME].equals(DF_NAME_DATA)) {
					this.opposeVolume = input_data[1];
					Log.d(LOGTAG, "reduce oppose를 위한, extend타겟 저장 : "+input_data[i][VOLUME_NAME].substring(5));
				}
				else if(input_data[i][VOLUME_NAME].equals(DF_NAME_DATA_MEDIA)) {
					this.opposeVolume = input_data[0];
					Log.d(LOGTAG, "reduce oppose를 위한 extend타겟 저장 : "+input_data[i][VOLUME_NAME].substring(5));
				}
				
				/* 임계치 넘었음이 확인되었을 때
				 * Resize sh생성이 완료되었었는지 체크한다. 
				 * 완료되어 있지 않으면 완료 flag on하고 실행한다.
				 * 완료되었으면 하지 않는다. */
				if(!RESIZE_SH_GENERATE_COMPLETED) {
					Log.i(LOGTAG, "not generated ShellScript. START GENERATING");
					RESIZE_SH_GENERATE_COMPLETED = true;
					ExtendSizeOfLV(input_data[i], input_data);
				} else
					Log.i(LOGTAG, "Already generated ShellScript. STOP GENERATING");
			}
			Log.i(LOGTAG, "Don't over ExtendThreshold! :) so not generate ShellScript :)");
		}
		return PercentOfUsage;
	}
	
	/* 체크한 용량이 임계치보다 큰 경우, command에 의해 용량을 지정한 사이즈 만큼 증가 시킨다. */
	private void ExtendSizeOfLV(String[] targetVolume, String[][] other_Volume) {
		String cmdOfdf;
		StringBuilder cmdReturn;
		
		//target의 확장하려는 사이즈 1보다 크면 그 사이즈로 아예 만드는 것이고 1보다 작은 값이면 그만큼을 증가하는 것이다. 결국 같음
		float targetExtendSize = (Float.parseFloat(targetVolume[SIZE_TOTAL]) * ExpandSizePercent);
		targetExtendSize = Math.round(targetExtendSize);
		/**
			리턴된 값이 Success면 상관없이 PE가 부족하다면 우선순위에 의해 다른 것들을 지우거나 줄여야 한다.
			그리고 줄일 수 있는 minimum들도 찾아서 줄일 수 있게해야 한다.
		*/	
		/* Step1. 먼저 임의로 증가 시도 -- 증가가 가능한지 체크한다. 가능할 경우 리붓해서 resize partition만 해주기 위해 Step2로 간다. */
		/* 일단 전역변수에 던져 놓는다. */
		this.targetExtendSize = targetExtendSize;
		this.targetVolumeName = targetVolume[VOLUME_NAME].substring(5); 
		this.targetVolume = targetVolume;
		//일단 반대 것도 미리 static 저장
		
		//용량이 8G가 넘어가는 것을 막기위해 8G이상 extend하려고 하면 일부러 더 크게 Extend요청한다.
		//여기서 Snapshot옵션을 읽어서 Snapshot을 지우고 Extend를 원하면 8G까지 확장하게 해주는 조건도 추가해야한다.
		//지금은 Max8G까지 (Snapshot지워진 것을 가정)해서 조건 추가됨
		//if(targetExtendSize < MyFullSizeVolume) {
		if(targetExtendSize < 5000) {
			Log.d(LOGTAG, "targetExtend Size is SMALLER THAN 5000!");
			Log.d(LOGTAG, "Will Extend Size? "+targetExtendSize+"\ntarget name : "+targetVolume[VOLUME_NAME].substring(5));
			/* target Extend */
			m_pipeWithLVM.ActionWritePipe("lvextend -n /dev/vg"+targetVolume[VOLUME_NAME].substring(5)+" -L"+Math.round(targetExtendSize)+"M");
			Log.d(LOGTAG, "lvresize -n /dev/vg"+targetVolume[VOLUME_NAME].substring(5)+" -L"+Math.round(targetExtendSize)+"M");
		}
		else {
			Log.d(LOGTAG, "targetExtend Size is BIGGER THAN 5000!");
			/* target Extend */
			m_pipeWithLVM.ActionWritePipe("lvextend -n /dev/vg"+targetVolume[VOLUME_NAME].substring(5)+" -L"+99999+"M");
			Log.d(LOGTAG, "lvresize -n /dev/vg"+targetVolume[VOLUME_NAME].substring(5)+" -L"+Math.round(targetExtendSize)+"M");
		}
		
		/**
		//리턴된 결과 Handler처리 필요
		//리턴되었을 때 lvextend가 되면 그냥 진행
		//안되었다고 할 때는 옵션체크
		//우선적으로는 다른 파티션 reduce&extend
		//용량체크하였는데 여유공간이 없을 경우에는 STEP3
		//체크된 옵션이 스냅샷 제거일 경우 스냅샷 제거 후 다시 lvextend
		//lvextend -n /dev/vg/usersdcard -L +100M
		//lvm에서 y버튼 눌러야 하므로 안누르게 변경 필요할 수도 있지만 ubuntu상에서 테스트 했을 때는 안눌러도 되었음
		*/
		
		
		/* Step2. 다른 것들을 reduce하기 위해 어떻게 할 것인지 결정ㅈ하고 Shell Scripting으로 정보를 전달한다. */
		/**
		 * 용량을 줄일 때에는,
		 * e2fsck -f /dev/vg/tempLV
		 * resize2fs /dev/vg/tempLV 200M
		 * lvreduce /dev/vg/tempLV -L 200M 으로, 
		 * 먼저 파티션을 줄이고 lv를 줄인다.
		 * 
		 * 반대로 용량을 늘릴 때에는,
		 * lv부터 늘리고 파티션을 늘린다.
		 */

	}
	
	private void ReduceSizeOfLV(String[] opposeVolume) {
		float opposeReduceSize = (Float.parseFloat(opposeVolume[SIZE_TOTAL]) * ReduceSizePercent);
		
		//target의 확장하려는 사이즈 1보다 크면 그 사이즈로 아예 만드는 것이고 1보다 작은 값이면 그만큼을 증가하는 것이다. 결국 같음
		//opposeVolume타겟은 Reduce, 실질 (확장)타겟은 extend 실행
		Log.d(LOGTAG, "Will Reduce Size? "+opposeReduceSize+"\nreduce target name : "+opposeVolume[VOLUME_NAME].substring(5));
		Log.e(LOGTAG, "opposeVolume name : "+opposeVolume[VOLUME_NAME].substring(5));
				ShellScriptGenerator ssg_reduce = 
						new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_REDUCE, 
						"/dev/vg"+opposeVolume[VOLUME_NAME].substring(5), opposeReduceSize);
				
		//확장 타겟이 늘리려는 용량이, 축소할 oppose의 용량보다 작으면 그냥 해도됨
		//targetVolumeName, targetExtendSize는 전역변수!!!
		if(Float.parseFloat(opposeVolume[SIZE_FREE]) > this.targetExtendSize) {
			opposeReduceSize = Math.round(opposeReduceSize);
			
			ShellScriptGenerator ssg_extend = 
					new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
					"/dev/vg"+targetVolumeName, targetExtendSize);
		}
		
		//그러나 확장하려는 용량이 줄이려는 용량보다 크면, 줄인 만큼만 확장시켜줌 
		else {
			float myTempSize = Math.round((Float.parseFloat(opposeVolume[SIZE_TOTAL])-opposeReduceSize)
								+Float.parseFloat(targetVolume[SIZE_TOTAL]));
			Log.d(LOGTAG, "Will Reduce at Extend Size? "+myTempSize+"\nextend target name : "+targetVolumeName);
			Log.e(LOGTAG, "in reduce for extend Volume name : "+targetVolume[VOLUME_NAME].substring(5));
		//확장도 opposeReduceSize만큼 확장 한다.
		ShellScriptGenerator ssg_extend = 
				new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
				"/dev/vg"+targetVolume[VOLUME_NAME].substring(5), myTempSize);
		}
	}
	
	public void CheckingStart(boolean isRun) {
		//checkTimer = new Timer();
		
		//현재 타이머가 돌고 있는지 체크, 돌고 있으면 종료시키고 새로 시작
		if(checkTimer != null) {
			checkTimer.cancel();
			Log.d(LOGTAG, "existed Timer cancel!");
		}
		
		checkTimer = new java.util.Timer();
		//시작하는 인자 isRun == true
		if(isRun) {	
			TimerTask checktask = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Log.w(LOGTAG, "Checking LVSize Observer, Time schedule start!");
						//m_pipeWithLVM.ActionWritePipe("lvdisplay -c");
						CommandCheckSize();
							
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			
			//period start
			checkTimer.scheduleAtFixedRate(checktask, STEP_SEC/10, STEP_SEC); //task, delay, period
			
			
			
		}
		
		//종료하는 인자 isRun == false
		else if(!isRun) {
			checkTimer.cancel();
			checkTimer = null;
			Log.d(LOGTAG, "Timer was force canceled");
			
		}
	
	}
	
	public String[] parseLVdisplay_Colons(String rawString) {
		String[] resultString = null;
		resultString = rawString.split(":"); // 구분자 : 로 구분해서 배열에 저장
		
		return resultString;
	}
	
	public void run() {
		
		Log.i(LOGTAG, "Thread start");
		CheckingStart(true);
	}


}
