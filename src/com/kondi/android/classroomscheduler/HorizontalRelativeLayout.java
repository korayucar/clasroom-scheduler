package com.kondi.android.classroomscheduler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.kondi.android.classroomscheduler.R;

public class HorizontalRelativeLayout extends  RelativeLayout implements  OnClickListener
{

	 
	private static final String TAG = "HorizontalRelativeLayout";
	public HorizontalRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 
	// setBackgroundColor(Color.RED);
	 setWillNotDraw(false);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		Log.v("gfdgf", "layout pass happening"+getRotation());
	 
			super.onLayout(changed, l, t, r, b); 
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
  
		 
		super.onSizeChanged(w, h, oldw, oldh);
//		setPivotX(getWidth()
//				/2);
//		setPivotY( getHeight()/2);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
		 
	}
	
	
	 
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		 if(getRotation()==0){
				int w =  getWidth();
				int h =  getHeight();
		
				setRotation(270.0f);
				setTranslationX((w - h) / 2);
				setTranslationY((h - w) / 2);
				ViewGroup.LayoutParams lp =  getLayoutParams();
				lp.height = w;
				lp.width = h;
				requestLayout();
			 }
	}
	
	
@Override
protected void onAttachedToWindow() {
	// TODO Auto-generated method stub
	super.onAttachedToWindow();
	 
	 
}


@Override
public void onClick(View v) {
 
}

 
}
