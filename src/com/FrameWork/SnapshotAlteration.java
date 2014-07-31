package com.FrameWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SnapshotAlteration {

	public SnapshotAlteration() {

	}

	/**
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public String getAppAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot 을 ext4 로 마운트하여 내용을 확인한다.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root 계정상태에서 ls -lR (sub directory 까지 read)
			// ----------------------- 마운트 어디에다 할거야?
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(com.getBytes());

			// root 종료
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // 명령의
																	// 결과
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// 라인별로 읽기 시작한다.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root 쉘

		return result;
	}

	/**
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public String getUserDataAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot 을 ext4 로 마운트하여 내용을 확인한다.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root 계정상태에서 ls -lR (sub directory 까지 read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(com.getBytes());

			// root 종료
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // 명령의
																	// 결과
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// 라인별로 읽기 시작한다.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root 쉘

		return result;
	}

	/**
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public String getSettingAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot 을 ext4 로 마운트하여 내용을 확인한다.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root 계정상태에서 ls -lR (sub directory 까지 read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(com.getBytes());

			// root 종료
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // 명령의
																	// 결과
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// 라인별로 읽기 시작한다.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root 쉘

		return result;
	}

}
