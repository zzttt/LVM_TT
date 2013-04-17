package com.androidhuman.example.CameraPreview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;


public class CameraPreview extends Activity {    
	private ProcessCore mPreview;
	//public ImageView mImageview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DrawOnTop mDraw = new DrawOnTop(this);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new ProcessCore(this);
		//mImageview = new ImageView(this);
		setContentView(mPreview);
		//setContentView(mImageview);
		addContentView(mDraw,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
	}
}