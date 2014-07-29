package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class ShellScriptGenerator {
	
	public static final String scriptpath = "/data/data/com.example.timetraveler/resize.sh"; // "/initvol/resize.sh";
	
	public static final int EXECUTE_EXTEND = 1;
	public static final int EXECUTE_REDUCE= 0;
	
	private String targetVolume = "";
	private float targetSize = 0; 	//targetSize는 MB단위로 던져준다.
	private int t_targetSize = 0;
	private int executeOp = 2;		//초기설정은 0,1아닌 다른 값
	private BufferedWriter sw;
	
	//targetSize는 MB단위로 던져준다.
	public ShellScriptGenerator(int executeOp, String targetVolume, float targetExtendSize) {
		this.targetSize = targetExtendSize;
		this.t_targetSize = (int)targetExtendSize;
		this.targetVolume = targetVolume;
		this.executeOp = executeOp;
		
		Log.d("SSG", "executeOp :"+executeOp +" --> 0 : REDUCE, 1 : EXTEND");
		switch(executeOp) {
			case EXECUTE_EXTEND:
				scriptGenerateToExtendPartition();
				break;
				
			case EXECUTE_REDUCE:
				scriptGenerateToReducePartition();
				break;
		}
		
	}
	
	private void scriptGenerateToExtendPartition() {
		Log.i("SSG", "Generate to Extend script");
			
		try {
			
			//FileWriter의 두번째 인자가 true이면 이어서 쓰기
			//계속해서 이어 써지는 것을 방지하기 위하여 SnapshotService가 켜지면 무조건 삭제
			sw = new BufferedWriter(new FileWriter(scriptpath, true));
			//String myScript ="#!/system/bin/sh";
			//StringBuilder reScript = new StringBuilder("#!/system/bin/sh\n");
			
			StringBuilder reScript = new StringBuilder("#!/initvol/sh\n");
			/*lv extend는 리붓 전에 App단에서 처리되었을 수도 있고
			아닐 경우도 있으므로 일단 먼저 확장 시도 */
			
			reScript.append("/lvm/lvm lvresize "+targetVolume+" -L"+t_targetSize+"M\n");
			//partition extend
			reScript.append("./e2fsck -f ");
			reScript.append(targetVolume+"\n");
			reScript.append("./resize2fs "+targetVolume+" "+t_targetSize+"M\n");

			//write to script
			sw.write(reScript.toString());
			sw.flush();
			sw.close();
			Log.d("SSG", "writing a shellscript.\n");
			
		} catch (IOException e) {
			Log.e("SSG", ""+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void scriptGenerateToReducePartition() {
		
		Log.i("SSG", "Generate to Reduce script");
		try {
			
			//FileWriter의 두번째 인자가 true이면 이어서 쓰기
			//계속해서 이어 써지는 것을 방지하기 위하여 SnapshotService가 켜지면 무조건 삭제
			sw = new BufferedWriter(new FileWriter(scriptpath, true));
			//String myScript ="#!/system/bin/sh";
			StringBuilder reScript = new StringBuilder("#!/initvol/sh\n");
			
			//lvreduce는 파티션 축소 후에 해야함! 먼저 lv를 줄이면 파티션 깨짐.
			//partition extend
			reScript.append("./e2fsck -fy ");
			reScript.append(targetVolume+"\n");
			reScript.append("./resize2fs "+targetVolume+" "+t_targetSize+"M\n");
			
			//파티션 축소 후에 lvSize도 축소
			reScript.append("/lvm/lvm lvresize "+targetVolume+" -L"+t_targetSize+"M\n");
			//write to script
			sw.write(reScript.toString());
			sw.flush();
			sw.close();
			Log.d("SSG", "writing a shellscript.\n");
			
		} catch (IOException e) {
			Log.e("SSG", ""+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
}
