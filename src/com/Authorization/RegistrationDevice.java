package com.Authorization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import com.FrameWork.ConnServer;
import com.FrameWork.Payload;
import com.example.timetraveler.MainActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class RegistrationDevice {

	private String userCode; // 사용자 코드
	private Handler handler; // 핸들러

	/**
	 * 
	 * @param mng : wifiManager
	 */
	public RegistrationDevice(WifiManager mng, Handler handler) {

		WifiInfo info = mng.getConnectionInfo();

		// MAC 이용 인증코드 생성
		CodeGenerator cg = new CodeGenerator(info.getMacAddress());

		// 사용자 코드 생성
		userCode = cg.genCode();
		
		this.handler = handler;

	}

	public String getUserCode() {
		return this.userCode;
	}

	/**
	 * 소켓에 연결 후 사용자 등록 여부 확인
	 */
	public boolean chkUserOnSrv() { // 서버에 사용자 등록여부 확인

		userCode = this.getUserCode(); // 기기코드

		// 사용자 확인 . opcode 3
		ConnServer conn = new ConnServer(MainActivity.srvIp, 12345, 3, userCode , handler);
		conn.start();

		try {
			conn.join();
			ObjectInputStream ois = new ObjectInputStream(conn.getSocket().getInputStream());
			
			if(ois.readBoolean()){ // true 면 등록된 기기를 의미함
				System.out.println("등록된 기기");
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
	 * 사용자 정보 생성
	 */
	public void createUser() { // 사용자 정보 생성
		
		ConnServer conn = new ConnServer(MainActivity.srvIp , 12345 , 4 , userCode, handler);
		conn.start();

	}

	/**
	 * 사용자 정보 읽기
	 */
	public void getUserInfo() {
		ConnServer conn = new ConnServer(MainActivity.srvIp , 12345 , 5, userCode, handler);
		conn.start();
	}

}
