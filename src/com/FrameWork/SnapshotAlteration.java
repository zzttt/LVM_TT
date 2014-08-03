package com.FrameWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.Toast;

import com.FileManager.FileInfo;

public class SnapshotAlteration {
	
	ArrayList<FileInfo> fiList = new ArrayList<FileInfo>(); // ���Ϻ������� ����� ���� ����Ʈ
	
	public SnapshotAlteration() {

	}
	
	public String getSettingStrAlteration(String sName) {
		fiList = getAppAlteration(sName);
		StringBuffer result = new StringBuffer();

		for( int i = 0 ; i < 3  && i < fiList.size() ; i++) // item �� 3���� ����
			result.append(fiList.get(i).getName()+"\n");
		
		return result.toString();
	}
	
	
	/**
	 * ����� ������ ��������� String ������ ��ȯ
	 * @param sName
	 * @return
	 */
	public String getUserDataStrAlteration(String sName) {
		fiList = getUserDataAlteration(sName);
		StringBuffer result = new StringBuffer();

		for( int i = 0 ; i < 3  && i < fiList.size() ; i++)
			result.append(fiList.get(i).getName()+"\n");
		
		return result.toString();
	}
	
	public String getStrAppAlteration(String sName) {
		fiList = getAppAlteration(sName);
		StringBuffer result = new StringBuffer();

		
		for( int i = 0 ; i < 3 && i < fiList.size() ; i++)
			result.append(fiList.get(i).getName()+"\n");
		
		return result.toString();
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�.
	 * Application�� ���������� �����ͷ� ����
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public ArrayList<FileInfo> getAppAlteration(String sName) {
		String result = null;


		return fiList;
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�.
	 * ����� �������� ���������� ����
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public ArrayList<FileInfo> getUserDataAlteration(String sName) {
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

			
			// ���κ� �Ľ�
			for (String s : lineArr) {

				String[] info = s.split(" ");
				ArrayList<String> splitedInfo = new ArrayList<String>();

				for (String ss : info) {
					ss = ss.trim();
					if (ss.length() != 0)
						splitedInfo.add(ss);
				}

				FileInfo fi;
				// split ����� ���� ������ ���� , ���� ���丮 �̸� ���� ��������.
				// �������丮 �̸��� �����Ѵ�
				int idx = 0;

				char fileType = ' ';

				if (splitedInfo.size() != 0) { // �� ������ ����
												// ù��° ���ڴ�
												// ���� ������
												// ��Ÿ��..
					fileType = splitedInfo.get(0).charAt(0);
					// Log.d("lvm",
					// "("+String.valueOf(fileType)+")");

					if (fileType == 'l') { // ��ũ������ ��� ���ϸ�
											// ���� �ʿ� ( idx 5
											// ���� fileName..
											// 5 ���� ���ڿ��� ����
											// )
						String fName = splitedInfo.get(5)
								+ splitedInfo.get(6)
								+ splitedInfo.get(7);
						splitedInfo.set(5, fName);
						splitedInfo.remove(7);
						splitedInfo.remove(6);
					}

				}

				if (fileType == 'd' || fileType == 'b'
						|| fileType == 'c'
						|| fileType == 'p'
						|| fileType == 'l'
						|| fileType == 's') { // special
												// files
					// b(Block file(b) , Character device
					// file(c) , Named pipe file or just a
					// pipe file(p)
					// Symbolic link file(l), Socket file(s)

					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5));
					fiList.add(fi); // fiList �� ���
				} else if (fileType == '-') { // general
												// files
					// general file���� �뷮�������� ���� ��.
					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5), splitedInfo
									.get(6));
					fiList.add(fi); // fiList �� ���
				} else { // directory ������ ��ü�� ���� �������� ����.
							// nothing to do
				}

			}
			
			
			
			
			
			try {
				p.waitFor();
				if (p.exitValue() != 255) {
					// TODO Code to run on success
/*					Toast.makeText(vv.getContext(), "root",
							Toast.LENGTH_SHORT).show();*/
				} else {
					// TODO Code to run on unsuccessful
					/*Toast.makeText(vv.getContext(),
							"not root", Toast.LENGTH_SHORT)
							.show();*/
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				/*Toast.makeText(vv.getContext(), "not root",
						Toast.LENGTH_SHORT).show();*/
			}

			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return fiList;
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�.
	 * �������� ��ȭ�� �����ͷ� ����.
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public ArrayList<FileInfo> getSettingAlteration(String sName) {
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
			
			
			// ���κ� �Ľ�
			for (String s : lineArr) {

				String[] info = s.split(" ");
				ArrayList<String> splitedInfo = new ArrayList<String>();

				for (String ss : info) {
					ss = ss.trim();
					if (ss.length() != 0)
						splitedInfo.add(ss);
				}

				FileInfo fi;
				// split ����� ���� ������ ���� , ���� ���丮 �̸� ���� ��������.
				// �������丮 �̸��� �����Ѵ�
				int idx = 0;

				char fileType = ' ';

				if (splitedInfo.size() != 0) { // �� ������ ����
												// ù��° ���ڴ�
												// ���� ������
												// ��Ÿ��..
					fileType = splitedInfo.get(0).charAt(0);
					// Log.d("lvm",
					// "("+String.valueOf(fileType)+")");

					if (fileType == 'l') { // ��ũ������ ��� ���ϸ�
											// ���� �ʿ� ( idx 5
											// ���� fileName..
											// 5 ���� ���ڿ��� ����
											// )
						String fName = splitedInfo.get(5)
								+ splitedInfo.get(6)
								+ splitedInfo.get(7);
						splitedInfo.set(5, fName);
						splitedInfo.remove(7);
						splitedInfo.remove(6);
					}

				}

				if (fileType == 'd' || fileType == 'b'
						|| fileType == 'c'
						|| fileType == 'p'
						|| fileType == 'l'
						|| fileType == 's') { // special
												// files
					// b(Block file(b) , Character device
					// file(c) , Named pipe file or just a
					// pipe file(p)
					// Symbolic link file(l), Socket file(s)

					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5));
					fiList.add(fi); // fiList �� ���
				} else if (fileType == '-') { // general
												// files
					// general file���� �뷮�������� ���� ��.
					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5), splitedInfo
									.get(6));
					fiList.add(fi); // fiList �� ���
				} else { // directory ������ ��ü�� ���� �������� ����.
							// nothing to do
				}

			}
			
			
			
			
			
			try {
				p.waitFor();
				if (p.exitValue() != 255) {
					// TODO Code to run on success
/*					Toast.makeText(vv.getContext(), "root",
							Toast.LENGTH_SHORT).show();*/
				} else {
					// TODO Code to run on unsuccessful
					/*Toast.makeText(vv.getContext(),
							"not root", Toast.LENGTH_SHORT)
							.show();*/
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				/*Toast.makeText(vv.getContext(), "not root",
						Toast.LENGTH_SHORT).show();*/
			}

			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return fiList;
	}

}
