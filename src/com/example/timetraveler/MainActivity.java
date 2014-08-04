package com.example.timetraveler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.ParseException;
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

import org.json.JSONException;
import org.json.JSONObject;

import net.kkangsworld.lvmexec.pipeWithLVM;
import net.kkangsworld.lvmexec.readHandler;

import com.Authorization.CodeGenerator;
import com.Authorization.RegistrationDevice;
import com.FileManager.FileInfo;
import com.FileManager.SnapshotDiskManager;
import com.FrameWork.ConnServer;
import com.FrameWork.SnapshotAlteration;
import com.FrameWork.InstalledAppInfo;
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
	public static String homePath = "/data/data/com.example.timetraveler/";
	//public static String homePath = "/dev/vg/";
	public static String mapperPath = "/dev/mapper/";
	private PagerAdapterClass pac;
	private RegistrationDevice rd;

	public static boolean setVal0 = false; // auto snapshot On // Off
	public static int setVal1 = 0; // 백업 용량 세팅 값 1
	public static int setVal2 = 1; // 백업 용량 세팅 값 2

	public static File[] snapshotListInSrv = null;
	public static File[] snapshotListInDev = null;
	ArrayList<String> onlySnapshotInsertList = null;
	
	readHandler rh;
	pipeWithLVM pl;
	String readResult;
	InstalledAppInfo mInsAppInfo;
	
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
		
		/* Install App Loader Instanced */
		mInsAppInfo = new InstalledAppInfo(getApplicationContext());
		
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
		SnapshotDiskManager sdm = new SnapshotDiskManager(mapperPath);
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
			
			SnapshotDiskManager sdm = new SnapshotDiskManager(mapperPath);
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
								sBackIntent.putExtra("userCode", rd.getUserCode()); // 사용자 코드를 다음 인텐트로 전송
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
							pl.ActionWritePipe("lvcreate -s -L 200M -n "+today+" /dev/vg/usersdcard");
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							/**
							 * 생성되는 lv snapshot에 상응되는 어플리스트를 백업한다.
							 * 어플리스트는 ArrayList로 존재하고 이를 저장할 HashMap은,
							 * lv snapshot에 대해 날짜를 Key, ArrayList를 Value로 가진다.
							 */
							/* 어플 리스트를 읽어 들여서 SharedPrefs 또는 특정 파일에에 
							 * HashMap형태로 저장한다. 
							 * today를 key로 저장 */
							
							//mInsAppInfo.resultToSaveFile("ABC");
							
							//mInsAppInfo.ReadAppInfo(today);
							
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
				
				
				// 두번째 메뉴를 inflate 하여 세팅
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
								//Log.i("eee", Integer.toString(setVal2));

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
		
		
		// 스냅샷 리스트는 동적으로 변하므로 핸들러를 이용해서 받아온다.
		
		
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
//					Log.e("eee", Integer.toString(MainActivity.snapshotListInSrv.length) );
					for (int i = 0; i < MainActivity.snapshotListInSrv.length; i++) {
						mGroupList.add(MainActivity.snapshotListInSrv[i].getName()+" [Server]");
						
						childList.add("어플리케이션");
						childDestList.add(("s"));
						childList.add("사용자 데이터");
						childDestList.add(("s"));
						childList.add("Contacts, Settings");
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
				
				//스냅샷을 넣었는지 체크하기 위해 String ArrayList에 넣어놓는다.
				//substring(3,15)는 vg-빼고, ~~-cow빼고 순수 이름이다.
				onlySnapshotInsertList = new ArrayList<String>();
				
				if(MainActivity.snapshotListInDev != null){
					for (int i = 0; i < MainActivity.snapshotListInDev.length; i++) {
						
						// String에서 vg- 빼고 -cow빼고만 추가하기 위함 -- cow있는지 확인
						if( !MainActivity.snapshotListInDev[i].getName().contains("cow"))
							continue;
						//포함하고 있으면, 이미 추가된 같은 시점의 스냅샷이면 추가 안함
						String tempSs = MainActivity.snapshotListInDev[i].getName().substring(3, 15);
						if(onlySnapshotInsertList.contains(tempSs))
							continue;
						
						Log.i("ccc3", tempSs);
						onlySnapshotInsertList.add(tempSs);
						
						/* 날짜 스타일로 변환한다. */
						java.util.Date date = null;
						//기존형식을 파싱하여 Date형태로 만들고 다시 새로운 형식으로 만들어서 string화
						java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmm");
						   try {
							date = format.parse(tempSs);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						   java.text.SimpleDateFormat format1 = new java.text.SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
						   String dateStringSs = format1.format(date);
						   
						/* Listview에 추가한다. */
						mGroupList.add(dateStringSs+" [Device]");
						//mGroupList.add(MainActivity.snapshotListInDev[i].getName()+" [Device]");

						/**
						 *  각각 영역에 대한 mapping 필요 
						 *  어플리케이션 --> 어떤 위치
						 *  사용자데이터 --> /usersdcard
						 *  전번/SMS/설정 --> useDB, /usersystem
						 *  전체복원 --> lvconvert
						 */
						childList.add("어플리케이션");
						childDestList.add(("d"));
						childList.add("사용자 데이터");
						childDestList.add(("d"));
						childList.add("Contacts, Settings");
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
							sName = onlySnapshotInsertList.get(gPosition-srvSnapshotLen); // Click 한 리스트를 읽음.
						}else{
							sName = MainActivity.snapshotListInSrv[gPosition].getName(); // Click 한 리스트를 읽음.
						}
						
						sName = sName.replace("vg-","").replace("-cow", "");
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
							sName = onlySnapshotInsertList.get(groupPosition-srvSnapshotLen); // Click 한 리스트를 읽음.
						}else{
							sName = MainActivity.snapshotListInSrv[groupPosition].getName(); // Click 한 리스트를 읽음.
						}
						
						SnapListExpandableAdapter eAdapter = (SnapListExpandableAdapter) mListView.getExpandableListAdapter();
						String mName = eAdapter.getChild(groupPosition, childPosition);
						
						
						//
						Toast.makeText(vv.getContext(), "sName : "+sName+"\nmName:"+mName,
								Toast.LENGTH_SHORT).show();
						
						
						// 스냅샷을 마운트 해서 변경리스트 로딩함 ( 스레드 처리 필요성 )
						// 장치 내의 스냅 샷 만을 의미한다.
						if(groupPosition >= srvSnapshotLen){// groupPosition-srvSnapshotLen 이 devList idx
							/*Log.d("lvm",
									"file count : "
											+ Integer.toString(fiList
													.size()));
							*/
							SnapshotAlteration sa = new SnapshotAlteration();

							sName = sName.replace("vg-","").replace("년","").replace("월", "").replace("일","").trim();
							
							Log.e("eee", sName);
							if(mName.equals("어플리케이션")){
								fiList.addAll(sa.getAppAlteration(sName));
							}else if(mName.equals("사용자 데이터")){
								fiList.addAll(sa.getUserDataAlteration(sName)); // sName에 해당하는 FileInfoList를 얻는다.	
							}else if(mName.equals("Contacts, Settings")){
								fiList.addAll(sa.getSettingAlteration(sName));
							}else{ //전체복원
								 
							}
							
							// ------------ 읽어온 리스트를 정렬한다 --------------
							Collections.sort(fiList, timeComparator); // 날짜별
																		// 정렬
							Collections.reverse(fiList);

							// log
							/*
							 * for(int i = 0 ; i < fiList.size() ; i ++){
							 * Log.i("lvm",
							 * "type "+fiList.get(i).getType()+"/"
							 * +fiList.get
							 * (i).getName()+"/"+fiList.get(i).getDate()
							 * +"/"+ fiList.get(i).getTime()); }
							 */
/*
							String mountedDirLoc = "/sdcard/ssDir/" + sName; // sName
																				// 은
																				// 스냅샷
																				// 이
																				// 마운트되는
																				// 디렉터리

							SnapshotDiskManager sdm = new SnapshotDiskManager(
									mountedDirLoc);*/
							// 스냅샷 디렉터리 내의 모든 리스트를 읽어온다.
							// ArrayList<File> fileArrInDir =
							// sdm.getAllFilesInDepth();

						}else{ // groupPosition 이 곧 srv pos.
							Toast.makeText(vv.getContext(), "Server Img", Toast.LENGTH_SHORT).show();
						}
						
						pd.dismiss();
						
						// 변경 리스트 로딩 끝
						if(groupPosition >= srvSnapshotLen){ // 장치내에 존재하는 스냅샷의 변경 내역을 읽는다.
							if (mName.equals("[ 복원 대상 ]")) {
								// nothing to do
							} else {
								// scroll View changed List

								// final ScrollView linear =
								// (ScrollView)View.inflate(context,
								// R.layout.scrolldialog, null);

								// Dialog as user want to see
								AlertDialog.Builder ab = new AlertDialog.Builder(
										context);

								ab.setTitle("Recently changed items  [" + mName
										+ "]");

								// 최근 변경사항을 Message에 띄움.

								// 변경사항 Read
								ArrayList<String> changedList = new ArrayList<String>();
								StringBuffer sbMessage = new StringBuffer();
								int vListSize = 0;

								for (int i = 0; i < fiList.size()
										&& vListSize < 3; i++) {
									changedList.add(fiList.get(i).getName());

									if (!fiList.get(i).getType().equals("d")) {
										vListSize++;
										sbMessage.append(vListSize + ") "
												+ fiList.get(i).getName()
												+ " ( 수정 시간 : "
												+ fiList.get(i).getDate() + " "
												+ fiList.get(i).getTime() + ")"
												+ "\n\n");
									}
								}

								ab.setMessage(sbMessage);

								ab.setCancelable(false); // Cancelable

								// custom view 필요
								final String f_sName = sName;
								final String f_mName = mName;

								ab.setPositiveButton(mName + " 복원",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												setDismiss(mDialog);

												// NextActivity > Recv Activity
												// 메뉴로 이동
												Intent recvIntent = new Intent(
														context,
														RecvActivity.class)
														.putExtra("sName",
																f_sName)
														.putExtra("mName",
																f_mName)
														.putExtra("loc",
																"dev");
												context.startActivity(recvIntent);

											}

										});

								ab.setNegativeButton("이전으로",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												setDismiss(mDialog);
											}
										});
								mDialog = ab.create();

								mDialog.show();
							}
						}else{ // 서버에 있는 스냅샷의 변경 리스트를 읽는다.
							
							// scroll View changed List

							// final ScrollView linear =
							// (ScrollView)View.inflate(context,
							// R.layout.scrolldialog, null);

							// Dialog as user want to see
							final AlertDialog.Builder ab = new AlertDialog.Builder(
									context);

							ab.setTitle("Recently changed items  [" + mName
									+ "]");

							// 최근 변경사항을 Message에 띄움.

							// 변경사항 Read
							final String sName_f = sName;
							final String mName_f = mName;
							
							Handler mHandler = new Handler(){
								@Override
								public void handleMessage(Message msg){
									switch(msg.what){
									case 0:
										
										// msg obj 는  
										String uData = (String)msg.obj;
										
										try {
											JSONObject jsonObj = new JSONObject(uData);
											Log.i("eee", sName_f);

											Log.i("eee", jsonObj.toString());
											
											JSONObject itmeObj = (JSONObject) jsonObj.get(sName_f);
											
											String changedItem = null;
											if(mName_f.equals("어플리케이션")){
												changedItem = itmeObj.get("appAlt").toString();
												Log.d("eee", itmeObj.get("appAlt").toString());
											}else if(mName_f.equals("사용자 데이터")){
												changedItem = itmeObj.get("udAlt").toString();
												Log.d("eee", itmeObj.get("udAlt").toString());
											}else if(mName_f.equals("Contacts, Settings")){
												changedItem = itmeObj.get("svAlt").toString();
												Log.d("eee", itmeObj.get("svAlt").toString());
												
											}else{
												
											}

											ab.setMessage(changedItem);
											
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										ab.setCancelable(false); // Cancelable

										
										// custom view 필요

										ab.setPositiveButton(mName_f + " 복원",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface arg0, int arg1) {
														setDismiss(mDialog);

														// NextActivity > Recv Activity 메뉴로
														// 이동
														Intent recvIntent = new Intent(
																context,
																RecvActivity.class)
																.putExtra(
																		"sName",
																		sName_f)
																.putExtra(
																		"mName",
																		mName_f)
																.putExtra(
																		"loc",
																		"dev");
														context.startActivity(recvIntent);

													}

												});

										ab.setNegativeButton("이전으로",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface arg0, int arg1) {
														setDismiss(mDialog);
													}
												});
										mDialog = ab.create();

										mDialog.show();
										
										
										break;
									case 1:
										
										
										break;				
									}
								}
							};
							
							// 스냅샷 정보를 읽어들임 
							ConnServer cs = new ConnServer( srvIp , 12345 , 7 ,  rd.getUserCode() , sName, mName, mHandler); 
							cs.start();
							
							try {
								cs.join(); // waiting thread
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
							
						
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


