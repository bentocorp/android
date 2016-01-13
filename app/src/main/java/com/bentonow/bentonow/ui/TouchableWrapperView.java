package com.bentonow.bentonow.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.bentonow.bentonow.listener.OnCustomDragListener;


public class TouchableWrapperView extends FrameLayout{
	
	private OnCustomDragListener mOnDragListener;

	public TouchableWrapperView(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mOnDragListener != null) 
			mOnDragListener.onDrag(ev);

		return super.dispatchTouchEvent(ev);
	}


	public void setOnDragListener(OnCustomDragListener mOnDragListener) {
		this.mOnDragListener = mOnDragListener;
	}
}
