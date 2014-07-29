package net.kkangsworld.lvmexec;

import java.io.BufferedReader;
import java.io.FileReader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ResultReader extends Thread {
	
	Handler rHandler;
	public ResultReader() {
		
	}
	
	public ResultReader(Handler rHandler) {
		this.rHandler = rHandler;
		Log.i("LVMJava", "ResultReader thread init");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.i("LVMJava", "ResultReader thread start");
		
		/* �ٸ� App���� ������ �ʿ��� ��� ���� path�� �����ؼ� �׽�Ʈ �Ѵ�. */
		//String path = "/data/data/net.kkangsworld.lvmexec/result_pipe";
		String path = pipeWithLVM.RESULTFIFO;
		
			try
			{ 
				/* ���� package ����� result_pipe */
				//String path = getApplicationInfo().dataDir+"/result_pipe";
				
				/* return �� txt */
				String txt = "";
				//Log.i("LVMJava", "openning input stream");
				Log.i("LVMJava", "PATH : "+path);
				
				//����ؼ� BufferedReade�� �Էµ� result pipe data�� gettering
				
				while(true) {
					Log.i("LVMJavaRR", "ResultReader exists and run whiling..");
					if(rHandler == null)
						Log.i("LVMJavaRR", "rHandler is null");
					BufferedReader reader = 
							 new BufferedReader(new FileReader(path));
				    while((txt = reader.readLine()) != null) 
				    {
				    	Log.d("LVMJavaRR","txt : "+ txt);
				    	
				    	if(txt.startsWith("[in run_command]"))
				    		Log.d("LVMJava", ""+txt);
				    	//if(txt.contains("[in run_command]"))
				    			//Log.d("LVMJava", "in run command : "+txt);
				    	if(!txt.isEmpty()) { // �̺κ� ������ �ݺ��� -> handler�� �޼��� ������ �ѹ��� �ص� ��
					    	Message msg = Message.obtain();
							msg.what = 0; msg.arg1 = 0; //���⿡ �ش� COMMAND���п� �����ɵ�
		
							msg.obj = txt; 
							
							//�� ������ use Handler
							rHandler.sendMessage(msg);
							Log.d("LVMJavaRR", "To handler success");	
							
				    	}
				    	else {
				    		Log.d("LVMJavaRR", "Result is Null :)");
				    	}
						
				    } 
				    reader.close();
			    }
				
			 
			 }catch(Exception e) {
				 e.printStackTrace();
				 Log.d("LVMJavaRR", e.getMessage());
			 }
			Log.i("LVMJava", "ResultReader thread end");
		}
}
