package com.androidhuman.example.CameraPreview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

public class PopView {
	protected final View anchor;
	protected final PopupWindow window;
	private View root;
	private Drawable background = null;
	
	protected final WindowManager windowManager;
	public PopView(View anchor) {
		this.anchor = anchor;
		this.window = new PopupWindow(anchor.getContext());
		
		window.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) { // popupview영역 외의 바깥부분을 터치할 시
					PopView.this.window.dismiss();
					return true;
				}
				return false;
			}

		});  
		windowManager = (WindowManager) anchor.getContext().getSystemService(Context.WINDOW_SERVICE);
		onCreate();
	}
	protected void onCreate() {}
	protected void onShow() {}
	protected void preShow() {
		if (root == null) {
			throw new IllegalStateException("IllegalStateException preShow.");
		}  
		onShow();
		if (background == null) {
			window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			window.setBackgroundDrawable(background);
		}
		window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		window.setTouchable(true);
		window.setFocusable(true);
		window.setOutsideTouchable(true);
		window.setContentView(root);
	}
	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
	}
	public void setContentView(View root) {
		this.root = root;
		window.setContentView(root);
	}
	public void setContentView(int layoutResID) {
		LayoutInflater inflator = (LayoutInflater) anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setContentView(inflator.inflate(layoutResID, null));
	}
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		window.setOnDismissListener(listener);
	}
	public void dismiss() {
		window.dismiss();  // popupview를 닫는다.
	}
}