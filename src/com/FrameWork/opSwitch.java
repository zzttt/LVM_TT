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

				// snapshot �젙蹂� �쟾�넚

				FileSender fs = new FileSender("/home/armin/temp/", opSocket); // �뙆�씪�씠
																				// 議댁?���븯�뒗
																				// �뵒�젆�넗?���?
																				// �꽕�젙
				File hDir = fs.getHomeDir(); // FileSender �쓽 �솃 �뵒�젆�넗?��?���? 媛��졇�샂

				File fileList[] = hDir.listFiles(); // �뵒�젆�넗?���? �궡 �뙆�씪 ?��?�뒪�듃
				int fileCnt = 0;

				if (fileList.length != 0) {
					oos.writeObject(Integer.toString(fileList.length)); // �쟾泥� �뙆�씪
																		// 媛쒖?���젙蹂�
																		// �쟾�넚
					while (fileList.length > fileCnt) {
						oos.writeLong(fileList[fileCnt].length()); // file size
																	// �쟾�넚
						oos.writeObject(fileList[fileCnt]); // File �젙蹂�?뱾��? ?��?���? 蹂�?�?�떎
						oos.writeObject(fileList[fileCnt].getName()); // File
																		// �젙蹂�(�씠?���?)�뱾��
																		// ?��?���?
																		// 蹂�?�?�떎
						fileCnt++;
					}

					fileCnt = 0;

					while (fileList.length > fileCnt) {
						System.out.println("�쟾�넚�븷 �뙆�씪紐� : " + fileList[fileCnt]);
						fs.sendFile(fileList[fileCnt].getName()); // �뙆�씪 �쟾�넚
						fileCnt++;
					}
				} else {
					oos.writeObject(Integer.toString(fileList.length)); // �쟾泥� �뙆�씪
																		// 媛쒖?���젙蹂�
																		// �쟾�넚
					System.out.println("File is not exist");
					return; // �뙆�씪�씠 議댁?���븯吏� �븡�쑝硫� socket ?��?���?
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
				File[] fileList = (File[]) ois.readObject(); // �꽌踰꾩�? �벑濡앸�? �뙆�씪 ?��?�뒪�듃?���?
																// 諛쏅?���떎.

				System.out.println("�떎�슫 �븷 �뙆�씪 �닔 : " + fileList.length);

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
					FileOutputStream fos = new FileOutputStream(wFile); // �떎�슫濡쒕�?
																		// �븷 �뙆�씪�쓣
																		// �엯�젰�븷
																		// �뒪�듃?���?
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

						if (fileSize + BUFFSIZE >= fileList[fileCnt].length()) { // �떎�쓬踰꾪?��?���?
																					// �씫�쓣�떆
																					// �슜�웾�쓣
																					// ?��?��?���븷
																					// 寃쎌?��
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
			System.out.println("-1 : ?��?���? || 0 : noOp || 1 : send snapshot || 2 : download snapshot || 3 : get info || 4 : compress files || 5 : decompress files");
			break;
		case 3:
			System.out.println("request file info");
			
			
			break;
		case 4:// compress files
			System.out.println("file compressing");
			// ?��꾪븷�븬?���? 諛� �빐�젣
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