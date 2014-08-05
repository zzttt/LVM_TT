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
			case 0: // ��� code �� ���� ������ ���� ��ȸ
				// Snapshot ������ȸ
				/*
				 * 1. opcode ���� payload ���� 2. Snapshot Object ����
				 */

				// 1 - 1 .. Server�� Connect �� auth Code ����
				// oos.writeObject(authCode); // authCode == userCode
				Payload pl = new Payload(0, authCode);
				oos.writeObject(pl);

				Calendar time = Calendar.getInstance();
				String today = (new SimpleDateFormat("yyyyMMddHHmm")
						.format(time.getTime()));
				System.out.println(today);

				// Date ����
				oos.writeObject(today);

				try {
					ois = new ObjectInputStream(sc.getInputStream());

					// ������ ���� ����Ʈ
					int len = ois.readInt(); // ���� ������ �Ѱܹ���.

					snapshotList = new File[len];
					// Log.e("eee", Integer.toString(len));

					for (int i = 0; i < len; i++) {
						snapshotList[i] = (File) ois.readObject();
					}
					MainActivity.snapshotListInSrv = snapshotList.clone();

					Snapshot ss = (Snapshot) ois.readObject(); // ������ �б�

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					// Log.e("eee", "Loading error");
					e.printStackTrace();
				} finally {

					ois.close();
					oos.close();
					// ���� ��ȸ�� ������ �˸�. Snapshot List ������Ʈ��
					// looper �ʿ��Ѱ�?
					andHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							andHandler.sendEmptyMessage(100);
						}
					});

				}

				break;
			case 1: // ���� ���ε�
				if(filePath == null){
					Log.d("eee", "filePath  null ");
					FileSender fs = new FileSender(MainActivity.homePath,
							this.sc);

					// File ���� ����
					/*
					 * 1. ���Ͽ��� , FileSender �ʱ�ȭ(�տ��� �̸� ����) 2. Opcode ������ Payload
					 * �� Object output Stream���� ���� ���� 3. HomeDir���� ���� ������ ���� 4.
					 * HomeDir���� ���� �������� ��� ���� 5. FileSender�� ���� ��������
					 */
					// 2
					
					pl = new Payload(1, authCode);
					oos.writeObject(pl);

					// 3. HomeDir���� ���� ������ ����
					File toSendFile = new File(MainActivity.homePath);
					File[] snapshotList = toSendFile.listFiles();
					int FileCount = 0;

					for (int i = 0; i < snapshotList.length; i++) {
						if (snapshotList[i].isFile()) {
							FileCount++;
						}
					}
					// Log.i("eee", Integer.toString(FileCount));
					oos.writeObject(FileCount); // ���� ����

					// 4. HomeDir���� ���� �������� ��� ����
					for (int i = 0; i < snapshotList.length; i++) {
						if (snapshotList[i].isFile()) {
							oos.writeLong(snapshotList[i].length()); // file
																		// Size
							oos.writeObject(snapshotList[i]);
							oos.writeObject(snapshotList[i].getName());
						}
					}

					// 5. FileSender�� ���� ��������
					for (int i = 0; i < snapshotList.length; i++) {
						if (snapshotList[i].isFile()) {
							System.out.println("������ ���ϸ� : " + snapshotList[i]);
							fs.sendFile(snapshotList[i].getName()); // ���� ����
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
						Log.d("eee", "���� ����");
					}else{
						Log.d("eee", "���� ����");
					}
					oos.writeObject(1); // ���� ���� ����

					oos.writeLong(f.length()); // file Size
					oos.writeObject(f);
					oos.writeObject(f.getName());
					
					fs.sendFile(f.getName()); // ���� ����
					
				}
				

				break;
			case 2: // file download

				pl = new Payload(2, authCode);
				oos.writeObject(pl);

				break;
			case 3: // chk Device ( ��Ͽ��� üũ )

				pl = new Payload(3, authCode);
				oos.writeObject(pl); // ��Ͽ��� Ȯ�� ��û

				break;
			case 4: // add user ( ����� ��� )
				pl = new Payload(4, authCode);
				oos.writeObject(pl);
				break;
			case 5: // get user Information ( ����� ���� ��ȸ )

				pl = new Payload(5, authCode);
				oos.writeObject(pl);

				break;

			case 6: // �̹��� ��Ʈ�� ���ε�
				Log.i("lvm2", "image stream payload transfer");
				pl = new Payload(6, authCode);
				oos.writeObject(pl); // payload ����

				// ���� Ŭ���� �������� ���� ������ ���� ����� �����͸� ����
				Snapshot ssData = new Snapshot(authCode);

				// this.itemName : ������ �̸�

				// ������ ������ �а� ���� ���ε� ���ش�.

				SnapshotInfoReader sir = new SnapshotInfoReader(this.itemName); // ���ε�
																				// ��
																				// ������
																				// �����͸�
																				// ����
				SnapshotInfoLists sInfoLists = sir.getSnapshotInfo(); // ������ ������
																		// �����ؼ�
																		// �о����.

				// ���� �� ������ �������� �Է� �ʿ�

				// JsonWriter jw = new JsonWriter(new Writer());

				// 1. ���ø����̼� ��������
				String changedItem1 = null,
				changedItem2 = null,
				changedItem3 = null;
				SnapshotAlteration sa = new SnapshotAlteration(); // ������ �о��
																	// Alteration
																	// ��ü

				changedItem1 = sa.getStrAppAlteration(this.itemName , null ); // itemName
																		// ==
																		// sName;
				ssData.setAppChanged(changedItem1);

				// 2. ����� ������ ��������
				changedItem2 = sa.getUserDataStrAlteration(this.itemName); // ����ڵ�������
																			// ���������
																			// ����.
				
				ssData.setUserDataChanged(changedItem2);

				// 3. Contacts, Setting ��������
				changedItem3 = sa.getSettingStrAlteration(this.itemName);
				ssData.setSettingValChanged(changedItem3);
				

				// sInfoList �� �ִ� �����͵��� ssData �� �Է� (snapshot ��üȭ )
				ssData.setInfoLists(sInfoLists);

				// Snapshot ���� ����
				oos.writeObject(ssData);

				// ������ �̹���
				SnapshotImageMaker sim = new SnapshotImageMaker(this.itemName,
						oos);
				sim.start();

				try {
					sim.join(); // ������ ���
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("lvm2", "�̹��� ���� ����");

				pd.cancel();

				break;
			case 7: // get Snapshot Altered Info
				pl = new Payload(7, authCode);
				oos.writeObject(pl);
				oos.writeObject(sName);
				oos.writeObject(itemName);

				ois = new ObjectInputStream(sc.getInputStream());
				// authCode�� �ش��ϴ� snapshot �����͸� �о�´�.
				
				
				Message msg = andHandler.obtainMessage();

				try {
					// uData String �� �״�� �о��
					StringBuffer uData = (StringBuffer) ois.readObject();

					// sName : ������ �̸�
					try {
						JSONObject jsonObj = new JSONObject(uData.toString());
						JSONObject sObj = (JSONObject) jsonObj.get("sList"); // snapshot
																				// ����Ʈ
						Log.e("eee", sObj.toString());

						msg.what = 0;
						msg.obj = sObj.toString();
					
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// mName : ���ϴ� ����
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				andHandler.sendMessage(msg); // ��ȸ�� snapshot ������ �ڵ鷯�� ����

				break;
				
			case 8: //���� ���ε�
				
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