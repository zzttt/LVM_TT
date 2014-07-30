package com.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class FileClient {

	public static void main(String[] args) {
		String host = "localhost";
		int port = 10000;

		// String filename = "test.txt";
		String filename = "/home/armin/snapshot.tar";
		File file = new File(filename);
		long fileSize = file.length();
		System.out.println("File size: " + (fileSize) + " Byte(s)");

		try {
			long startTime = System.currentTimeMillis();
			System.out.println("Start time: " + new Date());

			FileInputStream fis = new FileInputStream(file);

			System.out.println("This client is connecting to " + host + ":"
					+ port + "...");
			Socket socket = new Socket(host, port);
			System.out.println("This client is Connected.");

			OutputStream os = socket.getOutputStream();

			long totalReadBytes = 0;

			byte[] buffer = new byte[1024];
			int readBytes;
			while ((readBytes = fis.read(buffer)) > 0) {
				os.write(buffer, 0, readBytes);

				totalReadBytes += readBytes;
				System.out.println("In progress: " + totalReadBytes + "/"
						+ fileSize + " Byte(s) ("
						+ (totalReadBytes * 100 / fileSize) + " %)");
			}

			System.out.println("File transfer completed.");

			fis.close();

			os.close();
			socket.close();

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
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}