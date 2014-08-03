package com.kondi.android.classroomscheduler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.provider.CalendarContract.Attendees;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.kondi.android.classroomscheduler.calendar.CalendarController;
import com.kondi.android.classroomscheduler.calendar.DayView;
import com.kondi.android.classroomscheduler.calendar.Event;
import com.kondi.android.classroomscheduler.calendar.EventLoader;
import com.kondi.android.classroomscheduler.calendar.Utils;

public class HorizontalDayView extends DayView {

	private static final String TAG = "HorizontalDayView";

	public HorizontalDayView(Context context, CalendarController controller,
			ViewSwitcher viewSwitcher, EventLoader eventLoader, int numDays) {
		super(context, controller, viewSwitcher, eventLoader, numDays);
		// TODO Auto-generated constructor stub
	
		 //setRotation(-90);
		 
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		//super.onDraw(canvas);
		//canvas.rotate(-90, getWidth()/2, getHeight()/2);
		//canvas.scale(getWidth(), getHeight(), getWidth()/2, getHeight()/2);
		Log.v(TAG, "fuck");
		Log.v(TAG, "mviewstartx : " + mViewStartX);
		setPivotX( getWidth()/2);
		setPivotY(getHeight()/2);
		if (mRemeasure) {
			remeasure(getWidth(), getHeight());
			mRemeasure = false;
		}
		//mFirstHour = 50;//!!!!!!!!!!!!!!!
		canvas.save();

		float yTranslate = -mViewStartY + DAY_HEADER_HEIGHT + mAlldayHeight;
		// offset canvas by the current drag and header position
		canvas.translate(-mViewStartX,  yTranslate );
		// clip to everything below the allDay area
		Rect dest = mDestRect;
		dest.top = (int) (mFirstCell - yTranslate);
		dest.bottom = (int) (mViewHeight - yTranslate);
		dest.left = 0;
		dest.right = mViewWidth;
		canvas.save();
		canvas.clipRect(dest);
		// Draw the movable part of the view
		doDraw(canvas);
		// restore to having no clip
		canvas.restore();

		if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
			float xTranslate;
			if (mViewStartX > 0) {
				xTranslate = mViewWidth;
			} else {
				xTranslate = -mViewWidth;
			}
			// Move the canvas around to prep it for the next view
			// specifically, shift it by a screen and undo the
			// yTranslation which will be redone in the nextView's onDraw().
			canvas.translate(xTranslate, -yTranslate);
			DayView nextView = (DayView) mViewSwitcher.getNextView();

			// Prevent infinite recursive calls to onDraw().
			nextView.mTouchMode = TOUCH_MODE_INITIAL_STATE;
  
			nextView.draw(canvas);
			// Move it back for this view
			canvas.translate(-xTranslate, 0);
		} else {
			// If we drew another view we already translated it back
			// If we didn't draw another view we should be at the edge of the
			// screen
			canvas.translate(mViewStartX, -yTranslate);
		}

		// Draw the fixed areas (that don't scroll) directly to the canvas.
		drawAfterScroll(canvas);
		if (mComputeSelectedEvents && mUpdateToast) {
			updateEventDetails();
			mUpdateToast = false;
		}
		mComputeSelectedEvents = false;

		// Draw overscroll glow
		if (!mEdgeEffectTop.isFinished()) {
			if (DAY_HEADER_HEIGHT != 0) {
				canvas.translate(0, DAY_HEADER_HEIGHT);
			}
			if (mEdgeEffectTop.draw(canvas)) {
				invalidate();
			}
			if (DAY_HEADER_HEIGHT != 0) {
				canvas.translate(0, -DAY_HEADER_HEIGHT);
			}
		}
		if (!mEdgeEffectBottom.isFinished()) {
			canvas.rotate(180, mViewWidth/2, mViewHeight/2);
			if (mEdgeEffectBottom.draw(canvas)) {
				invalidate();
			}
		}
		
		 
		canvas.restore();
	}
	
	 public void drawEventText(StaticLayout eventLayout, Rect rect, Canvas canvas, int top,
			 int bottom, boolean center) {
		 // drawEmptyRect(canvas, rect, 0xFFFF00FF); // for debugging

		 int width = rect.right - rect.left;
		 int height = rect.bottom - rect.top;

		 // If the rectangle is too small for text, then return
		 if (eventLayout == null || width < MIN_CELL_WIDTH_FOR_TEXT) {
			 return;
		 }

		 int totalLineHeight = 0;
		 int lineCount = eventLayout.getLineCount();
		 for (int i = 0; i < lineCount; i++) {
			 int lineBottom = eventLayout.getLineBottom(i);
			 if (lineBottom <= height) {
				 totalLineHeight = lineBottom;
			 } else {
				 break;
			 }
		 }

		 if (totalLineHeight == 0 || rect.top > bottom || rect.top + totalLineHeight < top) {
			 return;
		 }

		 // Use a StaticLayout to format the string.
		 canvas.save();
		 //  canvas.translate(rect.left, rect.top + (rect.bottom - rect.top / 2));
		 int padding = center? (rect.bottom - rect.top - totalLineHeight) / 2 : 0;
		   canvas.translate(rect.left+width-padding, rect.top  + padding);
		 canvas.rotate(90);
		
		 rect.left = 0;
		 rect.right = height;
		 rect.top = 0;
		 rect.bottom = totalLineHeight;

		 // There's a bug somewhere. If this rect is outside of a previous
		 // cliprect, this becomes a no-op. What happens is that the text draw
		 // past the event rect. The current fix is to not draw the staticLayout
		 // at all if it is completely out of bound.
		 canvas.clipRect(rect);
		 eventLayout.draw(canvas);
		 canvas.restore();
	 }
	 
	 /**
		 * Return the layout for a numbered event. Create it if not already existing
		 */
		 public StaticLayout getEventLayout(StaticLayout[] layouts, int i, Event event, Paint paint,
				 Rect r) {
			 if (i < 0 || i >= layouts.length) {
				 return null;
			 }

			 StaticLayout layout = layouts[i];
			 // Check if we have already initialized the StaticLayout and that
			 // the width hasn't changed (due to vertical resizing which causes
					 // re-layout of events at min height)
			 if (layout == null || r.height() != layout.getWidth()) {
				 SpannableStringBuilder bob = new SpannableStringBuilder();
				 if (event.title != null) {
					 // MAX - 1 since we add a space
					 bob.append(drawTextSanitizer(event.title.toString(), MAX_EVENT_TEXT_LEN - 1));
					 bob.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, bob.length(), 0);
					 bob.append(' ');
				 }
				 if (event.location != null) {
					 bob.append(drawTextSanitizer(event.location.toString(),
							 MAX_EVENT_TEXT_LEN - bob.length()));
				 }

				 switch (event.selfAttendeeStatus) {
				 case Attendees.ATTENDEE_STATUS_INVITED:
					 paint.setColor(event.color);
					 break;
				 case Attendees.ATTENDEE_STATUS_DECLINED:
					 paint.setColor(mEventTextColor);
					 paint.setAlpha(Utils.DECLINED_EVENT_TEXT_ALPHA);
					 break;
				 case Attendees.ATTENDEE_STATUS_NONE: // Your own events
				 case Attendees.ATTENDEE_STATUS_ACCEPTED:
				 case Attendees.ATTENDEE_STATUS_TENTATIVE:
				 default:
					 paint.setColor(mEventTextColor);
					 break;
				 }

				 // Leave a one pixel boundary on the left and right of the rectangle for the event
				 layout = new StaticLayout(bob, 0, bob.length(), new TextPaint(paint), r.height(),
						 Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true, null, r.height());

				 layouts[i] = layout;
			 }
			 layout.getPaint().setAlpha(mEventsAlpha);
			 return layout;
		 }
	 
 
}
