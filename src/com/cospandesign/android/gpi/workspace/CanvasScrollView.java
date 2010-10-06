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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class CanvasScrollView extends FrameLayout {

	Rect mDisplayRect;
	Rect mScrollRect;
	
	float mStartX = 0.0f;
	float mStartY = 0.0f;
	
	float mPaddingX = 10;
	float mPaddingY = 10;
	
	float mLocationX = 0;
	float mLocationY = 0;

	boolean mEnabled = true;
	
	public CanvasScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//setup the display, and scroll rectangles to a default size
		mDisplayRect = new Rect(0, 0, 100, 100);
		mScrollRect = new Rect(0, 0 , 200, 200);
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		//reset the Display Rectangle
		mDisplayRect.set(left, top, right, bottom);
		//this might be done automatically with super
		//getChildAt(0).layout(left, top, right, bottom);
		
		super.onLayout(changed, left, top, right, bottom);
	
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		
		this.getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
		int scrollWidth = this.getChildAt(0).getMeasuredWidth();
		int scrollHeight = this.getChildAt(0).getMeasuredHeight();
		
		mScrollRect.set(0, 0, scrollWidth, scrollHeight);
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//this.getChildAt(0).draw(canvas);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
	
		if (!mEnabled){
			return false;
		}
		
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
			updateScroll(x, y, scrollByX, scrollByY);
			invalidate();
			break;
		}
		return false;
	}
	public void updateScroll(float screenX, float screenY, float scrollByX, float scrollByY){
		float locationX = mLocationX - scrollByX;
		float locationY = mLocationY - scrollByY;
		
		//Sanity Checks for location, just in case something gets screwy
		if ((mLocationX + mPaddingX) < 0){
			scrollTo((int)-mPaddingX, (int)mLocationY);
			mLocationX = mPaddingX;
		}
		else if ((mLocationX - mPaddingX) > (mScrollRect.width() - mDisplayRect.width())){
			scrollTo((int)((mScrollRect.width() - mDisplayRect.width()) + mPaddingX), (int) mLocationY);
			mLocationX = (mScrollRect.width() - mDisplayRect.width() + mPaddingX);
		}
		
		if ((mLocationY + mPaddingY)< 0){
			scrollTo((int)mLocationX, (int)	-mPaddingY);
			mLocationY = mPaddingY;
		}
		else if ((mLocationY - mPaddingY) > (mScrollRect.height() - mDisplayRect.height())){
			scrollTo(0, (int) (mScrollRect.height() - mDisplayRect.height() + mPaddingY));
			mLocationY = (mScrollRect.height() - mDisplayRect.height()) + mPaddingY;
		}
		
		if (locationX < -mPaddingX){
			scrollByX = mPaddingX + mLocationX;

		}
		else if (locationX > (mScrollRect.width() - mDisplayRect.width() + mPaddingX)){
			scrollByX = mLocationX - (mScrollRect.width() - mDisplayRect.width() + mPaddingX);
		}
		
		if (locationY < -mPaddingY){
			scrollByY = mPaddingY + mLocationY;
		}
		else if (locationY > (mScrollRect.height() - mDisplayRect.height() + mPaddingY)){
			scrollByY = mLocationY - (mScrollRect.height() - mDisplayRect.height() + mPaddingY);
		}
		
		mLocationX = mLocationX - ((int)scrollByX);
		mLocationY = mLocationY - ((int)scrollByY);
		scrollBy((int)-scrollByX, (int)-scrollByY);
	}
	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}
	
	

}
