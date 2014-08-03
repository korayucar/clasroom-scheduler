package com.kondi.android.classroomscheduler.attendance;

import java.io.IOException;
import java.util.Map.Entry;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events.List;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class CalendarLoadAttendees extends AsyncTask<Void, Void, Boolean> {

	AttendanceActivity activity;
	 final com.google.api.services.calendar.Calendar client;
	 final String starttime;
	 final String calendarid;
	 Event event;
	 
	 CalendarLoadAttendees(AttendanceActivity activity ,
			 com.google.api.services.calendar.Calendar client,
			 String start , String cal) {
		 this.activity = activity;
		 this.client = client;
		 starttime = start;
		 calendarid = cal;
	}
  
 
	protected void doInBackground() throws IOException {
		String s = calendarid;
	List l = 	client.events().list( s);//.get("kjh", "kh").execute().getAttendees().get(0).setOptional(true);	
	 l.setOrderBy("startTime");
	 l.setSingleEvents(true);
	l.setTimeMin(new DateTime(starttime));
	l.setMaxResults(1);
	
	Events e = l.execute(); 
	event = e.getItems().get(0);
//	for(int i = 0; i < e.getItems().size() ; i++)
//		{
//			Event t = e.getItems().get(0);
//			EventAttendee att = new EventAttendee();
//			att.setEmail("koraytest13@gmail.com");
//			t.getAttendees().add( att); 
//			client.events().update(s, t.getId(), t).execute();
//		}
//	l = 	client.events().list( s);//.get("kjh", "kh").execute().getAttendees().get(0).setOptional(true);	
//	  e = l.execute(); 
	for(Entry z : e.entrySet() )
	{
		Log.v("events", z.getKey() + "  " + z.getValue());
	}
	}
	
	 static void run(CalendarSampleActivity calendarSample) {
		    new AsyncLoadCalendars(calendarSample).execute();
	 }

	 @Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		 
		super.onPostExecute(result);
		if(result)
		{
			activity.onEventLoaded(event);
		}
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		  try {
		      doInBackground();
		      return true;
		    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
		      activity.showGooglePlayServicesAvailabilityErrorDialog(
		          availabilityException.getConnectionStatusCode());
		    } catch (UserRecoverableAuthIOException userRecoverableException) {
		      activity.startActivityForResult(
		          userRecoverableException.getIntent(), CalendarSampleActivity.REQUEST_AUTHORIZATION);
		    } catch (IOException e) {
		      Utils.logAndShow(activity, CalendarSampleActivity.TAG, e);
		    }
		    return false;
	}

}
