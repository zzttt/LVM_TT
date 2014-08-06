package com.example.timetraveler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class SnapshotSetup {

	Context context;
	public SnapshotSetup(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public void setup_time(boolean mode, int interval, Calendar current_time){
	
		StringBuilder time = new StringBuilder();
		Calendar calendar = new GregorianCalendar();
	
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	
		SharedPreferences pref = context.getSharedPreferences("SaveState", context.MODE_PRIVATE);
		SharedPreferences.Editor edit = pref.edit();
		
		Intent alarm_intent = new Intent(this.context, AlarmBroadcast.class);
		PendingIntent pintent = PendingIntent.getBroadcast(this.context, 0, alarm_intent, 0);
	
		mode = pref.getBoolean("mode", true);
		interval = pref.getInt("spinnerSelection",0);
		interval++;
		Log.i("interval", Integer.toString(interval));
		
		if(mode == false){
			
			Toast.makeText(context,"자동 스냅샷이 설정되었습니다.", Toast.LENGTH_SHORT).show();
			
			int year = calendar.get(Calendar.YEAR);          // 연도를 리턴
			int month = calendar.get(Calendar.MONTH)+1;    // 월을 리턴
			int date = calendar.get(Calendar.DATE);          // 일을 리턴
			int amPm = calendar.get(Calendar.AM_PM);    // 오전/오후 구분을 리턴
			int hour = calendar.get(Calendar.HOUR_OF_DAY);         // 시를 리턴
			int min = calendar.get(Calendar.MINUTE);       // 분을 리턴
			int sec = calendar.get(Calendar.SECOND);      // 초를 리턴

			Log.i("mydate", "before date : "+calendar.get(Calendar.YEAR)+"/"+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.DATE)+"/"+calendar.get(Calendar.HOUR_OF_DAY)+"/"+calendar.get(Calendar.MINUTE)+"/"+calendar.get(Calendar.SECOND));
			//calendar.add(Calendar.DATE,interval);
			calendar.add(Calendar.MINUTE,interval);
			Log.i("mydate", "target date : "+calendar.get(Calendar.YEAR)+"/"+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.DATE)+"/"+calendar.get(Calendar.HOUR_OF_DAY)+"/"+calendar.get(Calendar.MINUTE)+"/"+calendar.get(Calendar.SECOND));
					
			edit.putInt("interval", interval);
			edit.putInt("year", calendar.get(Calendar.YEAR));
			edit.putInt("month", calendar.get(Calendar.MONTH));
			edit.putInt("date", calendar.get(Calendar.DATE));
			edit.putInt("ampm", calendar.get(Calendar.AM_PM));
			edit.putInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
			edit.putInt("min", calendar.get(Calendar.MINUTE));
			edit.putInt("sec", calendar.get(Calendar.SECOND));
			
			alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pintent);

			Log.d("AM", "Alarm set Repeating ok");
			
		}
		else{
			Toast.makeText(context,"자동 스냅샷이 해제되었습니다.", Toast.LENGTH_SHORT).show();
			alarm.cancel(pintent);
		}
		
		Log.i("mode", "========================================================");
		// mode change //
		edit.putBoolean("mode", !mode);
		edit.commit();
	}
}
