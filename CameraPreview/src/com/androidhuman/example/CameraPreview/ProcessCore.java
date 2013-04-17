package com.androidhuman.example.CameraPreview;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ProcessCore extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	boolean mPreviewState;
	private Bitmap prBitmap;
	private CameraPreview _MActivity = null;
	protected boolean toggle=false;


	/*static {
		System.loadLibrary("histogram");
	}

	private native void HISTOGRAMCOMPRESS(Bitmap _outBitmap, byte[] _in);*/
	
	/*Preview(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }*/
	
	ProcessCore(CameraPreview aaa) {
		super(aaa);

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(768, 1024);
		//Log.i("mydata", "width:"+w+"/height:"+h);
		mCamera.setParameters(parameters);
				
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera _camera) {
					// TODO Auto-generated method stub
					Camera.Parameters params = _camera.getParameters();
					int w = params.getPreviewSize().width;
					int h = params.getPreviewSize().height;
					Log.i("mydata", "width:"+w+"/height:"+h);

					prBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);

					//_MActivity.mImageview.setImageBitmap(prBitmap);
				}
			});
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@SuppressLint("NewApi")
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(h, w);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		Log.i("mydata", "surCh width:"+w+"/height:"+h);
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
//		mCamera.autoFocus(new Camera.AutoFocusCallback() {
//			@Override
//			public void onAutoFocus(boolean success, Camera camera) {
//				// TODO Auto-generated method stub
//			}
//		});
		mCamera.startPreview();
	}



}