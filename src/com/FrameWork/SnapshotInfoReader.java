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
	
	public SnapshotInfoLists getSnapshotInfo(){ // �ش� �̸��� ������ �̸��� �о��
		
		Log.i("lvdisplay", "Snapshot Info �� �о��");
		Process p;
		try {
			p = new ProcessBuilder("su").start();

			String command = "lvm lvdisplay /dev/vg/"+this.sName+"\n";
			p.getOutputStream().write(command.getBytes());
			p.getOutputStream().write("exit\n".getBytes());
			
			
			Log.i("lvdisplay", command
					);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String s;
			StringBuffer sb = new StringBuffer();
			
			while(( s = br.readLine() ) != null ){
				Log.i("lvdisplay", s);
				sb.append(s+"\n");
				
			}
			
			Log.i("lvdisplay", sb.toString());
			
			
			// sil = new SnapshotInfoLists(); // ������ �ʱ�ȭ
			
			
			p.destroy(); //end process
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return this.sil;
	}
	
	
	
}
