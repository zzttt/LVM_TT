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

	ArrayList<FileInfo> fiList = new ArrayList<FileInfo>(); // 파일변경정보 출력을 위한 리스트

	public SnapshotAlteration() {

	}

	public String getSettingStrAlteration(String sName) {

		return getSettingAlteration(sName).toString();
	}

	/**
	 * 사용자 데이터 변경사항을 String 을으로 반환
	 * 
	 * @param sName
	 * @return
	 */
	public String getUserDataStrAlteration(String sName) {
		fiList = getUserDataAlteration(sName);
		StringBuffer result = new StringBuffer();

		// ------------ 읽어온 리스트를 정렬한다 --------------
		Collections.sort(fiList, date); // 날짜별
										// 정렬
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

		Collections.sort(fiList, date); // 날짜별
		// 정렬
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
	 * sName에 해당하는 FileInfoList를 얻는다. Application의 변동사항을 데이터로 받음
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public ArrayList<FileInfo> getAppAlteration(String sName, Context context) {
		StringBuffer result = new StringBuffer();
		
		ArrayList<FileInfo> appFiList = new ArrayList<FileInfo>();
		
		InstalledAppInfo mInsAppInfo = new InstalledAppInfo(context);
		 // 최근 변경사항을 Message에 띄움.
        /**
         * 스냅샷 백업 시점에 설치되어 있던 어플리케이션 현황 출력
         */
        // 변경사항 Read
        ArrayList<String> changedList = new ArrayList<String>();
        //스냅샷 시점의 설치된 어플 arylist 불러옴
        //테스트를 위한 하드코딩 -- ABC로 저장
        ArrayList<InstalledAppInfo> InSsApp = mInsAppInfo.ReadAppInfo(sName);
        StringBuffer sbMessage = new StringBuffer();
        int vListSize = 0;
        
        //어플리스트 출력
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
	 * sName에 해당하는 FileInfoList를 얻는다. 사용자 데이터의 변동사항을 받음
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public ArrayList<FileInfo> getUserDataAlteration(String sName) {
		String result = null;

		sName = sName+"_usersdcard";
		
		
		try {
			Process p = Runtime.getRuntime().exec("su");

			// snapshot 을 ext4 로 마운트하여 내용을 확인한다.
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			// root 계정상태에서 ls -lR (sub directory 까지 read)
			String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(com.getBytes());

			mountCom = "umount /sdcard/ssDir/" + sName + "\n";
			p.getOutputStream().write(mountCom.getBytes());

			
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

			// 라인별 파싱
			for (String s : lineArr) {

				String[] info = s.split(" ");
				ArrayList<String> splitedInfo = new ArrayList<String>();

				for (String ss : info) {
					ss = ss.trim();
					if (ss.length() != 0)
						splitedInfo.add(ss);
				}

				FileInfo fi;
				// split 결과는 실제 파일의 정보 , 하위 디렉토리 이름 으로 나누어짐.
				// 하위디렉토리 이름은 무시한다
				int idx = 0;

				char fileType = ' ';

				if (splitedInfo.size() != 0) { // 한 라인의 가장
												// 첫번째 문자는
												// 파일 형식을
												// 나타냄..
					fileType = splitedInfo.get(0).charAt(0);
					// Log.d("lvm",
					// "("+String.valueOf(fileType)+")");

					if (fileType == 'l') { // 링크파일의 경우 파일명
											// 수정 필요 ( idx 5
											// 부터 fileName..
											// 5 이후 문자열을 통합
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
					fiList.add(fi); // fiList 에 등록
				} else if (fileType == '-') { // general
												// files
					// general file에는 용량정보까지 포함 됨.

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
					// 확장자를 이용한 체크
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
						fiList.add(fi); // fiList 에 등록
				} else { // directory 정보는 객체를 따로 저장하지 않음.
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
		} // root 쉘

		return fiList;
	}

	/**
	 * sName에 해당하는 FileInfoList를 얻는다. 설정값의 변화를 데이터로 받음.
	 * 
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	// setting 값 복원 //
	public StringBuilder getSettingAlteration(String sName) {
		String result = null;
		int end = 0;
		StringBuilder sb = new StringBuilder();
		SystemSetting SysSet = new SystemSetting(null);

		// DatabaseHandler dh = new DatabaseHandler(context);
		// dh.getContactsCount();
		//
		// 여기서 스냅샷 시점의 파일을 마운트 해야함 //
		Process p;
		SystemSetting ss = new SystemSetting(null);
		ss.set_permission();
		
		try {
			p = Runtime.getRuntime().exec("su");
			// snapshot 을 ext4 로 마운트하여 내용을 확인한다.
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


		if (SystemSetting.sdcard_passwordkey_file.length() == 0 ) { // 마운트된 경로
			sb.append("1) PIN_비밀번호     : 사용중이지 않았음 \n");
		} else {
			if (SysSet.compare(SystemSetting.sdcard_passwordkey_file, SystemSetting.passwordkey_file)) //
			{
				sb.append("1) PIN_비밀번호      : 변경사항 없음\n");
			} else {
				sb.append("1) PIN_비밀번호      : 변경사항 있음\n");
			}
		}
		// 스냅샷 시점에서 사용중이지 않았음
		if (SystemSetting.sdcard_gesture_file.length() == 0) { // 스냅샷 시점에서 패턴이
																// 사용중이지 않았음
			sb.append("2) 패턴_비밀번호    : 사용중이지 않았음 \n");
		} else {
			if (SysSet.compare(SystemSetting.sdcard_gesture_file, SystemSetting.gesturekey_file)) //
			{
				sb.append("2) 패턴_비밀번호    : 변경사항 없음\n");
			} else {
				sb.append("2) 패턴_비밀번호    : 변경사항 있음\n");
			}
		}

		if (SysSet.compare(SystemSetting.sdcard_wifi_file, SystemSetting.wifi_file)) //
		{
			sb.append("3) WiFi_설정        : 변경사항 없음\n");
		} else {
			sb.append("3) WiFi_설정        : 변경사항 있음\n");
		}
		return sb;
	}

	private final Comparator<FileInfo> date = new Comparator<FileInfo>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			return collator.compare(object1.getDate(), object2.getDate()); // 내림차순
																			// 정렬

		}
	};

	private final Comparator<FileInfo> time = new Comparator<FileInfo>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			return collator.compare(object1.getTime(), object2.getTime()); // 내림차순

		}
	};
	
	
}
