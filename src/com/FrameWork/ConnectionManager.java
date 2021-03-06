package com.FrameWork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.FileManager.FileSender;
import com.example.timetraveler.MainActivity;

public class ConnectionManager extends Thread {

	private String filePath = null;
	
	private Socket sc;
	private String authCode;

	private String srvIp;
	private String itemName;
	private String sName;

	private int port;
	private int opCode;

	private File[] snapshotList;
	private Handler andHandler;

	private ObjectInputStream ois = null;

	private ProgressDialog pd;

	public ConnectionManager(String srvIp, int port) {
		this.srvIp = srvIp;
		this.port = port;
	}

	/**
	 * 
	 * @param srvIp
	 * @param port
	 * @param opCode
	 *            : operation Code ( 0 : read sInfo / 2: file download / 3: chk
	 *            device / 4 : add user / 6 : img stream transfer
	 * @param userCode
	 */
	public ConnectionManager(String srvIp, int port, int opCode, String userCode) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
	}
	
	public ConnectionManager(String srvIp, int port, int opCode, String userCode, String filePath) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
		this.filePath = filePath;
	}
	

	public ConnectionManager(String srvIp, int port, int opCode, String userCode,
			String itemName, ProgressDialog pd) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
		this.andHandler = andHandler;
		this.itemName = itemName;
		this.pd = pd;
	}

	public ConnectionManager(String srvIp, int port, int opCode, String userCode,
			String sName, String itemName, Handler andHandler) {
		this.srvIp = srvIp;
		this.port = port;
		this.opCode = opCode;
		this.authCode = userCode;
		this.andHandler = andHandler;
		this.sName = sName;
		this.itemName = itemName;
	}

	/**
	 * 
	 * @param srvIp
	 * @param port
	 * @param opCode
	 *            : operation Code ( 0 : read sInfo / 2: file download / 3: chk
	 *            device / 4 : add user /
	 * @param userCode
	 * @param andHandler
	 */
	public ConnectionManager(String srvIp, int port, int opCode, String userCode,
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

			// Log.i("eee", "opCode :" + Integer.toString(opCode));

			switch (this.opCode) {
			case 0: // 기기 code 에 따라 스냅샷 정보 조회
				// Snapshot 정보조회
				/*
				 * 1. opcode 포함 payload 전송 2. Snapshot Object 수신
				 */

				// 1 - 1 .. Server에 Connect 시 auth Code 전송
				// oos.writeObject(authCode); // authCode == userCode
				Payload pl = new Payload(0, authCode);
				oos.writeObject(pl);

				Calendar time = Calendar.getInstance();
				String today = (new SimpleDateFormat("yyyyMMddHHmm")
						.format(time.getTime()));
				System.out.println(today);

				// Date 전송
				oos.writeObject(today);

				try {
					ois = new ObjectInputStream(sc.getInputStream());

					// 스냅샷 파일 리스트
					int len = ois.readInt(); // 파일 갯수를 넘겨받음.

					snapshotList = new File[len];
					// Log.e("eee", Integer.toString(len));

					for (int i = 0; i < len; i++) {
						snapshotList[i] = (File) ois.readObject();
					}
					MainActivity.snapshotListInSrv = snapshotList.clone();

					Snapshot ss = (Snapshot) ois.readObject(); // 스냅샷 읽기

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					// Log.e("eee", "Loading error");
					e.printStackTrace();
				} finally {

					ois.close();
					oos.close();
					// 정보 조회가 끝남을 알림. Snapshot List 업데이트됨
					// looper 필요한가?
					andHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							andHandler.sendEmptyMessage(100);
						}
					});

				}

				break;
			case 1: // 파일 업로드
				if(filePath == null){
					Log.d("eee", "filePath  null ");
					FileSender fs = new FileSender(MainActivity.homePath,
							this.sc);

					// File 전송 절차
					/*
					 * 1. 소켓연결 , FileSender 초기화(앞에서 미리 실행) 2. Opcode 포함한 Payload
					 * 를 Object output Stream으로 전송 전송 3. HomeDir내의 파일 개수를 전송 4.
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
					// Log.i("eee", Integer.toString(FileCount));
					oos.writeObject(FileCount); // 파일 갯수

					// 4. HomeDir내의 파일 정보들을 모두 전송
					for (int i = 0; i < snapshotList.length; i++) {
						if (snapshotList[i].isFile()) {
							oos.writeLong(snapshotList[i].length()); // file
																		// Size
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
				}else{
					Log.d("eee", "filePath isn't null ");
					FileSender fs = new FileSender(this.sc);
					
					Log.d("eee", "filePath // "  + this.filePath);
					
					File f = new File(this.filePath);
					
					pl = new Payload(1, authCode);
					oos.writeObject(pl);
					
					
					if(f.exists()){
						Log.d("eee", "파일 존재");
					}else{
						Log.d("eee", "파일 없음");
					}
					oos.writeObject(1); // 단일 파일 전송

					oos.writeLong(f.length()); // file Size
					oos.writeObject(f);
					oos.writeObject(f.getName());
					
					fs.sendFile(f.getName()); // 파일 전송
					
				}
				

				break;
			case 2: // file download

				pl = new Payload(2, authCode);
				oos.writeObject(pl);

				break;
			case 3: // chk Device ( 등록여부 체크 )

				pl = new Payload(3, authCode);
				oos.writeObject(pl); // 등록여부 확인 요청

				break;
			case 4: // add user ( 사용자 등록 )
				pl = new Payload(4, authCode);
				oos.writeObject(pl);
				break;
			case 5: // get user Information ( 사용자 정보 조회 )

				pl = new Payload(5, authCode);
				oos.writeObject(pl);

				break;

			case 6: // 이미지 스트림 업로드
				Log.i("lvm2", "image stream payload transfer");
				pl = new Payload(6, authCode);
				oos.writeObject(pl); // payload 전송

				// 현재 클릭한 스냅샷에 대한 정보를 통해 사용자 데이터를 구축
				Snapshot ssData = new Snapshot(authCode);

				// this.itemName : 스냅샷 이름

				// 스냅샷 정보를 읽고 같이 업로드 해준다.

				SnapshotInfoReader sir = new SnapshotInfoReader(this.itemName); // 업로드
																				// 할
																				// 스냅샷
																				// 데이터를
																				// 읽음
				SnapshotInfoLists sInfoLists = sir.getSnapshotInfo(); // 스냅샷 정보를
																		// 구성해서
																		// 읽어들임.

				// 스냅 샷 데이터 변경정보 입력 필요

				// JsonWriter jw = new JsonWriter(new Writer());

				// 1. 어플리케이션 변경정보
				String changedItem1 = null,
				changedItem2 = null,
				changedItem3 = null;
				SnapshotAlteration sa = new SnapshotAlteration(); // 변경을 읽어내는
																	// Alteration
																	// 객체

				changedItem1 = sa.getStrAppAlteration(this.itemName , null ); // itemName
																		// ==
																		// sName;
				ssData.setAppChanged(changedItem1);

				// 2. 사용자 데이터 변경정보
				changedItem2 = sa.getUserDataStrAlteration(this.itemName); // 사용자데이터의
																			// 변경사항을
																			// 읽음.
				
				ssData.setUserDataChanged(changedItem2);

				// 3. Contacts, Setting 변경정보
				changedItem3 = sa.getSettingStrAlteration(this.itemName);
				ssData.setSettingValChanged(changedItem3);
				
				

				// sInfoList 에 있는 데이터들을 ssData 에 입력 (snapshot 객체화 )
				ssData.setInfoLists(sInfoLists);

				// Snapshot 정보 전송
				oos.writeObject(ssData);

				// 스냅샷 이미지
				SnapshotImageMaker sim = new SnapshotImageMaker(this.itemName,
						oos);
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
			case 7: // get Snapshot Altered Info
				pl = new Payload(7, authCode);
				oos.writeObject(pl);
				oos.writeObject(sName);
				oos.writeObject(itemName);

				ois = new ObjectInputStream(sc.getInputStream());
				// authCode에 해당하는 snapshot 데이터를 읽어온다.
				
				
				Message msg = andHandler.obtainMessage();

				try {
					// uData String 을 그대로 읽어옴
					StringBuffer uData = (StringBuffer) ois.readObject();

					// sName : 스냅샷 이름
					try {
						JSONObject jsonObj = new JSONObject(uData.toString());
						JSONObject sObj = (JSONObject) jsonObj.get("sList"); // snapshot
																				// 리스트
						Log.e("eee", sObj.toString());

						msg.what = 0;
						msg.obj = sObj.toString();
					
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// mName : 원하는 정보
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				andHandler.sendMessage(msg); // 조회한 snapshot 정보를 핸들러로 전달

				break;
				
			case 8: //파일 업로드
				
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
