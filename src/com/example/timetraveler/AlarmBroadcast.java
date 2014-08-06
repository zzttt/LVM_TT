package com.example.timetraveler;

import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;


import android.R.integer;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.BoringLayout;
import android.util.Log;
import android.widget.Toast;

public class AlarmBroadcast extends BroadcastReceiver{

	private NotificationManager nm;
	
	public AlarmBroadcast() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// TODO Auto-generated method stub
		Log.d("AM", "ALARM");
		
		/* Snapshot 생성하라는 Intent를 broadcast한다. SnapshotService에서 받을 것임 */
		Intent i = new Intent(SnapshotReceiver.SNAPSHOT_SERVICE_SS_GENERATE_START);
		context.sendBroadcast(i);
		
		String action = intent.getAction();
		
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Calendar cal_broad = Calendar.getInstance();
		//Calendar current_cal = Calendar.getInstance();
		Calendar current_cal = new GregorianCalendar();		
		
		Intent broad_intent = new Intent(context, AlarmBroadcast.class);
		PendingIntent pintent = PendingIntent.getBroadcast(context, 0, broad_intent, 0);
		
		SharedPreferences pref = context.getSharedPreferences("SaveState",0);
		SharedPreferences.Editor edit = pref.edit();

		// Create Notification Object
		nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		//PendingIntent noti_pintent = PendingIntent.getActivity( context, 0, new Intent(context, NotificationMessage.class), 0);
		PendingIntent noti_pintent = PendingIntent.getActivity( context, 0, new Intent(context, MainActivity.class), 0);
		
		boolean mode = pref.getBoolean("mode", true);
		
		int broad_interval = pref.getInt("spinnerSelection",0);												// load interval value
		broad_interval++;																					
		Log.i("broad_interval", Integer.toString(broad_interval));
		
		try{
			if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){					// reboot --> alarm setting
				Toast.makeText(context, "in broadcast!!!", Toast.LENGTH_SHORT).show();
				
				if( mode == true ){

					int target_year = pref.getInt("year", 0);
					int target_month = pref.getInt("month", 0);
					int target_date = pref.getInt("date", 0);
					int target_amPm = pref.getInt("amPm", 0);
					int target_hour = pref.getInt("hour", 0);
					int target_min = pref.getInt("min", 0);
					int target_sec = pref.getInt("sec", 0);
					
					cal_broad.set(target_year, target_month, target_date, target_hour, target_min, target_sec);		// target time setting

										
					Toast.makeText(context, cal_broad.getTimeInMillis() +"//"+ current_cal.getTimeInMillis() , Toast.LENGTH_LONG).show();
					// 		목표시간					<l 		현재시간? 
					if( cal_broad.getTimeInMillis() < current_cal.getTimeInMillis() ){								// (target time < current time)  -->> resetting --> command sending...	
						Toast.makeText(context, "testing...!!!", Toast.LENGTH_SHORT).show();
						
						// Create Notification Object
						Notification notification = new Notification(android.R.drawable.ic_input_add, "자동 복원시점 생성 시간이 지났습니다.", current_cal.getTimeInMillis());
						notification.setLatestEventInfo(context, "자동 복원시점 생성 시간이 지났습니다.", "현재시점기준으로 자동 복원시점 생성", noti_pintent);	
						nm.notify(1111, notification);
						
						Log.i("mydate", "cur_before date : "+current_cal.get(Calendar.YEAR)+"/"+current_cal.get(Calendar.MONTH)+"/"+current_cal.get(Calendar.DATE)+"/"+current_cal.get(Calendar.HOUR_OF_DAY)+"/"+current_cal.get(Calendar.MINUTE)+"/"+current_cal.get(Calendar.SECOND));
						//current_cal.add(Calendar.MONTH, broad_interval);
						//current_cal.add(Calendar.DATE, broad_interval);
						current_cal.add(current_cal.MINUTE, broad_interval);
						Log.i("mydate", "cur_after date : "+current_cal.get(Calendar.YEAR)+"/"+current_cal.get(Calendar.MONTH)+"/"+current_cal.get(Calendar.DATE)+"/"+current_cal.get(Calendar.HOUR_OF_DAY)+"/"+current_cal.get(Calendar.MINUTE)+"/"+current_cal.get(Calendar.SECOND));
					
						edit.putInt("year", current_cal.get(Calendar.YEAR));
						edit.putInt("month", current_cal.get(Calendar.MONTH));
						edit.putInt("date", current_cal.get(Calendar.DATE));
						edit.putInt("ampm", current_cal.get(Calendar.AM_PM));
						edit.putInt("hour", current_cal.get(Calendar.HOUR_OF_DAY));
						edit.putInt("min", current_cal.get(Calendar.MINUTE));
						edit.putInt("sec", current_cal.get(Calendar.SECOND));
						edit.commit();
						
						alarm.set(AlarmManager.RTC_WAKEUP, current_cal.getTimeInMillis(), pintent);						// ( current_time + interval ) 	alarm setting
						
					}else{		
						// 목표 시간으로 알람 재등록 //
						Toast.makeText(context, "1"+cal_broad.getTimeInMillis(), Toast.LENGTH_LONG).show();
						alarm.set(AlarmManager.RTC_WAKEUP, cal_broad.getTimeInMillis(), pintent);						// ( target_time )				alarm setting	
					}

				}
				
				Toast.makeText(context, "reboot_alarm_setting ok", Toast.LENGTH_SHORT).show();
			}

		}catch(NullPointerException e){																		
			Toast.makeText(context, "in broadcast!!!(no data)", Toast.LENGTH_SHORT).show();					
			
			Log.i("mydate", "before date : "+cal_broad.get(Calendar.YEAR)+"/"+cal_broad.get(Calendar.MONTH)+"/"+cal_broad.get(Calendar.DATE)+"/"+cal_broad.get(Calendar.HOUR_OF_DAY)+"/"+cal_broad.get(Calendar.MINUTE)+"/"+cal_broad.get(Calendar.SECOND));
			//cal_broad.add(Calendar.MONTH, broad_interval);
			//cal_broad.add(Calendar.DATE, broad_interval);
			cal_broad.add(Calendar.MINUTE, broad_interval);
					
			Log.i("mydate", "target date : "+cal_broad.get(Calendar.YEAR)+"/"+cal_broad.get(Calendar.MONTH)+"/"+cal_broad.get(Calendar.DATE)+"/"+cal_broad.get(Calendar.HOUR_OF_DAY)+"/"+cal_broad.get(Calendar.MINUTE)+"/"+cal_broad.get(Calendar.SECOND));
	
			//Create Notification Object
			Notification notification = new Notification(android.R.drawable.ic_input_add, "자동 복원시점 생성 합니다.", current_cal.getTimeInMillis());
			//notification.setLatestEventInfo(context, "자동 복원시점 생성 합니다.", cal_broad.getTime().toString() , noti_pintent);
			notification.setLatestEventInfo(context, "자동 복원시점 생성 합니다.", "다음생성:"+broad_interval+"일 후" , noti_pintent);
			nm.notify(2222, notification);
			
			edit.putInt("year", cal_broad.get(Calendar.YEAR));
			edit.putInt("month", cal_broad.get(Calendar.MONTH));
			edit.putInt("date", cal_broad.get(Calendar.DATE));
			edit.putInt("ampm", cal_broad.get(Calendar.AM_PM));
			edit.putInt("hour", cal_broad.get(Calendar.HOUR_OF_DAY));
			edit.putInt("min", cal_broad.get(Calendar.MINUTE));
			edit.putInt("sec", cal_broad.get(Calendar.SECOND));
			edit.commit();
			
			alarm.set(AlarmManager.RTC_WAKEUP, cal_broad.getTimeInMillis(), pintent);
			
			// command sending..to Service..... //
			/* Snapshot 생성하라는 Intent를 broadcast한다. SnapshotService에서 받을 것임 */
			/*Intent intent2 = new Intent(SnapshotReceiver.SNAPSHOT_SERVICE_SS_GENERATE_START);
			context.sendBroadcast(intent2);*/
		}
		
	}
}
