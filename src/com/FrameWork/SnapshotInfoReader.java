package com.FrameWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class SnapshotInfoReader {

	private String sName;
	private SnapshotInfoLists sil;
	
	public SnapshotInfoReader(String sName){
		this.sName = sName;
	}
	
	public SnapshotInfoLists getSnapshotInfo(){ // 해당 이름의 스냅샷 이름을 읽어옴
		
		Log.i("lvdisplay", "Snapshot Info 를 읽어옴");
		Process p;
		try {
			p = new ProcessBuilder("su").start();

			//String command = "lvm lvdisplay /dev/vg/"+this.sName+"\n";
			String command = "lvm lvdisplay /dev/vg/201407311809\n";
			p.getOutputStream().write(command.getBytes());
			p.getOutputStream().write("exit\n".getBytes());
			
			
			Log.i("lvdisplay", command
					);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String s;
			StringBuffer sb = new StringBuffer();
			
			while(( s = br.readLine() ) != null ){
			//	Log.i("lvdisplay", s);
				sb.append(s+"\n");
			}
			
			String arr[] = sb.toString().split("\n");
			
			
			
			String lv_path, lv_name, vg_name, lv_uuid, lv_wa, lv_time, lv_satus, lv_size, lv_cow_table_size;
			for(String a : arr){
				Log.i("lvdisplay", a.replace("LV Path", ""));
			}
			
			
			
			//Log.i("lvdisplay", sb.toString());
			
			
			// sil = new SnapshotInfoLists(); // 데이터 초기화
			
			
			p.destroy(); //end process
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return this.sil;
	}
	
	
	
}
