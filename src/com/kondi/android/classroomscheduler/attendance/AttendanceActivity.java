/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kondi.android.classroomscheduler.attendance;

import java.io.IOException;
import java.util.Collections;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;


public final class AttendanceActivity  extends Activity {

	
	public static final String START_TIME = "start_time_of_the_event";
	public static final String CALENDAR_ID= "calendar_id";
	
	  private static final int CONTEXT_EDIT = 0;

	  private static final int CONTEXT_DELETE = 1;

	  private static final int CONTEXT_BATCH_ADD = 2;

	  static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

	  static final int REQUEST_AUTHORIZATION = 1;

	  static final int REQUEST_ACCOUNT_PICKER = 2;
	  
	  private static final String PREF_ACCOUNT_NAME = "accountName";
	  
	GoogleAccountCredential credential;
	
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	  final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	  
	  ProgressBar mProgressBar ;
	  com.google.api.services.calendar.Calendar client;
	private String organizer;
	private String time;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mProgressBar = new ProgressBar(this);
		setContentView(mProgressBar);
		Intent intent = getIntent();
		  organizer  = intent.getStringExtra(CALENDAR_ID);
		  time = intent.getStringExtra(START_TIME);
		Log.v(organizer,time);
		// Google Accounts
		credential =
				GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		// Calendar client
		client = new com.google.api.services.calendar.Calendar.Builder(
				transport, jsonFactory, credential).setApplicationName("822175141607-n8r6ojmdgo96b35sd829tg532pv104vs.apps.googleusercontent.com")
				.build();
		 loadParticipants();
	}
	
	

	

	  private void loadParticipants() {
		  CalendarLoadAttendees task = new CalendarLoadAttendees(this , client ,time , organizer );
			 
		    task.execute();
	}

	  public void onEventLoaded(Event e)
	  {
		 	EventAttendee att = new EventAttendee();
			att.setEmail("koraytest13@gmail.com");
			e.getAttendees().add( att); 
		 
		 new UpdateAttendenceTask(this, client, e, organizer).execute();
		  
	  }




	void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
	    runOnUiThread(new Runnable() {
	      public void run() {
	        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
	            connectionStatusCode, AttendanceActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
	        dialog.show();
	      }
	    });
	  }
	
	
	  private void chooseAccount() {
		    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
		  }
	  
	  private void haveGooglePlayServices() {
		    // check if there is already an account selected
		    if (credential.getSelectedAccountName() == null) {
		      // ask user to choose account
		      chooseAccount();
		    } 
		  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    if (checkGooglePlayServicesAvailable()) {
	      haveGooglePlayServices();
	    }
	  }

	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
	      case REQUEST_GOOGLE_PLAY_SERVICES:
	        if (resultCode == Activity.RESULT_OK) {
	          haveGooglePlayServices();
	        } else {
	          checkGooglePlayServicesAvailable();
	        }
	        break;
	      case REQUEST_AUTHORIZATION:
	        if (resultCode == Activity.RESULT_OK) {
	        	loadParticipants();
	        } else {
	          chooseAccount();
	        }
	        break;
	      case REQUEST_ACCOUNT_PICKER:
	        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
	          String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
	          if (accountName != null) {
	            credential.setSelectedAccountName(accountName);
	            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
	            SharedPreferences.Editor editor = settings.edit();
	            editor.putString(PREF_ACCOUNT_NAME, accountName);
	            editor.commit();
	            loadParticipants();
	          }
	        }
	        break;
	      
	    }
	  }

	  /** Check that Google Play services APK is installed and up to date. */
	  private boolean checkGooglePlayServicesAvailable() {
	    final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
	      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
	      return false;
	    }
	    return true;
	  }
	  
	  
	  private class UpdateAttendenceTask extends CalendarLoadAttendees {

		  Event ev ;
		UpdateAttendenceTask(AttendanceActivity activity, Calendar client,
				Event e, String cal) {
			super(activity, client, null, cal);
			ev = e;
		}
		
		@Override
		protected void doInBackground() throws IOException {
		
			client.events().update(organizer, ev.getId(), ev).execute();
		}
		  
		 @Override
			protected void onPostExecute(Boolean result) {
				// TODO Auto-generated method stub
		 }
	
	  }
		 

}

