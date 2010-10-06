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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.text.format.Time;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas.ConnectLabelLayoutParams.IO;

public class ConnectionPointView extends TextView
{
	WorkspaceEntity mWorkspaceEntity;
	
	
	//Animation Set
	private static int LTPURPLE = Color.argb(255, 180, 80, 250);
	private static int DKPURPLE = Color.argb(255, 110, 50, 160);
	private static int LTORANGE = Color.argb(255, 250, 200, 170);
	private static int DKORANGE = Color.argb(255, 250, 90, 0);
	private static int DKGREEN = Color.argb(255, 0, 130, 20);

	private static int SPEED_LIMIT_0 = 2000;
	private static int SPEED_LIMIT_1 = 500;
	private static int SPEED_LIMIT_2 = 100;
	private static int SPEED_LIMIT_3 = 10;
	private static int SPEED_LIMIT_4 = 1;
	
	private static int SPEED_COLOR_0 = LTPURPLE;
	private static int SPEED_COLOR_1 = DKPURPLE;
	private static int SPEED_COLOR_2 = LTORANGE;
	private static int SPEED_COLOR_3 = DKORANGE;
	private static int SPEED_COLOR_4 = DKGREEN;

	private static int SPEED_PERIOD_0 = 5;
	private static int SPEED_PERIOD_1 = 10;
	private static int SPEED_PERIOD_2 = 15;
	private static int SPEED_PERIOD_3 = 20;
	private static int SPEED_PERIOD_4 = 25;
	
	private static float SPEED_RATIO_0 = 0.80f;
	private static float SPEED_RATIO_1 = 0.60f;
	private static float SPEED_RATIO_2 = 0.40f;
	private static float SPEED_RATIO_3 = 0.20f;
	//private static int SPEED_RATIO_4 = 0.01f;

	int mChangeDelta = 5;
	
	long mPrevDataTime = 0;
	long mDataTimeDelta = 0;
	volatile boolean newDataFlag = false;
	
	int mCurrentColor = Color.WHITE;
	//int mCurrentAlpha = 255;
	int mDestinationColor = SPEED_COLOR_0;
	int mDefaultBackground = Color.WHITE;
	int mPeriod = SPEED_PERIOD_0;
	int mNewPeriod = SPEED_PERIOD_0;
	
	boolean mAnimationDirectionIn = false;
	int mAnimationCount = 0;

	TimerTask mTimerTask;
	Timer mTimer;

	private int mTimerTaskRate = 100;
	
	StatusDrawable mStatusDrawable;
	boolean mConnected = false;
	
	enum STATE{
		NOT_CONNECTED,
		CONNECTED,
		CONNECT_GOOD,
		CONNECT_BAD,
		CONNECT_WARNING,
		NEW_DATA
	}
	STATE mState = STATE.NOT_CONNECTED;
	boolean Selected = false;
	
	ControlCanvas.ConnectLabelLayoutParams mLayoutParams;
	Paint mPaint;
	Path mPath;

	//Place where the connection line is drawn
	int ConnectionPointX = 0;
	int ConnectionPointY = 0;
	
	public ConnectionPointView(Context context, WorkspaceEntity workspaceEntity, String connectionName, IO io, Class type)
	{
		super(context);
		mWorkspaceEntity = workspaceEntity;
		this.setText(connectionName);
		this.setTag(type);
		this.setBackgroundColor(Color.WHITE);
		mLayoutParams = (ControlCanvas.ConnectLabelLayoutParams)new ControlCanvas.ConnectLabelLayoutParams();
		super.setLayoutParams(mLayoutParams);
		mLayoutParams.width = LayoutParams.WRAP_CONTENT;
		mLayoutParams.height = LayoutParams.WRAP_CONTENT;
		mLayoutParams.setIO(io);
		setPadding(2, 2, 2, 2);
		setLayoutParams(mLayoutParams);
		setOnClickListener(mWorkspaceEntity.mWorkspace);
		setOnLongClickListener(mWorkspaceEntity.mWorkspace);
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLACK);
		mPaint.setStrokeWidth(2.0f);
		mPath = new Path();

		mStatusDrawable = new StatusDrawable(getBackground());
		this.setBackgroundDrawable(mStatusDrawable);

		
		//this.scheduleDrawable(mStatusDrawable, mStatusDrawable, TimerTaskRate);
		//mStatusDrawable = (StatusDrawable)getBackground();
		/*
		mRollingAverage[0] = 0;
		for (int i = 1; i < mRollingAverageLength; i++){
			mRollingAverage[i] = mRollingAverage[0] + 10;
		}
		*/
		
		mTimerTask = new TimerTask(){
			@Override
			public void run()
			{
				//dataAnimation();
				mStatusDrawable.run();
			}
		};
		

	}
	public void setState(STATE state){
		mState = state;
		
	
		switch(mState){
		case NOT_CONNECTED:
			mConnected = false;
			setConnectionColor(Color.WHITE);
			mStatusDrawable.setAlpha(255);
			break;
		case CONNECTED:
			mConnected = true;
			setConnectionColor(Color.BLUE);
			mStatusDrawable.setAlpha(255);
			break;
		case CONNECT_GOOD:
			setConnectionColor(Color.GREEN);
			mStatusDrawable.setAlpha(255);
			break;
		case CONNECT_BAD:
			setConnectionColor(Color.RED);
			mStatusDrawable.setAlpha(255);
			break;
		case CONNECT_WARNING:
			setConnectionColor(Color.YELLOW);
			mStatusDrawable.setAlpha(255);
			break;
		case NEW_DATA:
			newData();
			
			if ((mTimer == null) && !isInput()){
				mTimer = new Timer();
				mTimer.scheduleAtFixedRate(mTimerTask, 100, mTimerTaskRate);
			}
			postInvalidate();
			break;
		}
	}
	public void stop(){
		if (mTimerTask != null){
			mTimerTask.cancel();
		}
		if (mTimer != null){
			mTimer.cancel();
			mTimer.purge();
		}
		
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom)
	{
		//Setup the place where there is a connection to this device
		/*
		if (mIO == IO.INPUT){
			ConnectionPointX = left;
		}
		else{
			ConnectionPointY = right;
		}
		ConnectionPointY = bottom - top;
		*/
		
		left = mLayoutParams.X;
		right = mLayoutParams.X + this.getMeasuredWidth();
		top = mLayoutParams.Y;
		bottom = mLayoutParams.Y + this.getMeasuredHeight();
		super.onLayout(changed, left, top, right, bottom);
		
		mPath.reset();
		mPath.moveTo(0, 0);
		mPath.lineTo(0, getMeasuredHeight());
		mPath.lineTo(getMeasuredWidth(), getMeasuredHeight());
		mPath.lineTo(getMeasuredWidth(), 0);
		mPath.lineTo(0, 0);
		
		mStatusDrawable.setBounds(left, top, right, bottom);
		mStatusDrawable.bitmap = Bitmap.createBitmap(mStatusDrawable.getBounds().width(), mStatusDrawable.getBounds().height(), Bitmap.Config.ARGB_8888);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//setMeasuredDimension(mLayoutParams.width, mLayoutParams.height);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	@Override
	protected void onDraw(Canvas canvas)
	{
		if (Selected){
			mPaint.setColor(Color.BLUE);
		}
		else{
			mPaint.setColor(Color.BLACK);
		}

		super.onDraw(canvas);
		canvas.drawPath(mPath, mPaint);
	}
	
	public boolean isInput(){
		return mLayoutParams.isInput();
	}
	public boolean isConnected(){
		return mConnected;
	}
	public boolean isSelected(){
		return Selected;
	}
	public boolean isConnectionPointVisible(){
		return mLayoutParams.isLabelVisible;
	}
	public void setSelected(boolean selected){
		Selected = selected;
	}
	@Override
	public String toString(){
		return (String) getText();
	}
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof String){
			return (((String)o).equals(getText().toString()));
		}
		return super.equals(o);
	}
	public WorkspaceEntity getWorkspaceEntity()
	{
		return mWorkspaceEntity;
	}
	public Entity getEntity(){
		return mWorkspaceEntity.getEntity();
	}
	public int[] getConnectionPoint(){
		int [] connectionPoint = {mLayoutParams.AttachPointEntityX, mLayoutParams.AttachPointEntityY};
		if (mLayoutParams.isLabelVisible()){
			//Label is visible
			if (mLayoutParams.isInput()){
				//Label is input
				connectionPoint[0] = (mLayoutParams.AttachPointLabelX - this.getMeasuredWidth());
				connectionPoint[1] = mLayoutParams.AttachPointLableY;
				return connectionPoint;
			}
			//Label is output
			connectionPoint[0] = (mLayoutParams.AttachPointLabelX + this.getMeasuredWidth());
			connectionPoint[1] = mLayoutParams.AttachPointLableY;
		}
		return connectionPoint;
	}
	@Override
	public void setLayoutParams(LayoutParams params)
	{

	}

	public void newData(){

		Time now = new Time();
		now.setToNow();

		now.normalize(true);
		long timeNow = now.toMillis(true);
		
		int speedLimit = SPEED_LIMIT_0;
		
		mDataTimeDelta = timeNow - mPrevDataTime;
		mPrevDataTime = timeNow;
		
		//Get the Destination Color
		if (mDataTimeDelta >= SPEED_LIMIT_0){
			speedLimit = SPEED_LIMIT_0;
			mDestinationColor = SPEED_COLOR_0; 

		}
		else if (mDataTimeDelta >= SPEED_LIMIT_1){
			speedLimit = SPEED_LIMIT_1;
			mDestinationColor = SPEED_COLOR_1;
			
		}
		else if (mDataTimeDelta >= SPEED_LIMIT_2){
			speedLimit = SPEED_LIMIT_2;
			mDestinationColor = SPEED_COLOR_2;
		}
		else if (mDataTimeDelta >= SPEED_LIMIT_3){
			speedLimit = SPEED_LIMIT_3;
			mDestinationColor = SPEED_COLOR_3;
		}
		else{
			speedLimit = SPEED_LIMIT_4;
			mDestinationColor = SPEED_COLOR_4;
		}
		
		//Get the Destination Pulse Width
		if (mDataTimeDelta >= speedLimit * SPEED_RATIO_0){
			mNewPeriod = SPEED_PERIOD_0;
		}
		else if (mDataTimeDelta >= speedLimit * SPEED_RATIO_1){
			mNewPeriod = SPEED_PERIOD_1;
		}
		else if (mDataTimeDelta >= speedLimit * SPEED_RATIO_2){
			mNewPeriod = SPEED_PERIOD_2;
		}
		else if (mDataTimeDelta >= speedLimit * SPEED_RATIO_3){
			mNewPeriod = SPEED_PERIOD_3;
		}
		else {
			mNewPeriod = SPEED_PERIOD_4;
		}
		//now we have a destination color, and a new period
		newDataFlag = true;
	}	

	public int getConnectionColor(){
		return mCurrentColor;
	}
	public void setConnectionColor(int color){
		mCurrentColor = color;
		mDestinationColor = color;
		mState = STATE.NEW_DATA;
		//newData();
		//mStatusDrawable.invalidateSelf();
		//invalidateDrawable(getBackground());
		mStatusDrawable.run();
		postInvalidate();
	}

	public class StatusDrawable extends Drawable implements Callback, Runnable{

		
		Bitmap bitmap;

		
		public StatusDrawable(Drawable background)
		{
			// TODO Auto-generated constructor stub
			setBounds(background.getBounds());
			setCallback(this);
			
		}

		@Override
		public void draw(Canvas canvas)
		{

			canvas.drawARGB(Color.alpha(mCurrentColor), Color.red(mCurrentColor), Color.green(mCurrentColor), Color.blue(mCurrentColor));

		}

		@Override
		public int getOpacity()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setAlpha(int alpha)
		{
			mCurrentColor = Color.argb(alpha, Color.red(mCurrentColor), Color.green(mCurrentColor), Color.blue(mCurrentColor));
			
		}
		@Override
		public void setColorFilter(ColorFilter cf)
		{
			// TODO Auto-generated method stub
			
		}

		
		//Callback for animation
//		@Override
		public void invalidateDrawable(Drawable who)
		{
			//bitmap = Bitmap.createBitmap(this.getBounds().width(), this.getBounds().height(), Bitmap.Config.ARGB_8888);
			if (this.getBounds().width() <= 0 || this.getBounds().height() <= 0){
				return;
			}
			if (bitmap == null){
				bitmap = Bitmap.createBitmap(this.getBounds().width(), this.getBounds().height(), Bitmap.Config.ARGB_8888);
			}
			Canvas canvas = new Canvas();
			canvas.setBitmap(bitmap);
			draw(canvas);
			
		}
//		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when)
		{
			// TODO Auto-generated method stub
			
		}
//		@Override
		public void unscheduleDrawable(Drawable who, Runnable what)
		{
			// TODO Auto-generated method stub
			
		}
		
		//Runnable
//		@Override
		public void run()
		{
			
			if (!newDataFlag){
				invalidateDrawable(getBackground());
				return;
				
			}

			newDataFlag = false;
			
			int grn = Color.green(mCurrentColor);
			int newGrn = Color.green(mDestinationColor);
			int rd = Color.red(mCurrentColor);
			int newRd = Color.red(mDestinationColor);
			int blu = Color.blue(mCurrentColor);
			int newBlu = Color.blue(mDestinationColor);
			int alpha = Color.alpha(mCurrentColor);
			
			mPeriod = mNewPeriod;
			
			//Set Color
			if (grn > newGrn + mChangeDelta){
				grn -= mChangeDelta;
				if (grn < 0){
					grn = 0;
				}
			}
			else if (grn < newGrn - mChangeDelta){
				grn += mChangeDelta;
				if (grn > 255){
					grn = 255;
				}
			}

			if (rd > newRd + mChangeDelta){
				rd -= mChangeDelta;
				if (rd < 0){
					rd = 0;
				}
			}
			else if (rd < newRd - mChangeDelta){
				rd += mChangeDelta;
				if (rd > 255){
					rd = 255;
				}
			}
			
			if (blu > newBlu + mChangeDelta){
				blu -= mChangeDelta;
				if (blu < 0){
					blu = 0;
				}
			}
			else if (blu < newBlu - mChangeDelta){
				blu += mChangeDelta;
				if (blu > 255){
					blu = 255;
				}
			}
			
			//Adjust Alpha
			
			if (mAnimationDirectionIn){
				alpha += mPeriod;
				if (alpha > 255){
					alpha = 255;
					mAnimationDirectionIn = false;
				}
			}
			else{
				alpha -= mPeriod;
				if (alpha < 10){
					alpha = 10;
					mAnimationDirectionIn = true;
				}
			}
			
			mCurrentColor = Color.argb(alpha, rd, grn, blu);
			
			
			invalidateDrawable(getBackground());
			
		}
		
	}
}
