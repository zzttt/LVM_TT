package com.FileManager;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

public class FileSender {

	private String path;
	private Socket scTarget;
	private BufferedReader in;
	private BufferedOutputStream out;

	public FileSender(){
		
	}
	
	public FileSender(Socket scTarget) {
		this.scTarget = scTarget;
	}
	
	/**
	 * 
	 * @param path
	 * @param scTarget
	 */	
	public FileSender(String path, Socket scTarget) {
		this.path = path;
		this.scTarget = scTarget;
	}
	
	/**
	 * 
	 * @param fileName 
	 */
	public void sendFile(String fileName) {  // 
		
		String realPath = path + fileName;

		System.out.println("start to send a file " + realPath);

		File file = new File(realPath);

		long fileSize = file.length();
		System.out.println("File size: " + (fileSize) + " Byte(s)");

		try {
			long startTime = System.currentTimeMillis();
			System.out.println("Start time: " + new Date());

			FileInputStream fis = new FileInputStream(file);

			System.out.println("This client is connecting to "
					+ scTarget.getInetAddress() + ":" + scTarget.getPort()
					+ "...");
			System.out.println("This client is Connected.");

			long totalReadBytes = 0;

			byte[] buffer = new byte[1024];
			int readBytes;
			
			while ((readBytes = fis.read(buffer)) > 0) {
				scTarget.getOutputStream().write(buffer, 0, readBytes);

				totalReadBytes += readBytes;
				/*
				System.out.println("In progress: " + totalReadBytes + "/"
						+ fileSize + " Byte(s) ("
						+ (totalReadBytes * 100 / fileSize) + " %)");
						*/
			}
			
			System.out.println("File transfer completed.");

			long endTime = System.currentTimeMillis();
			System.out.println("End time: " + new Date());

			long diffTime = endTime - startTime;
			long diffTimeInSeconds = diffTime / 1000;
			System.out.println("Elapsed time: " + diffTimeInSeconds
					+ " second(s)");

			if (diffTimeInSeconds != 0) {
				System.out.println("Average transfer speed: "
						+ (fileSize / 1000) / diffTimeInSeconds + " KB/s");
			}
			
			
			fis.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			//delSentFile(fileName); //
		}

		 
	}
	
	/**
	 * delete file which be sent
	 * @param fileName : a file will be deleted
	 */
	public void delSentFile(String fileName){
		File delFile = new File(path+fileName);
		if(delFile.delete()){
			System.out.println("file is deleted");
		}else{
			System.out.println("file is not deleted");
		}
	}
	
	/**
	 * 
	 * @return home directory of object
	 */
	public File getHomeDir(){
		File file = new File(path);
		return file;
	}
	
	
}
