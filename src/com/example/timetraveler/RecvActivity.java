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
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
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

	MyCustomAdapter dataAdapter = null;

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

		displayListView();

	}
	


	private void displayListView() {
		// TODO Auto-generated method stub
		// Array list of countries
		ArrayList<Country> countryList = new ArrayList<Country>();
	/*	Country country = new Country("AFG", "Afghanistan", false);
		countryList.add(country);
	*/
		
		
		String sName = getIntent().getStringExtra("sName").replace("/dev", "")
				.replace("-cow", "");
		String mName = getIntent().getStringExtra("mName");
		String loc = getIntent().getStringExtra("loc"); // 데이터 위치 ( dev : 장치 내 ,
														// srv : 서버 )
		/*
		 * sName ( snapshot name ) mName ( selected menu name ) sName 데이터에서
		 * mName 에 해당하는 데이터를 읽어온다.*/
		 

		if (mName.equals("어플리케이션")) { // 어플리케이션 복원
			func_code = RECV_APP;
		} else if (mName.equals("사용자 데이터")) { // 사용자 데이터 복원
			func_code = RECV_USER_DATA;
		} else if (mName.equals("전화번호부, SMS, 설정 복원")) { // 전화, sms , 설정 복원
			func_code = RECV_SETTINGS;
		} else { // 전체 복원
			func_code = RECV_ALL;
		}

		switch (func_code) {
		case RECV_ALL:
			// 전체 복원 , 바로 lvconvert 수행

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
			// 사용자 데이터 백업
			try {
				p = new ProcessBuilder("su").start();

				// func_code 에 따라서 snapshot 을 읽어들임.
				Log.i("ccc", sName + "/" + loc);

				String mountCom = "mount -t ext4 /dev/vg/" + sName
						+ " /sdcard/ssDir/" + sName + "\n";

				p.getOutputStream().write(mountCom.getBytes());

				String com = "ls -lR /sdcard/ssDir/" + sName + "\n";

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

				// List View , adapter ----------------------------------------------- 리스트 추가부분
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
							fi = new FileInfo(String.valueOf(fileType),
									splitedInfo.get(0).substring(1),
									splitedInfo.get(3), splitedInfo.get(4),
									splitedInfo.get(5), splitedInfo.get(6));
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

				for (int i = 0; i < fiList.size(); i++) {
					if (fiList.get(i).getName().contains(":") && i != 0) { // 하위
																			// 디렉터리
						//fList.add(" ");
						//fList.add("[Dir]  " + fiList.get(i).getName());
						Country country = new Country("row", "[Dir]  " + fiList.get(i).getName(), false);
						countryList.add(country);
					} else if (!fiList.get(i).getType().equals("d")) { // 해당
																		// 디렉토리
																		// 내의 파일
						//fList.add(fiList.get(i).getName());
						Country country = new Country("row", fiList.get(i).getName(), false);
						countryList.add(country);
					}

				}

/*				ItemListArrayAdapter adapter = new ItemListArrayAdapter(this,
						android.R.layout.simple_list_item_1, fList);
				lv.setAdapter(adapter);
*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case RECV_SETTINGS:
			break;
		}

		// create an ArrayAdaptar from the String Array
		dataAdapter = new MyCustomAdapter(this, R.layout.file_info, countryList);
		ListView listView = (ListView) findViewById(R.id.lv_recvList);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Country country = (Country) parent.getItemAtPosition(position);
				Toast.makeText(getApplicationContext(),
						"Clicked on Row: " + country.getName(),
						Toast.LENGTH_LONG).show();
			}
		});
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

	private class MyCustomAdapter extends ArrayAdapter<Country> {
		private ArrayList<Country> countryList;

		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<Country> countryList) {
			super(context, textViewResourceId, countryList);
			this.countryList = new ArrayList<Country>();
			this.countryList.addAll(countryList);
		}

		private class ViewHolder {
			TextView code;
			CheckBox name;
			RelativeLayout row;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			//Log.v("ConvertView", String.valueOf(position));

			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.file_info, null);

				holder = new ViewHolder();
				holder.code = (TextView) convertView.findViewById(R.id.code);

				holder.name = (CheckBox) convertView // name view 는 code 를 match_parent 로 바꿔서 안보인다.
						.findViewById(R.id.checkBox1);
				//holder.row = (RelativeLayout) convertView.findViewById(R.id.file_row);
				
				convertView.setTag(holder);

				
				holder.name.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						Country country = (Country) cb.getTag();
						Toast.makeText(
								getApplicationContext(),
								"Clicked on Checkbox: " + cb.getText() + " is "
										+ cb.isChecked(), Toast.LENGTH_LONG)
								.show();
						country.setSelected(cb.isChecked());
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Country country = countryList.get(position);
		
			
			if(country.getName().contains("[Dir]")){
				holder.code.setText(country.getName().replace("[Dir]", ">  "));
				holder.name.setVisibility(View.GONE);
				holder.name.setTag(country);
			}else{
				holder.name.setVisibility(View.VISIBLE);
				holder.name.setText(country.getName());
				holder.name.setChecked(country.isSelected());
				holder.name.setTag(country);
			}
			return convertView;

		}
	}

	public void mOnClick(View v) {
		switch (v.getId()) {
		case R.id.startRecv: // startRecovery
			StringBuffer responseText = new StringBuffer();
			responseText.append("The following were selected...\n");

			ArrayList<Country> countryList = dataAdapter.countryList; // checkbox 선택 리스트
			
			
			
			for (int i = 0; i < countryList.size(); i++) {
				Country country = countryList.get(i);
				if (country.isSelected()) {
					responseText.append("\n" + country.getName());
				}
			}

			Toast.makeText(getApplicationContext(), responseText,
					Toast.LENGTH_LONG).show();
			
			
			
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Notice");
			adb.setMessage("복원을 진행하시겠습니까?");
			final Dialog mDialog = adb.create();

			adb.setPositiveButton("복원시작", new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					mDialog.dismiss();

					ProgressDialog progressDialog;
					progressDialog = new ProgressDialog(RecvActivity.this);
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setMessage("복원 중 입니다...");
					progressDialog.setCancelable(true);
					progressDialog.show();
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

	class Country {

		String code = null;
		String name = null;
		boolean selected = false;

		public Country(String code, String name, boolean selected) {
			super();
			this.code = code;
			this.name = name;
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

		public void setName(String name) {
			this.name = name;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

	}

}
