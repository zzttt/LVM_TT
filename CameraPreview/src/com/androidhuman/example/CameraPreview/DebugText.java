package com.androidhuman.example.CameraPreview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;


@SuppressLint("DrawAllocation")
public class DebugText extends View{
	private String data = new String("Start 버튼을 누르세요");

	//private String message = new String("Test text");
	private String message = new String("");
	private String trashhold = new String("");
	private String upper = new String("");
	private String under = new String("");


	public DebugText(Context context){
		super(context);
	}

	
	
	public void setStringData(int input){
		data = "추적값 : "+input + "개";
		//return message;
	}

	
	public void setStringMessegeInit(){
		data = "Start 버튼을 누르세요";
		trashhold = "";
	}


	public void setStringMessege(String data){
		message = data;
		//return message;
	}


	public void setStringTrashhold(int data){
		trashhold = "임계값 : "+data;
		//return message;
	}


	public void setStringUpper(int input){
		upper = "상위20%임계값  : "+input;
		//return message;
	}
	public void setStringUnder(int input){
		under = "하위20%임계값 : "+input;
		//return message;
	}


	@Override
	protected void onDraw(Canvas canvas){
		Paint paint = new Paint();
		
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.GREEN);
		paint.setTextSize(35);
		canvas.drawText(data, 20, 150, paint);
		canvas.drawText(trashhold, 20, 190, paint);
		paint.setColor(Color.RED);
		canvas.drawText(upper, 20, 340, paint);
		canvas.drawText(under, 20, 380, paint);
		canvas.drawText(message, 220, 660, paint);

		super.onDraw(canvas);
	}
}
