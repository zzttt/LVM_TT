package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.kkangsworld.lvmexec.pipeWithLVM;

import com.FileManager.AsyncFileSender;
//import com.FileManager.AsyncFileSender;
import com.FileManager.FileInfo;
import com.FileManager.FileSender;
import com.FrameWork.ConnectionManager;
import com.FrameWork.Payload;
import com.FileManager.FileSender;
import com.FrameWork.ConnectionManager;
import com.FrameWork.InstalledAppInfo;
import com.FrameWork.Payload;
import com.FrameWork.SystemSetting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.opengl.Visibility;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecvActivity extends Activity {

	private Socket sc;
	MyCustomAdapter dataAdapter = null;

	static int CONTACTS = 0;
	static int PASSWORD = 0;
	static int GESTURE = 0;
	static int WIFI = 0;

	final static int RECV_APP = 1;
	final static int RECV_USER_DATA = 2;
	final static int RECV_SETTINGS = 3;
	final static int RECV_ALL = 4;

	static String cur_Loc = null; // ���� ���丮

	private int func_code = 0;
	private Process p = null;
	private String sName = null;
	private String mName = null;
	private String loc = null;
	private Thread recovProcess = null;

	private String pName = "";
	private String UsedsName = "";
	private InstalledAppInfo mInsAppInfo = new InstalledAppInfo(this);

	private ArrayList<String> resultAppListByAppName;
	private ArrayList<String> resultAppListByPName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recv);

		sName = getIntent().getStringExtra("sName").replace("/dev", "")
				.replace("-cow", "");

		loc = getIntent().getStringExtra("loc"); // ������ ��ġ ( dev : ��ġ �� ,
													// srv : ���� )

		mName = getIntent().getStringExtra("mName");
		
		if (mName.equals("���ø����̼�")) { // ���ø����̼� ����
			func_code = RECV_APP;
		} else if (mName.equals("����� ������")) { // ����� ������ ����
			func_code = RECV_USER_DATA;
		} else if (mName.equals("Contacts, Settings")) { // ��ȭ, sms , ���� ����
			func_code = RECV_SETTINGS;
		} else { // ��ü ����
			func_code = RECV_ALL;
		}
		
		Log.v("lll", ""+func_code);

		if(func_code == RECV_SETTINGS){
			sName = sName + "_usersystem";
			displayListView(sName, sName); // ������ ��ġ�� device �� ���
		}
		else if(func_code == RECV_ALL){
			displayListView(sName, sName); // ������ ��ġ�� device �� ���
		}else if (loc.equals("dev") && func_code == RECV_APP){
			sName = sName + "_userdata";
			displayListView(sName, sName); // ������ ��ġ�� device �� ���
		}else if (loc.equals("dev") && func_code == RECV_USER_DATA) {
			sName = sName + "_usersdcard";
			displayListView(sName, sName + "/0/"); // ������ ��ġ�� device �� ���
		} else {
			sName = sName + "_usersystem";
		}

	}

	public void displayListView(String sName, String subDir) {
		// TODO Auto-generated method stub
		// Array list of countries
		ArrayList<Item> ItemList = new ArrayList<Item>();
		/*
		 * Item Item = new Item("AFG", "Afghanistan", false);
		 * ItemList.add(Item);
		 */
		cur_Loc = subDir; // current Location
		// Log.i("ddd", subDir);

		/*
		 * sName ( snapshot name ) mName ( selected menu name ) sName �����Ϳ���
		 * mName �� �ش��ϴ� �����͸� �о�´�.
		 */
		switch (func_code) {
		case RECV_ALL:
			// ��ü ���� , �ٷ� lvconvert ����
		
			
			Handler tmpHandler = new Handler(){
					
			};
				
			pipeWithLVM pwl = new pipeWithLVM(tmpHandler);
			
			try {
				Thread.sleep(500);
				pwl.ActionWritePipe("lvconvert --merge /dev/vg/" + sName
						+ "_userdata");
				Thread.sleep(500);
				pwl.ActionWritePipe("lvconvert --merge /dev/vg/" + sName
						+ "_usersdcard");
				Thread.sleep(500);
				pwl.ActionWritePipe("lvconvert --merge /dev/vg/" + sName
						+ "_usersystem");
				Thread.sleep(500);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			Log.d("lll", "lvconvert end");
			
			
			try {
				
				p = Runtime.getRuntime().exec("su -c reboot"); 
				BufferedReader in = new BufferedReader(  
						new InputStreamReader(
						p.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					Log.d("lll", line);
				}
				
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			break;
		case RECV_APP:
			try {
				p = new ProcessBuilder("su").start();

				String mountCom = "mount -t ext4 /dev/vg/" + sName
						+ " /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(mountCom.getBytes());

				// /data/data����
				String com = "ls -l /sdcard/ssDir/" + subDir + "/data/\n";

				Log.e("ccc", com);

				p.getOutputStream().write(com.getBytes());

				mountCom = "umount /sdcard/ssDir/" + sName + "\n";

				Log.e("ccc", mountCom);

				p.getOutputStream().write(mountCom.getBytes());

				p.getOutputStream().write("exit\n".getBytes());
				p.getOutputStream().flush();

				try {
					p.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));

				String line = null;

				// List View , adapter
				// ----------------------------------------------- ����Ʈ �߰��κ�
				ListView lv = (ListView) findViewById(R.id.lv_recvList);
				ArrayList<String> fList = new ArrayList<String>();
				ArrayList<FileInfo> fiList = new ArrayList<FileInfo>();

				while ((line = br.readLine()) != null) {
					Log.e("lvm2", line);
					fList.add(line);
				}

				for (String s : fList) {
					if (s.length() != 0) {

						String[] info = s.split(" ");
						// Log.d("ddd", Integer.toString(info.length) );
						ArrayList<String> splitedInfo = new ArrayList<String>();

						for (String ss : info) {
							ss = ss.trim(); // ��������
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

							StringBuffer fileName = new StringBuffer();
							int maxIdx = splitedInfo.size();

							for (int i = 6; i < maxIdx; i++) {
								if (i == 6)
									fileName.append(splitedInfo.get(i));
								else
									fileName.append(" " + splitedInfo.get(i));
							}

							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0).substring(1),
									splitedInfo.get(3), splitedInfo.get(4),
									splitedInfo.get(5), fileName.toString());

							// Log.v("ddd", splitedInfo.get(6));

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

				/* AppList ���� */
				ArrayList<InstalledAppInfo> appList = new ArrayList<InstalledAppInfo>();
				appList = mInsAppInfo.ReadAppInfo(sName.replace("_userdata", ""));

				HashMap<String, String> appmap = new HashMap<String, String>();
				HashMap<String, String> appmapByPack = new HashMap<String, String>();
				resultAppListByAppName = new ArrayList<String>();
				resultAppListByPName = new ArrayList<String>();

				// ���ø���Ʈ�� Hashmapȭ -- Key�� packages�̸������ؼ� appname�� value�� �Ѵ�.
				for (int i = 0; i < appList.size(); i++) {
					appmap.put(appList.get(i).resultOfPackagesNamePrint(),
							appList.get(i).resultOfAppNamePrint());
					appmapByPack.put(appList.get(i).resultOfAppNamePrint(),
							appList.get(i).resultOfPackagesNamePrint());
					// resultAppListByPName.add(appList.get(i).resultOfPackagesNamePrint());
					// //��Ű�� �̸��� ����Ʈȭ
					// Log.d("APP", appList.get(i).resultOfAppNamePrint());
				}

				// ���ø���Ʈ�� ����
				for (int i = 0; i < fiList.size(); i++) {
					// fiList�̸����� appmap�� key���ؼ� resultAppList�� �߰��Ѵ�. �̰� List��
					// �Ѹ���.
					if (appmap.get(fiList.get(i).getName()) != null) {
						resultAppListByAppName.add(appmap.get(fiList.get(i)
								.getName()));
						//
						// Log.d("APP", resultAppList.get(i));
					}
				}

				for (int i = 0; i < resultAppListByAppName.size(); i++) {
					/*
					 * HashMap���� Key�� AppName���κ��� �ٽ� Package�̸��� ���ؼ�
					 * resultAppListByPName�� �ִ´�.
					 */
					resultAppListByPName.add(appmapByPack
							.get(resultAppListByAppName.get(i)));
					Item Item = new Item("row", resultAppListByAppName.get(i),
							appmapByPack.get(resultAppListByAppName.get(i)),
							false);
					ItemList.add(Item);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case RECV_USER_DATA:
			// ����� ������ ���
			try {
				p = new ProcessBuilder("su").start();

				// func_code �� ���� snapshot �� �о����.
				Log.i("ccc", sName + "/" + loc);

				String mountCom = "mount -t ext4 /dev/vg/" + sName
						+ " /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(mountCom.getBytes());

				String com = "ls -l /sdcard/ssDir/" + subDir + "\n";

				p.getOutputStream().write(com.getBytes());

				mountCom = "umount /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(mountCom.getBytes());

				p.getOutputStream().write("exit\n".getBytes());
				p.getOutputStream().flush();

				try {
					p.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));

				String line = null;

				// List View , adapter
				// ----------------------------------------------- ����Ʈ �߰��κ�
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
						// Log.d("ddd", Integer.toString(info.length) );
						ArrayList<String> splitedInfo = new ArrayList<String>();

						for (String ss : info) {
							ss = ss.trim(); // ��������
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

							StringBuffer fileName = new StringBuffer();
							int maxIdx = splitedInfo.size();

							for (int i = 6; i < maxIdx; i++) {
								if (i == 6)
									fileName.append(splitedInfo.get(i));
								else
									fileName.append(" " + splitedInfo.get(i));
							}

							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0).substring(1),
									splitedInfo.get(3), splitedInfo.get(4),
									splitedInfo.get(5), fileName.toString());

							// Log.v("ddd", splitedInfo.get(6));

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

				if (!subDir.equals(sName + "/0/")) { // �� ��� ���丮�� �ƴѰ��
					// �����޴��� ����� ��
					Item Item = new Item("row", "..", false);
					ItemList.add(Item);
				}

				for (int i = 0; i < fiList.size(); i++) {
					if (fiList.get(i).getName().contains(":") && i != 0) { // ����
																			// ���͸�
						// fList.add(" ");
						// fList.add("[Dir]  " + fiList.get(i).getName());
						Item Item = new Item("row", ">  "
								+ fiList.get(i).getName(), false);
						ItemList.add(Item);
					} else if (!fiList.get(i).getType().equals("d")) { // �ش�
																		// ���丮
																		// ���� ����
						// fList.add(fiList.get(i).getName());
						Item Item = new Item("row", fiList.get(i).getName(),
								false);
						ItemList.add(Item);
					} else if (fiList.get(i).getType().equals("d")
							&& !fiList.get(i).getName().contains("ssDir")
							&& !fiList.get(i).getName().contains("Android")) {
						Item Item = new Item("row", ">  "
								+ fiList.get(i).getName(), false);
						ItemList.add(Item);
					}

				}

				/*
				 * ItemListArrayAdapter adapter = new ItemListArrayAdapter(this,
				 * android.R.layout.simple_list_item_1, fList);
				 * lv.setAdapter(adapter);
				 */
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case RECV_SETTINGS:
			Log.i("test", "test");
			Item Item = new Item("contacts", "          ��ȭ��ȣ��", false);
			ItemList.add(Item);
			Item Item1 = new Item("password", "          ��й�ȣ", false);
			ItemList.add(Item1);
			Item Item2 = new Item("gesture", "          ����lock", false);
			ItemList.add(Item2);
			Item Item3 = new Item("wifi", "          WiFi ������", false);
			ItemList.add(Item3);

			break;
			
		}

		// create an ArrayAdaptar from the String Array
		dataAdapter = new MyCustomAdapter(this, R.layout.file_info, ItemList);
		ListView listView = (ListView) findViewById(R.id.lv_recvList);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);

		/*
		 * listView.setOnItemClickListener(new OnItemClickListener() { public
		 * void onItemClick(AdapterView parent, View view, int position, long
		 * id) { // When clicked, show a toast with the TextView text Item Item
		 * = (Item) parent.getItemAtPosition(position);
		 * Toast.makeText(getApplicationContext(), "Clicked on Row: " +
		 * Item.getName(), Toast.LENGTH_LONG).show(); } });
		 */

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

	private class MyCustomAdapter extends ArrayAdapter<Item> {
		private ArrayList<Item> ItemList;
		
		
		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<Item> ItemList) {
			super(context, textViewResourceId, ItemList);
			this.ItemList = new ArrayList<Item>();
			this.ItemList.addAll(ItemList);
			
			
		}

		private class ViewHolder {
			TextView code;
			CheckBox name;
			RelativeLayout row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			// Log.v("ConvertView", String.valueOf(position));

			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.file_info, null);

				holder = new ViewHolder();
				holder.code = (TextView) convertView.findViewById(R.id.code);

				holder.name = (CheckBox) convertView // name view �� code ��
														// match_parent �� �ٲ㼭
														// �Ⱥ��δ�.
						.findViewById(R.id.checkBox1);
				// holder.row = (RelativeLayout)
				// convertView.findViewById(R.id.file_row);

				convertView.setTag(holder);

				holder.code.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						TextView tv = (TextView) v;
						if (tv.getText().toString().contains(">")) {
							/*
							 * Toast.makeText( getApplicationContext(),
							 * "���ο� �޴� �ε�", Toast.LENGTH_LONG) .show();
							 */

							String dir = cur_Loc
									+ "/"
									+ tv.getText()
											.toString()
											.substring(3, tv.getText().length());
							displayListView(sName, dir);

						} else if (tv.getText().toString().equals("..")) {
							// ���� �޴��� �̵�
							String dir = cur_Loc.substring(0,
									cur_Loc.lastIndexOf("/"));
							displayListView(sName, dir);
						}
					}
				});

				holder.name.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						CheckBox cb = (CheckBox) v;

						Item Item = (Item) cb.getTag();
						String targetText = null;
						switch (func_code) {
						case RECV_APP:
							targetText = Item.getPackName();
							break;
						case RECV_USER_DATA:
							targetText = (String) cb.getText();
							break;
						case RECV_SETTINGS:
							if (cb.getText().toString().contains("��ȭ��ȣ��")) {
								if (CONTACTS == 1)
									CONTACTS = 0;
								else
									CONTACTS = 1;
							} else if (cb.getText().toString().contains("��й�ȣ")) {
								if (PASSWORD == 1)
									PASSWORD = 0;
								else
									PASSWORD = 1;
							} else if (cb.getText().toString()
									.contains("����lock")) {
								if (GESTURE == 1)
									GESTURE = 0;
								else
									GESTURE = 1;
							} else if (cb.getText().toString()
									.contains("WiFi ������")) {
								if (WIFI == 1)
									WIFI = 0;
								else
									WIFI = 1;
							} else
								;
							
							break;
						}

						Toast.makeText(
								getApplicationContext(),
								"Clicked on Checkbox: " + cur_Loc + "/"
										+ targetText + " is " + cb.isChecked(),
								Toast.LENGTH_LONG).show();
						Item.setSelected(cb.isChecked(), cur_Loc,targetText.toString(), func_code); // ���õ��� üũ
																	// (���� �� �ش�
																	// ��ο� �̸���
																	// ���� )

					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Item Item = ItemList.get(position);

			if (Item.getName().contains(">") || Item.getName().equals("..")) {
				holder.code.setText(Item.getName());
				holder.code.setGravity(Gravity.CENTER_VERTICAL);
				holder.name.setVisibility(View.GONE);
				holder.name.setTag(Item);
			} else {
				holder.name.setVisibility(View.VISIBLE);
				holder.name.setText(Item.getName());
				holder.name.setChecked(Item.isSelected());
				holder.name.setTag(Item);
				holder.name.setGravity(Gravity.CENTER_VERTICAL);
			}
			return convertView;

		}
	}
/*
	private void StartInstall(String packageName, String pwdPath) {

		 -2 -1���� �Ľ��ϱ� 
		String apkName = ExtractAPKName(packageName, pwdPath);
		
		apkName = "file://" + pwdPath + apkName;
		Log.d("eee", apkName);
		// Log.d("eee", "��:"+pwdPath+apkName);

		 APK ���� 
		Intent cmdToInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
				Uri.parse(apkName), "application/vnd.android.package-archive");
		startActivity(cmdToInstall);
		// startActivityForResult(cmdToInstall, 1);

		 Tar�� ���� 
		TarTieDir(packageName, apkName);
	}
*/
	private void StartInstall2(String packageName, String pwdPath, String sName) {

		/* -2 -1���� �Ľ��ϱ� */
		String apkName = packageName;
		// apkName = "file://"+pwdPath+apkName;
		Log.d("eee", "apk"+apkName);
		// Log.d("eee", "��:"+pwdPath+apkName);

		pName = packageName;
		UsedsName = sName;

		/* APK ���� */
		Intent cmdToInstall = new Intent(Intent.ACTION_VIEW).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setDataAndType(
				Uri.parse("file:///sdcard/" + apkName),"application/vnd.android.package-archive");
		//startActivity(cmdToInstall);
		
	
		//startActivityForResult(cmdToInstall, 1);

		/*
		Bundle tempBundle = new Bundle();

		
		tempBundle.putString("pName", packageName);
		tempBundle.putString("sName", sName);*/
		
		startActivityForResult(cmdToInstall, 1);

		//CopyToAppData(packageName, sName);
		
		/* Tar�� ���� */
		// TarTieDir("com.example.applist", apkName);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			CopyToAppData(pName, UsedsName);
			Log.v("lll", "ddododododododod");
			
			
			break;

		default:
			break;
		}
	}
	
	private void CopyToAppData(String packageName,  String sName) {
		try {
			Process p = new ProcessBuilder("su").start();
			
			Log.v("lll", sName+","+packageName);
			String mountCom = "mount -t ext4 /dev/vg/" + sName
					+ " /sdcard/ssDir/" + sName + "\n";

			p.getOutputStream().write(mountCom.getBytes());

			
			String packageDirectory = packageName.substring(0, packageName.indexOf("-"));
			// /data/data����
			String com = "cp -r /sdcard/ssDir/" + sName + "/data/"+packageDirectory+" /data/data/\n";
			
			//String com = "ls -l /sdcard/ssDir/" + sName + "/data/\n";

			Log.e("lll", com);

			p.getOutputStream().write(com.getBytes());

			mountCom = "umount /sdcard/ssDir/" + sName + "\n";

			Log.e("ccc", mountCom);

			p.getOutputStream().write(mountCom.getBytes());

			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// ����Ʈ ��� -> /data/data

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	private String ExtractAPKName(String packageName, String pwdPath) {
		ArrayList<String> fList = new ArrayList<String>();
		Process p;
		String resultAppName = null;

		try {
			p = new ProcessBuilder("su").start();

			String com2 = "ls -l " + pwdPath + "\n";
			Log.e("ccc", "command "+com2);

			p.getOutputStream().write(com2.getBytes());

			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				if (line.substring(0, 1).equals("-")){
					Log.d("ccc", line);
					fList.add(line);
				}

				//System.out.println(line);

			}
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String s : fList) {

			// Log.d("APP", s);
			if (s.length() != 0) {

				String[] info = s.split(" ");
				// Log.d("ddd", Integer.toString(info.length) );
				ArrayList<String> splitedInfo = new ArrayList<String>();

				for (String ss : info) {
					ss = ss.trim(); // ��������
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

					if (fileType == '-') { // general files
						// general file���� �뷮�������� ���� ��.

						StringBuffer fileName = new StringBuffer();
						int maxIdx = splitedInfo.size();

						for (int i = 6; i < maxIdx; i++) {
							if (i == 6)
								fileName.append(splitedInfo.get(i));
							else
								fileName.append(" " + splitedInfo.get(i));
						}

						if (fileName.toString().contains(packageName)) {
							resultAppName = fileName.toString();
						}

						fi = new FileInfo(String.valueOf(fileType), splitedInfo
								.get(0).substring(1), splitedInfo.get(3),
								splitedInfo.get(4), splitedInfo.get(5),
								fileName.toString());

						// Log.v("ddd", splitedInfo.get(6));

						// fiList.add(fi); // fiList �� ���
					} else { // directory ������ ��ü�� ���� �������� ����.
						fi = new FileInfo(String.valueOf(fileType),
								splitedInfo.get(0));
						// fiList.add(fi); // fiList �� ���
					}

				}
			}
		}
		Log.d("APP", "searched Apk : " + resultAppName);

		return resultAppName;

	}

	private int TarTieDir(String packageName, String apkName) {
		String command = "tar -cvf "
				+ Environment.getExternalStorageDirectory() + "/cp/"
				+ packageName + ".tar" + " " + "/data/data/" + packageName
				+ " " + "/data/app/" + apkName + "\n";

		// apk �߰�
		// command += " "+ "/data/app/"+apkName;
		Process p;

		try {
			p = new ProcessBuilder("su").start();

			p.getOutputStream().write(command.getBytes());

			Log.e("APP", "tar command : " + command);

			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = null;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}

				System.out.println(line);
			}

			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		return 0;

	}

	
	


	

	
	
	public void mOnClick(View v) {
		switch (v.getId()) {
		case R.id.startRecv: // startRecovery

			final ArrayList<Item> ItemList = dataAdapter.ItemList; // checkbox
																	// ���� ����Ʈ

			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Notice");
			adb.setMessage("������ �����Ͻðڽ��ϱ�?");
			final Dialog mDialog = adb.create();

			switch (func_code) {
			case RECV_APP:
				recovProcess = new Thread() {

					@Override
					public void run() {
						
						if(func_code == RECV_SETTINGS ){
//							try {
//								p.getOutputStream().write(("mount -t ext4 /dev/vg/"+sName+"_usersystem /sdcard/ssDir/"+sName+"_usersystem\n").getBytes());
//							} catch (IOException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
							
							SystemSetting ss = new SystemSetting();
							ss.set_permission();   // root ���� �� ��ο� 777���� �ο� 
			                  
			                  if(CONTACTS == 1){
			                     // ��ȭ��ȣ�� ������ƾ ����
			                  }
			                  if(PASSWORD == 1){
			                     // ��й�ȣ ���� ��ƾ ����
			                	  String copy_password = "cp /sdcard/ssDir/"+sName+"_usersystem/system/password.key /sdcard/password.key\n";
			                	  try {
									p.getOutputStream().write(copy_password.getBytes());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			                  }
			                  if(WIFI == 1){
			                     // �������� ���� ��ƾ ����                  
					        		String copy_wifi = "cp /sdcard/ssDir/"+sName+"_usersystem/misc/wifi/wpa_supplicant.conf /sdcard/wpa_supplicant.conf\n";
					        		try {
										p.getOutputStream().write(copy_wifi.getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			                  }
			                  if(GESTURE == 1){
			                     // ���� ���� ��ƾ ����
								String copy_gesture = "cp /sdcard/ssDir/"+sName+"_usersystem/system/gesture.key /sdcard/gesture.key\n";
								try {
									p.getOutputStream().write(copy_gesture.getBytes());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			                  }
			                  
						}
						else{
							ProgressDialog progressDialog;
							progressDialog = new ProgressDialog(RecvActivity.this);
							progressDialog
									.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							progressDialog.setMax(ItemList.size());
							progressDialog.setMessage("���� ���� �� �Դϴ�...");

							progressDialog.setCancelable(true);
							progressDialog.show();

							for (int i = 0; i < ItemList.size(); i++) {
								Item Item = ItemList.get(i);

								if (Item.isSelected()) {
									/* /data/data���� üũ */
									String dataCoverPath = Item.getPath().replace(
											sName, "/sdcard/");
									String apkCoverPath = null;
									// TODO : ���üũ �ʿ�

									Log.v("path",
											Item.getPath().replace(sName, "/data")); // ����
																						// ���
																						// ���üũ

									progressDialog.setProgress(i);

									// ����Ʈ ���� �� ������ �ű��. ( Sdcard Ȥ�� Server�� ���� )
									try {
										p = new ProcessBuilder("su").start();

										String mountCom = "mount -t ext4 /dev/vg/"
												+ sName + " /sdcard/ssDir/" + sName
												+ "\n";

										Log.v("eee", mountCom);

										p.getOutputStream().write(
												mountCom.getBytes());

										/*
										 * pName-1.apk�� ������ pName-2.apk�� ���ش�.
										 */

										String apkName = ExtractAPKName(Item
												.getPath().replace(sName, "")
												.replace("/app/", ""),
												"/sdcard/ssDir/" + sName + "/app/");
										String cpCom = "cp /sdcard/ssDir/" + sName
												+ "/app/" + apkName + " "
												+ "/sdcard/" + "\n";

										
										p.getOutputStream().write(cpCom.getBytes());

										String modCom = "chmod 777 /sdcard/"
												+ apkName+"\n";

										p.getOutputStream()
												.write(modCom.getBytes());

										p.getOutputStream().write(
												"exit\n".getBytes());
										p.getOutputStream().flush();

										Log.d("APP",
												Item.getPath().replace(sName, "")
														.replace("/app/", ""));

										// ExtractAPKName(Item.getPath().replace(sName,
										// "").replace("/app/", ""),
										// "/sdcard/ssDir/" + sName);
										StartInstall2(apkName, "/sdcard/", sName);

										mountCom = "umount /sdcard/ssDir/" + sName
												+ "\n";

										p.getOutputStream().write(
												mountCom.getBytes());

										p.getOutputStream().write(
												"exit\n".getBytes());
										p.getOutputStream().flush();

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							progressDialog.dismiss();
							
						}
						
					
					}
				};
				break;
			case RECV_USER_DATA:
				recovProcess = new Thread() {

					@Override
					public void run() {
						ProgressDialog progressDialog;
						progressDialog = new ProgressDialog(RecvActivity.this);
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setMax(ItemList.size());
						progressDialog.setMessage("���� ���� �� �Դϴ�...");

						progressDialog.setCancelable(true);
						progressDialog.show();

						
						
						
						
						for (int i = 0; i < ItemList.size(); i++) {
							Item Item = ItemList.get(i);

							if (Item.isSelected()) {
								String finalPath = Item.getPath().replace(
										sName + "/0/", "/sdcard/");
								Log.v("eee",
										Item.getPath().replace(sName + "/0/",
												"/sdcard/")); // ���� ���

								progressDialog.setProgress(i);

								
								Thread socTh = new Thread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										try {
											
											sc = new Socket(MainActivity.srvIp, MainActivity.srvPort);
											
										} catch (UnknownHostException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}); 
								socTh.start();
								
								// ����Ʈ ���� �� ������ �ű��. ( Sdcard Ȥ�� Server�� ���� )
								try {
									p = new ProcessBuilder("su").start();

									String mountCom = "mount -t ext4 /dev/vg/"
											+ sName + " /sdcard/ssDir/" + sName
											+ "\n";

									Log.v("eee", mountCom);

									p.getOutputStream().write(
											mountCom.getBytes());

									
									String com = "ls -l /sdcard/ssDir/"
											+ Item.getPath().substring(
													0,
													Item.getPath().lastIndexOf(
															"/"))
											+ " | grep \""
											+ Item.getPath()
													.substring(
															Item.getPath()
																	.lastIndexOf(
																			"/") + 2,
															Item.getPath()
																	.length() - 1)
											+ "\" \n";

									Log.v("eee", com);

									
									p.getOutputStream().write(com.getBytes());

									// dd �� obs

									// 1) ls �Ľ� �� file size �� �о���δ�
									

									BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
									String ss = null; 
									ss = br.readLine();
									Log.w("kkk", "ls result : "+ss);
									
									String resArr[] = ss.split(" ");
									
									int idx = 0;
									long fileSize = 0;
									
									for(String s : resArr){
										idx++;
										Log.w("kkk",s.trim()+"("+idx+")");
										if(s.equals("115061133"))
											fileSize = 115061133;
									}
									
									//long fileSize = Long.parseLong(resArr[3]);
									
									
									/*
									if(resArr[5].equals("166107")){
										fileSize = Long.parseLong(resArr[5]);
									}else if(resArr[4].equals("6643061")){
										fileSize = Long.parseLong(resArr[4]);
									}else{
										fileSize = Long.parseLong(resArr[3]);
									}*/
									//br.reset();
									Log.w("kkk", "end?");
									
									// 2) file size �� 1000block �� ���� �����Ѵ�.
									
									// 1 block = 512byte >> 1000block == 512000byte;
									
									int total = 0;
									
									if(fileSize % 512000 > 0){
										total = (int) (fileSize / 512000) + 1;
									}else if(fileSize % 512000 == 0){
										total = (int) (fileSize / 512000);
									}
									
									int count = 1000;
									
									String sendToSocket = null;
									
									
									
									sendToSocket = "dd if=/sdcard/ssDir/"
											+ Item.getPath()
											+ " obs=512k"
											+ "\n";
									Log.v("eee", sendToSocket);

									// ���� ��ɾ� ����
									p.getOutputStream().write(
											sendToSocket.getBytes());
									p.getOutputStream().flush();
									
									AsyncFileSender afs = new
									AsyncFileSender(sc, p.getInputStream(), progressDialog , Item.getName());
									afs.execute();
									
									
									
									
									/*for(int j = 0 ; j < 1; j++){
								
										// 3) 1000block (500k) �� �ݺ��ϸ鼭 �������� ����
										if (((j * 1000) + count) * 512 > fileSize) {

										}

										
										 sendToSocket = "dd if=/sdcard/ssDir/"
												+ Item.getPath()
												+ " obs=512k skip="
												+ (j * 1000)
												+ " count="
												+ count + "\n";

										Log.v("eee", sendToSocket);

										// ���� ��ɾ� ����
										p.getOutputStream().write(
												sendToSocket.getBytes());
										p.getOutputStream().flush();
										
										AsyncFileSender afs = new
										AsyncFileSender(sc, p.getInputStream(), progressDialog , Item.getName());
										afs.execute();
										
										
									}*/
									
									/*
									 * ObjectOutputStream oos = new
									 * ObjectOutputStream(sc.getOutputStream());
									 * 
									 * Payload pl = new Payload(8,
									 * MainActivity.rd.getUserCode());
									 * oos.writeObject(pl); // code 8 ���� �ӽ����� ����
									 */
									

									byte buffer[] = new byte[1024 * 512]; // 512k
									int size = 0;
									long totalSize = 0;

									mountCom = "umount /sdcard/ssDir/" + sName
											+ "\n";

									// p.getOutputStream().write(mountCom.getBytes());

									p.getOutputStream().write(
											"exit\n".getBytes());
									p.getOutputStream().flush();

									/*
									 * while( (size =
									 * p.getInputStream().read(buffer)) > 0){
									 * //Log.i("eee", Integer.toString(size));
									 * totalSize += size; }
									 */

									
									Log.e("FileName", Item.getName());
									

									 
									/*
									 * Log.i("eee", "total : " +
									 * Long.toString(totalSize
									 * )+" stream complete"); FileSender fs =
									 * new FileSender(); fs.SendFile(totalSize);
									 */

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						progressDialog.dismiss();
					}
				};
				break;
			}

			adb.setPositiveButton("��������", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					// mDialog.dismiss();
					recovProcess.run();
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

	class Item {

		String code = null;
		String name = null;
		String path = null;
		String pname = null;

		boolean selected = false;

		public Item(String code, String name, boolean selected) {
			super();
			this.code = code;
			this.name = name;
			this.selected = selected;
		}

		public Item(String code, String name, String pname, boolean selected) {
			super();
			this.code = code;
			this.name = name;
			this.pname = pname;
			this.selected = selected;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public String getPackName() {
			return pname;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setPackName(String pname) {
			this.pname = pname;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected, String path, String fileName,
				int func_code) {
			this.selected = selected;
			if (func_code == RECV_USER_DATA)
				this.path = path + "/\"" + fileName + "\"";
			else if (func_code == RECV_APP)
				this.path = path + "/app/" + fileName;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			this.path = path;
		}

	}

}
