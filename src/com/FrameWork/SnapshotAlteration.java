package com.FrameWork;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.FileManager.FileInfo;

public class SnapshotAlteration {

	ArrayList<FileInfo> fiList = new ArrayList<FileInfo>(); // ���Ϻ������� ����� ���� ����Ʈ

	public SnapshotAlteration() {

	}

	public String getSettingStrAlteration(String sName) {

		return getSettingAlteration(sName).toString();
	}

	/**
	 * ����� ������ ��������� String ������ ��ȯ
	 * 
	 * @param sName
	 * @return
	 */
	public String getUserDataStrAlteration(String sName) {
		fiList = getUserDataAlteration(sName);
		StringBuffer result = new StringBuffer();

		// ------------ �о�� ����Ʈ�� �����Ѵ� --------------
		Collections.sort(fiList, date); // ��¥��
										// ����
		Collections.sort(fiList, time);

		Collections.reverse(fiList);

		/*
		 * for( int i = 0 ;i < fiList.size() ; i++) Log.w("ddd",
		 * fiList.get(i).getType
		 * ()+"//"+fiList.get(i).getName()+"//"+fiList.get(i
		 * ).getDate()+"//"+fiList.get(i).getTime());
		 */
		for (int i = 0; i < 3 && i < fiList.size(); i++) {

			if (!fiList.get(i).getType().equals("d")) {
				result.append(fiList.get(i).getName() + "\n( time : "
						+ fiList.get(i).getDate() + " "
						+ fiList.get(i).getTime() + ")" + "\n\n");
			}

		}

		return result.toString();
	}

	public String getStrAppAlteration(String sName , Context context) {
		/*		fiList = getAppAlteration(sName, context);
		StringBuffer result = new StringBuffer();

		Collections.sort(fiList, date); // ��¥��
		// ����
		Collections.sort(fiList, time);

		Collections.reverse(fiList);

		for (int i = 0; i < 3 && i < fiList.size(); i++) {

			if (!fiList.get(i).getType().equals("d")) {
				result.append(fiList.get(i).getName() + "\n( time : "
						+ fiList.get(i).getDate() + " "
						+ fiList.get(i).getTime() + ")" + "\n\n");
			}

		}*/
		
		
		return "zz";
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�. Application�� ���������� �����ͷ� ����
	 * 
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public ArrayList<FileInfo> getAppAlteration(String sName, Context context) {
		StringBuffer result = new StringBuffer();
		
		ArrayList<FileInfo> appFiList = new ArrayList<FileInfo>();
		
		InstalledAppInfo mInsAppInfo = new InstalledAppInfo(context);
		 // �ֱ� ��������� Message�� ���.
        /**
         * ������ ��� ������ ��ġ�Ǿ� �ִ� ���ø����̼� ��Ȳ ���
         */
        // ������� Read
        ArrayList<String> changedList = new ArrayList<String>();
        //������ ������ ��ġ�� ���� arylist �ҷ���
        //�׽�Ʈ�� ���� �ϵ��ڵ� -- ABC�� ����
        ArrayList<InstalledAppInfo> InSsApp = mInsAppInfo.ReadAppInfo(sName);
        StringBuffer sbMessage = new StringBuffer();
        int vListSize = 0;
        
        //���ø���Ʈ ���
        for(int i=0;i<InSsApp.size();i++) {
           changedList.add(InSsApp.get(i).resultOfAppNamePrint());
           vListSize++;
           sbMessage.append(vListSize+") "+InSsApp.get(i).resultOfAppNamePrint()+"\n");
           
           Log.d("AppName", "aa"+InSsApp.get(i).resultOfAppNamePrint());
         //FileInfo fi = new FileInfo("-","rwxrwxrwx" , "",InSsApp.get(i).getInstallTimePrint().substring(0,8) ,InSsApp.get(i).getInstallTimePrint().substring(8,12),
           FileInfo fi = new FileInfo("-","rwxrwxrwx" , "",InSsApp.get(i).getInstallTimePrint().substring(0,10) ,InSsApp.get(i).getInstallTimePrint().substring(11,16),
           InSsApp.get(i).resultOfAppNamePrint() );
           
           //FileInfo(String fType, String permission, String fSize,
			//String mDate, String mTime, String fName) {
           appFiList.add(fi);
        }

        
        
		return appFiList;
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�. ����� �������� ���������� ����
	 * 
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	public ArrayList<FileInfo> getUserDataAlteration(String sName) {
		String result = null;

		sName = sName+"_usersdcard";
		
		
		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot �� ext4 �� ����Ʈ�Ͽ� ������ Ȯ���Ѵ�.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root �������¿��� ls -lR (sub directory ���� read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(com.getBytes());

			mountCom = "umount /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(mountCom.getBytes());

			
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
						String fName = splitedInfo.get(5) + splitedInfo.get(6)
								+ splitedInfo.get(7);
						splitedInfo.set(5, fName);
						splitedInfo.remove(7);
						splitedInfo.remove(6);
					}

				}

				if (fileType == 'd' || fileType == 'b' || fileType == 'c'
						|| fileType == 'p' || fileType == 'l'
						|| fileType == 's') { // special
												// files
					// b(Block file(b) , Character device
					// file(c) , Named pipe file or just a
					// pipe file(p)
					// Symbolic link file(l), Socket file(s)

					fi = new FileInfo(String.valueOf(fileType), splitedInfo
							.get(0).substring(1), splitedInfo.get(3),
							splitedInfo.get(4), splitedInfo.get(5));
					fiList.add(fi); // fiList �� ���
				} else if (fileType == '-') { // general
												// files
					// general file���� �뷮�������� ���� ��.

					StringBuffer fileName = new StringBuffer();
					String strFileName;
					int maxIdx = splitedInfo.size();

					for (int i = 6; i < maxIdx; i++) {
						fileName.append(splitedInfo.get(i));
					}

					strFileName = fileName.toString();
					fi = new FileInfo(String.valueOf(fileType), splitedInfo
							.get(0).substring(1), splitedInfo.get(3),
							splitedInfo.get(4), splitedInfo.get(5), strFileName);
					// Ȯ���ڸ� �̿��� üũ
					if (strFileName.contains(".txt")
							|| strFileName.contains(".avi")
							|| strFileName.contains(".xml")
							|| strFileName.contains(".mp3")
							|| strFileName.contains(".mp4")
							|| strFileName.contains(".gif")
							|| strFileName.contains(".jpeg")
							|| strFileName.contains(".jpg")
							|| strFileName.contains(".img")
							|| strFileName.contains(".png")
							|| strFileName.contains(".bmp")
							|| strFileName.contains(".pdf")
							|| strFileName.contains(".hwp")
							|| strFileName.contains(".html")
							|| strFileName.contains(".gul")
							|| strFileName.contains(".php")
							|| strFileName.contains(".html")
							|| strFileName.contains(".tar")
							|| strFileName.contains(".jar")
							|| strFileName.contains(".zip")
							|| strFileName.contains(".gz")
							|| strFileName.contains(".xls")
							|| strFileName.contains(".wmf")
							|| strFileName.contains(".wma")
							|| strFileName.contains(".xcf")
							|| strFileName.contains(".wav")
							|| strFileName.contains(".svg")
							|| strFileName.contains(".rc")
							|| strFileName.contains(".rar")
							|| strFileName.contains(".mpeg")
							|| strFileName.contains(".gsp")
							|| strFileName.contains(".fon")
							|| strFileName.contains(".exp")
							|| strFileName.contains(".bak")
							|| strFileName.contains(".aac")
							|| strFileName.contains(".ac3")
							|| strFileName.contains(".ai")
							|| strFileName.contains(".alz")
							|| strFileName.contains(".asp")
							|| strFileName.contains(".jpe")
							|| strFileName.contains(".java")
							|| strFileName.contains(".mpg")
							|| strFileName.contains(".swf"))
						fiList.add(fi); // fiList �� ���
				} else { // directory ������ ��ü�� ���� �������� ����.
							// nothing to do
				}

			}

			try {
				p.waitFor();
				if (p.exitValue() != 255) {
					// TODO Code to run on success
					/*
					 * Toast.makeText(vv.getContext(), "root",
					 * Toast.LENGTH_SHORT).show();
					 */
				} else {
					// TODO Code to run on unsuccessful
					/*
					 * Toast.makeText(vv.getContext(), "not root",
					 * Toast.LENGTH_SHORT) .show();
					 */
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				/*
				 * Toast.makeText(vv.getContext(), "not root",
				 * Toast.LENGTH_SHORT).show();
				 */
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // root ��

		return fiList;
	}

	/**
	 * sName�� �ش��ϴ� FileInfoList�� ��´�. �������� ��ȭ�� �����ͷ� ����.
	 * 
	 * @param sName
	 *            // ��ȭ������ �˰��� �ϴ� ������ �̸�
	 * @return
	 */
	// setting �� ���� //
	public StringBuilder getSettingAlteration(String sName) {
		String result = null;
		int end = 0;
		StringBuilder sb = new StringBuilder();
		SystemSetting SysSet = new SystemSetting(null);

		// DatabaseHandler dh = new DatabaseHandler(context);
		// dh.getContactsCount();
		//
		// ���⼭ ������ ������ ������ ����Ʈ �ؾ��� //
		Process p;
		SystemSetting ss = new SystemSetting(null);
		ss.set_permission();
		
		try {
			p = Runtime.getRuntime().exec("su");
			// snapshot �� ext4 �� ����Ʈ�Ͽ� ������ Ȯ���Ѵ�.
			// String mountCom = "mount -t ext4 /dev/vg/" + sName +
			// " /sdcard/ssDir/" + sName + "\n";
			// p.getOutputStream().write(mountCom.getBytes());

			// String copy_file = "cp /dev/vg/"+sName+"/sdcard/ssDir"+sName
			// " /storage/sdcard0/password.key";
			p.getOutputStream().write(("mount -t ext4 /dev/vg/"+sName+"_usersystem /sdcard/ssDir/"+sName+"_usersystem\n").getBytes());
				
			 String copy_password = "cp /sdcard/ssDir/"+sName+"_usersystem/system/password.key /sdcard/password.key\n";
			 p.getOutputStream().write(copy_password.getBytes());
			 
			 String copy_wifi = "cp /sdcard/ssDir/"+sName+"_usersystem/misc/wifi/wpa_supplicant.conf /sdcard/wpa_supplicant.conf\n";
			 p.getOutputStream().write(copy_wifi.getBytes());
			
			 String copy_gesture = "cp /sdcard/ssDir/"+sName+"_usersystem/system/gesture.key /sdcard/gesture.key\n";
			 p.getOutputStream().write(copy_gesture.getBytes());
			   
			 p.getOutputStream().write(("umount /sdcard/ssDir/"+sName+"_usersystem\n").getBytes());
			 
			 p.getOutputStream().write("exit\n".getBytes());
	         p.getOutputStream().flush();
	         p.getOutputStream().close();
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if (SystemSetting.sdcard_passwordkey_file.length() == 0 ) { // ����Ʈ�� ���
			sb.append("1) PIN_��й�ȣ     : ��������� �ʾ��� \n");
		} else {
			if (SysSet.compare(SystemSetting.sdcard_passwordkey_file, SystemSetting.passwordkey_file)) //
			{
				sb.append("1) PIN_��й�ȣ      : ������� ����\n");
			} else {
				sb.append("1) PIN_��й�ȣ      : ������� ����\n");
			}
		}
		// ������ �������� ��������� �ʾ���
		if (SystemSetting.sdcard_gesture_file.length() == 0) { // ������ �������� ������
																// ��������� �ʾ���
			sb.append("2) ����_��й�ȣ    : ��������� �ʾ��� \n");
		} else {
			if (SysSet.compare(SystemSetting.sdcard_gesture_file, SystemSetting.gesturekey_file)) //
			{
				sb.append("2) ����_��й�ȣ    : ������� ����\n");
			} else {
				sb.append("2) ����_��й�ȣ    : ������� ����\n");
			}
		}

		if (SysSet.compare(SystemSetting.sdcard_wifi_file, SystemSetting.wifi_file)) //
		{
			sb.append("3) WiFi_����        : ������� ����\n");
		} else {
			sb.append("3) WiFi_����        : ������� ����\n");
		}
		return sb;
	}

	private final Comparator<FileInfo> date = new Comparator<FileInfo>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			return collator.compare(object1.getDate(), object2.getDate()); // ��������
																			// ����

		}
	};

	private final Comparator<FileInfo> time = new Comparator<FileInfo>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			return collator.compare(object1.getTime(), object2.getTime()); // ��������

		}
	};
	
	
}
