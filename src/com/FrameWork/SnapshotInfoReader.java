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
			
			
			
			String lv_path = null, lv_name = null, lv_uuid = null, lv_wa = null, lv_time = null, lv_satus = null, lv_size = null, lv_cow_table_size = null;
			
			for(String a : arr){
				if(a.contains("LV Path")){
					a = a.replace("LV Path", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_path = a.trim();
				}else if(a.contains("LV Name")){
					a = a.replace("LV Name", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_name = a.trim();
				}else if(a.contains("LV UUID")){
					a = a.replace("LV UUID", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_uuid = a.trim();
				}else if(a.contains("LV Creation host, time")){
					a = a.replace("LV Creation host, time", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_time = a.trim();
				}else if(a.contains("LV Status")){
					a = a.replace("LV Status", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_satus = a.trim();
				}else if(a.contains("LV Size")){ // gb or mb 데이터를 알맞게 변경한다.
					a = a.replace("LV Size", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_size = a.trim();
				}else if(a.contains("COW-table size")){ 
					a = a.replace("COW-table size", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_cow_table_size = a.trim();
				}else if(a.contains("LV Write Access")){
					a = a.replace("LV Write Access", "");
					a = a.replace("\t", "");
					a = a.trim();
					lv_wa = a.trim();
				}
				Log.i("lvdisplay", a);
				//Log.i("lvdisplay", a.replace("LV Path", ""));
			}
			
			
			
			//Log.i("lvdisplay", sb.toString());
			
			//String uid, String status, String data, String snapshotName , double lv_size , double cow_table_size , String path){
			
			sil = new SnapshotInfoLists(lv_uuid, lv_satus,lv_time, lv_name, lv_size, lv_cow_table_size , lv_path); // 데이터 초기화
			
			
			p.destroy(); //end process
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return this.sil;
	}
	
	
	
}
