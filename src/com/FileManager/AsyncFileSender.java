package com.FileManager;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.FrameWork.Payload;
import com.example.timetraveler.MainActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncFileSender extends AsyncTask<Void, Void, Integer >{

	private InputStream is;
	private ProgressDialog pd;
	private String fileName;
	private Socket sc;
	
	public AsyncFileSender(Socket sc ,InputStream is, ProgressDialog pd, String fileName) {
		// TODO Auto-generated constructor stub
		this.sc = sc;
		this.is = is;
		this.pd = pd;
		this.fileName = fileName;
	}

	public Integer doInBackground(Void... params) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());
			
			byte buffer[] = new byte[1024*512]; // 512k
			int size = 0;
			long totalSize = 0;

			
			Payload pl = new Payload(8, MainActivity.rd.getUserCode());
			
			oos.writeObject(pl);
			
			// 파일 이름 전송
			oos.writeObject(fileName);
			
			while ((size = is.read(buffer)) > 0) {
				Log.i("ccc", Integer.toString(size));
				oos.writeInt(0); // 전송을 알림
				oos.writeInt(size); // 전송할 크기를 알림
				oos.write(buffer, 0 , size);
				totalSize += size;
			}
			
			oos.writeInt(-1);
			oos.writeLong(totalSize);
			
			oos.flush();
			oos.close();
			
			
			pd.dismiss();
			
		} catch (Exception e) {

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
}
