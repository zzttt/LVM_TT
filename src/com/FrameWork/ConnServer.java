package com.FrameWork;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.FileManager.FileSender;
import com.example.timetraveler.MainActivity;

public class ConnServer extends Thread {

	private Socket sc;
	private String authCode;

	private String srvIp;
	private String itemName;
	
	
	private int port;
	private int opCode;

	private File[] snapshotList;
	private Handler andHandler;
	
	private ObjectInputStream ois = null;
	
	private ProgressDialog pd;
	
	public ConnServer(String srvIp, int port) {
		this.srvIp = srvIp;
		this.port = port;
	}

	/**
	 * 
	 * @param srvIp
	 * @param port
	 * @param opCode : operation Code ( 0 : read sInfo  / 2: file download  / 3: chk device / 4 : add user / 6 : img stream transfer 
	 * @param userCode
	 */
	public ConnServer(String srvIp, int port, int opCode, String userCode) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
	}

	public ConnServer(String srvIp, int port, int opCode, String userCode,  String itemName, ProgressDialog pd) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
		this.andHandler = andHandler;
		this.itemName = itemName;
		this.pd = pd;
	}
	/**
	 * 
	 * @param srvIp
	 * @param port
	 * @param opCode : operation Code ( 0 : read sInfo  / 2: file download  / 3: chk device / 4 : add user / 
	 * @param userCode 
	 * @param andHandler
	 */
	public ConnServer(String srvIp, int port, int opCode, String userCode,
			Handler andHandler) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
		this.andHandler = andHandler;
	}
	
	
	@Override
	public void run() {
		try {
			sc = new Socket(srvIp, port);
			ObjectOutputStream oos = new ObjectOutputStream(
					sc.getOutputStream());


			Log.i("eee", "opCode :" + Integer.toString(opCode));
			switch (this.opCode) {
			case 0: // 기기 code 에 따라 스냅샷 정보 조회
				// Snapshot 정보조회
				/*
				 * 1. opcode 포함 payload 전송 2. Snapshot Object 수신
				 */
				
				// 1 - 1 .. Server에 Connect 시 auth Code 전송
				//oos.writeObject(authCode); // authCode == userCode
				Payload pl = new Payload(0, authCode);
				oos.writeObject(pl);
				
				Calendar time = Calendar.getInstance();
				String today = (new SimpleDateFormat("yyyyMMddHHmm").format(time
						.getTime()));
				System.out.println(today);

				// Date 전송
				oos.writeObject(today);
				

				try {
					ois = new ObjectInputStream(sc.getInputStream());

					// 스냅샷 파일 리스트
					int len = ois.readInt();
					snapshotList = new File[len];

					for (int i = 0; i < len; i++) {
						snapshotList[i] = (File) ois.readObject();
					}
					MainActivity.snapshotListInSrv = snapshotList.clone();
					Snapshot ss = (Snapshot) ois.readObject(); // 스냅샷 읽기
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					Log.e("eee", "Loading error");
					e.printStackTrace();
				} finally {

					ois.close();
					oos.close();
					// 정보 조회가 끝남을 알림. Snapshot List 업데이트됨
					// looper 필요한가?
					andHandler.post(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							andHandler.sendEmptyMessage(100);
						}
					});
					
				}

				break;
			case 1: // 파일 업로드

				FileSender fs = new FileSender(MainActivity.homePath, this.sc);
				
				// File 전송 절차
				/*
				 * 1. 소켓연결 , FileSender 초기화(앞에서 미리 실행) 2. Opcode 포함한 Payload 를
				 * Object output Stream으로 전송 전송 3. HomeDir내의 파일 개수를 전송 4.
				 * HomeDir내의 파일 정보들을 모두 전송 5. FileSender를 통한 파일전송
				 */
				// 2
				pl = new Payload(1, authCode);
				oos.writeObject(pl);

				// 3. HomeDir내의 파일 개수를 전송
				File toSendFile = new File(MainActivity.homePath);
				File[] snapshotList = toSendFile.listFiles();
				int FileCount = 0;

				for (int i = 0; i < snapshotList.length; i++) {
					if (snapshotList[i].isFile()) {
						FileCount++;
					}
				}
				Log.i("eee", Integer.toString(FileCount));
				oos.writeObject(FileCount); // 파일 갯수

				// 4. HomeDir내의 파일 정보들을 모두 전송
				for (int i = 0; i < snapshotList.length; i++) {
					if (snapshotList[i].isFile()) {
						oos.writeLong(snapshotList[i].length()); // file Size
						oos.writeObject(snapshotList[i]);
						oos.writeObject(snapshotList[i].getName());
					}
				}

				// 5. FileSender를 통한 파일전송
				for (int i = 0; i < snapshotList.length; i++) {
					if (snapshotList[i].isFile()) {
						System.out.println("전송할 파일명 : " + snapshotList[i]);
						fs.sendFile(snapshotList[i].getName()); // 파일 전송
					}
				}

				break;
			case 2: // file download
				
				pl = new Payload(2,authCode);
				oos.writeObject(pl);
				
				
				break;
			case 3:  // chk Device ( 등록여부 체크 )
				
				pl = new Payload(3,authCode);
				oos.writeObject(pl); // 등록여부 확인 요청

				
				break;
			case 4: // add user ( 사용자 등록 )
				pl = new Payload(4,authCode);
				oos.writeObject(pl);
				break;
			case 5: // get user Information ( 사용자 정보 조회 )
				
				pl = new Payload(5,authCode);
				break;
				
			case 6: // 이미지 스트림 업로드
				Log.i("lvm2", "image stream payload transfer");
				pl = new Payload(6,authCode);
				oos.writeObject(pl); // payload 전송
				// 스냅샷 이미지
				SnapshotImageMaker sim = new SnapshotImageMaker("test.txt" ,oos);
				sim.start();
				
				try {
					sim.join(); // 스레드 대기
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("lvm2", "이미지 전송 종료");
				
				pd.cancel();
				
				break;
			case 7:
				break;
			}
			
			oos.close();
			sc.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	public Socket getSocket() {
		return this.sc;
	}

	public File[] getSsList() {
		return snapshotList;
	}

}
