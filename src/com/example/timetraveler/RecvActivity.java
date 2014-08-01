package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.FileManager.FileInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecvActivity extends Activity {

	final static int RECV_APP = 1;
	final static int RECV_USER_DATA = 2;
	final static int RECV_SETTINGS = 3;
	final static int RECV_ALL = 4;
	
	private int func_code = 0;
	private Process p = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recv);
		
		String sName = getIntent().getStringExtra("sName");
		String mName = getIntent().getStringExtra("mName");
		
		/*
		 *  sName ( snapshot name )
		 *  mName ( selected menu name )
		 *  sName �����Ϳ��� mName �� �ش��ϴ� �����͸� �о�´�.
		 */
		
		if(mName.equals("���ø����̼�")){ // ���ø����̼� ����
			func_code = RECV_APP;
		}else if(mName.equals("����� ������")){ // ����� ������ ����
			func_code = RECV_USER_DATA;
		}else if(mName.equals("��ȭ��ȣ��, SMS, ���� ����")){ // ��ȭ, sms , ���� ����
			func_code = RECV_SETTINGS;
			Log.i("func", "testtest");
		}else{ // ��ü ����
			func_code = RECV_ALL;
		}

		switch (func_code) {
		case RECV_ALL:
			// ��ü ���� , �ٷ� lvconvert ����
			
			try {
				p = new ProcessBuilder("su").start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			break;
		case RECV_APP:

			break;
		case RECV_USER_DATA:
			// ����� ������ ���
			try {
				p = new ProcessBuilder("su").start();

				// func_code �� ���� snapshot �� �о����.

				String mountCom = "mount -t ext4 /dev/vg/" + sName
						+ " /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(mountCom.getBytes());

				String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(com.getBytes());
				p.getOutputStream().write("exit\n".getBytes());
				p.getOutputStream().flush();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));

				String line = null;

				// List View , adapter
				ListView lv = (ListView) findViewById(R.id.lv_recvList);
				ArrayList<String> fList = new ArrayList<String>();
				ArrayList<FileInfo> fiList = new ArrayList<FileInfo>();

				while ((line = br.readLine()) != null) {
					Log.e("lvm", line);
					fList.add(line);
				}

				for (String s : fList) {
					if (s.length() != 0) {

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

						if (splitedInfo.size() != 0) { // �� ������ ���� ù��° ���ڴ� ����
														// ������
														// ��Ÿ��..
							fileType = splitedInfo.get(0).charAt(0);
							// Log.d("lvm", "("+String.valueOf(fileType)+")");

							if (fileType == 'l') { // ��ũ������ ��� ���ϸ� ���� �ʿ� ( idx 5
													// ����
													// fileName.. 5 ���� ���ڿ��� ���� )
								String fName = splitedInfo.get(5)
										+ splitedInfo.get(6)
										+ splitedInfo.get(7);
								splitedInfo.set(5, fName);
								splitedInfo.remove(7);
								splitedInfo.remove(6);
							}

						}

						if (fileType == 'd' || fileType == 'b'
								|| fileType == 'c' || fileType == 'p'
								|| fileType == 'l' || fileType == 's') { // special
																			// files
							// b(Block file(b) , Character device file(c) ,
							// Named
							// pipe file or just a pipe file(p)
							// Symbolic link file(l), Socket file(s)

							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0).substring(1),
									splitedInfo.get(3), splitedInfo.get(4),
									splitedInfo.get(5));
							fiList.add(fi); // fiList �� ���
						} else if (fileType == '-') { // general files
							// general file���� �뷮�������� ���� ��.
							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0).substring(1),
									splitedInfo.get(3), splitedInfo.get(4),
									splitedInfo.get(5), splitedInfo.get(6));
							fiList.add(fi); // fiList �� ���
						} else { // directory ������ ��ü�� ���� �������� ����.
							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0));
							fiList.add(fi); // fiList �� ���
						}

					}
				}
				Log.d("lvm", "file count : " + Integer.toString(fiList.size()));

				fList.clear();

				for (int i = 0; i < fiList.size(); i++) {
					if (fiList.get(i).getName().contains(":") && i != 0) { // ����
																			// ���͸�
						fList.add(" ");
						fList.add("[Dir]  " + fiList.get(i).getName());
					} else if (!fiList.get(i).getType().equals("d")) { // �ش�
																		// ���丮
																		// ���� ����
						fList.add(fiList.get(i).getName());
					}

				}

				ItemListArrayAdapter adapter = new ItemListArrayAdapter(this,
						android.R.layout.simple_list_item_1, fList);
				lv.setAdapter(adapter);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case RECV_SETTINGS:	// ��ȭ, sms , ���� ����
		{
			
			
			
			break;
		}
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recv, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	// �����׸� ����Ʈ �����
	private class ItemListArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public ItemListArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	
	public void mOnClick(View v) {
		switch (v.getId()) {
		case R.id.startRecv: // startRecovery
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Notice");
			adb.setMessage("������ �����Ͻðڽ��ϱ�?");
			final Dialog mDialog = adb.create();

			adb.setPositiveButton("��������", new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					mDialog.dismiss();

					ProgressDialog progressDialog;
					progressDialog = new ProgressDialog(RecvActivity.this);
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMessage("���� �� �Դϴ�...");
					progressDialog.setCancelable(true);
					progressDialog.show();
				}

			});

			adb.setNegativeButton("���", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mDialog.dismiss();// ����
				}

			});
			
			
			adb.show();
			

		}
	}
}
