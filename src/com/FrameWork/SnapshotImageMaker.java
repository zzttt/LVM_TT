package com.FrameWork;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import com.FileManager.FileSender;
import com.FileManager.GzipGenerator;
import com.example.timetraveler.MainActivity;

import android.util.Log;

public class SnapshotImageMaker extends Thread {

	private String sName;
	private ObjectOutputStream oos;
	private Socket sc;
	
	private GZIPOutputStream gOut;
	
	public SnapshotImageMaker(String sName) {
		this.sName = sName;
	}
	
	public SnapshotImageMaker(String sName, ObjectOutputStream oos) {
		this.sName = sName;
		this.oos = oos;
	}
	
	public SnapshotImageMaker(String sName, Socket sc) {
		this.sName = sName;
		this.sc = sc;
	}

	@Override
	public void run() {
		Log.e("eee", "snapshot maker!!!");
		Process p;
		try {
			
			Log.v("eee","---------------- s NAme : "+sName);
			
			//int cnt = 1*1024*1024*1024 / 512*3000;
			
			p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream(); // 프로세스용 output stream

			String command = "dd if=/dev/vg/" + sName
					+ " of=/data/userrecover/test.img count=100 bs=4096\n"; // 1mb
																				// 단위로
																				// 읽음

			os.write(command.getBytes());

			os.flush();
			os.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root 쉘

		
		
		
		

	}
}



class readThread extends Thread{
	private byte buffer[] = new byte[1024];
	private int size = 0;
	private int totalSize = 0;
	private InputStream is;
	private int option;
	private Socket sc;
	
	private ObjectOutputStream oos;
	
	public readThread(InputStream is ){
		this.is = is;
	}
	
	public readThread(InputStream is , Socket sc ){
		this.is = is;
		this.sc = sc;
	}
	
	/**
	 * 
	 * @param is : 장치로부터 받는 Input Stream
	 * @param oos : 서버로 전송하기 위한 Object Stream
	 */
	public readThread(InputStream is , ObjectOutputStream oos){
		this.is = is;
		this.oos = oos;
	}
	
	
	public readThread(InputStream is, int option){ // option : 
		this.is = is;
		this.option = option;
	}
	
	
	@Override
	public void run(){

		try {
			// obs 읽기 테스트
			
			/*BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String l;
			while(( l = br.readLine()) == null){ // size == 0일동안 대기	

				Log.d("thread", "waiting .." );
			}

			Log.d("lvm", l );
			while(( l = br.readLine()) != null){
				Log.d("lvm", l );
			}*/
			
			/*while ((size = is.read(buffer)) == 0) { // size == 0 일 동안 대기
				
			}
			while ((size = is.read(buffer)) > 0) {
				Log.d("lvm", Integer.toString(size) );
				totalSize += size;
			}*/
			
			// 읽어들인 Stream 직접 압축, 서버로 전송
			
			GzipGenerator gg = new GzipGenerator(is , oos); // InputStream 으로 객체 초기화
			gg.SendCompImgToSrv();
			
			
			
		} finally{
			
		}

		
		
	}

}
