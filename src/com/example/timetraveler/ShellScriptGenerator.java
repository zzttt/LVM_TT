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
	private float targetSize = 0; 	//targetSize�� MB������ �����ش�.
	private int t_targetSize = 0;
	private int executeOp = 2;		//�ʱ⼳���� 0,1�ƴ� �ٸ� ��
	private BufferedWriter sw;
	
	//targetSize�� MB������ �����ش�.
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
			
			//FileWriter�� �ι�° ���ڰ� true�̸� �̾ ����
			//����ؼ� �̾� ������ ���� �����ϱ� ���Ͽ� SnapshotService�� ������ ������ ����
			sw = new BufferedWriter(new FileWriter(scriptpath, true));
			//String myScript ="#!/system/bin/sh";
			//StringBuilder reScript = new StringBuilder("#!/system/bin/sh\n");
			
			StringBuilder reScript = new StringBuilder("#!/initvol/sh\n");
			/*lv extend�� ���� ���� App�ܿ��� ó���Ǿ��� ���� �ְ�
			�ƴ� ��쵵 �����Ƿ� �ϴ� ���� Ȯ�� �õ� */
			
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
			
			//FileWriter�� �ι�° ���ڰ� true�̸� �̾ ����
			//����ؼ� �̾� ������ ���� �����ϱ� ���Ͽ� SnapshotService�� ������ ������ ����
			sw = new BufferedWriter(new FileWriter(scriptpath, true));
			//String myScript ="#!/system/bin/sh";
			StringBuilder reScript = new StringBuilder("#!/initvol/sh\n");
			
			//lvreduce�� ��Ƽ�� ��� �Ŀ� �ؾ���! ���� lv�� ���̸� ��Ƽ�� ����.
			//partition extend
			reScript.append("./e2fsck -fy ");
			reScript.append(targetVolume+"\n");
			reScript.append("./resize2fs "+targetVolume+" "+t_targetSize+"M\n");
			
			//��Ƽ�� ��� �Ŀ� lvSize�� ���
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
