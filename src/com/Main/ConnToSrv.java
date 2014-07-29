package com.Main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import com.Authorization.CodeGenerator;
import com.FileManager.FileSender;
import com.FileManager.GzipGenerator;
import com.FrameWork.Payload;
import com.FrameWork.Snapshot;
import com.FrameWork.opSwitch;

public class ConnToSrv {

	public static void main(String args[]) {
		String authCode;
		Socket sc;
		
		try {
			sc = new Socket("211.189.19.45", 1234);

			// gg.partCompress("/home/armin/snapshot.tar","/home/armin/snapshot.tar.gz");
			ObjectOutputStream oos = new ObjectOutputStream(
					sc.getOutputStream()); // 

			String opCode = null;
			Scanner keyScan = new Scanner(System.in);

			while (true) {
				if (sc.isClosed())
					break;

				System.out.println("insert opCode on Colsole");
				opCode = keyScan.nextLine();

				opSwitch op = new opSwitch(Integer.parseInt(opCode), oos, sc);
				op.start();

				if (Integer.parseInt(opCode) == -1) {
					break;
				}
			}


		} catch (Exception e) {
			System.out.println("exception : " + e.getMessage());
		}

	}

}

