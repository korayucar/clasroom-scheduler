package com.kondi.android.classroomscheduler;
 
import android.app.Activity;
import android.os.Bundle;

import com.kondi.android.classroomscheduler.R;

public class AssignColorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	 ColorPickerPalette pp = new ColorPickerPalette(this);
	 setContentView(R.layout.color_assigner);
	
	}

}
