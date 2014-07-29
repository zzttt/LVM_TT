package com.example.timetraveler;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationMessage extends Activity{

	public NotificationMessage() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("위의 notification이 사라진것을 확인?:");
		setContentView(tv);
		
		NotificationManager nm1 = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NotificationManager nm2 = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm1.cancel(1111);
		nm2.cancel(2222);
	}

}
