package com.example.timetraveler;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SnapshotService extends Service {

	public static final String LOGTAG = "TTService";
	private IntentFilter Snapshotfilter;
	private SnapshotReceiver ssReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/* LVSizeObserver ���� */
		CheckSizeLV();
		
		/* AlarmBroadcast���� ������� Intent Broadcast�� ���� Broadcast Receiver ���
		 * �ʿ�� �߰� ���� --> SnapshotReceiver class��
		 */
		Snapshotfilter = new IntentFilter(SnapshotReceiver.SNAPSHOT_SERVICE_SS_GENERATE_START);
		ssReceiver = new SnapshotReceiver();
		registerReceiver(ssReceiver, Snapshotfilter);
		
		
		return startId;
	}


	@Override
	public void onCreate() {
		
		
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(ssReceiver);
		
	}
	
	/* LVSize�� üũ�ؼ� autoExtend ���ִ� �ν��Ͻ� ���, �޼ҵ� ȣ�� */
	private void CheckSizeLV() {
		Log.i(LOGTAG, "start checksizelv()");
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