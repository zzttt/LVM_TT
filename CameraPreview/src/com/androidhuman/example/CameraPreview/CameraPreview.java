package com.androidhuman.example.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.io.IOException;


public class CameraPreview extends Activity {    
	private ProcessCore mPreview;
	public ImageView mImageview;
	public DrawOnTop mDraw;
	
	private int pxWidth;
	private int pxHeight;

	/*private final long	FINSH_INTERVAL_TIME    = 2000;
	private long		backPressedTime        = 0;*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title and full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		
		mDraw = new DrawOnTop(this);
		// Create our Preview view and set it as the content of our activity.
		mPreview = new ProcessCore(this);
		mImageview = new ImageView(this);

		

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		pxWidth  = displayMetrics.widthPixels/2;
		pxHeight = displayMetrics.heightPixels/2;
		
				
		//mImageview.setPadding(150, 150, 150, 150);

		Matrix m = new Matrix();
		m.setRotate(90);
		m.postTranslate((pxWidth-100), (pxHeight-100));
		
		
		//m.setRotate(90);
		
		mImageview.setScaleType(ScaleType.MATRIX);
		mImageview.setImageMatrix(m);

		setContentView(mPreview);
		addContentView(mImageview,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(mDraw,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
	}

	@Override 
	public void onBackPressed() {
		/*long tempTime        = System.currentTimeMillis();
		long intervalTime    = tempTime - backPressedTime;
		if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
			super.onBackPressed(); 
		} 
		else { 
			backPressedTime = tempTime; 
			Toast.makeText(getApplicationContext(),"'뒤로'버튼을한번더누르시면종료됩니다.",Toast.LENGTH_SHORT).show(); 
		}*/

		finish();
	} 
}