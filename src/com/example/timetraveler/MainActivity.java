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
	public static int setVal1 = 0; // ��� �뷮 ���� �� 1
	public static int setVal2 = 1; // ��� �뷮 ���� �� 2

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
		

		// Handler ����
		handler = new opHandler(MainActivity.this);

		/* SnapShot Service ���� */
		Intent i = new Intent(this, SnapshotService.class);
		startService(i);
		
		/* Install App Loader Instanced */
		mInsAppInfo = new InstalledAppInfo(getApplicationContext());
		
		pd = new ProgressDialog(this);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("Loading initial data ...");
		pd.show();

		handler.setProgressDialog(pd);
		// ��� Snapshot List �� Load (on Device & on Server)
		// Restore ���� ����� ����Ʈ�� �ε���.
		
		// 0. ��� ����ó�� ( ������ ��������� �����ϴ��� Ȯ�� ) 
		
		 rd = new RegistrationDevice(mng,handler);
		
		if(!rd.chkUserOnSrv()){ // ��� ��Ͽ��� Ȯ��
			//����� �ȵǾ� ������ �ϴ��� �ڵ�����.
			// ����ڿ��� ��� ���� �ִ°Ű�..
			rd.createUser(); // ��⿡ ����� ����.
		}else{
			
		}

		// 1. Load Snapshot List on Device
		SnapshotDiskManager sdm = new SnapshotDiskManager(mapperPath);
		File[] sList = sdm.getSnapshotList();
		
		snapshotListInDev = sList; // ��ġ���� ����Ʈ ������
		
		// 2. Load Server List on Server
		conn = new ConnServer(this.srvIp, 12345, 0, rd.getUserCode(),
				handler);
		conn.start();

		// �ϴ� �޴��� ���� Pager
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
			
			snapshotListInDev = sList; // ��ġ���� ����Ʈ ������

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
	public class PagerAdapterClass extends PagerAdapter { // Page Adapter������ ����

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
			if (position == 0) { // Back up ������
				SimpleCursorAdapter mAdapter;
				v = mInflater.inflate(R.layout.inflate_one, null);

				mGroupList = new ArrayList<String>();
				mChildList = new ArrayList<ArrayList<String>>();
				mChildListContent = new ArrayList<ArrayList<String>>();
				mDestList = new ArrayList<String>();
				mChildDestList = new ArrayList<ArrayList<String>>();
				childDestList = new ArrayList<String>();

				mGroupList.add("��������� ������ ���");
				mGroupList.add("�������� ����");
				mGroupList.add("�ڵ� �������� ����");

				ArrayList<String> child1 = new ArrayList<String>();
				ArrayList<String> child2 = new ArrayList<String>();
				ArrayList<String> child3 = new ArrayList<String>();

				child1.add("���� ���");

				child2.add("��� ����");

				child3.add("�ڵ� ������ ���");

				mChildListContent.add(child1);
				mChildListContent.add(child2);
				mChildListContent.add(child3);

				mChildList.add(mChildListContent.get(0));
				mChildList.add(mChildListContent.get(1));
				mChildList.add(mChildListContent.get(2));

				mDestList.add("- ��������� ��� �����͸� ������ �����մϴ�.");
				mDestList.add("- ������¸� ������������ �����մϴ�.");
				mDestList.add("- �ڵ����������� �����մϴ�.");

				childDestList.add("������ �̹����� ������ �����մϴ�.");
				mChildDestList.add(childDestList);

				mListView = (ExpandableListView) v.findViewById(R.id.elv_list1);
				mListView.setAdapter(new BaseExpandableAdapter(v.getContext(),
						mGroupList, mChildList, mDestList, mChildDestList, 0));

				// �׷� Ŭ�� ���� ��� �̺�Ʈ
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

				// Backup �޴����� ���ϵ� Ŭ�� ���� ��� �̺�Ʈ
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
						
						case 0: // ���� ������ ������ ���
							if (childPosition == 0) // Server Backup
							{
								Intent sBackIntent = new Intent(
										MainActivity.this,
										SrvBackupActivity.class);
								sBackIntent.putExtra("userCode", rd.getUserCode()); // ����� �ڵ带 ���� ����Ʈ�� ����
								startActivity(sBackIntent);
							}

							break;
						case 1: // ���� ���� ���� ------------------------------------------ Create Snapshot
							// child menu 1�� �̹Ƿ� �ٷ� ����
							Toast.makeText(getApplicationContext(),
									"����� �����մϴ�.", Toast.LENGTH_SHORT).show();

							String line = "";
							StringBuffer output = new StringBuffer();
							
							// pipe �̿��� Snapshot ����
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
							 * �����Ǵ� lv snapshot�� �����Ǵ� ���ø���Ʈ�� ����Ѵ�.
							 * ���ø���Ʈ�� ArrayList�� �����ϰ� �̸� ������ HashMap��,
							 * lv snapshot�� ���� ��¥�� Key, ArrayList�� Value�� ������.
							 */
							/* ���� ����Ʈ�� �о� �鿩�� SharedPrefs �Ǵ� Ư�� ���Ͽ��� 
							 * HashMap���·� �����Ѵ�. 
							 * today�� key�� ���� */
							
							//mInsAppInfo.resultToSaveFile("ABC");
							
							//mInsAppInfo.ReadAppInfo(today);
							
						case 2: // scheduled snapshot
							// Alarm Manager

							setVal0 = true;
							Toast.makeText(getApplicationContext(),
									"�ڵ� �������� �����Ǿ����ϴ�.", Toast.LENGTH_SHORT)
									.show();

							break;
						}
						return false;
					}
				});

				// �׷��� ���� ��� �̺�Ʈ
				mListView
						.setOnGroupCollapseListener(new OnGroupCollapseListener() {
							@Override
							public void onGroupCollapse(int groupPosition) {
								// Toast.makeText(getApplicationContext(),
								// "g Collapse = " + groupPosition,
								// Toast.LENGTH_SHORT).show();
							}
						});

				// �׷��� ���� ��� �̺�Ʈ
				mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						// Toast.makeText(getApplicationContext(), "g Expand = "
						// + groupPosition,
						// Toast.LENGTH_SHORT).show();
					}
				});

			} else if (position == 1) { // Restore ������ 

				SimpleCursorAdapter mAdapter;
				
				
				// �ι�° �޴��� inflate �Ͽ� ����
				v = mInflater.inflate(R.layout.inflate_two, null);

				views.add(v); // Restore Page �� �÷��������ӿ�ũ�� �־��ش�.

				
			} else { // // Setting View ( ���� ������ )

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

				// ��Ӵٿ� ȭ�鿡 ǥ��
				aa.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
				dateSpinner.setAdapter(aa);

				final CheckBox chkBox1 = (CheckBox) v
						.findViewById(R.id.checkbox_upToSrv);
				final CheckBox chkBox2 = (CheckBox) v
						.findViewById(R.id.checkbox_delSnapshot);
				final CheckBox chkBox3 = (CheckBox) v
						.findViewById(R.id.checkbox_delBackup);

				// ����� ������ �ҷ��ɴϴ�.
				Boolean chk1 = pref.getBoolean("check1", false);
				Boolean chk2 = pref.getBoolean("check2", false);
				Boolean chk3 = pref.getBoolean("check3", false);

				chkBox1.setChecked(chk1);
				chkBox2.setChecked(chk2);
				chkBox3.setChecked(chk3);
				
				dateSpinner.setSelection(pref.getInt("spinnerSelection",0));

				
				// ���� ������ üũ�ڽ� ��Ŭ�� ������
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

				
				// Spinner ���� ��
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

		private AlertDialog mDialog ; // restore menu ���� ����Ʈ Dialog
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
		
		
		// ������ ����Ʈ�� �������� ���ϹǷ� �ڵ鷯�� �̿��ؼ� �޾ƿ´�.
		
		
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
				
				// pac ���� View �� �о��
				
				
				// mChildDestList , mChildList �� group ������ŭ ����ؾ� ��
				// mChildList �� childList�� �׷�. ( ��������� ���������� ���� )
				if(MainActivity.snapshotListInSrv  != null){
//					Log.e("eee", Integer.toString(MainActivity.snapshotListInSrv.length) );
					for (int i = 0; i < MainActivity.snapshotListInSrv.length; i++) {
						mGroupList.add(MainActivity.snapshotListInSrv[i].getName()+" [Server]");
						
						childList.add("���ø����̼�");
						childDestList.add(("s"));
						childList.add("����� ������");
						childDestList.add(("s"));
						childList.add("Contacts, Settings");
						childDestList.add(("s"));
						childList.add("��ü");
						childDestList.add(("s"));
						
						//mChildDestList.add("����� �׸��� �����ϴ�."+i); 
						mChildList.add((ArrayList<String>) childList.clone()); //childList�� ����3
						mChildDestList.add((ArrayList<String>) childDestList.clone());
						
						childList.clear();
						childDestList.clear();
					}
				}
				
				//�������� �־����� üũ�ϱ� ���� String ArrayList�� �־���´�.
				//substring(3,15)�� vg-����, ~~-cow���� ���� �̸��̴�.
				onlySnapshotInsertList = new ArrayList<String>();
				
				if(MainActivity.snapshotListInDev != null){
					for (int i = 0; i < MainActivity.snapshotListInDev.length; i++) {
						
						// String���� vg- ���� -cow���� �߰��ϱ� ���� -- cow�ִ��� Ȯ��
						if( !MainActivity.snapshotListInDev[i].getName().contains("cow"))
							continue;
						//�����ϰ� ������, �̹� �߰��� ���� ������ �������̸� �߰� ����
						String tempSs = MainActivity.snapshotListInDev[i].getName().substring(3, 15);
						if(onlySnapshotInsertList.contains(tempSs))
							continue;
						
						Log.i("ccc3", tempSs);
						onlySnapshotInsertList.add(tempSs);
						
						/* ��¥ ��Ÿ�Ϸ� ��ȯ�Ѵ�. */
						java.util.Date date = null;
						//���������� �Ľ��Ͽ� Date���·� ����� �ٽ� ���ο� �������� ���� stringȭ
						java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmm");
						   try {
							date = format.parse(tempSs);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						   java.text.SimpleDateFormat format1 = new java.text.SimpleDateFormat("yyyy�� MM�� dd�� HH�� mm��");
						   String dateStringSs = format1.format(date);
						   
						/* Listview�� �߰��Ѵ�. */
						mGroupList.add(dateStringSs+" [Device]");
						//mGroupList.add(MainActivity.snapshotListInDev[i].getName()+" [Device]");

						/**
						 *  ���� ������ ���� mapping �ʿ� 
						 *  ���ø����̼� --> � ��ġ
						 *  ����ڵ����� --> /usersdcard
						 *  ����/SMS/���� --> useDB, /usersystem
						 *  ��ü���� --> lvconvert
						 */
						childList.add("���ø����̼�");
						childDestList.add(("d"));
						childList.add("����� ������");
						childDestList.add(("d"));
						childList.add("Contacts, Settings");
						childDestList.add(("d"));
						childList.add("��ü ����");
						childDestList.add(("d"));
						
						//mChildDestList.add("����� �׸��� �����ϴ�."+i); 
						
						mChildList.add((ArrayList<String>) childList.clone());
						mChildDestList.add((ArrayList<String>) childDestList.clone());
						
						childList.clear();
						childDestList.clear();
						
					}
				}
				
				// ����Ʈ View �� ����
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
						
						if(gPosition >= srvSnapshotLen){ // gPosition��  snapshotListInSrv �̻��̸� Device Snapshot
							sName = onlySnapshotInsertList.get(gPosition-srvSnapshotLen); // Click �� ����Ʈ�� ����.
						}else{
							sName = MainActivity.snapshotListInSrv[gPosition].getName(); // Click �� ����Ʈ�� ����.
						}
						
						sName = sName.replace("vg-","").replace("-cow", "");
						// snapshot File �� lvm ���͸��� mount
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
						
						if(groupPosition >= srvSnapshotLen){ // gPosition��  snapshotListInSrv �̻��̸� Device Snapshot
							sName = onlySnapshotInsertList.get(groupPosition-srvSnapshotLen); // Click �� ����Ʈ�� ����.
						}else{
							sName = MainActivity.snapshotListInSrv[groupPosition].getName(); // Click �� ����Ʈ�� ����.
						}
						
						SnapListExpandableAdapter eAdapter = (SnapListExpandableAdapter) mListView.getExpandableListAdapter();
						String mName = eAdapter.getChild(groupPosition, childPosition);
						
						
						//
						Toast.makeText(vv.getContext(), "sName : "+sName+"\nmName:"+mName,
								Toast.LENGTH_SHORT).show();
						
						
						// �������� ����Ʈ �ؼ� ���渮��Ʈ �ε��� ( ������ ó�� �ʿ伺 )
						// ��ġ ���� ���� �� ���� �ǹ��Ѵ�.
						if(groupPosition >= srvSnapshotLen){// groupPosition-srvSnapshotLen �� devList idx
							/*Log.d("lvm",
									"file count : "
											+ Integer.toString(fiList
													.size()));
							*/
							SnapshotAlteration sa = new SnapshotAlteration();

							sName = sName.replace("vg-","").replace("��","").replace("��", "").replace("��","").trim();
							
							Log.e("eee", sName);
							if(mName.equals("���ø����̼�")){
								fiList.addAll(sa.getAppAlteration(sName));
							}else if(mName.equals("����� ������")){
								fiList.addAll(sa.getUserDataAlteration(sName)); // sName�� �ش��ϴ� FileInfoList�� ��´�.	
							}else if(mName.equals("Contacts, Settings")){
								fiList.addAll(sa.getSettingAlteration(sName));
							}else{ //��ü����
								 
							}
							
							// ------------ �о�� ����Ʈ�� �����Ѵ� --------------
							Collections.sort(fiList, timeComparator); // ��¥��
																		// ����
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
																				// ��
																				// ������
																				// ��
																				// ����Ʈ�Ǵ�
																				// ���͸�

							SnapshotDiskManager sdm = new SnapshotDiskManager(
									mountedDirLoc);*/
							// ������ ���͸� ���� ��� ����Ʈ�� �о�´�.
							// ArrayList<File> fileArrInDir =
							// sdm.getAllFilesInDepth();

						}else{ // groupPosition �� �� srv pos.
							Toast.makeText(vv.getContext(), "Server Img", Toast.LENGTH_SHORT).show();
						}
						
						pd.dismiss();
						
						// ���� ����Ʈ �ε� ��
						if(groupPosition >= srvSnapshotLen){ // ��ġ���� �����ϴ� �������� ���� ������ �д´�.
							if (mName.equals("[ ���� ��� ]")) {
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

								// �ֱ� ��������� Message�� ���.

								// ������� Read
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
												+ " ( ���� �ð� : "
												+ fiList.get(i).getDate() + " "
												+ fiList.get(i).getTime() + ")"
												+ "\n\n");
									}
								}

								ab.setMessage(sbMessage);

								ab.setCancelable(false); // Cancelable

								// custom view �ʿ�
								final String f_sName = sName;
								final String f_mName = mName;

								ab.setPositiveButton(mName + " ����",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												setDismiss(mDialog);

												// NextActivity > Recv Activity
												// �޴��� �̵�
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

								ab.setNegativeButton("��������",
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
						}else{ // ������ �ִ� �������� ���� ����Ʈ�� �д´�.
							
							// scroll View changed List

							// final ScrollView linear =
							// (ScrollView)View.inflate(context,
							// R.layout.scrolldialog, null);

							// Dialog as user want to see
							final AlertDialog.Builder ab = new AlertDialog.Builder(
									context);

							ab.setTitle("Recently changed items  [" + mName
									+ "]");

							// �ֱ� ��������� Message�� ���.

							// ������� Read
							final String sName_f = sName;
							final String mName_f = mName;
							
							Handler mHandler = new Handler(){
								@Override
								public void handleMessage(Message msg){
									switch(msg.what){
									case 0:
										
										// msg obj ��  
										String uData = (String)msg.obj;
										
										try {
											JSONObject jsonObj = new JSONObject(uData);
											Log.i("eee", sName_f);

											Log.i("eee", jsonObj.toString());
											
											JSONObject itmeObj = (JSONObject) jsonObj.get(sName_f);
											
											String changedItem = null;
											if(mName_f.equals("���ø����̼�")){
												changedItem = itmeObj.get("appAlt").toString();
												Log.d("eee", itmeObj.get("appAlt").toString());
											}else if(mName_f.equals("����� ������")){
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

										
										// custom view �ʿ�

										ab.setPositiveButton(mName_f + " ����",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface arg0, int arg1) {
														setDismiss(mDialog);

														// NextActivity > Recv Activity �޴���
														// �̵�
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

										ab.setNegativeButton("��������",
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
							
							// ������ ������ �о���� 
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
									object2.getDate()+object1.getTime()); // �������� ����

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


