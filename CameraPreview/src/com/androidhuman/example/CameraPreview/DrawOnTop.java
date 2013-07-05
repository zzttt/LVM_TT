package com.androidhuman.example.CameraPreview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;


@SuppressLint("DrawAllocation")
public class DrawOnTop extends View{
	private boolean circle=false; 
	
	public DrawOnTop(Context context){
		super(context);
	}

	public void SetCircle(boolean a){
		circle=a;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		Paint paint = new Paint();
		Rect focus_rect = new Rect();
		Rect aid_rect = new Rect();
		Rect scale_rect = new Rect();
		Rect text_rect = new Rect();


		focus_rect.set((canvas.getWidth()/2)-100, (canvas.getHeight()/2)-25, (canvas.getWidth()/2)+100, (canvas.getHeight()/2)+25);
		aid_rect.set((canvas.getWidth()/2)-40, (canvas.getHeight()/2)-80, (canvas.getWidth()/2)+40, (canvas.getHeight()/2)+80);
		scale_rect.set((canvas.getWidth()/2)-100, (canvas.getHeight()/2)-100, (canvas.getWidth()/2)+100, (canvas.getHeight()/2)+100);
		//text_rect.set(0,0,250,120);canvas.getWidth()
		text_rect.set(0,0,canvas.getWidth(),100);

		//검은색 텍스트 박스 그리기
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(0xCF0C0C0C);
		canvas.drawRect(text_rect, paint);		

		//canvas.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight(), paint);
		canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);


		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.RED);
		canvas.drawRect(focus_rect, paint);


		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.MAGENTA);
		canvas.drawRect(aid_rect, paint);


		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.CYAN);
		canvas.drawRect(scale_rect, paint);

		if(circle){
			paint.setColor(Color.GREEN);
			paint.setStrokeWidth(10.0f);
			canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 150.0f, paint);
		}
		else{
			
		}


		/*paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.RED);
		Path path = new Path();
		path.moveTo(50, 50);    
		path.lineTo(100, 0);
		path.lineTo(150, 50);        
		path.close();
		path.offset(110, 150);
		canvas.drawPath(path, paint);*/




		super.onDraw(canvas);
		//invalidate();
	}


}

