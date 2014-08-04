package com.FileManager;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.FrameWork.Payload;
import com.example.timetraveler.MainActivity;

import android.os.AsyncTask;

public class AsyncFileSender extends AsyncTask<Void, Void, Void>{

	private InputStream is;
	public AsyncFileSender(InputStream is, Object object2, Object object3) {
		// TODO Auto-generated constructor stub
		this.is = is;
	}

	public Void doInBackground(Void... params) {
		try {
			Socket sc = new Socket(MainActivity.srvIp, MainActivity.srvPort);
			
			byte buffer[] = new byte[1024*512]; // 512k
			int size = 0;
			long totalSize = 0;

			ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());
			
			Payload pl = new Payload(8, MainActivity.rd.getUserCode());
			
			oos.writeObject(pl);
			
			while ((size = is.read(buffer)) > 0) {
				// Log.i("eee", Integer.toString(size));
				
				// 1.  쌓인 buffe 크기 전송				
				oos.writeObject(size);
				
				// 2. 실제 버퍼 내용 전송하고 버퍼를 비움				
				oos.write(buffer);
				
				// 소켓에 쏘면 된다.
				totalSize += size;
			}
			
			oos.writeObject(totalSize);
			
			oos.flush();
			oos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
