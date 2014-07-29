package com.example.timetraveler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.FrameWork.ConnServer;

import net.kkangsworld.lvmexec.pipeWithLVM;
import net.kkangsworld.lvmexec.readHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SrvBackupActivity extends Activity {

	private pipeWithLVM pl;
	private msgHandler rh;
	private TextView tv;
	private ListView lv;
	private String userCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_srv_backup);

		lv = (ListView) this.findViewById(R.id.lv_sList);

		tv = (TextView)findViewById(R.id.sInfo);
		
		Intent intent = getIntent();
		this.userCode = intent.getStringExtra("userCode");
		
		ProgressDialog pd = new ProgressDialog(this);
		pd.setTitle("waiting for handling..");
		pd.show();
		
		rh = new msgHandler(this , pd);
		
		//rh.sendEmptyMessage(0);
		
		// LVM pipe 에 핸들러와 함께 전송
		pl = new pipeWithLVM(rh);

		pl.ActionWritePipe("lvs --separator , ");
		
	}

	public void setText(Context context) {
		String res = rh.readResult();
		Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.srv_backup, menu);
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

	// Display Snapshot list in device

	public File[] getSnapshotList() {

		File sHome = new File(MainActivity.homePath);
		File[] sList = sHome.listFiles();

		return sList;
	}

	/**
	 * back button click
	 */
	@Override
	public void onBackPressed() {
		finish();
	}

	public void finishActivity() {

		// Activity finish
		this.finish();

	}

	
	// inner class
	class msgHandler extends Handler {
		private String readResult;
		private ArrayList<String> ssStrList;
		private AlertDialog aDialog;
		private Context context;
		private ProgressDialog pd;
		
		public msgHandler() {

		}

		public msgHandler(Context context, ProgressDialog pd) {
			this.context = context;
			this.pd = pd;
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0: // case 0
				Log.i("handler", "msg 0");
				// Toast.makeText(getApplicationContext(), (String)msg.obj,
				// Toast.LENGTH_LONG).show();
				readResult = null;
				this.readResult = (String) msg.obj;
				
				//Log.d("inAction", "[" + getClass() + "]" + readResult);
				
				Log.d("handler", readResult);
				
				//tv.setText(readResult);
				// this.sendEmptyMessage(100); //set ListView as data
				
				// ------------------------------ set lists -------------------------------------
				ssStrList = new ArrayList<String>();
				readResult = readResult.replace("Convert", ",");
				
				String[] strArr = readResult.split(",");
				
				//Log.i("handler", readResult);
				
				for(int i = 0 ; i < strArr.length ; i++){
					if(strArr[i].equals("vg")){
						ssStrList.add(strArr[i-1]); // 임시등록
						
						/*if(strArr[i+1].startsWith("s")) // 스냅샷만 출력해 주는 부분
							ssStrList.add(strArr[i-1]);*/
					}
				}
				//Toast.makeText(context, readResult, Toast.LENGTH_SHORT).show();
				
				//dd if="filePath" obs="bytes"
				
				// ssList를 이용하여 View Listing
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, ssStrList);
				
				Log.i("lvm", "set adapt");
				lv.setAdapter(adapter); // set Adapter
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View vv,
							int itemId, long id) {
						// TODO Auto-generated method stub

						// itemId ( 0 부터 등록된 순서대로 읽어들임 )

						final String ssName = ssStrList.get(itemId); // ssName read

						final AlertDialog.Builder adb = new AlertDialog.Builder(vv
								.getContext());
						adb.setTitle("Notice");
						adb.setMessage("서버에 스냅샷을 전송합니다.");
						aDialog = adb.create();

						adb.setPositiveButton("전송시작", new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								
								ProgressDialog pd = new ProgressDialog(
										context);
								pd.setTitle("전송중 .. ");
								pd.setMessage("Snapshot 을 서버로 전송 중 입니다..");
								pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								pd.setCancelable(true);
								pd.show();
								
								// 서버에 전송이 시작된다는 것을 알려야지	
								ConnServer conn = new ConnServer(MainActivity.srvIp, 12345, 6, userCode , ssName , pd);
								conn.start();		
								

								/*SnapshotImageMaker sim = new SnapshotImageMaker("ssName");
								sim.start();*/
								
								
								// Snapshot Imaging
								// Snapshot Send to Server
								

								// confirm

								// activity end

								// finishActivity();
							}
						});
						adb.setNegativeButton("취소", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								setDismiss(aDialog);
							}

						});

						adb.show();

					}

					
					private void setDismiss(Dialog dialog) {
						if (dialog != null && dialog.isShowing())
							dialog.dismiss();
					}

				});
				
				
				
				
				pd.cancel();
				
				break;
			case 100: 
				// lvs 에 따른 snapshot list 를 읽어 listView에 보임

				ssStrList = new ArrayList<String>();
				readResult = readResult.replace("Convert", ",");
				
				String[] strArr1 = readResult.split(",");
				
				Log.i("handler", readResult);
				
				for(int i = 0 ; i < strArr1.length ; i++){
					if(strArr1[i].equals("vg")){
						ssStrList.add(strArr1[i-1]); // 임시등록
						
						/*if(strArr[i+1].startsWith("s")) // 스냅샷만 출력해 주는 부분
							ssStrList.add(strArr[i-1]);*/
					}
				}
				
				//Toast.makeText(context, readResult, Toast.LENGTH_SHORT).show();
				
				//dd if="filePath" obs="bytes"
				
				// ssList를 이용하여 View Listing
				
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, ssStrList);
				
				Log.i("lvm", "set adapt");
				lv.setAdapter(adapter1); // set Adapter
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View vv,
							int itemId, long id) {
						// TODO Auto-generated method stub

						// itemId ( 0 부터 등록된 순서대로 읽어들임 )

						final String ssName = ssStrList.get(itemId); // ssName read

						final AlertDialog.Builder adb = new AlertDialog.Builder(vv
								.getContext());
						adb.setTitle("Notice");
						adb.setMessage("서버에 스냅샷을 전송합니다.");
						aDialog = adb.create();

						adb.setPositiveButton("전송시작", new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								
								ProgressDialog pd = new ProgressDialog(
										context);
								pd.setTitle("전송중 .. ");
								pd.setMessage("Snapshot 을 서버로 전송 중 입니다..");
								pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								pd.setCancelable(true);
								pd.show();
								
								// 서버에 전송이 시작된다는 것을 알려야지	
								ConnServer conn = new ConnServer(MainActivity.srvIp, 12345, 6, userCode , ssName , pd);
								conn.start();		
								
								
								/*SnapshotImageMaker sim = new SnapshotImageMaker("ssName");
								sim.start();*/
								
								
								// Snapshot Imaging
								// Snapshot Send to Server
								

								// confirm

								// activity end

								// finishActivity();
							}
						});
						adb.setNegativeButton("취소", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								setDismiss(aDialog);
							}

						});

						adb.show();

					}

					
					private void setDismiss(Dialog dialog) {
						if (dialog != null && dialog.isShowing())
							dialog.dismiss();
					}

				});
				

				Log.i("lvm", "end adapt");
				
				break;
			}

		}

		public String readResult() {
			return this.readResult;
		}

	}

}
