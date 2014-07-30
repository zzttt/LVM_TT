package com.example.timetraveler;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
	private pipeWithLVM m_pipeWithLVM;
	
	public SnapshotReceiver() {
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
			
			/* 기존 스냅샷 삭제 */
			m_pipeWithLVM.ActionWritePipe("lvremove ");
			
			/* command 변경 필요 path 설정 등.. */
			m_pipeWithLVM.ActionWritePipe("lvcreate -s -L 1G -n "+today+" /dev/vg/userdata");
		}
	}

}
