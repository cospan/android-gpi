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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

public class DragLayer extends FrameLayout implements DragController
{

//Members
	//private DragListener mDragListener;
	private ArrayList<DragListener> mDragListeners;
	//private DragScroller dragScroller;
	private DragSource mDragSource;	//defining where an object can be dragged
	private Object mDragInfo;	//information about the object
	
	//Conditions
	private boolean mDragging = false;
	private boolean mShouldDrop;

	//Image
	private Bitmap mDragBitmap;
	private View mOriginator;		//view that we came from
	private int mBitmapOffsetX;
	private int mBitmapOffsetY;
	private float mTouchOffsetX; //offset of where we touched the cell to the upper left corner
	private float mTouchOffsetY; //offset of where we touched the cell to the upper left corner
	private float mLastMotionX;
	private float mLastMotionY;
	private Rect mDragRect;
	private final Rect mRect = new Rect();
	private final int[] mDropCoordinates = new int[2];
	private RectF mDragRegion;
	private boolean mEnteredRegion;
	private DropTarget mLastDropTarget;
	private final Paint mTrashPaint = new Paint();
	private Paint mDragPaint;
	private ArrayList<View> mIgnoredDropTargets;
	//private View mIgnoredDropTarget;
	
	//States/Constants
	private enum ANIMATION_STATE {
		NOTHING,
		STARTING,
		RUNNING,
		DONE
	}
	private enum ANIMATION_TYPE {
		NOTHING,
		SCALE
	}
    private static final float DRAG_SCALE = 24.0f; 	// Number of pixels to add to the dragged item for scaling
	private static final int ANIMATION_SCALE_UP_DURATION = 110;
    
	private ANIMATION_STATE mAnimationState;
	private ANIMATION_TYPE mAnimationType;
	private float mAnimationFrom;
	private float mAnimationTo;
	private int mAnimationDuration;
	private long mAnimationStartTime;
	
	private InputMethodManager mInputMethodManager;
	
//Functions	
	
	//Constructor
	public DragLayer(Context context, AttributeSet attrs)
	{
		//attributes are used to create a new ControlCanvas from XML
		super(context, attrs);
		
		//setup variables
		
		//Conditions
		mDragging = false;
		mShouldDrop = false;
		
		//Image
		mDragBitmap = null;
		mOriginator = null;
		mBitmapOffsetX = 0;
		mBitmapOffsetY = 0;
		mTouchOffsetX = 0.0f;
		mTouchOffsetY = 0.0f;
		mLastMotionX = 0.0f;
		mLastMotionY = 0.0f;
		mDragRect = new Rect();
		mEnteredRegion = false;
		mLastDropTarget = null;
		mDragPaint = null;
		mAnimationState = ANIMATION_STATE.NOTHING;
		mAnimationType = ANIMATION_TYPE.NOTHING;
		
		//DragListener
		mDragListeners = new ArrayList<DragListener>();
		mIgnoredDropTargets = new ArrayList<View>();
		
		//Trash Can
		//final int srcColor = context.getResources().getColor(com.cospandesign.ucs.R.color.delete_color_filter);
		//mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));
	}

	//DragController Interface
	public void removeDragListener(DragListener l)
	{
		mDragListeners.remove(l);
	}
	public void setDragListener(DragListener l)
	{
		if (l != null){
			mDragListeners.add(l);
		}
	}
	public void startDrag(View v, DragSource source, Object dragInfo, int dragAction)
	{
		//Hide soft keyboard, if visible
		if (mInputMethodManager == null){
			mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
		
		//if the DragListener is not null, tell it weve got some movement
		for (DragListener dl: mDragListeners) {
			dl.onDragStart(v, source, dragInfo, dragAction);
		}
		
		//Get the rectangle that encompases the view that we are dragging
		Rect r = mDragRect;
		r.set(v.getScrollX(), v.getScrollY(), 0, 0);
		
		//get the touch offset from the top corner
		offsetDescendantRectToMyCoords(v, r);
		mTouchOffsetX = mLastMotionX - r.left;
		mTouchOffsetY = mLastMotionY - r.top;
		
		//?Clear the focus so we can focus on other windows the drag items goes to???
		v.clearFocus();
		//?clear the touch event so we don't fire off another touch event somewhere else???
		v.setPressed(false);
		
		//??
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);
		v.buildDrawingCache();
		
		//get a bitmap of the object, along with its width, and height
		Bitmap viewBitmap = v.getDrawingCache();
		int width = viewBitmap.getWidth();
		int height = viewBitmap.getHeight();
		
		//using Matrices scale up the drag to indicate we are dragging
		Matrix scale = new Matrix();
		float scaleFactor = v.getWidth();
		scaleFactor = (scaleFactor + DRAG_SCALE) / scaleFactor;
		
		//set up an animation from when user picks up item, to then intermediate dragging
		mAnimationTo = 1.0f;
		mAnimationFrom = 1.0f / scaleFactor;
		mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
		mAnimationState = ANIMATION_STATE.STARTING;
		mAnimationType = ANIMATION_TYPE.SCALE;
		
		//create a bitmap from the View, this is nice because regardless of if the image is a PNG, or BMP, it will just be a bitmap
		mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height, scale, true);
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);

		//Create a new bitmap holder attached to the mDragBitmap we got above
		//thie bitmap is scaled for dragging
		final Bitmap dragBitmap = mDragBitmap;
		//get the offset of where we are touching the bitmap image of the dragging bitmap
		mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
		mBitmapOffsetY = (dragBitmap.getHeight()- height) / 2;
		
		//we are moving the item, so delete its previous position
		if (dragAction == DRAG_ACTION_MOVE){
			v.setVisibility(GONE);
		}
		
		mDragPaint = null;
		mDragging = true;
		mShouldDrop = true;
		mOriginator = v;
		mDragSource = source;
		mDragInfo = dragInfo;
		
		mEnteredRegion = false;
		
		//redraw everything so that if we are moving the item it redraws the dragging, and hides the previous
		invalidate();
	}
	//User Events
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		//return true if we are dragging something, or the super got something going on
		return mDragging || super.dispatchKeyEvent(event);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		//create unchangeable integer for action
		final int action = ev.getAction();
		
		//create unchangeable floats for X, and Y
		final float x = ev.getX();
		final float y = ev.getY();
		
		switch (action){
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_DOWN:
			//remember location of down touch, so if things don't go well, we can revert to here
			mLastMotionX = x;
			mLastMotionY = y;
			mLastDropTarget = null;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			//User is lifting finger from surface, we are dropping here
			//if we should drop, and drop action succeeds, we shouldn't drop anymore (we did our job already!)
			if (mShouldDrop && drop(x, y)){
				mShouldDrop = false;
			}
			//end the drag
			endDrag();
			break;
		
		}
		
		
		return mDragging;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//if were not dragging bail
		if (!mDragging){
			//false means we haven't handled the event
			return false;
		}
		
		//get all the touch actions, and locations
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		
		switch (action){
		case MotionEvent.ACTION_DOWN:
			//Remember previous location
			mLastMotionX = x;
			mLastMotionY = y;
			
			//not scrolling
			break;
		case MotionEvent.ACTION_MOVE:
			//TODO: Add ability to scroll
			//get the location of the touch
			final float touchX = mTouchOffsetX;
			final float touchY = mTouchOffsetY;
			
			//get the offset of the bitmap
			final float offsetX = mBitmapOffsetX;
			final float offsetY = mBitmapOffsetY;
			
			//set the left, and top of the image
			int left = (int) (mLastMotionX - touchX - offsetX);
			int top = (int) (mLastMotionY - touchY - offsetY);
			
			//get a reference to the bitmap
			final Bitmap dragBitmap = mDragBitmap;
			//from the reference we just got, find the width, and height
			final int width = dragBitmap.getWidth();
			final int height = dragBitmap.getHeight();
			
			//get the drag rectangle reference from the member
			final Rect rect = mRect;
			rect.set(left - 1, top - 1, left + width, top + height + 1);
			
			//remember previous location
			mLastMotionX = x;
			mLastMotionY = y;
			
			//set the left, top of the image
			left = (int) (x - touchX - offsetX);
			top = (int) (y - touchY - offsetY);
			
			//set the rectangle to the size of our object
			rect.union(left - 1, top - 1, left + width + 1, top + height + 1);
			//redraw the rectangle we just moved
			invalidate(rect);
			
			final int[] coordinates = mDropCoordinates;
			DropTarget dropTarget = findDropTarget ((int) x, (int)y, coordinates);
			
			if (dropTarget != null)
				if (mLastDropTarget == dropTarget){
					dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1], (int)mTouchOffsetX, (int)mTouchOffsetY, mDragInfo);
				}
				else {
					if (mLastDropTarget != null){
						mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1], (int) mTouchOffsetX, (int)mTouchOffsetY, mDragInfo);
					}
					dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1], (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
				}
			mLastDropTarget = dropTarget;
			
			//find if we are in a drag region (like a trash can or something)
			boolean inDragRegion = false;
			if (mDragRegion != null) {
				final RectF region = mDragRegion;
				final boolean inRegion = region.contains(event.getRawX(), event.getRawY());
//TODO: write drop on device
//This is where we want to modify a color change for allowing widgets to drop on each other
//The default is trash can, but other stuff like a device, and a translator widget
				if (!mEnteredRegion && inRegion){
					//we are in the drag region (trash can)
					mDragPaint = mTrashPaint;
					mEnteredRegion = true;
					inDragRegion = true;
				}
				//not in any special region
				else if (mEnteredRegion && !inRegion){
					mDragPaint = null;
					mEnteredRegion = false;
				}
				
				//TODO: Scrolling Stuff here
				//NO SCROLLING YET!!
			}
			break;
		case MotionEvent.ACTION_UP:
			//TODO: Scrolling stuff here
			//NO SCROLLING YET!
            if (mShouldDrop) {
                drop(x, y);
                mShouldDrop = false;
            }
			endDrag();
			break;
		case MotionEvent.ACTION_CANCEL:
			endDrag();
			break;
		}
		return true;
	}
	//Drawing
	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
				
		if (mDragging && (mDragBitmap != null)){
			//STARTING
			if (mAnimationState == ANIMATION_STATE.STARTING){
				mAnimationStartTime = SystemClock.uptimeMillis();
				mAnimationState = ANIMATION_STATE.RUNNING;
			}
			//RUNNING
			if (mAnimationState == ANIMATION_STATE.RUNNING){
				float normalized = (float) ((SystemClock.uptimeMillis() - mAnimationStartTime) / mAnimationDuration);
				if (normalized >= 1.0f){
					mAnimationState = ANIMATION_STATE.DONE;
				}
				normalized = Math.min(normalized, 1.0f);
				final float value = mAnimationFrom + (mAnimationTo - mAnimationFrom) * normalized;
				
				switch (mAnimationType){
				case SCALE:
					final Bitmap dragBitmap = mDragBitmap;
					canvas.save();
					canvas.translate(mLastMotionX - mTouchOffsetX - mBitmapOffsetX, mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
					canvas.translate(
							(dragBitmap.getWidth() * (1.0f - value)) / 2,
							(dragBitmap.getHeight() * (1.0f - value)) / 2);
					canvas.scale(value, value);
					canvas.drawBitmap(dragBitmap, 0.0f, 0.0f, mDragPaint);
					canvas.restore();
					break;
				}
			}
			//DONE
			else{
				canvas.drawBitmap(
						mDragBitmap,
						mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
						mLastMotionY - mTouchOffsetY - mBitmapOffsetY,
						mDragPaint);
			}
			
		}
	}
	//Functions
	private boolean drop (float x, float y){
		invalidate();
		
		final int[] coordinates = mDropCoordinates;
		DropTarget dropTarget = findDropTarget((int) x, (int)y, coordinates);
		

		if (dropTarget != null){
			dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1], (int)mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
			if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1], (int)mTouchOffsetX, (int)mTouchOffsetY, mDragInfo))
			{
				dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1], (int)mTouchOffsetX, (int)mTouchOffsetY, mDragInfo);
				mDragSource.onDropCompleted((View) dropTarget, true);
				return true;
			}
			else {
				mDragSource.onDropCompleted((View) dropTarget, false);
				//we are moving the item, so delete its previous position
			}
		}
		
		return false;
	}
	DropTarget findDropTarget(int x, int y, int[] dropCoordinates){
		return findDropTarget(this, x, y, dropCoordinates);
	}
	private DropTarget findDropTarget(ViewGroup container, int x, int y, int[] dropCoordinates){
		final Rect r = mDragRect;
		final int count = container.getChildCount();
		final int scrolledX = x + container.getScrollX();
		final int scrolledY = y + container.getScrollY();
		//final View ignoredDropTarget = mIgnoredDropTarget;
		
		//were looking at every child of this viewgroup
		for (int i = count - 1; i >= 0; i--){
			//create a new child from the abstract View class
			final View child = container.getChildAt(i);
			//if the child is visible, and the child is not ignored
			if (child.getVisibility() == VISIBLE && (!mIgnoredDropTargets.contains(child))){
				//get the rectangle in which it was hit in
				child.getHitRect(r);
				if (r.contains(scrolledX, scrolledY)){
					DropTarget target = null;
					if (child instanceof ViewGroup){
						x = scrolledX - child.getLeft();
						y = scrolledY - child.getTop();
						//recursively drop down until we hit a View, not a ViewGroup
						target = findDropTarget((ViewGroup) child, x, y, dropCoordinates);
					}
					if (target == null){
						if (child instanceof DropTarget){
							dropCoordinates[0] = x;
							dropCoordinates[1] = y;
							return (DropTarget) child;
						}
					}
					else{
						//we got a DropTarget from either this call, or a recursive call
						return target;
					}	
				}
			}
		}
		return null;
	}
	void setIgnoredDropTarget(View view){
		mIgnoredDropTargets.add(view);
	}
	private void endDrag() {
		//if were dragging
		if (mDragging){
			//were dragging no more
			mDragging = false;
			//free the bitmap to the GC monster
			if (mDragBitmap != null){
				mDragBitmap.recycle();
			}
			//Visibalize the originator
			if (mOriginator != null){
				mOriginator.setVisibility(VISIBLE);
			}
			//Tell the listener we are done
			for (DragListener dl: mDragListeners) {
				dl.onDragEnd();
			}
		}
	}

}
