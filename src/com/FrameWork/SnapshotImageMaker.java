package com.FrameWork;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.FileManager.FileSender;
import com.FileManager.GzipGenerator;
import com.example.timetraveler.MainActivity;

import android.util.Log;

public class SnapshotImageMaker extends Thread {

	private String sName;
	private ObjectOutputStream oos;
	private Socket sc;
	
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

		
		try {
			
			Process p = Runtime.getRuntime().exec("su"); // root 쉘

			oos.writeObject("전송을 시작합니다");
			
			// 스레드로 처리해야 할 듯 함.. timeout 됨.

			// obs : 10240 (10 kb 씩 read)
			String command = "dd if=/dev/vg/" + sName + " obs=2m\n"; // 1mb 단위로 읽음

			// 명령어 실행
			// command = "dd if=/dev/vg/test.txt obs=1024\n";
			command = "dd if=/dev/vg/test.txt obs=512k\n";
			 
			Log.i("thread", "command : " + command );

			OutputStream os = p.getOutputStream(); // 프로세스용 output stream
			InputStream is = p.getInputStream(); // 
			
			/*readThread rT = new readThread(is , oos); 
			rT.start();*/

			// input command
			os.write(command.getBytes());
			os.write("exit\n".getBytes());
			os.flush();
			Log.d("lvm2", "Stream finished 11");
			
			
			// -----------------------------------------------------------------------------------------
			
			byte buffer[] = new byte[1024*1024]; // 512k
			int size = 0;
			long totalSize = 0;
			try {
				/*if (sc.isConnected()) { // 소켓 연결시 파일전송
					fs = new FileSender(sc);
				} else {
					return;
				}*/
				
				BufferedInputStream br = new BufferedInputStream(is);
				
				/*
				 * input stream 에서 읽어 socket 으로 쏜다.
				 */
				
				while ((size = br.read(buffer)) <= 0) { // size <= 0 일 동안 대기
					// do nothing
				}
				
				// while문을 지나면 한개의 버퍼를 읽어들인다.
				
				// 처음 읽은 buffer 전송
				
				//gOut = new GZIPOutputStream(oos);
				
				oos.write(buffer, 0, size);
				
				//gOut.write(buffer, 0, size); // 압축해서 서버로 바로 쏜다
				
				Log.d("lvm", "first size : "+Integer.toString(size));
				totalSize+=size;
				// while ((size = is.read(buffer)) > 0) {
				
				// avail  > input stream에 남아있는 바이트
				int avail ;
				int bunchSize = 0;
				
				while ((size = br.read(buffer)) > 0) {
					//Log.e("lvm2", "in while (avail : "+avail+")");
					String str = new String ( buffer, 0 , size);
					
					Log.e("lvm2", "string : " + str);
					// buffer 2 ~ end 까지 서버로 전송
					
					//gOut.write(buffer, 0, size); // 압축해서 서버로 바로 쏜다
					totalSize += size;
					bunchSize += size;
					
					if(bunchSize > 1024*1024){ // 1mb
						oos.writeInt(0); // 전송을 알림
						oos.write(buffer, 0, size);
						bunchSize = 0 ;
					}
					
				}
				oos.writeInt(-1); // 전송의 끝을 알림
				Log.d("lvm", Long.toString(totalSize));
				
				Log.e("lvm2", "out of while");
				
				oos.flush();
				/*gOut.flush();
				gOut.close();*/
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				Log.d("lvm", "total : " + Long.toString(totalSize / 1024)+ "kb");
			}
			
			
			// ----------------------------------------------------------
			
			os.close();
			oos.close();
			is.close();
			
			
			
			
			
			
			
			
			
			
			/*
			 * p.exitValue 로 끝을 체크하려고 함 끊길지 잘 모르겠다.
			 */
			try {
				//rT.join(); // 전송 스레드 대기
				
				p.waitFor();
				if (p.exitValue() != 255) {
					// TODO Code to run on success
					Log.i("lvm2", "su and dd command successed");
				} else {
					// TODO Code to run on unsuccessful
					Log.i("lvm2", "su fail");
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("lvm2", "not root");
			} 

			Log.d("lvm2", "Stream finished");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}

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
