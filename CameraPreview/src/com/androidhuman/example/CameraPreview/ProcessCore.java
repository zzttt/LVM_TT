package com.androidhuman.example.CameraPreview;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView.ScaleType;

public class ProcessCore extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	boolean mPreviewState;
	private Bitmap prBitmap;
	private CameraPreview _MActivity = null;
	private int ThreshHold = 127;
	private int ThreshHoldData = 0;
	
	private int data = 0;
	protected boolean toggle=false;


	static {
		System.loadLibrary("myproc");
	}

	private native int NativeProc(Bitmap _outBitmap, byte[] _in, int _ThreshHold);
	private native int Gonzalez(Bitmap _outBitmap, byte[] _in);
	//at btn/classes/$ javah -classpath ~/android/adt-bundle-linux-x86-20130219/sdk/platforms/android-16/android.jar: com.androidhuman.example.CameraPreview.ProcessCore
	
	/*Preview(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }*/
	
	ProcessCore(CameraPreview aaa) {
		super(aaa);
		_MActivity = aaa;
		mHolder = getHolder();
		mHolder.addCallback(this);
		//mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				public void onPreviewFrame(byte[] _data, Camera _camera) {
					// TODO Auto-generated method stub
					Camera.Parameters params = _camera.getParameters();
					int w = params.getPreviewSize().width;
					int h = params.getPreviewSize().height;
					Log.i("mydata", "width:"+w+"/height:"+h);

					
					//prBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
					prBitmap = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);
	
					ThreshHoldData = NativeProc(prBitmap, _data,ThreshHold);
					
					/*if(ThreshHoldData>127){
						data++;
					}*/
					_MActivity.mDraw.setStringTrashhold(ThreshHoldData);
					_MActivity.mDraw.setStringData(data);
					//_MActivity.mDraw.invalidate();
					_MActivity.mImageview.setImageBitmap(prBitmap);	
					//_MActivity.mImageview.invalidate();
				}
			});
		} catch (IOException exception) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}

	//@SuppressLint("NewApi")
	@SuppressLint("InlinedApi")
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(h, w);
		parameters.setRotation(90);
		//parameters.setPreviewFpsRange(28000, 35000);
		//parameters.setFocusMode(Camera.Parameters.FLASH_MODE_TORCH);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		Log.i("mymode", "surCh width:"+w+"/height:"+h);
		
		mCamera.setParameters(parameters);
		mCamera.setDisplayOrientation(90);
		/*mCamera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				// TODO Auto-generated method stub
			}
		});*/
		mCamera.startPreview();
		Log.i("mymode", "startPreview()");
	}
}