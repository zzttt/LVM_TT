package com.androidhuman.example.CameraPreview;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;




@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		setContentView(R.layout.splash);


		//CameraPreview를 호출해 오기위한 Intent
		final Intent indent = new Intent(this,CameraPreview.class);


		//핸들러 등록
		Handler handler = new Handler () {
			@Override
			public void handleMessage(Message msg) {
				finish();
				startActivity(indent);
			}
		};


		//0.5초후 MainActivity를 종료시키고 CameraPreview Activity를 호출해온다.
		handler.sendEmptyMessageDelayed(0, 500);
	}
}

