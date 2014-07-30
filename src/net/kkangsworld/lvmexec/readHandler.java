package net.kkangsworld.lvmexec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.FrameWork.ConnServer;
import com.FrameWork.SnapshotImageMaker;
import com.example.timetraveler.MainActivity;
import com.example.timetraveler.SrvBackupActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Piped LVM 을 이용한 결과를 반영하는 핸들러
 * @author Administrator
 *
 */
public class readHandler extends Handler {

	private String readResult;
	private ArrayList<String> ssStrList ;
	private AlertDialog aDialog;
	private Context context;
	private ListView lv;
	
	public readHandler(){
		
	}
	public readHandler( Context context, ListView lv) {
		this.context = context;
		this.lv = lv;
	}

	@Override
	public void handleMessage(Message msg) {
		Log.i("handler", "ResultReader Handler result get");
		switch (msg.what) {
		case 0: // case 0
			Log.i("handler", "msg 0");
			// Toast.makeText(getApplicationContext(), (String)msg.obj,
			// Toast.LENGTH_LONG).show();
			this.readResult = (String) msg.obj;
			Log.d("inAction", "[" + getClass() + "]" + readResult);

			//this.sendEmptyMessage(100); //set ListView as data
			
			
			//( msg 0 으로 들어오면 , readResult 를 저장하고 case 100 수행) 
			
			break;
		
		}

	}

	public String readResult() {
		return this.readResult;
	}
}
