package com.FrameWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.Toast;

import com.FileManager.FileInfo;

public class SnapshotAlteration {
	
	ArrayList<FileInfo> fiList = new ArrayList<FileInfo>(); // 파일변경정보 출력을 위한 리스트
	
	public SnapshotAlteration() {

	}

	/**
	 * sName에 해당하는 FileInfoList를 얻는다.
	 * Application의 변동사항을 데이터로 받음
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public ArrayList<FileInfo> getAppAlteration(String sName) {
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
					fiList.add(fi); // fiList 에 등록
				} else if (fileType == '-') { // general
												// files
					// general file에는 용량정보까지 포함 됨.
					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5), splitedInfo
									.get(6));
					fiList.add(fi); // fiList 에 등록
				} else { // directory 정보는 객체를 따로 저장하지 않음.
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
		} // root 쉘

		return fiList;
	}

	/**
	 * sName에 해당하는 FileInfoList를 얻는다.
	 * 사용자 데이터의 변동사항을 받음
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public ArrayList<FileInfo> getUserDataAlteration(String sName) {
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
					fiList.add(fi); // fiList 에 등록
				} else if (fileType == '-') { // general
												// files
					// general file에는 용량정보까지 포함 됨.
					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5), splitedInfo
									.get(6));
					fiList.add(fi); // fiList 에 등록
				} else { // directory 정보는 객체를 따로 저장하지 않음.
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
		} // root 쉘

		return fiList;
	}

	/**
	 * sName에 해당하는 FileInfoList를 얻는다.
	 * 설정값의 변화를 데이터로 받음.
	 * @param sName
	 *            // 변화내역을 알고자 하는 스냅샷 이름
	 * @return
	 */
	public ArrayList<FileInfo> getSettingAlteration(String sName) {
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
					fiList.add(fi); // fiList 에 등록
				} else if (fileType == '-') { // general
												// files
					// general file에는 용량정보까지 포함 됨.
					fi = new FileInfo(String
							.valueOf(fileType), splitedInfo
							.get(0).substring(1),
							splitedInfo.get(3), splitedInfo
									.get(4), splitedInfo
									.get(5), splitedInfo
									.get(6));
					fiList.add(fi); // fiList 에 등록
				} else { // directory 정보는 객체를 따로 저장하지 않음.
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
		} // root 쉘

		return fiList;
	}

}
