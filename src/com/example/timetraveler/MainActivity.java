package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import net.kkangsworld.lvmexec.pipeWithLVM;
import net.kkangsworld.lvmexec.readHandler;

import com.Authorization.CodeGenerator;
import com.Authorization.RegistrationDevice;
import com.FileManager.FileInfo;
import com.FileManager.SnapshotDiskManager;
import com.FrameWork.ConnServer;
import com.FrameWork.opSwitch;

import android.R.color;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private ViewPager mPager;
	private ArrayList<String> mGroupList = null;
	private ArrayList<ArrayList<String>> mChildList = null;
	private ArrayList<String> mDestList = null;
	private ArrayList<ArrayList<String>> mChildDestList = null;
	private ArrayList<ArrayList<String>> mChildListContent = null;
	private ArrayList<String> childDestList = null;
	
	private ExpandableListView mListView;

	private String userCode;

	private opHandler handler;
	
	private ProgressDialog pd;

	ConnectivityManager manager;

	NetworkInfo mobile;
	NetworkInfo wifi;
	
	
	private ConnServer conn;
	
	public static String srvIp = "211.189.19.45";
	public static int srvPort = 12345 ;
	public static String homePath = "/dev/vg/";
	private PagerAdapterClass pac;
	private RegistrationDevice rd;

	public static boolean setVal0 = false; // auto snapshot On // Off
	public static int setVal1 = 0; // 백업 용량 세팅 값 1
	public static int setVal2 = 1; // 백업 용량 세팅 값 2

	public static File[] snapshotListInSrv = null;
	public static File[] snapshotListInDev = null;
	
	readHandler rh;
	pipeWithLVM pl;
	String readResult;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WifiManager mng = (WifiManager) getSystemService(WIFI_SERVICE);
		
		manager = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		

		// Handler 세팅
		handler = new opHandler(MainActivity.this);
	

		/* SnapShot Service 시작 */
		Intent i = new Intent(this, SnapshotService.class);
		startService(i);
		
		pd = new ProgressDialog(this);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("Loading initial data ...");
		pd.show();

		handler.setProgressDialog(pd);
		// 모든 Snapshot List 를 Load (on Device & on Server)
		// Restore 에서 사용할 리스트를 로드함.
		
		// 0. 기기 인증처리 ( 서버에 기기정보가 존재하는지 확인 ) 
		
		 rd = new RegistrationDevice(mng,handler);
		
		if(!rd.chkUserOnSrv()){ // 기기 등록여부 확인
			
			//등록이 안되어 있으면 일단은 자동생성.
			// 사용자에게 물어볼 수도 있는거고..
			rd.createUser(); // 기기에 사용자 생성.
		}else{
			
		}

		// 1. Load Snapshot List on Device
		SnapshotDiskManager sdm = new SnapshotDiskManager(homePath);
		File[] sList = sdm.getSnapshotList();
		
		snapshotListInDev = sList; // 장치내의 리스트 가져옴
		
		// 2. Load Server List on Server
		conn = new ConnServer(this.srvIp, 12345, 0, rd.getUserCode(),
				handler);
		conn.start();

		// 하단 메뉴를 위한 Pager
		pac = new PagerAdapterClass(getApplicationContext());
		setLayout();

		mPager = (ViewPager) findViewById(R.id.pager);

		mPager.setAdapter(pac);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_one:
			setCurrentInflateItem(0);

			break;
		case R.id.btn_two: // Restore page button
			setCurrentInflateItem(1);
			snapshotListInDev = null;
			snapshotListInSrv = null;
			
			pd.setMessage("Loading...");
			pd.show();
			
			handler.sendEmptyMessage(101); // View Reset Handler
			
			SnapshotDiskManager sdm = new SnapshotDiskManager(homePath);
			File[] sList = sdm.getSnapshotList();
			
			snapshotListInDev = sList; // 장치내의 리스트 가져옴

			// 2. Load Server List on Server
			ConnServer conn = new ConnServer("211.189.19.45", 12345, 0, rd.getUserCode(),
					handler);
			conn.start();
			
			break;
		case R.id.btn_three:
		
			setCurrentInflateItem(2);
			break;
		}
	}

	private void setCurrentInflateItem(int type) {
		if (type == 0) {
			mPager.setCurrentItem(0);
		} else if (type == 1) {
			mPager.setCurrentItem(1);
		} else {
			mPager.setCurrentItem(2);
		}
	}

	private Button btn_one;
	private Button btn_two;
	private Button btn_three;

	/**
	 * Layout
	 */

	private void setLayout() {
		btn_one = (Button) findViewById(R.id.btn_one);
		btn_two = (Button) findViewById(R.id.btn_two);
		btn_three = (Button) findViewById(R.id.btn_three);

		btn_one.setOnClickListener(this);
		btn_two.setOnClickListener(this);
		btn_three.setOnClickListener(this);
	}

	private View.OnClickListener mPagerListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String text = ((Button) v).getText().toString();
			// Toast.makeText(getApplicationContext(), text,
			// Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * PagerAdapter
	 */
	public class PagerAdapterClass extends PagerAdapter { // Page Adapter에서의 동작

		private ArrayList<View> views = new ArrayList<View>();

		private LayoutInflater mInflater;
		private TextView tmp;

		public PagerAdapterClass(Context c) {
			super();
			mInflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(ViewGroup pager, int position) {
			View v = null;
			if (position == 0) { // Back up 페이지
				SimpleCursorAdapter mAdapter;
				v = mInflater.inflate(R.layout.inflate_one, null);

				mGroupList = new ArrayList<String>();
				mChildList = new ArrayList<ArrayList<String>>();
				mChildListContent = new ArrayList<ArrayList<String>>();
				mDestList = new ArrayList<String>();
				mChildDestList = new ArrayList<ArrayList<String>>();
				childDestList = new ArrayList<String>();

				mGroupList.add("현재시점을 서버에 백업");
				mGroupList.add("복원시점 생성");
				mGroupList.add("자동 복원시점 생성");

				ArrayList<String> child1 = new ArrayList<String>();
				ArrayList<String> child2 = new ArrayList<String>();
				ArrayList<String> child3 = new ArrayList<String>();

				child1.add("서버 백업");

				child2.add("백업 시작");

				child3.add("자동 스냅샷 사용");

				mChildListContent.add(child1);
				mChildListContent.add(child2);
				mChildListContent.add(child3);

				mChildList.add(mChildListContent.get(0));
				mChildList.add(mChildListContent.get(1));
				mChildList.add(mChildListContent.get(2));

				mDestList.add("- 현재시점의 백업 데이터를 서버에 저장합니다.");
				mDestList.add("- 현재상태를 복원시점으로 생성합니다.");
				mDestList.add("- 자동복원시점을 생성합니다.");

				childDestList.add("스냅샷 이미지를 서버에 전송합니다.");
				mChildDestList.add(childDestList);

				mListView = (ExpandableListView) v.findViewById(R.id.elv_list1);
				mListView.setAdapter(new BaseExpandableAdapter(v.getContext(),
						mGroupList, mChildList, mDestList, mChildDestList, 0));

				// 그룹 클릭 했을 경우 이벤트
				mListView.setOnGroupClickListener(new OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						// Toast.makeText(getApplicationContext(), "g click = "
						// + groupPosition,
						// Toast.LENGTH_SHORT).show();
						switch (groupPosition) {
						case 0:
							break;
						case 1:
							break;
						case 2:
							break;
						}

						return false;
					}
				});

				// Backup 메뉴에서 차일드 클릭 했을 경우 이벤트
				mListView.setOnChildClickListener(new OnChildClickListener() {
					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						/*
						 * Toast.makeText( getApplicationContext(), "c click = "
						 * + childPosition + "(" + groupPosition + ")",
						 * Toast.LENGTH_SHORT).show();
						 */
						switch (groupPosition) {
						case 0: // 현재 시점을 서버에 백업
							if (childPosition == 0) // Server Backup
							{
								Intent sBackIntent = new Intent(
										MainActivity.this,
										SrvBackupActivity.class);
								sBackIntent.putExtra("userCode", rd.getUserCode());
								startActivity(sBackIntent);
							}

							break;
						case 1: // 복원 시점 생성 ------------------------------------------ Create Snapshot
							// child menu 1개 이므로 바로 진행
							Toast.makeText(getApplicationContext(),
									"백업을 시작합니다.", Toast.LENGTH_SHORT).show();

							String line = "";
							StringBuffer output = new StringBuffer();
							
							// pipe 이용한 Snapshot 생성
							rh = new readHandler() {
								public void handleMessage(Message msg) {
									Log.i("LVMJava", "ResultReader Handler result get");
									switch(msg.what) {
									case 0: //case 0
										Toast.makeText(getApplicationContext(), (String)msg.obj, Toast.LENGTH_LONG).show();
										readResult = (String)msg.obj;
										Log.d("inMain", readResult);
										break;
									}	
								}
							};
							Calendar cal = Calendar.getInstance();
							
							String today = (new SimpleDateFormat("yyyyMMddHHmm").format(cal
									.getTime()));
							
							pl = new pipeWithLVM(rh);
							pl.ActionWritePipe("lvcreate -s -L 200M -n "+today+" /dev/vg/userdata");
							
							
							// 어플 리스트를 읽어들인다.
						/*	
							PackageManager pm = getPackageManager();

							List<PackageInfo> packs = getPackageManager()
									.getInstalledPackages(
											PackageManager.PERMISSION_GRANTED);

							for (PackageInfo pack : packs) {

								Log.i("TAG", pack.applicationInfo.loadLabel(pm)
										.toString());
								String sDir = pack.applicationInfo.sourceDir;
								//Log.i("TAG", pack.packageName);
								
								Log.i("TAG", "appDir : "+sDir);
								
							}*/
							
							
							// 어플리스트 백업
							
							
							
							// 개인정보
							
							// SMS
							/*String MESSAGE_TYPE_INBOX = "1";
							String MESSAGE_TYPE_SENT = "2";
							String MESSAGE_TYPE_CONVERSATIONS = "3";
							String MESSAGE_TYPE_NEW = "new";
							
							Uri allMessage = Uri.parse("content://sms/");
							
							Cursor cur = getContentResolver().query(allMessage,
									null, null, null, null);
							int count = cur.getCount();
							Log.i("TAG", "SMS count = " + count);
							String row = "";
							String msg = "";
							String date = "";
							String protocol = "";
							while (cur.moveToNext()) {
								row = cur.getString(cur
										.getColumnIndex("address"));
								msg = cur.getString(cur.getColumnIndex("body"));
								date = cur.getString(cur.getColumnIndex("date"));
								protocol = cur.getString(cur
										.getColumnIndex("protocol"));
								// Logger.d( TAG , "SMS PROTOCOL = " +
								// protocol);

								String type = "";
								if (protocol == MESSAGE_TYPE_SENT)
									type = "sent";
								else if (protocol == MESSAGE_TYPE_INBOX)
									type = "receive";
								else if (protocol == MESSAGE_TYPE_CONVERSATIONS)
									type = "conversations";
								else if (protocol == null)
									type = "send";

								Log.i("TAG", "SMS Phone: " + row + " / Mesg: "
										+ msg + " / Type: " + type
										+ " / Date: " + date);
							}

							break;*/
							

							// 통화내역
							
							
							
							// 설정
							
							// settings db 이용
							
							
							// 연락처
							
						case 2: // scheduled snapshot
							// Alarm Manager

							setVal0 = true;
							Toast.makeText(getApplicationContext(),
									"자동 스냅샷이 설정되었습니다.", Toast.LENGTH_SHORT)
									.show();

							break;
						}
						return false;
					}
				});

				// 그룹이 닫힐 경우 이벤트
				mListView
						.setOnGroupCollapseListener(new OnGroupCollapseListener() {
							@Override
							public void onGroupCollapse(int groupPosition) {
								// Toast.makeText(getApplicationContext(),
								// "g Collapse = " + groupPosition,
								// Toast.LENGTH_SHORT).show();
							}
						});

				// 그룹이 열릴 경우 이벤트
				mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						// Toast.makeText(getApplicationContext(), "g Expand = "
						// + groupPosition,
						// Toast.LENGTH_SHORT).show();
					}
				});

			} else if (position == 1) { // Restore 페이지

				SimpleCursorAdapter mAdapter;

				v = mInflater.inflate(R.layout.inflate_two, null);

				views.add(v); // Restore Page 만 컬렉션프레임워크에 넣어준다.

				
			} else { // // Setting View ( 세팅 페이지 )

				v = mInflater.inflate(R.layout.inflate_three, null);

				final SharedPreferences pref = getBaseContext().getSharedPreferences("SaveState", getBaseContext().MODE_PRIVATE);
				final SharedPreferences.Editor edit = pref.edit();
				
				setVal1 = pref.getInt("setVal1", 0);
				
				final Spinner dateSpinner = (Spinner) v
						.findViewById(R.id.dateSpinner);

				ArrayList<String> date = new ArrayList<String>();

				for (int i = 1; i <= 15; i++) {
					date.add(Integer.toString(i));
				}

				Log.i("rrr", Integer.toString(date.size()));

				ArrayAdapter<String> aa = new ArrayAdapter<String>(
						v.getContext(), android.R.layout.simple_spinner_item,
						date);

				// 드롭다운 화면에 표시
				aa.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
				dateSpinner.setAdapter(aa);

				final CheckBox chkBox1 = (CheckBox) v
						.findViewById(R.id.checkbox_upToSrv);
				final CheckBox chkBox2 = (CheckBox) v
						.findViewById(R.id.checkbox_delSnapshot);
				final CheckBox chkBox3 = (CheckBox) v
						.findViewById(R.id.checkbox_delBackup);

				// 저장된 값들을 불러옵니다.
				Boolean chk1 = pref.getBoolean("check1", false);
				Boolean chk2 = pref.getBoolean("check2", false);
				Boolean chk3 = pref.getBoolean("check3", false);

				chkBox1.setChecked(chk1);
				chkBox2.setChecked(chk2);
				chkBox3.setChecked(chk3);
				
				dateSpinner.setSelection(pref.getInt("spinnerSelection",0));

				
				// 설정 페이지 체크박스 온클릭 리스너
				chkBox1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						chkBox2.setChecked(false);
						chkBox3.setChecked(false);

						Log.i("1_setVal1", Integer.toString(setVal1));
						setVal1 = 1;
						edit.putBoolean("check1", chkBox1.isChecked());
						edit.putBoolean("check2", chkBox2.isChecked());
						edit.putBoolean("check3", chkBox3.isChecked());
						edit.commit();
					}

				});

				chkBox2.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						chkBox1.setChecked(false);
						chkBox3.setChecked(false);
						Log.i("2_setVal1", Integer.toString(setVal1));
						setVal1 = 2;
						edit.putBoolean("check2", chkBox2.isChecked());
						edit.putBoolean("check1", chkBox1.isChecked());
						edit.putBoolean("check3", chkBox3.isChecked());
						edit.commit();
					}

				});

				chkBox3.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						chkBox1.setChecked(false);
						chkBox2.setChecked(false);
						Log.i("3_setVal1", Integer.toString(setVal1));
						setVal1 = 3;
						edit.putBoolean("check3", chkBox3.isChecked());
						edit.putBoolean("check1", chkBox1.isChecked());
						edit.putBoolean("check2", chkBox2.isChecked());
						edit.commit();
					}

				});

				// Spinner 설정 값
				dateSpinner
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								
								setVal2 = Integer.parseInt(arg0
										.getItemAtPosition(arg2).toString());
								Log.i("eee", Integer.toString(setVal2));

								int selectedPosition = dateSpinner.getSelectedItemPosition();
								Log.i("position", "position : " + (Integer.toString(selectedPosition+1)));
								edit.putInt("spinnerSelection",selectedPosition);
								edit.commit();
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub
							}

						});

			}
			((ViewPager) pager).addView(v, 0);

			return v;
		}

		@Override
		public void destroyItem(View pager, int position, Object view) {
			((ViewPager) pager).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View pager, Object obj) {
			return pager == obj;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		public View getView(int position) {
			return views.get(position);
		}

		public void removeView(int postion) {
			views.remove(postion);
		}

		public int getViewCount() {
			return views.size();
		}

	}
	
	class opHandler extends Handler {
		private ArrayList<String> mGroupList = null;
		private ArrayList<ArrayList<String>> mChildList = null;
		private ArrayList<String> mDestList = null;
		private ArrayList<ArrayList<String>> mChildDestList = null;
		private ArrayList<ArrayList<String>> mChildListContent = null;
		private ExpandableListView mListView;
		private ArrayList<String> childList;
		private ArrayList<String> childDestList;

		private AlertDialog mDialog ; // restore menu 변경 리스트 Dialog
		private Context context;
		private ProgressDialog pd;
		
		public opHandler(Context context) {
			mGroupList = new ArrayList<String>();
			mChildList = new ArrayList<ArrayList<String>>();
			mChildListContent = new ArrayList<ArrayList<String>>();
			mDestList = new ArrayList<String>();
			mChildDestList = new ArrayList<ArrayList<String>>();
			childList = new ArrayList<String>();
			childDestList = new ArrayList<String>();
			this.context = context;
		}
		
		public void setProgressDialog(ProgressDialog pd){
			this.pd = pd;
		}
		
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

			View vv = pac.getView(0);

			switch (msg.what) {
			case 0:

				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
				
			case 100: // Snapshot List Handling
				
				// pac 에서 View 를 읽어옴
				
				
				// mChildDestList , mChildList 는 group 개수만큼 등록해야 함
				// mChildList 는 childList의 그룹. ( 변경사항이 여러개임을 감안 )
				if(MainActivity.snapshotListInSrv  != null){
					for (int i = 0; i < MainActivity.snapshotListInSrv.length; i++) {
						mGroupList.add(MainActivity.snapshotListInSrv[i].getName()+" [Server]");
						
						childList.add("어플리케이션");
						childDestList.add(("s"));
						childList.add("사용자 데이터");
						childDestList.add(("s"));
						childList.add("전화번호부, SMS, 설정 복원");
						childDestList.add(("s"));
						childList.add("전체");
						childDestList.add(("s"));
						
						//mChildDestList.add("변경된 항목이 없습니다."+i); 
						mChildList.add((ArrayList<String>) childList.clone()); //childList를 복제3
						mChildDestList.add((ArrayList<String>) childDestList.clone());
						
						childList.clear();
						childDestList.clear();

					}
				}
				
				if(MainActivity.snapshotListInDev != null){
					for (int i = 0; i < MainActivity.snapshotListInDev.length; i++) {
						mGroupList.add(MainActivity.snapshotListInDev[i].getName()+" [Device]");
						
						
						childList.add("어플리케이션");
						childDestList.add(("d"));
						childList.add("사용자 데이터");
						childDestList.add(("d"));
						childList.add("전화번호부, SMS, 설정 복원");
						childDestList.add(("d"));
						childList.add("전체 복원");
						childDestList.add(("d"));
						
						//mChildDestList.add("변경된 항목이 없습니다."+i); 
						
						mChildList.add((ArrayList<String>) childList.clone());
						mChildDestList.add((ArrayList<String>) childDestList.clone());
						
						childList.clear();
						childDestList.clear();
						
					}
				}
				

				
				// 리스트 View 에 적용
				mListView = (ExpandableListView) vv.findViewById(R.id.elv_list2);
				mListView.setAdapter(new SnapListExpandableAdapter(vv.getContext(),
						mGroupList, mChildList, mDestList, mChildDestList, 1));

				
				mListView.setOnGroupClickListener(new OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView elv, View vv,
							int gPosition, long arg3) {
						// TODO Auto-generated method stub

						
						String sName = null;
						int srvSnapshotLen = MainActivity.snapshotListInSrv.length;
						
						if(gPosition >= srvSnapshotLen){ // gPosition이  snapshotListInSrv 이상이면 Device Snapshot
							sName = MainActivity.snapshotListInDev[gPosition-srvSnapshotLen].getName(); // Click 한 리스트를 읽음.
						}else{
							sName = MainActivity.snapshotListInSrv[gPosition].getName(); // Click 한 리스트를 읽음.
						}
						
						// snapshot File 을 lvm 디렉터리에 mount
						File f = new File("/sdcard/ssDir/"+sName);
						
						if(f.mkdirs())
						{
							Log.i("lvm","created");
						}else{
							Log.i("lvm","mkdir error!");
						}
						
						return false;
					}
					
				});
				
				mListView.setOnChildClickListener(new OnChildClickListener(){

					@Override
					public boolean onChildClick(ExpandableListView parent, View vv,
							int groupPosition, int childPosition, long id) {
						// TODO Auto-generated method stub
						
						String sName = null;
						int srvSnapshotLen = MainActivity.snapshotListInSrv.length;
						ArrayList<FileInfo> fiList = new ArrayList<FileInfo>(); // fileInfo List
						
						ProgressDialog pd = new ProgressDialog(context);
						pd.setTitle("Processing...");
						pd.setMessage("Please wait ..");
						pd.show();
						
						if(groupPosition >= srvSnapshotLen){ // gPosition이  snapshotListInSrv 이상이면 Device Snapshot
							sName = MainActivity.snapshotListInDev[groupPosition-srvSnapshotLen].getName(); // Click 한 리스트를 읽음.
						}else{
							sName = MainActivity.snapshotListInSrv[groupPosition].getName(); // Click 한 리스트를 읽음.
						}
						
						SnapListExpandableAdapter eAdapter = (SnapListExpandableAdapter) mListView.getExpandableListAdapter();
						String mName = eAdapter.getChild(groupPosition, childPosition);
						
						
						//
						Toast.makeText(vv.getContext(), "sName : "+sName+"\nmName:"+mName,
								Toast.LENGTH_SHORT).show();
						
						// 변경리스트 로딩 ( 스레드 처리 필요성 )
						
						try {

							Process p =  Runtime.getRuntime().exec("su"); //  root 쉘
							
							// gName ( 해당 스냅샷 이름 ) 에 mount 후 해당 디렉토리 리스트를 읽어들임.
							
							String mountCom = "mount -t ext4 /dev/vg/"+sName+" /sdcard/ssDir/"+sName+"\n";
							
							p.getOutputStream().write(mountCom.getBytes());
							
							// root 계정상태에서 ls -lR (sub directory 까지 read)
							String com = "ls -lR /sdcard/ssDir/"+sName+"\n";
							p.getOutputStream().write( com.getBytes());

							// roote 종료
							p.getOutputStream().write("exit\n".getBytes());			
							p.getOutputStream().flush();
							
							// snapshot list load standard i/o
							BufferedReader br = new BufferedReader(new InputStreamReader( p.getInputStream()));

							String line = null;
							ArrayList<String> lineArr = new ArrayList<String>(); // 명령의 결과 String line
							//StringBuffer sTotalList = new StringBuffer();
							
							while((line = br.readLine()) != null){
								//sTotalList.append(line+"\n");
								lineArr.add(line);
							}
							
							
							
							for(String s : lineArr){
								
								String[] info = s.split(" ");
								ArrayList<String> splitedInfo = new ArrayList<String>();
								
								for(String ss : info){
									ss = ss.trim();
									if(ss.length() != 0)
										splitedInfo.add(ss);
								}

								FileInfo fi;
								// split 결과는 실제 파일의 정보 , 하위 디렉토리 이름 으로 나누어짐.
								// 하위디렉토리 이름은 무시한다
								int idx = 0;
								
								
								char fileType = ' ';
								
								if(splitedInfo.size() != 0){ // 한 라인의 가장 첫번째 문자는 파일 형식을 나타냄..
									fileType = splitedInfo.get(0).charAt(0);
									//Log.d("lvm", "("+String.valueOf(fileType)+")");
									
									if(fileType == 'l'){ // 링크파일의 경우 파일명 수정 필요 ( idx 5 부터 fileName.. 5 이후 문자열을 통합 )
										String fName = splitedInfo.get(5)+splitedInfo.get(6)+splitedInfo.get(7);
										splitedInfo.set(5, fName);
										splitedInfo.remove(7);
										splitedInfo.remove(6);
									}
									
								}
								
								if(fileType == 'd' || fileType == 'b' || fileType == 'c' ||fileType == 'p' || fileType == 'l' || fileType == 's' ){ // special files
									// b(Block file(b) , Character device file(c) , Named pipe file or just a pipe file(p)
									// Symbolic link file(l), Socket file(s)
								
									fi = new FileInfo(String.valueOf(fileType), splitedInfo.get(0).substring(1),splitedInfo.get(3) , splitedInfo.get(4) , splitedInfo.get(5) );
									fiList.add(fi); // fiList 에 등록
								}else if(fileType == '-'){ // general files
									// general file에는 용량정보까지 포함 됨.
									fi = new FileInfo(String.valueOf(fileType), splitedInfo.get(0).substring(1), splitedInfo.get(3) , splitedInfo.get(4) , splitedInfo.get(5) ,  splitedInfo.get(6));
									fiList.add(fi); // fiList 에 등록
								}else{ // directory 정보는 객체를 따로 저장하지 않음.
									// nothing to do
								}
								
							}
							Log.d("lvm", "file count : "+Integer.toString(fiList.size()) );

							
							// ------------ 읽어온 리스트를 정렬한다 --------------
							Collections.sort(fiList ,timeComparator); // 날짜별 정렬
							Collections.reverse(fiList);
							
							//log
	/*						for(int i = 0 ; i < fiList.size() ; i ++){
								Log.i("lvm", "type "+fiList.get(i).getType()+"/"+fiList.get(i).getName()+"/"+fiList.get(i).getDate() +"/"+ fiList.get(i).getTime());
							}
	*/					
							
							
							String mountedDirLoc = "/sdcard/ssDir/"+sName; // sName 은 스냅샷 이 마운트되는 디렉터리
							
							SnapshotDiskManager sdm = new SnapshotDiskManager(mountedDirLoc);
							// 스냅샷 디렉터리 내의 모든 리스트를 읽어온다.
							//ArrayList<File> fileArrInDir = sdm.getAllFilesInDepth();
							
							//Log.i("lvm",Integer.toString(fileArrInDir.size()) );
							/*
							ArrayList<File> latestThree = sdm.getLatModified(fileArrInDir);
							*/
							// latestThree 에서 File name list 로 출력
							
							
							// list view update
							
							
	/*						BaseExpandableAdapter eAdapter = (BaseExpandableAdapter) mListView.getExpandableListAdapter();

							Object obj1 = eAdapter.getChild(0, 0); // get Child
							eAdapter.setChildDesc(0, "eee");
							eAdapter.notifyDataSetChanged();
							
							
							Toast.makeText(vv.getContext(), obj1.toString(),
									Toast.LENGTH_SHORT).show();
	*/
							try {
								p.waitFor();
								if (p.exitValue() != 255) {
									// TODO Code to run on success
									Toast.makeText(vv.getContext(), "root",
											Toast.LENGTH_SHORT).show();
								} else {
									// TODO Code to run on unsuccessful
									Toast.makeText(vv.getContext(),
											"not root", Toast.LENGTH_SHORT).show();
								}

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(vv.getContext(), "not root",
										Toast.LENGTH_SHORT).show();
							} 
							
							
							
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("lvm", "error (ioError) : "+e.toString());
							e.printStackTrace();
						} 
						
						
						pd.dismiss();
						
						// 변경 리스트 로딩 끝
						
						if(mName.equals("[ 복원 대상 ]")){
							// nothing to do
						}else{
							// scroll View changed List
							
							//final ScrollView linear = (ScrollView)View.inflate(context, R.layout.scrolldialog, null);
							
							// Dialog as user want to see
							AlertDialog.Builder ab = new AlertDialog.Builder(context);
							
							ab.setTitle("Recently changed items  ["+mName+"]");
							
							// 최근 변경사항을 Message에 띄움.
							
							// 변경사항 Read 
							ArrayList<String> changedList = new ArrayList<String>();
							StringBuffer sbMessage = new StringBuffer();
							int vListSize = 0;
							
							for(int i = 0 ; i < fiList.size() && vListSize < 3 ; i++){
								changedList.add(fiList.get(i).getName());
								
								if(!fiList.get(i).getType().equals("d")){
									vListSize++;
									sbMessage.append(vListSize+") "+fiList.get(i).getName()+" ( 수정 시간 : "+fiList.get(i).getDate()+" "+fiList.get(i).getTime()+")"+"\n\n");	
								}
							}
							
							ab.setMessage(sbMessage); 
							
							ab.setCancelable(false); // Cancelable

						
							
							// custom view 필요
							final String f_sName = sName;
							final String f_mName = mName;
							
							ab.setPositiveButton(mName+" 복원",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0,
												int arg1) {
											setDismiss(mDialog);
											
											// NextActivity > Recv Activity 메뉴로 이동 
											Intent recvIntent = new Intent( context , RecvActivity.class).putExtra("sName", f_sName).putExtra("mName", f_mName);
											context.startActivity(recvIntent);
											
										}

									});

							ab.setNegativeButton("이전으로",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface arg0,
												int arg1) {
											setDismiss(mDialog);
										}
									});
							mDialog = ab.create();
							
							mDialog.show();
						}
						
						
						return false;
					}// child on Click end
					

					private final Comparator<FileInfo> timeComparator = new Comparator<FileInfo>() {
					
						private final Collator collator = Collator
								.getInstance();

						@Override
						public int compare(FileInfo object1, FileInfo object2) {
							return collator.compare(object1.getDate()+object1.getTime(),
									object2.getDate()+object1.getTime()); // 내림차순 정렬

						}
					};
					
					
				});
				
				
				Toast.makeText(vv.getContext(), "Reading complete...",
						Toast.LENGTH_SHORT).show();
				dismissDialog(pd);
				
				break;
			case 101: // Clearing Snapshot List 
				mGroupList.clear();
				mChildList.clear();
				mDestList.clear();
				mChildDestList.clear();
				
				mListView = (ExpandableListView) vv.findViewById(R.id.elv_list2);
				mListView.setAdapter(new SnapListExpandableAdapter(vv.getContext(),
						mGroupList, mChildList, mDestList, mChildDestList, 1));

				break;
			case 102:
				
				
				break;
			default:
				break;
			}
			
		}

		private void dismissDialog(ProgressDialog pd) {
			// TODO Auto-generated method stub
			pd.cancel();
		}

		private void setDismiss(Dialog dialog) {
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
		}
		
		
	}

}


