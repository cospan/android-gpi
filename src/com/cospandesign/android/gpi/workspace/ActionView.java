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
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;

public class ActionView extends ImageView implements MessageEndPoint
{
	boolean mMode = false;
	
	//WorkspaceActivity.ActionViewLayoutParams ActionLayoutParams;
	ActionViewLayoutParams ActionLayoutParams;
	
	String mName;
	//EntitySupervisor mSupervisor;
	WorkspaceActivity mWorkspace;
	FrameLayout mParentFrame;
	//ControlCanvas mControlCanvas;
	//WidgetCanvas mWidgetCanvas;
	
	AnimationSet mInAnimation;
	AnimationSet mOutAnimation;
	
	boolean InternalLayout = false;
	boolean InternalMeasure = false;
	boolean UseGravity = false;
	
	public ActionView(Context context, String name){
		super(context);
		mName = name;
	}
	public ActionView(Context context)
	{
		super(context);

	}
	public ActionView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public ActionView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	//Override
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		/*
		if (getVisibility() == VISIBLE){
			mMode = true;
		}
		else {
			mMode = false;
		}
		*/
	}
	
	//Functions
	protected void setupAnimations(int inputAnimationID, int outputAnimationID){
        mInAnimation = (AnimationSet) AnimationUtils.loadAnimation(getContext(), inputAnimationID);
        mOutAnimation = (AnimationSet) AnimationUtils.loadAnimation(getContext(), outputAnimationID);
	}
	public void setSize(float f, float g){
		ActionLayoutParams.width = (int) f;
		ActionLayoutParams.height = (int) g;
	}
	public void setLocation(int x, int y){
		ActionLayoutParams.setX(x);
		ActionLayoutParams.setY(y);
	}
	public void setInternalLayout(boolean internalLayout){
		InternalLayout = internalLayout;
	}
	public boolean isInternalLayout(){
		return InternalLayout;
	}
	public void setInternalMeasure(boolean internalMeasure){
		InternalMeasure = internalMeasure;
	}
	public boolean isInternalMeasure(){
		return InternalMeasure;
	}
	public void setUseGravity(boolean useGravity){
		UseGravity = useGravity;
	}
	public boolean isUseGravity(){
		return UseGravity;
	}
	public int getX(){
		return ActionLayoutParams.getX();
	}
	public int getY(){
		return ActionLayoutParams.getY();
	}
	public int getGravity(){
		return ActionLayoutParams.getGravity();
	}
	public void setGravity(int gravity){
		ActionLayoutParams.setGravity(gravity);
	}

	//Drawing

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if (InternalMeasure){
			setMeasuredDimension(getLayoutParams().width, getLayoutParams().height);	
		}
		else{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom)
	{
		if (ActionLayoutParams.AlwaysVisible){
			bringToFront();
		}
		if (ActionLayoutParams.ControlVisible && !mWorkspace.isWIDGET_CANVAS_VISIBLE()){
			bringToFront();
		}
		if (ActionLayoutParams.MaifestVisible && mWorkspace.isWIDGET_CANVAS_VISIBLE()){
			bringToFront();
		}
		if (ActionLayoutParams.DragVisible && mWorkspace.isDRAGGING()){
			bringToFront();
		}
				
		super.onLayout(changed, left, top, right, bottom);
	}
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	@Override
	public void setLayoutParams(LayoutParams params)
	{
		super.setLayoutParams(params);
		if (params instanceof ActionViewLayoutParams){
			//ActionLayoutParams = (WorkspaceActivity.ActionViewLayoutParams)params;
			ActionLayoutParams = (ActionViewLayoutParams)params;
		}
		else{
			ActionLayoutParams = new ActionViewLayoutParams(params);
		}
	}

	//Functions
	public void show(){
		if (getVisibility() != VISIBLE){
			//mMode = true;
			startAnimation(mInAnimation);
			setVisibility(VISIBLE);
		}
	}
	public void hide(){
		if (getVisibility() != GONE){
			//mMode = false;
			startAnimation(mOutAnimation);
			setVisibility(GONE);
		}
	}
	public void setupActionView(WorkspaceActivity workspace, FrameLayout parentFrame, int imageId, int nameId){
		mWorkspace = workspace;
		mParentFrame = parentFrame;
		//mControlCanvas = controlCanvas;
		//mWidgetCanvas = widgetCanvas;
		setImageResource(imageId);
		mName = getContext().getResources().getString(nameId);
		setOnClickListener(mWorkspace);
		setOnLongClickListener(mWorkspace);
		
	}
	/*
	public boolean getMode(){
		return mMode;
	}
	public void setMode(boolean mode){
		mMode = mode;
	}
	*/
	public String getName(){
		return mName;
	}
	@Override
	public String toString(){
		return mName;
	}
	
	//Message End Point
	public void ReceiveMessage(MESSAGE_TYPE messageType,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		// TODO Auto-generated method stub
		
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		mWorkspace.MessageToRouter(messageType, directed, from, to, Data);
		
	}
	public void setMessageRouter(MessageRouter router)
	{
		// mWorkspace is the router
		
	}
	
	public static class ActionViewLayoutParams extends LayoutParams{

		public boolean AlwaysVisible = false;
		public boolean MaifestVisible = false;
		public boolean ControlVisible = false;
		public boolean DragVisible = false;
		private int X = 0;
		private int Y = 0;
		private int mGravity;
		
		public ActionViewLayoutParams(Context context, AttributeSet attrs)
		{
			super(context, attrs);
		}

		public ActionViewLayoutParams(int width, int height)
		{
			super(width, height);
		}

		public ActionViewLayoutParams(LayoutParams lp)
		{
			super(lp);
		}
	
		public void setX(int x){
			X = x;
		}
		public void setY(int y){
			Y = y;
		}
		public int getX(){
			return X;
		}
		public int getY(){
			return Y;
		}
		public void setGravity(int gravity){
			mGravity = gravity;
		}
		public int getGravity(){
			return mGravity;
		}
	}
	
}
