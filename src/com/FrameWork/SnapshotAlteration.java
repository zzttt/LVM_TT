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
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public String getAppAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot �� ext4 �� ����Ʈ�Ͽ� ������ Ȯ���Ѵ�.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root �������¿��� ls -lR (sub directory ���� read)
			// ----------------------- ����Ʈ ��𿡴� �Ұž�?
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(com.getBytes());

			// root ����
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // �����
																	// ���
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// ���κ��� �б� �����Ѵ�.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return result;
	}

	/**
	 * 
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public String getUserDataAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot �� ext4 �� ����Ʈ�Ͽ� ������ Ȯ���Ѵ�.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root �������¿��� ls -lR (sub directory ���� read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(com.getBytes());

			// root ����
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // �����
																	// ���
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// ���κ��� �б� �����Ѵ�.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return result;
	}

	/**
	 * 
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public String getSettingAlteration(String sName) {
		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot �� ext4 �� ����Ʈ�Ͽ� ������ Ȯ���Ѵ�.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root �������¿��� ls -lR (sub directory ���� read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(com.getBytes());

			// root ����
			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// snapshot list load standard i/o
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			ArrayList<String> lineArr = new ArrayList<String>(); // �����
																	// ���
																	// String
																	// line
			// StringBuffer sTotalList = new StringBuffer();

			// ���κ��� �б� �����Ѵ�.
			while ((line = br.readLine()) != null) {
				// sTotalList.append(line+"\n");
				lineArr.add(line);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return result;
	}

}
