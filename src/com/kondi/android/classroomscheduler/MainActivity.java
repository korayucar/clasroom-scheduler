package com.kondi.android.classroomscheduler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kondi.android.classroomscheduler.R;
import com.kondi.android.classroomscheduler.calendar.AllInOneActivity;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener{

    private int CONTENT_VIEW_ID = 300;
 
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
       Button color = (Button) findViewById(R.id.attach_colors);
       Button takvim = (Button) findViewById(R.id.takvim);
       color.setOnClickListener(this);
       takvim.setOnClickListener(this);
       
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.takvim)
		{
			Intent myIntent = new Intent(this, AllInOneActivity.class);
			startActivity(myIntent);
		}
		else if(v.getId() == R.id.attach_colors)
		{
			Intent myIntent = new Intent(this, AssignColorActivity.class);
			startActivity(myIntent);
		}
	}

 

    
    
}
