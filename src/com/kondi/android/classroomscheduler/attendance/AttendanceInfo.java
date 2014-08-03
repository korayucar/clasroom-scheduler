package com.kondi.android.classroomscheduler.attendance;

import com.google.api.client.util.Objects;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

public class AttendanceInfo implements Comparable<CalendarInfo>  {

	  static final String FIELDS = "id,summary";
	  static final String FEED_FIELDS = "items(" + FIELDS + ")";

	  String id;
	  String summary;

	  AttendanceInfo(String mail, String name) {
	    this.id = mail;
	    this.summary = name;
	  }

	 

	  @Override
	  public String toString() {
	    return Objects.toStringHelper(CalendarInfo.class).add("id", id).add("summary", summary)
	        .toString();
	  }

	  public int compareTo(CalendarInfo other) {
	    return id.compareTo(other.id);
	  }

	 
	}
