package com.FrameWork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.Authorization.CodeGenerator;
import com.FileManager.FileSender;
import com.FileManager.GzipGenerator;

public class opSwitch extends Thread {
	private int opCode;
	private ObjectOutputStream oos;
	private Socket opSocket;
	private static int BUFFSIZE = 1024;
	private String ssPath;

	public opSwitch(int opCode, ObjectOutputStream oos, Socket opSocket) {
		this.opCode = opCode;
		this.oos = oos;
		this.opSocket = opSocket;
	}
	
	
	public void setSsPath(String path){
		this.ssPath = path;
	}

	@Override
	public void run() {
		Payload pl = new Payload(opCode);
		try {
			oos.writeObject(pl);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		switch (opCode) {
		case -1:
			break;
		case 0:

			break;
		case 1: // send files
			System.out.println("file upload");
			try {

				// snapshot ï¿½ì ™è¹‚ï¿½ ï¿½ìŸ¾ï¿½ë„š

				FileSender fs = new FileSender("/home/armin/temp/", opSocket); // ï¿½ë™†ï¿½ì”ªï¿½ì” 
																				// è­°ëŒ?˜±ï¿½ë¸¯ï¿½ë’—
																				// ï¿½ëµ’ï¿½ì †ï¿½ë„—?”±ï¿?
																				// ï¿½ê½•ï¿½ì ™
				File hDir = fs.getHomeDir(); // FileSender ï¿½ì“½ ï¿½ì†ƒ ï¿½ëµ’ï¿½ì †ï¿½ë„—?”±?‰ï¿½ï¿? åª›ï¿½ï¿½ì¡‡ï¿½ìƒ‚

				File fileList[] = hDir.listFiles(); // ï¿½ëµ’ï¿½ì †ï¿½ë„—?”±ï¿? ï¿½ê¶¡ ï¿½ë™†ï¿½ì”ª ?”±?Šë’ªï¿½ë“ƒ
				int fileCnt = 0;

				if (fileList.length != 0) {
					oos.writeObject(Integer.toString(fileList.length)); // ï¿½ìŸ¾ï§£ï¿½ ï¿½ë™†ï¿½ì”ª
																		// åª›ì’–?‹”ï¿½ì ™è¹‚ï¿½
																		// ï¿½ìŸ¾ï¿½ë„š
					while (fileList.length > fileCnt) {
						oos.writeLong(fileList[fileCnt].length()); // file size
																	// ï¿½ìŸ¾ï¿½ë„š
						oos.writeObject(fileList[fileCnt]); // File ï¿½ì ™è¹‚ë?ë±¾ï¿½ï¿? ?™’?‡±ï¿? è¹‚ë?ê¶?ï¿½ë–
						oos.writeObject(fileList[fileCnt].getName()); // File
																		// ï¿½ì ™è¹‚ï¿½(ï¿½ì” ?”±ï¿?)ï¿½ë±¾ï¿½ï¿½
																		// ?™’?‡±ï¿?
																		// è¹‚ë?ê¶?ï¿½ë–
						fileCnt++;
					}

					fileCnt = 0;

					while (fileList.length > fileCnt) {
						System.out.println("ï¿½ìŸ¾ï¿½ë„šï¿½ë¸· ï¿½ë™†ï¿½ì”ªï§ï¿½ : " + fileList[fileCnt]);
						fs.sendFile(fileList[fileCnt].getName()); // ï¿½ë™†ï¿½ì”ª ï¿½ìŸ¾ï¿½ë„š
						fileCnt++;
					}
				} else {
					oos.writeObject(Integer.toString(fileList.length)); // ï¿½ìŸ¾ï§£ï¿½ ï¿½ë™†ï¿½ì”ª
																		// åª›ì’–?‹”ï¿½ì ™è¹‚ï¿½
																		// ï¿½ìŸ¾ï¿½ë„š
					System.out.println("File is not exist");
					return; // ï¿½ë™†ï¿½ì”ªï¿½ì”  è­°ëŒ?˜±ï¿½ë¸¯ï§ï¿½ ï¿½ë¸¡ï¿½ì‘ï§ï¿½ socket ?†«?‚…ì¦?
				}

				opSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			System.out.println("file download");
			try {
				ObjectInputStream ois = new ObjectInputStream(
						opSocket.getInputStream());

				// op 2 - get file list
				File[] fileList = (File[]) ois.readObject(); // ï¿½ê½Œè¸°ê¾©ë¿? ï¿½ë²‘æ¿¡ì•¸ë§? ï¿½ë™†ï¿½ì”ª ?”±?Šë’ªï¿½ë“ƒ?‘œï¿?
																// è«›ì…?’—ï¿½ë–.

				System.out.println("ï¿½ë–ï¿½ìŠ« ï¿½ë¸· ï¿½ë™†ï¿½ì”ª ï¿½ë‹” : " + fileList.length);

				int fileCnt = 0;
				while (fileList.length > fileCnt) {
					System.out.println(fileList[fileCnt].getName());
					fileCnt++;
				}
				fileCnt = 0;

				while (fileList.length > fileCnt) {
					File wFile = new File("/home/armin/ssHome/downloads/"
							+ fileList[fileCnt].getName()); // set home
															// directory on
															// client
					FileOutputStream fos = new FileOutputStream(wFile); // ï¿½ë–ï¿½ìŠ«æ¿¡ì’•ë±?
																		// ï¿½ë¸· ï¿½ë™†ï¿½ì”ªï¿½ì“£
																		// ï¿½ì—¯ï¿½ì °ï¿½ë¸·
																		// ï¿½ë’ªï¿½ë“ƒ?”±ï¿?
					if (fileList[fileCnt].isFile()) {
						byte[] byteArr = new byte[1024];
						int readBytes = 0;
						long fileSize = 0;
						System.out.println("receiving file name "
								+ fileList[fileCnt].getName());
						System.out.println("file path to be written "
								+ wFile.getPath());

						InputStream is = opSocket.getInputStream();

						while (fileSize + BUFFSIZE < fileList[fileCnt].length()) {

							if ((readBytes = is.read(byteArr)) != -1)
								fileSize += readBytes;
							fos.write(byteArr, 0, readBytes);

						}

						if (fileSize + BUFFSIZE >= fileList[fileCnt].length()) { // ï¿½ë–ï¿½ì“¬è¸°ê¾ª??‘œï¿?
																					// ï¿½ì”«ï¿½ì“£ï¿½ë–†
																					// ï¿½ìŠœï¿½ì›¾ï¿½ì“£
																					// ?¥?‡?‚µï¿½ë¸·
																					// å¯ƒìŒ?Š¦
							byte[] tmpBuf;

							int overSize = (int) (fileSize + BUFFSIZE - fileList[fileCnt]
									.length());
							int tempBufSize = BUFFSIZE - overSize;
							tmpBuf = new byte[tempBufSize];

							if ((readBytes = is.read(tmpBuf)) != -1) {

								System.out.println("readBytes : " + readBytes);
								fileSize += readBytes;

								System.out.println("total size : " + fileSize);
								fos.write(tmpBuf, 0, readBytes);
							}
						} else {

						}

						fileCnt++;

						fos.flush();
						fos.close();
					} else { // if wFile is a directory
						fileCnt++;
						wFile.delete();
						fos.flush();
						fos.close();
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("insert opCode on Colsole");
			System.out.println("-1 : ?†«?‚…ì¦? || 0 : noOp || 1 : send snapshot || 2 : download snapshot || 3 : get info || 4 : compress files || 5 : decompress files");
			break;
		case 3:
			System.out.println("request file info");
			
			
			break;
		case 4:// compress files
			System.out.println("file compressing");
			// ?ºê¾ªë¸·ï¿½ë¸¬?•°ï¿? è«›ï¿½ ï¿½ë¹ï¿½ì £
			GzipGenerator ggForComp = new GzipGenerator();
			try {
				ggForComp.partCompress("/home/armin/ssHome/capstone.tar",
						"/home/armin/ssHome/comp/");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			break;
		case 5: // decompress a file
			System.out.println("decompress a file");
			GzipGenerator ggForDeComp = new GzipGenerator();
			try {
				ggForDeComp.decompress("/home/armin/ssHome/comp/",
						"/home/armin/ssHome/decomp/");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case 6:
			System.out.println("code Generate");
			String code1,code2;
			
			CodeGenerator cg = new CodeGenerator("devCode", "additionalCode");
			
			break;
		}
	}

}