package com.androidhuman.example.CameraPreview;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;


public class CameraPreview extends Activity {    
	private ProcessCore mPreview;
	public ImageView mImageview;
	public ImageView[] snapImageview = new ImageView[5];
	public DrawOnTop mDraw;

	private Button mButton;
	private FrameLayout.LayoutParams params;

	private int pxWidth;
	private int pxHeight;
	
	private Pop pop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title and full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//기본적인 화면을 구성하고있는 View 들
		mDraw = new DrawOnTop(this);
		// Create our Preview view and set it as the content of our activity.
		mPreview = new ProcessCore(this);
		mImageview = new ImageView(this);
		
		//찍혀진 프레임을 저장하여 보여주기 위한 Image View들
		snapImageview[0] = new ImageView(this);
		snapImageview[1] = new ImageView(this);
		snapImageview[2] = new ImageView(this);
		snapImageview[3] = new ImageView(this);
		snapImageview[4] = new ImageView(this);

		// 버튼을 추가합니다.
		mButton = new Button(this);
		mButton.setText("Start");
		//mButton.setGravity(Gravity.BOTTOM);
		mButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "반복적 이진화를 통한 임계값 추적을 시작합니다.", Toast.LENGTH_SHORT).show();
				mPreview.SetState(true);
			}
		});
		
		// http://developer.android.com/reference/android/widget/RelativeLayout.html
		// http://www.verious.com/qa/programmatically-set-image-button-layout-gravity/
		params = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		//아래 내용의 주석을 해제하면 버튼이 아래로 내려갑니다.
		//params.gravity = Gravity.BOTTOM;
		//params.gravity = Gravity.;
		
		//params.topMargin = displayMetrics.heightPixels - 100;
		//params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);


		//mImageview를 가운데로 위치시키고 90도 회전하기 위하여 사용한 matrix 입니다.
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		pxWidth  = displayMetrics.widthPixels/2;
		pxHeight = displayMetrics.heightPixels/2;
		Matrix m = new Matrix();
		m.setRotate(90);
		m.postTranslate((pxWidth-100), (pxHeight-100));
		
		mImageview.setScaleType(ScaleType.MATRIX);
		mImageview.setImageMatrix(m);
		
		
		//프레임을 보여주기 위한 Imageview도  90도 회전 및 위치설정을 합니다.
		Matrix snap_m = new Matrix();
		snap_m.setRotate(90);
		snap_m.postTranslate(displayMetrics.widthPixels, 0);
		//m.postTranslate((displayMetrics.widthPixels-200), (0));
		snapImageview[0].setScaleType(ScaleType.MATRIX);
		snapImageview[0].setImageMatrix(snap_m);
		
		snap_m.postTranslate(0, (202));
		snapImageview[1].setScaleType(ScaleType.MATRIX);
		snapImageview[1].setImageMatrix(snap_m);
		
		snap_m.postTranslate(0, (202));
		snapImageview[2].setScaleType(ScaleType.MATRIX);
		snapImageview[2].setImageMatrix(snap_m);
		
		snap_m.postTranslate(0, (202));
		snapImageview[3].setScaleType(ScaleType.MATRIX);
		snapImageview[3].setImageMatrix(snap_m);
		
		snap_m.postTranslate(0, (202));
		snapImageview[4].setScaleType(ScaleType.MATRIX);
		snapImageview[4].setImageMatrix(snap_m);
		
		
		//지금까지 정의했던 모든 View들을 화면에 추가합니다.
		setContentView(mPreview);
		addContentView(mDraw,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		
		addContentView(mImageview,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(mButton,params);
		
		addContentView(snapImageview[0],new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(snapImageview[1],new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(snapImageview[2],new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(snapImageview[3],new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(snapImageview[4],new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	}

	//뒤로가기 버튼을 눌렀을떄 바로 꺼지는것을 막기위한 소스였지만 지금은 사용하지 않습니다.
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

	//메뉴 내용
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0,0,"사용 설명서");
		menu.add(0,1,0,"About");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			pop = new Pop(mDraw);  
			pop.show();
			break;
		case 1:
			Toast.makeText(getApplicationContext(), "hoomcar@naver.com", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}