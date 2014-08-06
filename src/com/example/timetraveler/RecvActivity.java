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

	static String cur_Loc = null; // 현재 디렉토리

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

		loc = getIntent().getStringExtra("loc"); // 데이터 위치 ( dev : 장치 내 ,
													// srv : 서버 )

		mName = getIntent().getStringExtra("mName");
		
		if (mName.equals("어플리케이션")) { // 어플리케이션 복원
			func_code = RECV_APP;
		} else if (mName.equals("사용자 데이터")) { // 사용자 데이터 복원
			func_code = RECV_USER_DATA;
		} else if (mName.equals("Contacts, Settings")) { // 전화, sms , 설정 복원
			func_code = RECV_SETTINGS;
		} else { // 전체 복원
			func_code = RECV_ALL;
		}
		
		Log.v("lll", ""+func_code);

		if(func_code == RECV_SETTINGS){
			sName = sName + "_usersystem";
			displayListView(sName, sName); // 스냅샷 위치가 device 일 경우
		}
		else if(func_code == RECV_ALL){
			displayListView(sName, sName); // 스냅샷 위치가 device 일 경우
		}else if (loc.equals("dev") && func_code == RECV_APP){
			sName = sName + "_userdata";
			displayListView(sName, sName); // 스냅샷 위치가 device 일 경우
		}else if (loc.equals("dev") && func_code == RECV_USER_DATA) {
			sName = sName + "_usersdcard";
			displayListView(sName, sName + "/0/"); // 스냅샷 위치가 device 일 경우
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
		 * sName ( snapshot name ) mName ( selected menu name ) sName 데이터에서
		 * mName 에 해당하는 데이터를 읽어온다.
		 */
		switch (func_code) {
		case RECV_ALL:
			// 전체 복원 , 바로 lvconvert 수행
		
			
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

				// /data/data영역
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
				// ----------------------------------------------- 리스트 추가부분
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
							ss = ss.trim(); // 공백제거
							if (ss.length() != 0)
								splitedInfo.add(ss);
						}

						FileInfo fi;
						// split 결과는 실제 파일의 정보 , 하위 디렉토리 이름 으로 나누어짐.
						// 하위디렉토리 이름은 무시한다
						int idx = 0;

						char fileType = ' ';

						if (splitedInfo.size() != 0) { // 한 라인의 가장 첫번째 문자는 파일
														// 형식을
														// 나타냄..
							fileType = splitedInfo.get(0).charAt(0);
							// Log.d("lvm", "("+String.valueOf(fileType)+")");

							if (fileType == 'l') { // 링크파일의 경우 파일명 수정 필요 ( idx 5
													// 부터
													// fileName.. 5 이후 문자열을 통합 )
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
							fiList.add(fi); // fiList 에 등록
						} else if (fileType == '-') { // general files
							// general file에는 용량정보까지 포함 됨.

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

							fiList.add(fi); // fiList 에 등록
						} else { // directory 정보는 객체를 따로 저장하지 않음.
							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0));
							fiList.add(fi); // fiList 에 등록
						}

					}
				}
				Log.d("lvm", "file count : " + Integer.toString(fiList.size()));

				fList.clear();

				/* AppList 추출 */
				ArrayList<InstalledAppInfo> appList = new ArrayList<InstalledAppInfo>();
				appList = mInsAppInfo.ReadAppInfo(sName.replace("_userdata", ""));

				HashMap<String, String> appmap = new HashMap<String, String>();
				HashMap<String, String> appmapByPack = new HashMap<String, String>();
				resultAppListByAppName = new ArrayList<String>();
				resultAppListByPName = new ArrayList<String>();

				// 어플리스트를 Hashmap화 -- Key를 packages이름으로해서 appname을 value로 한다.
				for (int i = 0; i < appList.size(); i++) {
					appmap.put(appList.get(i).resultOfPackagesNamePrint(),
							appList.get(i).resultOfAppNamePrint());
					appmapByPack.put(appList.get(i).resultOfAppNamePrint(),
							appList.get(i).resultOfPackagesNamePrint());
					// resultAppListByPName.add(appList.get(i).resultOfPackagesNamePrint());
					// //패키지 이름도 리스트화
					// Log.d("APP", appList.get(i).resultOfAppNamePrint());
				}

				// 어플리스트를 생성
				for (int i = 0; i < fiList.size(); i++) {
					// fiList이름으로 appmap을 key로해서 resultAppList로 추가한다. 이걸 List로
					// 뿌린다.
					if (appmap.get(fiList.get(i).getName()) != null) {
						resultAppListByAppName.add(appmap.get(fiList.get(i)
								.getName()));
						//
						// Log.d("APP", resultAppList.get(i));
					}
				}

				for (int i = 0; i < resultAppListByAppName.size(); i++) {
					/*
					 * HashMap에서 Key로 AppName으로부터 다시 Package이름을 구해서
					 * resultAppListByPName에 넣는다.
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
			// 사용자 데이터 백업
			try {
				p = new ProcessBuilder("su").start();

				// func_code 에 따라서 snapshot 을 읽어들임.
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
				// ----------------------------------------------- 리스트 추가부분
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
							ss = ss.trim(); // 공백제거
							if (ss.length() != 0)
								splitedInfo.add(ss);
						}

						FileInfo fi;
						// split 결과는 실제 파일의 정보 , 하위 디렉토리 이름 으로 나누어짐.
						// 하위디렉토리 이름은 무시한다
						int idx = 0;

						char fileType = ' ';

						if (splitedInfo.size() != 0) { // 한 라인의 가장 첫번째 문자는 파일
														// 형식을
														// 나타냄..
							fileType = splitedInfo.get(0).charAt(0);
							// Log.d("lvm", "("+String.valueOf(fileType)+")");

							if (fileType == 'l') { // 링크파일의 경우 파일명 수정 필요 ( idx 5
													// 부터
													// fileName.. 5 이후 문자열을 통합 )
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
							fiList.add(fi); // fiList 에 등록
						} else if (fileType == '-') { // general files
							// general file에는 용량정보까지 포함 됨.

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

							fiList.add(fi); // fiList 에 등록
						} else { // directory 정보는 객체를 따로 저장하지 않음.
							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0));
							fiList.add(fi); // fiList 에 등록
						}

					}
				}
				Log.d("lvm", "file count : " + Integer.toString(fiList.size()));

				fList.clear();

				if (!subDir.equals(sName + "/0/")) { // 최 상단 디렉토리가 아닌경우
					// 상위메뉴를 만들어 줌
					Item Item = new Item("row", "..", false);
					ItemList.add(Item);
				}

				for (int i = 0; i < fiList.size(); i++) {
					if (fiList.get(i).getName().contains(":") && i != 0) { // 하위
																			// 디렉터리
						// fList.add(" ");
						// fList.add("[Dir]  " + fiList.get(i).getName());
						Item Item = new Item("row", ">  "
								+ fiList.get(i).getName(), false);
						ItemList.add(Item);
					} else if (!fiList.get(i).getType().equals("d")) { // 해당
																		// 디렉토리
																		// 내의 파일
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
			Item Item = new Item("contacts", "          전화번호부", false);
			ItemList.add(Item);
			Item Item1 = new Item("password", "          비밀번호", false);
			ItemList.add(Item1);
			Item Item2 = new Item("gesture", "          패턴lock", false);
			ItemList.add(Item2);
			Item Item3 = new Item("wifi", "          WiFi 설정값", false);
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

	// 복원항목 리스트 어댑터
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

				holder.name = (CheckBox) convertView // name view 는 code 를
														// match_parent 로 바꿔서
														// 안보인다.
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
							 * "새로운 메뉴 로드", Toast.LENGTH_LONG) .show();
							 */

							String dir = cur_Loc
									+ "/"
									+ tv.getText()
											.toString()
											.substring(3, tv.getText().length());
							displayListView(sName, dir);

						} else if (tv.getText().toString().equals("..")) {
							// 상위 메뉴로 이동
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
							if (cb.getText().toString().contains("전화번호부")) {
								if (CONTACTS == 1)
									CONTACTS = 0;
								else
									CONTACTS = 1;
							} else if (cb.getText().toString().contains("비밀번호")) {
								if (PASSWORD == 1)
									PASSWORD = 0;
								else
									PASSWORD = 1;
							} else if (cb.getText().toString()
									.contains("패턴lock")) {
								if (GESTURE == 1)
									GESTURE = 0;
								else
									GESTURE = 1;
							} else if (cb.getText().toString()
									.contains("WiFi 설정값")) {
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
						Item.setSelected(cb.isChecked(), cur_Loc,targetText.toString(), func_code); // 선택됨을 체크
																	// (선택 시 해당
																	// 경로와 이름을
																	// 저장 )

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

		 -2 -1인지 파싱하기 
		String apkName = ExtractAPKName(packageName, pwdPath);
		
		apkName = "file://" + pwdPath + apkName;
		Log.d("eee", apkName);
		// Log.d("eee", "합:"+pwdPath+apkName);

		 APK 실행 
		Intent cmdToInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
				Uri.parse(apkName), "application/vnd.android.package-archive");
		startActivity(cmdToInstall);
		// startActivityForResult(cmdToInstall, 1);

		 Tar로 묶기 
		TarTieDir(packageName, apkName);
	}
*/
	private void StartInstall2(String packageName, String pwdPath, String sName) {

		/* -2 -1인지 파싱하기 */
		String apkName = packageName;
		// apkName = "file://"+pwdPath+apkName;
		Log.d("eee", "apk"+apkName);
		// Log.d("eee", "합:"+pwdPath+apkName);

		pName = packageName;
		UsedsName = sName;

		/* APK 실행 */
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
		
		/* Tar로 묶기 */
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
			// /data/data영역
			String com = "cp -r /sdcard/ssDir/" + sName + "/data/"+packageDirectory+" /data/data/\n";
			
			//String com = "ls -l /sdcard/ssDir/" + sName + "/data/\n";

			Log.e("lll", com);

			p.getOutputStream().write(com.getBytes());

			mountCom = "umount /sdcard/ssDir/" + sName + "\n";

			Log.e("ccc", mountCom);

			p.getOutputStream().write(mountCom.getBytes());

			p.getOutputStream().write("exit\n".getBytes());
			p.getOutputStream().flush();

			// 마운트 경로 -> /data/data

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
					ss = ss.trim(); // 공백제거
					if (ss.length() != 0)
						splitedInfo.add(ss);
				}

				FileInfo fi;
				// split 결과는 실제 파일의 정보 , 하위 디렉토리 이름 으로 나누어짐.
				// 하위디렉토리 이름은 무시한다
				int idx = 0;

				char fileType = ' ';

				if (splitedInfo.size() != 0) { // 한 라인의 가장 첫번째 문자는 파일
												// 형식을
												// 나타냄..
					fileType = splitedInfo.get(0).charAt(0);
					// Log.d("lvm", "("+String.valueOf(fileType)+")");

					if (fileType == '-') { // general files
						// general file에는 용량정보까지 포함 됨.

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

						// fiList.add(fi); // fiList 에 등록
					} else { // directory 정보는 객체를 따로 저장하지 않음.
						fi = new FileInfo(String.valueOf(fileType),
								splitedInfo.get(0));
						// fiList.add(fi); // fiList 에 등록
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

		// apk 추가
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
																	// 선택 리스트

			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Notice");
			adb.setMessage("복원을 진행하시겠습니까?");
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
							ss.set_permission();   // root 권한 및 경로에 777권한 부여 
			                  
			                  if(CONTACTS == 1){
			                     // 전화번호부 복원루틴 수행
			                  }
			                  if(PASSWORD == 1){
			                     // 비밀번호 복원 루틴 수행
			                	  String copy_password = "cp /sdcard/ssDir/"+sName+"_usersystem/system/password.key /sdcard/password.key\n";
			                	  try {
									p.getOutputStream().write(copy_password.getBytes());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			                  }
			                  if(WIFI == 1){
			                     // 와이파이 복원 루틴 수행                  
					        		String copy_wifi = "cp /sdcard/ssDir/"+sName+"_usersystem/misc/wifi/wpa_supplicant.conf /sdcard/wpa_supplicant.conf\n";
					        		try {
										p.getOutputStream().write(copy_wifi.getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			                  }
			                  if(GESTURE == 1){
			                     // 패턴 복원 루틴 수행
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
							progressDialog.setMessage("파일 복원 중 입니다...");

							progressDialog.setCancelable(true);
							progressDialog.show();

							for (int i = 0; i < ItemList.size(); i++) {
								Item Item = ItemList.get(i);

								if (Item.isSelected()) {
									/* /data/data영역 체크 */
									String dataCoverPath = Item.getPath().replace(
											sName, "/sdcard/");
									String apkCoverPath = null;
									// TODO : 경로체크 필요

									Log.v("path",
											Item.getPath().replace(sName, "/data")); // 실제
																						// 경로
																						// 경로체크

									progressDialog.setProgress(i);

									// 마운트 진행 후 파일을 옮긴다. ( Sdcard 혹은 Server로 전송 )
									try {
										p = new ProcessBuilder("su").start();

										String mountCom = "mount -t ext4 /dev/vg/"
												+ sName + " /sdcard/ssDir/" + sName
												+ "\n";

										Log.v("eee", mountCom);

										p.getOutputStream().write(
												mountCom.getBytes());

										/*
										 * pName-1.apk가 없으면 pName-2.apk로 해준다.
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
						progressDialog.setMessage("파일 복원 중 입니다...");

						progressDialog.setCancelable(true);
						progressDialog.show();

						
						
						
						
						for (int i = 0; i < ItemList.size(); i++) {
							Item Item = ItemList.get(i);

							if (Item.isSelected()) {
								String finalPath = Item.getPath().replace(
										sName + "/0/", "/sdcard/");
								Log.v("eee",
										Item.getPath().replace(sName + "/0/",
												"/sdcard/")); // 실제 경로

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
								
								// 마운트 진행 후 파일을 옮긴다. ( Sdcard 혹은 Server로 전송 )
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

									// dd 로 obs

									// 1) ls 파싱 후 file size 를 읽어들인다
									

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
									
									// 2) file size 를 1000block 씩 분할 수행한다.
									
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

									// 복사 명령어 실행
									p.getOutputStream().write(
											sendToSocket.getBytes());
									p.getOutputStream().flush();
									
									AsyncFileSender afs = new
									AsyncFileSender(sc, p.getInputStream(), progressDialog , Item.getName());
									afs.execute();
									
									
									
									
									/*for(int j = 0 ; j < 1; j++){
								
										// 3) 1000block (500k) 씩 반복하면서 소켓으로 전송
										if (((j * 1000) + count) * 512 > fileSize) {

										}

										
										 sendToSocket = "dd if=/sdcard/ssDir/"
												+ Item.getPath()
												+ " obs=512k skip="
												+ (j * 1000)
												+ " count="
												+ count + "\n";

										Log.v("eee", sendToSocket);

										// 복사 명령어 실행
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
									 * oos.writeObject(pl); // code 8 번은 임시파일 전송
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

			adb.setPositiveButton("복원시작", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					// mDialog.dismiss();
					recovProcess.run();
				}
			});

			adb.setNegativeButton("취소", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mDialog.dismiss();// 종료
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
