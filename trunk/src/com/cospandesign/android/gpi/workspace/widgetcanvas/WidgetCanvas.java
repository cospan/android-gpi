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

package com.cospandesign.android.gpi.workspace.widgetcanvas;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.cospandesign.android.gpi.GpiConstants;
import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.DragController;
import com.cospandesign.android.gpi.workspace.DragLayer;
import com.cospandesign.android.gpi.workspace.DragSource;
import com.cospandesign.android.gpi.workspace.DropTarget;
import com.cospandesign.android.gpi.workspace.WorkspaceActivity;

public class WidgetCanvas extends ViewGroup implements DropTarget, DragSource, MessageEndPoint
{


	
	WorkspaceActivity mWorkspace;
	Widget mSelectedWidget;
	DragLayer mDragLayer;
	MessageRouter Router;
	//ScrollView mScroller;
	
	//ArrayList<WidgetView> WidgetViews;

	int CanvasSizeX = 4000;
	int CanvasSizeY = 4000;
	
	//Constructors
	public WidgetCanvas(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	public WidgetCanvas(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public WidgetCanvas(Context context)
	{
		super(context);
	}

	//Overrides
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		//make the background semi-transparent
		getBackground().setAlpha(128);
		
	}
	
	//Drawing
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom)
	{
		WidgetCanvasViewPort.setSize(right - left, bottom - top);
		WidgetCanvasViewPort.update(CanvasSizeX, CanvasSizeY);
		//Display Widget Views
		for (int i = 0; i < this.getChildCount(); i++){
			WidgetCanvas.LayoutParams lp = (WidgetCanvas.LayoutParams)getChildAt(i).getLayoutParams();
			getChildAt(i).layout(lp.X - WidgetCanvasViewPort.X, lp.Y - WidgetCanvasViewPort.Y, lp.X + lp.width - WidgetCanvasViewPort.X, lp.Y + lp.height - WidgetCanvasViewPort.Y);
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(CanvasSizeX, MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(CanvasSizeY, MeasureSpec.EXACTLY);
		
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		
		WidgetCanvasViewPort.setSize(widthSpecSize, heightSpecSize);
		//if the width of the viewport increased to a point where some thing could go weird, update things
		WidgetCanvasViewPort.update(CanvasSizeX, CanvasSizeY);
		
		//run through all the children to setup measure
		for (int i = 0; i < this.getChildCount(); i++){
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		
	}

	//Functions
	public void setWorkspace(WorkspaceActivity workspace){
		mWorkspace = workspace;
	}
	public void setDragLayer(DragLayer dragLayer){
		mDragLayer = dragLayer;
	}
	/*
	public void setScrollLayer (ScrollView scrollView){
		mScroller = scrollView;
	}
	*/
	public void setupListeners(){
		setOnClickListener(mWorkspace);
		setOnLongClickListener(mWorkspace);
	}
	public void setSize(int sizeX, int sizeY){
		CanvasSizeX = sizeX;
		CanvasSizeY = sizeY;
		WidgetCanvasViewPort.update(CanvasSizeX, CanvasSizeY);
	}
	/*
	public void show (){
		setVisibility(VISIBLE);
		mScroller.setVisibility(VISIBLE);
		mScroller.bringToFront();
		mScroller.requestLayout();
		mScroller.invalidate();
	}
	public void hide (){
		//setVisibility(GONE);
		mScroller.setVisibility(GONE);
	}
	*/
/*
	public void addWidgetView(WidgetView view, int x, int y){
		//TODO setup the widget view to be at home
		WidgetCanvas.LayoutParams wlp = new WidgetCanvas.LayoutParams(x, y, WidgetCanvasViewPort.Zoom, view);
		wlp.setCanvasLocation(x, y);
		wlp.setDefaultSize(view);
		wlp.setZoom(WidgetCanvasViewPort.Zoom);
		addView(view, wlp);
		requestLayout();
		view.setLayoutParams(wlp);
		//this.measureChild(view, this.getMeasuredWidth() | MeasureSpec.AT_MOST, this.getMeasuredHeight() | MeasureSpec.AT_MOST);
		invalidate();
	}
*/	
	public void addWidgetView(WidgetView view){
		addView(view, view.getWidgtViewLayoutParams());
		requestLayout();
		invalidate();
	}
	public void onWidgetClick(WidgetView widgetView, Widget widget)
	{
		
		mWorkspace.onClick(widgetView);
		
	}
	public boolean onWidgetLongClick(WidgetView widgetView, Widget widget)
	{
		mWorkspace.onLongClick(widgetView);
		return false;
	}
	@Override
	public String toString(){
		return "Widget Canvas";
	}
	public int getCanvasWidth(){
		return CanvasSizeX;
	}
	public int getCanvasHeight(){
		return CanvasSizeY;
	}
	
	//Drag
	public boolean acceptDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		if (source instanceof WidgetView){
			return true;
		}
		return false;
	}
	public void onDragEnter(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		// TODO Auto-generated method stub
		
	}
	public void onDragExit(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		// TODO Auto-generated method stub
		
	}
	public void onDragOver(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		// TODO Auto-generated method stub
		
	}
	public void onDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		if (!(source instanceof WidgetView)){
			return;
		}
		WidgetView widgetView = (WidgetView) source;
		WidgetCanvas.LayoutParams lp = (LayoutParams) widgetView.getLayoutParams();
		lp.setCanvasLocation(x, y);
		invalidate();
		
	}
	public void onDropCompleted(View target, boolean success)
	{
		// TODO Auto-generated method stub
		
	}
	public void setDragger(DragController dragger)
	{
		// TODO Auto-generated method stub
		
	}
	
	//Layout Parameters For Individual Entities
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
	{
		return p instanceof WidgetCanvas.LayoutParams;
	}
	/*
	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs)
	{
		return new WidgetCanvas.LayoutParams(getContext(), attrs);
	}
	*/
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
	{
		return new WidgetCanvas.LayoutParams(p);
	}
	public static class LayoutParams extends ViewGroup.LayoutParams{

		//entity is only aware of its scale, not the zoom
		float Zoom = 1.0f;	//set to normal scale initially
		
		int Default_width;
		int Default_height;
	
		int X;
		int Y;
		
		WidgetCanvas.WidgetCanvasViewPort ParentViewPort;
		
		public boolean IsDragging;
		
		public LayoutParams(Context c, AttributeSet attrs)
		{
			super(c, attrs);
		}

		public LayoutParams(ViewGroup.LayoutParams source)
		{
			super(source);
		}
		
		public LayoutParams(int x, int y, float zoom, WidgetView wv){
			super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			setCanvasLocation(x, y);
			setDefaultSize(wv);
			setZoom (zoom);
		}
		
		public void setDefaultSize(WidgetView wv){
			
			Default_width = wv.getDefaultWidth();
			Default_height = wv.getDefaultHeight();
			width = (int) (Default_width * Zoom);
			height = (int) (Default_height * Zoom);
			
		}
		
		public void setCanvasLocation(int x, int y){
			X = x;
			Y = y;
		}
		
		public void setZoom (float zoom){
			Zoom = zoom;
			width = (int) (Default_width * Zoom);
			height = (int) (Default_height * Zoom);
				
		}
		
	}
	public float getViewPortX(){
		return WidgetCanvasViewPort.X;
	}
	public float getViewPortY(){
		return WidgetCanvasViewPort.Y;
	}
	
	public static class WidgetCanvasViewPort{	
		static int Width = 100;
		static int Height = 100;
		
		static int X = 0;
		static int Y = 0;
		
		static int MaxX;
		static int MaxY;
		
		public static float Zoom = GpiConstants.DEFAULT_ZOOM;
		
		public static void setSize(int width, int height){
			Width = width;
			Height = height;
		}
		public static void setLocation(int x, int y){
			X = x;
			Y = y;
		}
		public static void setSafeZoom(float zoom){
			if (zoom < GpiConstants.MIN_ZOOM){
				Zoom = GpiConstants.MIN_ZOOM;
			}
			else if (zoom > GpiConstants.MAX_ZOOM){
				Zoom = GpiConstants.MAX_ZOOM;
			}
			else {
				Zoom = zoom;
			}
		}
		public static void setSafeLocation(int x, int y){
			if (x < 0){
				X = 0;
			}
			else if (x > MaxX){
				X = MaxX;
			}
			else{
				X = x;
			}
			
			if (y < 0){
				Y = 0;
			}
			else if (y > MaxY){
				Y = MaxY;
			}
			else{
				Y = y;
			}
		}
		public static void calculateMaxSize(int sizeX, int sizeY){
			if (sizeX - Width < 0){
				MaxX = sizeX;
			}
			else{
				MaxX = sizeX - Width;
			}
			if (sizeY - Height < 0){
				MaxY = sizeY;
			}
			else{
				MaxY = sizeY - Height;
			}
		}
		public static void move (int deltaX, int deltaY){
			if ((X + deltaX >= 0) && (X + deltaX <= MaxX)){
				X += deltaX;
			}
			else{
				if (X + deltaX < 0){
					X = 0;
				}
				else{
					X = MaxX;
				}
			}
			if ((Y + deltaY > 0) && (Y + deltaY < MaxY)){
				Y += deltaY;
			}
			else{
				if (Y + deltaY < 0){
					Y = 0;
				}
				else{
					Y = MaxY;
				}
			}
		}
		public static void update (int canvasWidth, int canvasHeight){
			calculateMaxSize((int)Zoom * canvasWidth, (int)Zoom * canvasHeight);
			setSafeLocation(X, Y);
		}
	}


	//Message EndPoint
	public void ReceiveMessage(MESSAGE_TYPE messageType,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		// TODO Receive Message
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		mWorkspace.MessageToRouter(messageType, directed, from, to, Data);
	}
	public void setMessageRouter(MessageRouter router)
	{
		//mWorkspace is the router
		
	}
	

}
