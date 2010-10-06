/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of Android GPI.
 *
 *  Android GPI is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android GPI is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Android GPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cospandesign.android.gpi.workspace;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas;
import com.cospandesign.gpi.R;

public class WorkspaceLayout extends FrameLayout implements MessageEndPoint {



	ActionView mOptionsView;
	DeleteZone mDeleteView;
	ActionView mAddConnectionView;
	ActionView mRemoveConnectionView;
	ActionView mToWidgetView;
	StatusView mStatusView;
	
	CanvasScrollView mWidgetScroller;
	WidgetCanvas mWidgetCanvas;
	
	WorkspaceActivity mWorkspace;
	
	CanvasScrollView mControlScroller;
	ControlCanvas mControlCanvas;
	
	float mWCWidthRatio = 1;
	float mWCHeightRatio = 1;
	
	float mStartX = 0.0f;
	float mStartY = 0.0f;
	
	final GpiConsole mConsole = GpiConsole.getinstance();
	
	public WorkspaceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		

	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mOptionsView = (ActionView) findViewById(R.id.options_view);
		mDeleteView = (DeleteZone) findViewById(R.id.delete_zone);
		mStatusView = (StatusView) findViewById(R.id.status_view);
		mWidgetCanvas = (WidgetCanvas) findViewById(R.id.WidgetCanvas);
		mWidgetScroller = (CanvasScrollView) findViewById(R.id.WidgetCanvasScrollView);
		//android.view.ViewGroup.LayoutParams wslp = mWidgetScroller.getLayoutParams();
		//wslp.width = mWidgetCanvas.getCanvasWidth();
		//wslp.height = mWidgetCanvas.getCanvasHeight();
		mWidgetScroller.setHorizontalScrollBarEnabled(true);
		
		mControlCanvas = (ControlCanvas) findViewById(R.id.ControlCanvasLayout);
		mControlScroller = (CanvasScrollView) findViewById(R.id.ControlCanvasScrollView);
		//android.view.ViewGroup.LayoutParams cslp = mControlScroller.getLayoutParams();
		//cslp.width = mControlCanvas.getCanvasWidth();
		//cslp.height = mControlCanvas.getCanvasHeight();
		mControlScroller.setHorizontalScrollBarEnabled(true);
	}

	//User Events
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mWidgetScroller.getVisibility() == VISIBLE){
			switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				mStartX = event.getX();
				mStartY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				float x = event.getX();
				float y = event.getY();
				float scrollByX = x - mStartX;
				float scrollByY = y - mStartY;
				mStartX = x;
				mStartY = y;
				mControlScroller.updateScroll(x/mWCWidthRatio, y/mWCHeightRatio, scrollByX/mWCWidthRatio, scrollByY/mWCHeightRatio);
				invalidate();
				break;
			}
			return false;
		}
		return false;
	}

	//Functions
	private void getRatio (){
		float controlWidth = mControlCanvas.getCanvasWidth();
		float controlHeight = mControlCanvas.getCanvasHeight();
		
		float widgetWidth = mWidgetCanvas.getCanvasWidth();
		float widgetHeight = mWidgetCanvas.getCanvasHeight();
		
		mWCWidthRatio = widgetWidth/controlWidth;
		mWCHeightRatio = widgetHeight/controlHeight;
	}
	
	//Draw
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		//super.onLayout(changed, left, top, right, bottom);
		final int count = getChildCount();
		for (int i = 0; i < count; i++){
			View child = this.getChildAt(i);
			if (child instanceof ActionView){
				ActionView av = (ActionView)child;
				if (av.isInternalLayout()){
					if (av.isUseGravity()){
						Rect inRect = new Rect(left, top, right, bottom);
						Rect outRect = new Rect();
						Gravity.apply(av.getGravity(), av.getMeasuredWidth(), av.getMeasuredHeight(), inRect, av.getX(), av.getY(), outRect);
						child.layout(outRect.left, outRect.top, outRect.right, outRect.bottom);
						
					}
					else{
						child.layout(av.getX(), av.getY(), av.getX() + av.getWidth(), av.getY() + av.getHeight());
					}
				}
				else{
					child.layout(left, top, right, bottom);
				}
				
			}
			
			else if (child instanceof StatusView){
				//child.layout(left + mInfoView.getMeasuredWidth(), bottom - child.getMeasuredHeight(), right, bottom);
				child.layout(left + mOptionsView.getMeasuredWidth(), top, right, child.getMeasuredHeight());
			}
			else{
				child.layout(left, top, right, bottom);
			}
		}
		

	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		
		//int maxWidthSpec = widthSpecSize | MeasureSpec.AT_MOST;
		//int maxHeightSpec = heightSpecSize | MeasureSpec.AT_MOST;
		
		
		int count = getChildCount();
		for (int i = 0; i < count; i++){
			View child = getChildAt(i);
			if (child instanceof ActionView){
				child.measure(widthSpecSize | MeasureSpec.AT_MOST, heightSpecSize | MeasureSpec.AT_MOST);
				continue;
			}
			if (child instanceof StatusView){
				child.measure(widthSpecSize - mOptionsView.getLayoutParams().width, heightSpecSize | MeasureSpec.AT_MOST);
				continue;
			}
			try{
				child.measure(widthMeasureSpec, heightMeasureSpec);
			}catch (Exception ex){
				mConsole.error(ex.getMessage());
			}
		}
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}
	
	//setters/getters
	public void setWorkspace (WorkspaceActivity workspace){
		mWorkspace = workspace;
	}
	public void addActionView(ActionView actionView){
		addView(actionView);
	}

	//Router Functions
	public void ReceiveMessage(MESSAGE_TYPE messageType, MessageEndPoint from,
			MessageEndPoint to, Object Data) {
		
		switch (messageType){
		case START_SCROLLING:
			mWidgetScroller.setEnabled(true);
			break;
		case STOP_SCROLLING:
			mWidgetScroller.setEnabled(false);
			break;
		}

		
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data) {
		mWorkspace.MessageToRouter(messageType, directed, from, to, Data);
	}
	public void setMessageRouter(MessageRouter router) {
		
	}

	public void setWidgetCanvasVisible(boolean setVisible){
		if (setVisible){
			getRatio();
			mWidgetScroller.scrollTo((int)(mControlScroller.getScrollX() * mWCWidthRatio), (int)(mControlScroller.getScrollY() * mWCHeightRatio));
			mWidgetCanvas.setVisibility(VISIBLE);
			mWidgetScroller.setEnabled(true);
			mWidgetScroller.setVisibility(VISIBLE);
			mWidgetScroller.bringToFront();
			mWidgetScroller.requestLayout();
			mWidgetScroller.invalidate();
		}
		else {
			mWidgetScroller.setVisibility(GONE);
		}
	}

}
