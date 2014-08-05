package com.Authorization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import com.FrameWork.ConnectionManager;
import com.FrameWork.Payload;
import com.example.timetraveler.MainActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class RegistrationDevice {

	private String userCode; // ����� �ڵ�
	private Handler handler; // �ڵ鷯

	/**
	 * 
	 * @param mng : wifiManager
	 */
	public RegistrationDevice(WifiManager mng, Handler handler) {

		WifiInfo info = mng.getConnectionInfo();

		// MAC �̿� �����ڵ� ����
		CodeGenerator cg = new CodeGenerator(info.getMacAddress());

		// ����� �ڵ� ����
		userCode = cg.genCode();
		
		this.handler = handler;

	}

	public String getUserCode() {
		return this.userCode;
	}

	/**
	 * ���Ͽ� ���� �� ����� ��� ���� Ȯ��
	 */
	public boolean chkUserOnSrv() { // ������ ����� ��Ͽ��� Ȯ��

		userCode = this.getUserCode(); // ����ڵ�

		// ����� Ȯ�� . opcode 3
		ConnectionManager conn = new ConnectionManager(MainActivity.srvIp, 12345, 3, userCode , handler);
		conn.start();
		

		try {
			conn.join();
			ObjectInputStream ois = new ObjectInputStream(conn.getSocket().getInputStream());
			
			if(ois.readBoolean()){ // true �� ��ϵ� ��⸦ �ǹ���
				Log.e("boolean", "�̹� ��ϵ� ���");
				ois.close();
				return true;
				
			}
			
			ois.close();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}

	/**
	 * ����� ���� ����
	 */
	public void createUser() { // ����� ���� ����
		
		ConnectionManager conn = new ConnectionManager(MainActivity.srvIp , 12345 , 4 , userCode, handler);
		conn.start();

	}

	/**
	 * ����� ���� �б�
	 */
	public void getUserInfo() {
		ConnectionManager conn = new ConnectionManager(MainActivity.srvIp , 12345 , 5, userCode, handler);
		conn.start();
	}

}
