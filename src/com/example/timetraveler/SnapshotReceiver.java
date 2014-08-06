package com.example.timetraveler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.FrameWork.InstalledAppInfo;

import net.kkangsworld.lvmexec.pipeWithLVM;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class SnapshotReceiver extends BroadcastReceiver {

	private static final String LOGATAG = "SSRecv";
	public static final String SNAPSHOT_SERVICE_SS_GENERATE_START = "SnapshotGenerateStart";

	private Handler rh;
	private Context context;
	private pipeWithLVM m_pipeWithLVM;
	private InstalledAppInfo mInsAppInfo;

	public SnapshotReceiver() {
		rh = new Handler() {
			//
		};
		m_pipeWithLVM = new pipeWithLVM(rh);
	}
	
	public SnapshotReceiver(Context context) {
		this.context = context;
		
		rh = new Handler() {
			//
		};
		m_pipeWithLVM = new pipeWithLVM(rh);
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();

		if(action.equals(SNAPSHOT_SERVICE_SS_GENERATE_START)) {
			Log.d(LOGATAG, "into Snapshot Service from alarm manager");

			Calendar cal = Calendar.getInstance();
			String today = (new SimpleDateFormat("yyyyMMddHHmm").format(cal.getTime()));

			this.context = context;
			/* Install App Loader Instanced */
			mInsAppInfo = new InstalledAppInfo(context);
			
			try {
				Thread.sleep(300);
				/* 기존 스냅샷 삭제 */
				/*m_pipeWithLVM.ActionWritePipe("lvremove -f /dev/vg/2014*");
				Thread.sleep(300);
				m_pipeWithLVM.ActionWritePipe("lvremove -f /dev/vg/2014*");*/
				
				
				
				Process p = new ProcessBuilder("su").start();
				p.getOutputStream().write("lvm lvremove -f /dev/vg/2014*\n".getBytes());
				p.getOutputStream().write("exit\n".getBytes());
				p.getOutputStream().flush();
				p.getOutputStream().close();
				
				
				Thread.sleep(500);
				/* 새로운 스냅샷 생성 */
				m_pipeWithLVM.ActionWritePipe("lvcreate -s -L 1G -n "
						+ today + "_userdata /dev/vg/userdata");

				Thread.sleep(500);

				m_pipeWithLVM.ActionWritePipe("lvcreate -s -L 1G -n "
						+ today + "_usersdcard /dev/vg/usersdcard");
				Thread.sleep(500);
				m_pipeWithLVM.ActionWritePipe("lvcreate -s -L 100M -n "
						+ today + "_usersystem /dev/vg/usersystem");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/**
			 * 생성되는 lv snapshot에 상응되는 어플리스트를 백업한다.
			 * 어플리스트는 ArrayList로 존재하고 이를 저장할 HashMap은,
			 * lv snapshot에 대해 날짜를 Key, ArrayList를 Value로 가진다.
			 */
			/* 어플 리스트를 읽어 들여서 SharedPrefs 또는 특정 파일에에 
			 * HashMap형태로 저장한다. 
			 * today를 key로 저장 */
			
			mInsAppInfo.resultToSaveFile(today);

			mInsAppInfo.ReadAppInfo(today);
			mInsAppInfo.resultPrint();
			
		
		}
	}

}