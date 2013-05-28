package com.androidhuman.example.CameraPreview;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class ProcessCore extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	boolean mPreviewState;
	private Bitmap prBitmap;
	private CameraPreview _MActivity = null;
	private int ThreshHold = 127;
	private int ThreshHoldData = 127;

	private int data = 0;
	private boolean flag = true;
	private int[] drop_data = new int[2];
	protected boolean toggle=false;

	private boolean flag_start=false;
	private boolean flag_threshold=true;
	private boolean flag_snap=false;
	private boolean flag_snap_delay=true;
	private boolean flag_focus=false;
	private boolean flag_count=false;
	private char snap_delay_filter=0;

	private long start_time=0;
	private long end_time=0;
	private float result_time=0;


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
	public void SetState(boolean state){
		flag_start = state;
		flag_threshold = state;
		flag = state;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				public void onPreviewFrame(byte[] _data, Camera _camera) {
					// TODO Auto-generated method stub
					Camera.Parameters params = _camera.getParameters();
					//int w = params.getPreviewSize().width;
					//int h = params.getPreviewSize().height;
					//Log.i("mydata", "width:"+w+"/height:"+h);

					/*if(flag_start){
						params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					}
					else{
						params.setFlashMode(Parameters.FLASH_MODE_OFF);
					}
					_camera.setParameters(params);
					*/

					//prBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
					prBitmap = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);

					//if(flag_threshold && flag_start){
					if(flag_start){
						mCamera.autoFocus (new Camera.AutoFocusCallback() {
							public void onAutoFocus(boolean success, Camera camera) {
								if(success){
									// do something
									flag_focus=true;
									flag_start=false;
								}
							}
						});
					}
					
					if(flag_focus){
						data = 0;
						//flag_threshold = false;
						ThreshHoldData = Gonzalez(prBitmap, _data);
						_MActivity.mDraw.setStringTrashhold(ThreshHoldData);
						Toast.makeText(_MActivity, "임계값 "+ThreshHoldData+"을 구하였습니다.", Toast.LENGTH_SHORT).show();
						flag_focus=false;
						flag_count=true;
					}
					//NativeProc(prBitmap, _data,ThreshHold);

					//data += NativeProc(prBitmap, _data,ThreshHoldData);
					
					drop_data[1] = drop_data[0];
					drop_data[0] = NativeProc(prBitmap, _data,ThreshHoldData);
					Log.i("mydata", ""+drop_data[0]);
					
					if(flag_snap_delay){
						snap_delay_filter++;
					}
					if(snap_delay_filter>3){ //몇프레임 건너뛸지
						flag_snap=true;
						flag_snap_delay=false;
						snap_delay_filter=0;
					}


					_MActivity.mImageview.setImageBitmap(prBitmap);
					if(flag_count){
						if(flag){
							Toast.makeText(_MActivity, "추적을 시작합니다.", Toast.LENGTH_SHORT).show();
							start_time = System.currentTimeMillis();
							flag=false;
						}
						//						drop_data[1] = drop_data[0];
						//						drop_data[0] = NativeProc(prBitmap, _data,ThreshHoldData);

						if(Math.abs(drop_data[0] - drop_data[1]) > 300){
							if(flag_snap)
							{
								//if(data<5)	{
								_MActivity.snapImageview[data].setImageBitmap(prBitmap);
								//}
								data++;
								flag_snap=false;
								flag_snap_delay=true;
							}
						}

						_MActivity.mDraw.setStringData(data);
						//_MActivity.mDraw.setStringTrashhold(ThreshHoldData);
						//_MActivity.mDraw.invalidate();
						//_MActivity.mImageview.setImageBitmap(prBitmap);	
						//_MActivity.mImageview.invalidate();

						if(data>4){
							end_time =  System.currentTimeMillis();
							result_time = (float) ((end_time - start_time)/1000.0);
							flag_count = false;
							_MActivity.mDraw.setStringMessegeInit();
							Toast.makeText(_MActivity, "걸린시간 : "+result_time+"초", Toast.LENGTH_SHORT).show();
						}

						/*if((flag)&&(data>30)){
							Toast.makeText(_MActivity, ""+data, Toast.LENGTH_SHORT).show();
							flag=false;
						}*/
					}
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
		//parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		//parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
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