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
		
		/* 다른 App에서 실행이 필요할 경우 여기 path를 변경해서 테스트 한다. */
		//String path = "/data/data/net.kkangsworld.lvmexec/result_pipe";
		String path = pipeWithLVM.RESULTFIFO;
		
			try
			{ 
				/* 현재 package 경로의 result_pipe */
				//String path = getApplicationInfo().dataDir+"/result_pipe";
				
				/* return 될 txt */
				String txt = "";
				//Log.i("LVMJava", "openning input stream");
				Log.i("LVMJava", "PATH : "+path);
				
				//계속해서 BufferedReade로 입력된 result pipe data를 gettering
				
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
				    	if(!txt.isEmpty()) { // 이부분 여러번 반복됨 -> handler에 메세지 전송은 한번만 해도 됨
					    	Message msg = Message.obtain();
							msg.what = 0; msg.arg1 = 0; //여기에 해당 COMMAND구분용 넣음될듯
		
							msg.obj = txt; 
							
							//값 던져줌 use Handler
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
