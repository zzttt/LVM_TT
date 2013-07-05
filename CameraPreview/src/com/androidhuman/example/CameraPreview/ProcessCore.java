package com.androidhuman.example.CameraPreview;


import java.io.IOException;
import java.util.List;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
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
	private int Upper = 127;
	private int Under = 127;
	private int resolution = 0;
	private int diff = 0;
	private int post_diff = 0;


	private int data = 0;
	private boolean flag = true;
	private int[] drop_data = new int[100];
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




	//TODO : native 함수 등록
	static {
		System.loadLibrary("myproc");
	}
	/*
	 * NativeProc : 이진화 된 값의 개수를 구하는 함수
	 * Gonzalez : 반복적 이진화를 통한 임계값을 구하는 함수
	 * Upper : 상위 n%의 임계값을 구하는 함수
	 * Under : 하위 n%의 임계값을 구하는 함수
	 * 
	 * 즉, Gnzalez, Upper, Under등을 통해 임계값을 구하게 되고 구해진 임계값을
	 * NativeProc에 넣어서 이진화 되는 양을 측정하게 된다.
	 */
	private native int NativeProc(Bitmap _outBitmap, byte[] _in, int _ThreshHold, int resolution);
	private native int Gonzalez(Bitmap _outBitmap, byte[] _in, int resolution);
	private native int Upper(Bitmap _outBitmap, byte[] _in, int resolution);
	private native int Under(Bitmap _outBitmap, byte[] _in, int resolution);
	//at bin/classes/$ javah -classpath ~/android/adt-bundle-linux-x86-20130219/sdk/platforms/android-16/android.jar: com.androidhuman.example.CameraPreview.ProcessCore


	ProcessCore(CameraPreview aaa) {
		super(aaa);
		_MActivity = aaa;
		mHolder = getHolder();
		mHolder.addCallback(this);
		//푸쉬버퍼는 아이스크림샌드위치(ICS)이후로 사용되지 않습니다.
		//mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	}


	//각종 flag의 상태를 정의하기위한 method
	public void SetState(boolean state){
		flag_start = state;
		flag_threshold = state;
		flag = state;
	}


	//surfaceCreated - 서피스뷰가 생성되었을때 실행되는 메소드
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				//서피스뷰 콜백함수 등록
				public void onPreviewFrame(byte[] _data, Camera _camera) {
					// TODO Auto-generated method stub
					Camera.Parameters params = _camera.getParameters();
					int w = params.getPreviewSize().width;
					int h = params.getPreviewSize().height;
					//Log.i("mydata", "width:"+w+"/height:"+h);


					//아래 주석을 해제시키면 시작시에 플래시가 작동됩니다.
					/*if(flag_start){
						params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					}
					else{
						params.setFlashMode(Parameters.FLASH_MODE_OFF);
					}
					_camera.setParameters(params);
					 */




					//prBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
					//생성되는 이진화된 네모박스의 크기를 정의하고 있습니다.
					prBitmap = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);




					// 처리 시작을 위한 flag. 처리가 시작되면 포커싱을 맞추게 됩니다.
					//if(flag_threshold && flag_start){
					if(flag_start){
						mCamera.autoFocus (new Camera.AutoFocusCallback() {
							public void onAutoFocus(boolean success, Camera camera) {
								if(success){
									// do something
									flag_focus=true;
									flag_start=false;
									data=0;
								}
								else{
									flag_focus=false;
								} //여기에 flag_focue=false를 넣어야 할 것같으나 안넣어도 잘 되어서 일단 보류
							}
						});
					}


					//포커싱을 맞추는데 성공했으면 실질적인 측정에 들어갑니다.
					if(flag_focus){
						data = 0;


						//화면 해상도를 보정하기 위한 수식입니다.
						//w=1024, h=768 //// 1024 x ((768/2)-(200/2)) = 290816
						// 290816 + 412 ( 1024/2 -100 = 412)
						// 1024 * 359 ( 768/2 - 25) = 367616 + 412 = 368028
						// 1024 * 334 ( 768/2 - 50) = 342016 + 412 = 342428
						//resolution = ((w*(h>>1 -25) + (w>>1-100))<<11) + w;
						resolution = w*(h/2 -100) + (w/2-100);
						resolution = (resolution<<11)+w; 
						Log.i("my_message","Resolution : "+ (resolution>>11));
						Log.i("my_message","Width : "+ (resolution&0x7FF));
						/*int형 데이터(32비트)에 하위 11비트구간에는 가로길이를 넣습니다.
						 * 나머지 상위 21비트 구간에는 해상도를 보정하기 위한 값을 넣습니다.
						 *  
						 *  Native함수를 호출할때 데이터복사를 최소화하기 위함...이라기보다
						 *  사실은 함수 다시짜기 귀찮아서 이랬습니다...ㅠㅠ
						 */


						//TODO : Gonzalez 를 통해 임계값 추출
						//flag_threshold = false;
						ThreshHoldData = Gonzalez(prBitmap, _data,resolution);
						_MActivity.mDebugText.setStringTrashhold(ThreshHoldData);
						//Toast.makeText(_MActivity, "임계값 "+ThreshHoldData+"을 구하였습니다.", Toast.LENGTH_SHORT).show();
						_MActivity.mDebugText.setStringMessege("임계값 "+ThreshHoldData+"을 구하였습니다.");
						flag_focus=false;
						flag_count=true;
						Upper = Upper(prBitmap, _data,resolution);
						Under = Under(prBitmap, _data,resolution);
						_MActivity.mDebugText.setStringUpper(Upper);
						_MActivity.mDebugText.setStringUnder(Under);
					}




					//아래 부분 주석을 해제하되면 처리중인 영상이 항상 보이게 됩니다. 디버깅용
					//drop_data[1] = drop_data[0];
					//drop_data[0] = NativeProc(prBitmap, _data,ThreshHoldData);
					//drop_data[0] = NativeProc(prBitmap, _data, Upper);
					Log.i("mydata", ""+drop_data[0]);


					//임계값을 검출후 n개의 프레임을 건너뛰기 위한 flag 값들입니다.
					if(flag_snap_delay){
						snap_delay_filter++;
					}
					if(snap_delay_filter>6){ //몇프레임 건너뛸지
						flag_snap=true;
						flag_snap_delay=false;
						snap_delay_filter=0;
					}


					//임계값이 검출되면 시작하기휘한 flag_count입니다.
					if(flag_count){
						//처리된 이미지를 prBitmap에 뿌려준다.
						_MActivity.mImageview.setImageBitmap(prBitmap);
						//그냥 시작메세지를 한번 띄워주기 위한 flag
						if(flag){
							//Toast.makeText(_MActivity, "추적을 시작합니다.", Toast.LENGTH_SHORT).show();
							_MActivity.mDebugText.setStringMessege("추적을 시작합니다.");
							//start_time = System.currentTimeMillis();
							flag=false;
						}
						//미분을 위한 2개의 데이터 저장
						drop_data[2] = drop_data[1];
						drop_data[1] = drop_data[0];
						drop_data[0] = NativeProc(prBitmap, _data,ThreshHoldData,resolution);

						//TODO : 검출을 판단하는 부분
						// 미분값이 500이상일때 (이전 영상과 현재 영상의 이진화된 개수의 차가 500이상)
						//differentiation = Math.abs(drop_data[0] - drop_data[1]);
						//differentiation = Math.abs(drop_data[2] - drop_data[0]);
						diff = Math.abs(drop_data[2] - drop_data[1]) + Math.abs(drop_data[1] - drop_data[0]);
						post_diff = Math.abs(drop_data[1] - drop_data[0]);
						Log.i("myddata",""+diff);
						if(post_diff<2000){
							_MActivity.mDraw.SetCircle(true);
							_MActivity.mDraw.invalidate();
							if(diff > 600){
								//충분히 프레임을 건너 뛰었으면
								if(flag_snap)
								{
									//if(data<5)	{
									_MActivity.snapImageview[data].setImageBitmap(prBitmap);
									//}
									data++;
									flag_snap=false;
									flag_snap_delay=true;
									//검출된 물방울이 1개가 될 때 시간을 측정하기 시작합니다. 
									if(data == 2){
										start_time = System.currentTimeMillis();
									}
									//data++뒤로 옮겨와 호출 횟수가 적어져 좋을꺼같으나 일단 보류
									_MActivity.mDebugText.setStringData(data);
									_MActivity.beepsound.play(_MActivity.id, 1.0f, 1.0f, 0, 0, 1.0f);
									_MActivity.vibrator.vibrate(70);
									
								}
							}
						}
						else{
							_MActivity.mDraw.SetCircle(false);
							_MActivity.mDraw.invalidate();
							flag_count = false;
							_MActivity.mDebugText.setStringMessegeInit();
							_MActivity.mDebugText.setStringMessege("대상을 놓쳤습니다. 다시 시작하세요");
							Toast.makeText(_MActivity, "대상을 놓쳤습니다. 다시 시작하세요", Toast.LENGTH_SHORT).show();
						}

						//검출된 물방울의 개수가 5개가 되면 측정을 종료한다.
						if(data==5){
							end_time =  System.currentTimeMillis();
							result_time = (float) ((end_time - start_time)/1000.0);
							flag_count = false;
							_MActivity.mDebugText.setStringMessegeInit();
							//Toast.makeText(_MActivity, "걸린시간 : "+result_time+"초", Toast.LENGTH_SHORT).show();
							_MActivity.mDebugText.setStringMessege("걸린시간 : "+result_time+"초");
							_MActivity.mDraw.SetCircle(false);
							_MActivity.mDraw.invalidate();
						}


						/*if((flag)&&(data>30)){
							Toast.makeText(_MActivity, ""+data, Toast.LENGTH_SHORT).show();
							flag=false;
						}*/
					}
				}
			});
		} catch (IOException exception) {
			//이 예외처리를 안해두면 뒤로가기를 눌렀을때 카메라 release가 안되어 에러메세지가 호출된다.
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}


	// surface가 종료되었을때 실행되는 메소드
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}




	// surface가 변경되었을떄 실행되는 메소드. surfaceCreated 다음에 바로 실행된다.
	//@SuppressLint("NewApi")
	@SuppressLint("InlinedApi")
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		//카메라 파라메터를 정의
		Camera.Parameters parameters = mCamera.getParameters();


		//지원되는 카메라 해상도를 받아온다.
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();


		//0부터 n개의 해상도중 하나를 선택 할 수 있으며 일단은 1번째의 해상도를 선택
		Camera.Size previewSize = previewSizes.get(1);


		//정의된 해상도로 Preview를 띄운다.
		parameters.setPreviewSize(previewSize.height, previewSize.width);
		parameters.setRotation(90);
		//parameters.setPreviewFpsRange(28000, 35000);
		//parameters.setFocusMode(Camera.Parameters.FLASH_MODE_TORCH);
		//parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		//parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
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

