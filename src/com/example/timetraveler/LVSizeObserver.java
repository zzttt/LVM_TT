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
	public static final int STEP_SEC = 10 * 1000;	// �� �ʸ��� check�� ������ ����

	private static final int ONLY_NUMBER_OF_LV = 3;	//�׽�Ʈ�� tempLV�� �ֱ� ���� 4���� ����
	private static boolean RESIZE_SH_GENERATE_COMPLETED = false;	//resize�� shellscript ������ �Ϸ� �Ǿ����� complete �Ѵ�.
	
	/* df�Ľ� ������ */
	private static final int VOLUME_NAME = 0;
	private static final int SIZE_TOTAL = 1;
	private static final int SIZE_USED = 2;
	private static final int SIZE_FREE = 3;
	private static final int SIZE_BLOCK = 4;
	private static final int SIZE_PERCENT = 5; //splite���� string array�� �������ϱ⿡ �� �� ����
	
	public static final float ExecuteTHRESHOLD = 0.8f;		//extend ������ �Ӱ�ġ
	public static final float ExpandSizePercent = 1.15f;		//��ŭ Ȯ���� ������ -- 15%Ȯ��
	public static final float ReduceSizePercent = 0.85f;	//���� ������ -- 15% ���
	public static final float MyFullSizePercent = 0.95f;	//�� �ִ� �뷮�� ��ü 12G�� 0.95%������ ����
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
	//private Handler rh; 						//LVM�� ����� Handler
	private Handler observHandler; 					//��ũ üũ ��Ȳ�� �ްԵ� Handler
	private java.util.Timer checkTimer;				//STEP_SEC���� checkTimer�� üũ�Ѵ�. (repeat)
	private Handler 	/* pipe readHandler */
	rh = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 0:
				Log.i(LOGTAG, ("rawMsg : "+(String)msg.obj));
				String readit = (String)msg.obj;
				//���ϰ��� �޾Ƽ� Service���� ȣ���� observHandler�� ����� ������.
				//what =1 ���� =0�� ���� -1�� ��Ÿ����
					if(readit.contains("successfully")) {
						Log.d(LOGTAG, "return success");
						
						//�����߱⿡ ������ resize.sh�� �����ϱ� ���� Generate�Ѵ�.
						Log.i(LOGTAG, "SSG target :"+targetVolumeName+"/size:"+targetExtendSize);
						ShellScriptGenerator ssg = 
								new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
								"/dev/vg"+targetVolumeName, targetExtendSize);
						
						
						msg.what = 1;
					}
					
					/**
					 * LVȮ���� �ȵǾ����Ƿ� VG�� ���� ���̴�.
					 * ���� �ٸ� ���� ���̱� ���� ���̷��� Ÿ����,
					 * userdata/sdcard Ȯ���� �ϰ� �ݴ�� �뷮 ����� �Ѵ�.
					 */
					else if(readit.contains("Insufficient")){
						Log.d(LOGTAG, "return insufficient");
						
						//�ݴ�(oppose)Ÿ���� opposeVolume�� �� ����
						//�뷮 ��� �� ���̱� ����
						ReduceSizeOfLV(opposeVolume);

						msg.what = 0;
						//sdcard�� �뷮���� ��û�̸� userdata�� �Ӱ�ġ �ٽ� Ȯ���ϰ� ����
						//�ݴ��� ���� �ݴ��
						/* �뷮 ���̴� �޼ҵ� ���� */
						/* �뷮 ���� �޼ҵ� ���� */
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
					
					/* ��� ��ũ��Ʈ ���� �Ϸ� �� copytoinitvol�� �Ѵ�. */
					Log.i(LOGTAG, "in execute copystart");
					m_pipeWithLVM.ActionCopystartPipe();
					
				/**
				 * �Ľ��� �����Ϳ��� ���� �뷮�� Data Usage�� �Ľ��ϰ� �츮�� THRESHOLD�����Ͽ� ���� ���� ������� EXTEND
				 * THRESHOLD�� �ΰ���. 
				 * �ϴ� �뷮�� �ѿ뷮�� 95%�� ���� (10G * 0.95) -- MyFullSize
				 * 
				 * ExecuteTHRESHOLD = 80% (10G * 0.95 * 0.8)
				 * 
				 * ExtendSize = 15% ((10G * 0.95 * 0.8) * 1.15) 
				 * --> ExtendSize��ŭ �������״µ� ��ü�뷮�� ���� ���,
				 * ������� �ɼ� ������ ���� ��� ���ų� ���� ����� SnapShot�� ����ų� �� �� �ֵ��� �Ѵ�.
				 * 
				 * >>Extend�� �ش� ���� Ȯ���� �ʿ��� Threshold�� �Ͼ��.
				 * >>Extend�� �ٸ� �� reduce �켱����
				 * 1) snapshot���� 2) /usersdcard 3) /userdata --> /usersystem�� ������� ����
				 * */
				
				
				//Ŭ������ ȣ���� ���񽺷� msg����
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
	
	/* df�� üũ�ϰ� ���ϵǴ� �����͸�, StringBuilder�� �����鼭 �Ľ��� ���� �ϵ��� �Ͽ� ���� */
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
				   //������ ���ڰ� ������ �ƴϰ� ������ ���ڰ� �����̸� $�Է� 
				   if(before_c != ' ' && ((char)c) == ' ') {
					   cmdReturn.append(":");
				   }
				   //������ ���ڿ� ���� ���� �� �� ������ ���� pass
				   else if(before_c == ' ' && ((char)c) == ' ') {
					   continue;
				   }
				   else
					   cmdReturn.append((char) c);
				   //���� ���� ����
				   before_c = (char)c;
			   }
			 //call on parseDfSize(cmdReturn);
			   CheckWithUsagePercent(parseDFSize(cmdReturn)); //String[][] ��ȯ�� parseDFSize�����͸� �ٽ� Thresholdüũ�Ѵ�.
			 
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		   }
		 
	}
	
	/* ���ϵ� df�����͸� �Ľ��Ͽ� String[][]�� ��ȯ */
	private String[][] parseDFSize(StringBuilder cmdReturn) {
		String[] BeforeparsedString;
		//String[] MiddleparsedString = new String[ONLY_NUMBER_OF_LV];
		String[][] AfterparsedString = new String[ONLY_NUMBER_OF_LV][5]; //[Name][Total][Used][Free][Block][PERCENT]
		
		BeforeparsedString = cmdReturn.toString().split("\n");
		//Log.d(LOGTAG, "parsedString : "+cmdReturn.toString());
		
		int index = 0;
		/* �ش� path�� Parsing�ϱ� /data ���� �����ϸ� ����ؼ� �ɸ��� ������ ���� ���߿� ã�´�. */
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
			
			//�츮�� �ʿ�� �ϴ� ���� 3��, 3���� �� ���� �Ǵ� ���� �Ǹ����� �迭�� �� ������ ������ ���ԵǸ� �� ���� �� ���̴�.
			//���� for�� �� ���� �ʿ䰡 �����Ƿ� break;
			if(AfterparsedString[ONLY_NUMBER_OF_LV-1][SIZE_BLOCK] != null)
				break;
		}
		
		/* �ȿ� ���ڿ����� �����ϰ� ���ڸ� �����. GB->MB������ ����� �ش� */
		int i=0;
		for(i=0;i < AfterparsedString.length;i++) {
			int j=0;
			for(j=0;j < AfterparsedString[i].length;j++) {
				
				//Giga ������ * 1024��
				if(AfterparsedString[i][j].contains("G")) {
					float toMega = Float.parseFloat(
										AfterparsedString[i][j].
										substring(0, AfterparsedString[i][j].length()-1)
										) * 1024;
					AfterparsedString[i][j] = String.valueOf(toMega);
				}
				
				//Mega �����̸� �Ľ̸�
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
			
			/* Percent ��� */
			/*String PercentOfUsage = String.valueOf(
						Float.parseFloat(AfterparsedString[i][SIZE_USED]) / 
						Float.parseFloat(AfterparsedString[i][SIZE_TOTAL])
						);
			Log.d(LOGTAG, "temp : "+PercentOfUsage);*/
			
			//split�ϸ鼭 string array�� split������ ��ŭ���� �����ȴ�. �׷��� �߰��ȵ�
			//AfterparsedString[i][SIZE_PERCENT] = temp;
		}
		this.parsedVolume = AfterparsedString;
		return AfterparsedString;
	}
	
	/* Only Use My Splited :: input_data[0] = USED, input_data[1] = TOTAL */
	private float CheckWithUsagePercent(String[][] input_data) {
		float PercentOfUsage = 0;
		/* Percent ��� */
		
		for(int i=0;i<input_data.length;i++) {
			PercentOfUsage = Float.parseFloat(input_data[i][SIZE_USED]) / 
						Float.parseFloat(input_data[i][SIZE_TOTAL]);
			Log.d(LOGTAG, input_data[i][VOLUME_NAME]+"_PercentOfUsage : "+PercentOfUsage);
			
			/* üũ�� ��뷮�� �Ӱ�ġ���� Ŭ ��쿡�� �۵�!!!! */
			if(PercentOfUsage >= ExecuteTHRESHOLD) {
				/* sdcard�� Ÿ���̸� userdata��, userdata�� Ÿ���̸� sdcard�� �ݴ�� -- opposeVolume���� �߰� */
				if(input_data[i][VOLUME_NAME].equals(DF_NAME_DATA)) {
					this.opposeVolume = input_data[1];
					Log.d(LOGTAG, "reduce oppose�� ����, extendŸ�� ���� : "+input_data[i][VOLUME_NAME].substring(5));
				}
				else if(input_data[i][VOLUME_NAME].equals(DF_NAME_DATA_MEDIA)) {
					this.opposeVolume = input_data[0];
					Log.d(LOGTAG, "reduce oppose�� ���� extendŸ�� ���� : "+input_data[i][VOLUME_NAME].substring(5));
				}
				
				/* �Ӱ�ġ �Ѿ����� Ȯ�εǾ��� ��
				 * Resize sh������ �Ϸ�Ǿ������� üũ�Ѵ�. 
				 * �Ϸ�Ǿ� ���� ������ �Ϸ� flag on�ϰ� �����Ѵ�.
				 * �Ϸ�Ǿ����� ���� �ʴ´�. */
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
	
	/* üũ�� �뷮�� �Ӱ�ġ���� ū ���, command�� ���� �뷮�� ������ ������ ��ŭ ���� ��Ų��. */
	private void ExtendSizeOfLV(String[] targetVolume, String[][] other_Volume) {
		String cmdOfdf;
		StringBuilder cmdReturn;
		
		//target�� Ȯ���Ϸ��� ������ 1���� ũ�� �� ������� �ƿ� ����� ���̰� 1���� ���� ���̸� �׸�ŭ�� �����ϴ� ���̴�. �ᱹ ����
		float targetExtendSize = (Float.parseFloat(targetVolume[SIZE_TOTAL]) * ExpandSizePercent);
		targetExtendSize = Math.round(targetExtendSize);
		/**
			���ϵ� ���� Success�� ������� PE�� �����ϴٸ� �켱������ ���� �ٸ� �͵��� ����ų� �ٿ��� �Ѵ�.
			�׸��� ���� �� �ִ� minimum�鵵 ã�Ƽ� ���� �� �ְ��ؾ� �Ѵ�.
		*/	
		/* Step1. ���� ���Ƿ� ���� �õ� -- ������ �������� üũ�Ѵ�. ������ ��� �����ؼ� resize partition�� ���ֱ� ���� Step2�� ����. */
		/* �ϴ� ���������� ���� ���´�. */
		this.targetExtendSize = targetExtendSize;
		this.targetVolumeName = targetVolume[VOLUME_NAME].substring(5); 
		this.targetVolume = targetVolume;
		//�ϴ� �ݴ� �͵� �̸� static ����
		
		//�뷮�� 8G�� �Ѿ�� ���� �������� 8G�̻� extend�Ϸ��� �ϸ� �Ϻη� �� ũ�� Extend��û�Ѵ�.
		//���⼭ Snapshot�ɼ��� �о Snapshot�� ����� Extend�� ���ϸ� 8G���� Ȯ���ϰ� ���ִ� ���ǵ� �߰��ؾ��Ѵ�.
		//������ Max8G���� (Snapshot������ ���� ����)�ؼ� ���� �߰���
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
		//���ϵ� ��� Handleró�� �ʿ�
		//���ϵǾ��� �� lvextend�� �Ǹ� �׳� ����
		//�ȵǾ��ٰ� �� ���� �ɼ�üũ
		//�켱�����δ� �ٸ� ��Ƽ�� reduce&extend
		//�뷮üũ�Ͽ��µ� ���������� ���� ��쿡�� STEP3
		//üũ�� �ɼ��� ������ ������ ��� ������ ���� �� �ٽ� lvextend
		//lvextend -n /dev/vg/usersdcard -L +100M
		//lvm���� y��ư ������ �ϹǷ� �ȴ����� ���� �ʿ��� ���� ������ ubuntu�󿡼� �׽�Ʈ ���� ���� �ȴ����� �Ǿ���
		*/
		
		
		/* Step2. �ٸ� �͵��� reduce�ϱ� ���� ��� �� ������ �������ϰ� Shell Scripting���� ������ �����Ѵ�. */
		/**
		 * �뷮�� ���� ������,
		 * e2fsck -f /dev/vg/tempLV
		 * resize2fs /dev/vg/tempLV 200M
		 * lvreduce /dev/vg/tempLV -L 200M ����, 
		 * ���� ��Ƽ���� ���̰� lv�� ���δ�.
		 * 
		 * �ݴ�� �뷮�� �ø� ������,
		 * lv���� �ø��� ��Ƽ���� �ø���.
		 */

	}
	
	private void ReduceSizeOfLV(String[] opposeVolume) {
		float opposeReduceSize = (Float.parseFloat(opposeVolume[SIZE_TOTAL]) * ReduceSizePercent);
		
		//target�� Ȯ���Ϸ��� ������ 1���� ũ�� �� ������� �ƿ� ����� ���̰� 1���� ���� ���̸� �׸�ŭ�� �����ϴ� ���̴�. �ᱹ ����
		//opposeVolumeŸ���� Reduce, ���� (Ȯ��)Ÿ���� extend ����
		Log.d(LOGTAG, "Will Reduce Size? "+opposeReduceSize+"\nreduce target name : "+opposeVolume[VOLUME_NAME].substring(5));
		Log.e(LOGTAG, "opposeVolume name : "+opposeVolume[VOLUME_NAME].substring(5));
				ShellScriptGenerator ssg_reduce = 
						new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_REDUCE, 
						"/dev/vg"+opposeVolume[VOLUME_NAME].substring(5), opposeReduceSize);
				
		//Ȯ�� Ÿ���� �ø����� �뷮��, ����� oppose�� �뷮���� ������ �׳� �ص���
		//targetVolumeName, targetExtendSize�� ��������!!!
		if(Float.parseFloat(opposeVolume[SIZE_FREE]) > this.targetExtendSize) {
			opposeReduceSize = Math.round(opposeReduceSize);
			
			ShellScriptGenerator ssg_extend = 
					new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
					"/dev/vg"+targetVolumeName, targetExtendSize);
		}
		
		//�׷��� Ȯ���Ϸ��� �뷮�� ���̷��� �뷮���� ũ��, ���� ��ŭ�� Ȯ������� 
		else {
			float myTempSize = Math.round((Float.parseFloat(opposeVolume[SIZE_TOTAL])-opposeReduceSize)
								+Float.parseFloat(targetVolume[SIZE_TOTAL]));
			Log.d(LOGTAG, "Will Reduce at Extend Size? "+myTempSize+"\nextend target name : "+targetVolumeName);
			Log.e(LOGTAG, "in reduce for extend Volume name : "+targetVolume[VOLUME_NAME].substring(5));
		//Ȯ�嵵 opposeReduceSize��ŭ Ȯ�� �Ѵ�.
		ShellScriptGenerator ssg_extend = 
				new ShellScriptGenerator(ShellScriptGenerator.EXECUTE_EXTEND, 
				"/dev/vg"+targetVolume[VOLUME_NAME].substring(5), myTempSize);
		}
	}
	
	public void CheckingStart(boolean isRun) {
		//checkTimer = new Timer();
		
		//���� Ÿ�̸Ӱ� ���� �ִ��� üũ, ���� ������ �����Ű�� ���� ����
		if(checkTimer != null) {
			checkTimer.cancel();
			Log.d(LOGTAG, "existed Timer cancel!");
		}
		
		checkTimer = new java.util.Timer();
		//�����ϴ� ���� isRun == true
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
		
		//�����ϴ� ���� isRun == false
		else if(!isRun) {
			checkTimer.cancel();
			checkTimer = null;
			Log.d(LOGTAG, "Timer was force canceled");
			
		}
	
	}
	
	public String[] parseLVdisplay_Colons(String rawString) {
		String[] resultString = null;
		resultString = rawString.split(":"); // ������ : �� �����ؼ� �迭�� ����
		
		return resultString;
	}
	
	public void run() {
		
		Log.i(LOGTAG, "Thread start");
		CheckingStart(true);
	}


}
