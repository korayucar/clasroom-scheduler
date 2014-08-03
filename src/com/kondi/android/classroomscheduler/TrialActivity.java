package com.kondi.android.classroomscheduler;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.appspot.fancyscheduler.attendancepoint.Attendancepoint;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.kondi.android.classroomscheduler.calendar.CalendarController;
import com.kondi.android.classroomscheduler.calendar.DayView;
import com.kondi.android.classroomscheduler.calendar.EventLoader;

public class TrialActivity extends Activity{
	
	protected static final String TAG = "TrialActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Attendancepoint.Builder builder = new Attendancepoint.Builder(
				  AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
		final Attendancepoint service = builder.build();
		 
		
		 AsyncTask<Integer, Void, Attendancepoint> getAndDisplayGreeting =
		            new AsyncTask<Integer, Void, Attendancepoint> () {
		                @Override
	                protected Attendancepoint doInBackground(Integer... integers) {
		                    // Retrieve service handle.
		                 
		                	 try {
		                	      service.addperson("sadasd").
		                	      execute();
		                	       Log.v(TAG, "no exception");
		                	    } catch (Exception e) {
	                	      Log.d("TicTacToe", e.getMessage(), e);
                	    }
                    return null;
	                }

	                @Override
	                protected void onPostExecute(Attendancepoint greeting) {
	                    
	                }
		            };
			getAndDisplayGreeting.execute(54);
		EventLoader mEventLoader = new EventLoader(this);
		DayView view = new HorizontalDayView(this, CalendarController
                .getInstance(this), null, mEventLoader, 1);
		 setContentView(view);
	}


}
