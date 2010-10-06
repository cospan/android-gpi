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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;
import com.cospandesign.gpi.R;

public class StatusView extends TextView
{
	public static final int ANIMATION_DURATION = 250;
	public static final int DEFAULT_TIMEOUT = 1500;
	
	//EntitySupervisor mSupervisor;
	WorkspaceActivity mWorkspace;
	ControlCanvas mControlCanvas;
	
	AnimationSet mInAnimation;
	AnimationSet mOutAnimation;
	
	//Timers
	private Timer mTimer;
	private TimerTask mTimerTask;
	
	private TransitionDrawable mTransition;
	private boolean timerEnabled = false;
	
	boolean statusMode = false;
	
	//Constructor
	public StatusView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	public StatusView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		setOnClickListener(mWorkspace);
		
		mInAnimation = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.top_enter);
		mOutAnimation = (AnimationSet) AnimationUtils.loadAnimation(context, R.anim.top_exit);

		
		//mTimer = new Timer();
		mTimerTask = new TimerTask(){
			public void run(){
				hide();
				timerEnabled = false;
			}
		};
		
	}
	public StatusView(Context context)
	{
		super(context);
	}

	//Override
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mTransition = (TransitionDrawable) getBackground();
		statusMode = false;
		setVisibility(View.GONE);
		
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), getLayoutParams().height);
	}


	//Functions
	public void show(int timeoutMillis){
		
		if (statusMode == false){
			statusMode = true;
			startAnimation(mInAnimation);
			setVisibility(VISIBLE);
		}
		
		
		/*
		if (!timerEnabled){
			//if timeoutMillis = -1 use default
			//if timeoutMillis = 0 don't use timeout
			if (timeoutMillis != 0){
				mTimer = new Timer();
				if (timeoutMillis == -1){
					mTimer.schedule(mTimerTask, DEFAULT_TIMEOUT);
					timerEnabled = true;
				}
				else if (timeoutMillis > 0){
					mTimer.schedule(mTimerTask, timeoutMillis);
					timerEnabled = true;
				}
			}
		}
		else{
			mTimer.cancel();
			timerEnabled = false;
		}
		*/
		
	}
	public void hide(){
		
		if (statusMode){
			statusMode = false;
			startAnimation(mOutAnimation);
			setVisibility(GONE);

		}
	}
	public void clearText(){
		setText("");
	}
	public void setWorkspace(WorkspaceActivity workspace){
		mWorkspace = workspace;
	}
	public void setControlCanvas(ControlCanvas cc){
		mControlCanvas = cc;
	}
	private void createAnimation(){
		if (mInAnimation == null){
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));
			animationSet.addAnimation(
					new TranslateAnimation(
							Animation.ABSOLUTE, 0.0f,
							Animation.ABSOLUTE, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f,
							Animation.RELATIVE_TO_SELF, 0.0f));
			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mOutAnimation == null){
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.1f, 0.0f));
			animationSet.addAnimation(
					new TranslateAnimation(
							Animation.ABSOLUTE, 0.0f,
							Animation.ABSOLUTE, 0.0f,
							Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, -1.0f));
			
			animationSet.setDuration(ANIMATION_DURATION);
		}
	}
	
	private static class FastAnimationSet extends AnimationSet {

		public FastAnimationSet()
		{
			super (false);
		}
		@Override
		public boolean willChangeBounds()
		{
			return false;
		}
		@Override
		public boolean willChangeTransformationMatrix()
		{
			return true;
		}
		
	}
}
