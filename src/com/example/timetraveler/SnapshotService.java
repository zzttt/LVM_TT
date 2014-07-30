package com.example.timetraveler;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SnapshotService extends Service {

	public static final String LOGTAG = "TTService";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i(LOGTAG, "start checksizelv()");
		CheckSizeLV();
		
		return startId;
	}


	@Override
	public void onCreate() {
		
		
	}

	@Override
	public void onDestroy() {
		
		
	}
	
	/* LVSize�� üũ�ؼ� autoExtend ���ִ� �ν��Ͻ� ���, �޼ҵ� ȣ�� */
	private void CheckSizeLV() {
		Handler observHandler = null;
		
		LVSizeObserver lvSizeObserver = new LVSizeObserver(observHandler);
		lvSizeObserver.setDaemon(true);
		lvSizeObserver.start();
		
		observHandler = new Handler() {
		
			public void handleMessage(Message msg) {
				
			}
		
		};
	}
	
}