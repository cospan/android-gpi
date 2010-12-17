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

package com.cospandesign.android.gpi.workspace.controlcanvas;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.ConnectionPointView;
import com.cospandesign.android.gpi.workspace.DeleteZone;
import com.cospandesign.android.gpi.workspace.DragController;
import com.cospandesign.android.gpi.workspace.DragSource;
import com.cospandesign.android.gpi.workspace.DropTarget;
import com.cospandesign.android.gpi.workspace.ViewPort;
import com.cospandesign.android.gpi.workspace.WorkspaceActivity;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity.ConnectionPath;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetViewGroup;
import com.cospandesign.gpi.R;

public class ControlCanvas extends ViewGroup implements DropTarget, DragSource, MessageEndPoint 
{


	Context mActivityContext;
	DragController mDragger;
	WorkspaceActivity mWorkspace;
	//WorkspaceEntity mWse;
	DeleteZone mDeleteZone;
	ViewPort mViewPort;
	//ActionView mOptionsView;
	//StatusView mStatusView;
	WidgetCanvas mWidgetCanvas;
	//ScrollView mScroller;
	final GpiConsole mConsole = GpiConsole.getinstance(); 
	
	ArrayList<WorkspaceEntity.ConnectionPath> ConnectionPaths;
	ArrayList<Path> Paths;
	
	//Drawing stuff
	final Rect mSizeRect = new Rect();
	Paint mPaint;
	
	Paint mShadowPaint;
	ArrayList <Path> mShadowConnections;
	//Gravity mGravity;
	
	//Scrolling
	enum TOUCH_STATE{
		REST,
		SCROLLING
	}
	TOUCH_STATE mTouchState = TOUCH_STATE.REST;
	
	int ConnectionSpacing = 10;
	
	/*
	 * There are really two views the "View Port" and the "Canvas"
	 * 
	 * The Canvas is the entire grid of items (Visible or not), but the Canvas can
	 * be larger than the view, so I need to keep track of the location of where
	 * we the View Port is within the canvas
	 * 
	 */
	
	//private LayoutInflater mInflator;
	
	/*
	 * to simplify things for the developer, The developer only needs to say how
	 * many cells the canvas is in the X, and the Y, the pixel calculation will be
	 * done within the setupCanvas() function
	 */
	int mCanvasCellsX;
	int mCanvasCellsY;
	
	//Constructor
	public ControlCanvas(Context context, AttributeSet attrs){
		super(context, attrs);
//Check to make sure this is the activity context
		mActivityContext = context;
		//mEntitySupervisor = new EntitySupervisor(this);
	
		//mVisibleCellsX = attrs.getAttributeResourceValue(com.cospandesign.ucs.R.raw.VisibleCellsX, 5);
		//mVisibleCellsY = attrs.getAttributeResourcValue(com.cospandesign.ucs.R.raw.VisibleCellsY, 5);
		
		
		
		//ViewPort		
		int cellSizeX = attrs.getAttributeResourceValue(R.dimen.CellSizeX, 48);
		int cellSizeY = attrs.getAttributeResourceValue(R.dimen.CellSizeY, 48);
		
		int paddingX = attrs.getAttributeResourceValue(R.dimen.XAxisPadding, 10);
		int paddingY = attrs.getAttributeResourceValue(R.dimen.YAxisPadding, 10);
		
		mViewPort = new ViewPort(cellSizeX, cellSizeY, paddingX, paddingY);
		//mEntitySupervisor.setViewPort(mViewPort);
		int xCells = attrs.getAttributeResourceValue(R.raw.CanvasXCells, 50);
		int yCells = attrs.getAttributeResourceValue(R.raw.CanvasYCells, 50);
		
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2.0f);
		
		
		//mGravity = new Gravity();
		setCanvasCellsXY(xCells, yCells);
		
		ConnectionPaths = new ArrayList<ConnectionPath>();
		Paths = new ArrayList<Path>();

		mShadowPaint = new Paint();
		mShadowPaint.setStyle(Paint.Style.STROKE);
		mShadowPaint.setStrokeWidth(2.0f);
		mShadowPaint.setColor(Color.DKGRAY);
		
		this.mShadowConnections = new ArrayList<Path>();
		
	}

	//Functions
	private void setCanvasCellsXY(int xCells, int yCells){
		mCanvasCellsX = xCells;
		mCanvasCellsY = yCells;
		
		//Viewport is modified because of the change in X, and Y cells
		mViewPort.calculateMaxDimensions(mCanvasCellsX, mCanvasCellsY);
		
		
		int xSize = (int) (mCanvasCellsX * mViewPort.mCellSizeX + ((mCanvasCellsX + 1) * mViewPort.mPaddingX));
		int ySize = (int) (mCanvasCellsY * mViewPort.mCellSizeY + ((mCanvasCellsY + 1) * mViewPort.mPaddingY));
		mSizeRect.set(0, 0, xSize, ySize);
		//mOccupied = new boolean[mCanvasCellsX][mCanvasCellsY];
		
		//tell the WidgetCanvas how big it is
	}
	public void setWorkspace (WorkspaceActivity workspace){
		mWorkspace = workspace;
	}
	/*
	public void setDeleteZone (DeleteZone deleteZone){
		mDeleteZone = deleteZone;
	}

	public void setScrollLayer (ScrollView scrollView){
		mScroller = scrollView;
	}	
	
 	public void setOptionsView (ActionView optionsView){
		mOptionsView = optionsView;
	}
	public void setStatusView (StatusView statusView){
		mStatusView = statusView;
	}

	public void addActionView(ActionView actionView){
		addView(actionView);
	}
*/
	private int[] findCellByLocation(int Xoffset, int Yoffset){
		int [] cellLocationXY = {0, 0};
		
		
		int x = (int) mViewPort.x + Xoffset;
		int y = (int) mViewPort.y + Yoffset;
		
		int MaxX = mSizeRect.width();
		int MaxY = mSizeRect.height();
		
		boolean xFound = false;
		boolean yFound = false;
		
		/*
		 *if we are below the first padding threshold, 
		 *or after the last padding threshold return cell closest 
		 */
		
		if (x <= mViewPort.mPaddingX/2){
			cellLocationXY[0] = 0;
			xFound = true;
		}
		if (y <= mViewPort.mPaddingY/2){
			cellLocationXY[1] = 0;
			yFound = true;
		}
		
		if (x >= MaxX - mViewPort.mPaddingX/2){
			cellLocationXY[0] = mCanvasCellsX - 1;
			xFound = true;
		}
		if (y >= MaxY - mViewPort.mPaddingY/2){
			cellLocationXY[1] = mCanvasCellsY - 1;
			yFound = true;
		}
		
		//if we are not an extreme case then we can continue
		if (xFound == false){
			//subtract off the padding at the beginning of the X
			x -= mViewPort.mPaddingX/2;
			cellLocationXY[0] = (int)(x/(mViewPort.mCellSizeX + mViewPort.mPaddingX)); //kill the remainder, just need the whole number
		}
		if (yFound == false){
			//subtract off the padding a the beginning of Y
			y -= mViewPort.mPaddingY;
			cellLocationXY[1] = (int)(y/(mViewPort.mCellSizeY + mViewPort.mPaddingY));
		}


		return cellLocationXY;
	}
	public void setWidgetCanvas(WidgetCanvas widgetCanvas){
		mWidgetCanvas = widgetCanvas;
	}
	public void setupListener(){
		setOnClickListener(mWorkspace);
	}
	@Override
	public String toString(){
		return "Control Canvas";
	}
	public void setupIOLabels(WorkspaceEntity wse){
		//get all the input views
		ArrayList<ConnectionPointView> incpv = wse.getInputConnectionList();
		//get all the output views
		ArrayList<ConnectionPointView> outcpv = wse.getOutputConnectionList();
		
		//get the location of the current WSE
		int X = wse.getLocation()[0];
		int Y = wse.getLocation()[1];
		int CenterY = Y + wse.getMeasuredHeight()/2;
		int textInputHeight = 0;
		int textOutputHeight = 0;
		if (incpv.size() > 0){
			textInputHeight = incpv.get(0).getMeasuredHeight();
			CenterY = Y + wse.getMeasuredHeight()/2 - textInputHeight/2;
		}
		if (outcpv.size() > 0){
			textOutputHeight = outcpv.get(0).getMeasuredHeight();
			CenterY = Y + wse.getMeasuredHeight()/2 - textOutputHeight/2;
		}
		
		
		
		int YInputStart = CenterY;
		if (incpv.size() > 1){
			YInputStart = CenterY - (((incpv.size() - 1) * (textInputHeight + ConnectionSpacing)) + ConnectionSpacing / 2);
		}
		int YOutputStart = CenterY;
		if (outcpv.size() > 1){
			YOutputStart = CenterY - (((outcpv.size() - 1) * (textInputHeight + ConnectionSpacing)) + ConnectionSpacing / 2);
		}
		
		//Input
		//Since we want to set the length to be the farthest point of the farthest channel, except when we go negative, we ask if the next channel is larger than the last
		//Get X
		int InputX = X;
		int InputMaxWidth =	0;

		int OutputX = X;
		int OutputMaxWidth = 0;
		
		//Get the maximum widths for both the input and output
		for (ConnectionPointView cpv : incpv){
			if (cpv.getMeasuredWidth() > InputMaxWidth){
				InputMaxWidth = cpv.getMeasuredWidth();
			}
		
		}
		
		for (ConnectionPointView cpv : outcpv){
			if (cpv.getMeasuredWidth() > OutputMaxWidth){
				OutputMaxWidth = cpv.getMeasuredWidth();
			}
		}
		
		//If the input is before 0 shift over the X value to accommodate it
		if (X - InputMaxWidth - ConnectionSpacing < 0)
		{
			X = (-(X - InputMaxWidth - ConnectionSpacing) + wse.getMeasuredWidth());
		}
		//If the X output is after the end, shift over the X so that it can accommodate it
		else if ( (X + wse.getWidth() + OutputMaxWidth + ConnectionSpacing) > (mCanvasCellsX * mViewPort.mCellSizeX)){
			X = X - (int)((mCanvasCellsX * mViewPort.mCellSizeX) - (X + wse.getMeasuredWidth() + OutputMaxWidth + ConnectionSpacing));
		}
		
		InputX = X - InputMaxWidth - ConnectionSpacing;
		OutputX = X + wse.getMeasuredWidth() + ConnectionSpacing;
		
		//if the first Y value is above the screen, shift down the Y value
		if (YInputStart < 0){
			YInputStart = 0;
		}
		else if (YInputStart + (incpv.size() * (textInputHeight + ConnectionSpacing)) > (mCanvasCellsY * mViewPort.mCellSizeY)){
			YInputStart -= (mCanvasCellsY * mViewPort.mCellSizeY) - (YInputStart + (incpv.size()) * (textInputHeight + ConnectionSpacing));
		}
		
		if (YOutputStart < 0){
			YOutputStart = 0;
		}
		else if (YOutputStart + (outcpv.size() * (textOutputHeight + ConnectionSpacing)) > (mCanvasCellsY * mViewPort.mCellSizeY)){
			YOutputStart -= (mCanvasCellsY * mViewPort.mCellSizeY) - (YOutputStart + (outcpv.size()) * (textOutputHeight + ConnectionSpacing));
		}
		
		for (int i  = 0; i < incpv.size(); i++){
			ConnectionPointView cpv = incpv.get(i);
			
			ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams)cpv.getLayoutParams();

			int y = YInputStart + (i *(textInputHeight + ConnectionSpacing));
			
			lp.setLocation(InputX, y);
			lp.setAttachPoint(InputX + cpv.getMeasuredWidth(), y + cpv.getMeasuredHeight()/2, wse.getLeft(), wse.getTop() + wse.getMeasuredHeight()/2);
			cpv.setSelected(false);
			lp.setLabelVisibility(true);
			this.addView(cpv);
			//cpv.measure(this.mSizeRect.width() | MeasureSpec.AT_MOST, this.mSizeRect.height() | MeasureSpec.AT_MOST);
			
		}
		for (int i = 0; i < outcpv.size(); i++){
			ConnectionPointView cpv = outcpv.get(i);
			
			ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams)cpv.getLayoutParams();
			
			int y = YOutputStart + (i *(textOutputHeight + ConnectionSpacing));
			lp.setLocation(OutputX, y);
			lp.setAttachPoint(OutputX, y + cpv.getMeasuredHeight()/2, wse.getLeft() + wse.getMeasuredWidth(), wse.getTop() + wse.getMeasuredHeight()/2);
			cpv.setSelected(false);
			lp.setLabelVisibility(true);
			this.addView(cpv);
			//cpv.measure(this.mSizeRect.width() | MeasureSpec.AT_MOST, this.mSizeRect.height() | MeasureSpec.AT_MOST);
		}
	
		invalidate();
		
	}
	public void removeIOLabels(){
		int index = 0;
		while (index != -1){
		
			index = -1;
			for (int i = 0; i < getChildCount(); i++)
			{
				if (getChildAt(i) instanceof ConnectionPointView){
					((ConnectLabelLayoutParams)((ConnectionPointView)getChildAt(i)).getLayoutParams()).setLabelVisibility(false);
					index = i;
					break;
				}
			}
			if (index != -1){
				removeViewAt(index);
			}
		}
	}
	public void showAllIO(){
		for (WorkspaceEntity wse: this.mWorkspace.mWses){
			if (wse.equals(mWorkspace.mCurrentWse)){
				continue;
			}
			setupIOLabels(wse);
		}
	}
	public void guiInitialization(){
		this.measure(0, 0);
		removeIOLabels();
		drawConnections();
	}
	public void drawConnections(){
		Paths.clear();
		
		Path p = null;
		for (WorkspaceEntity.ConnectionPath cp : ConnectionPaths){
			p = new Path();
			p.moveTo(cp.x1, cp.y1);
			p.lineTo(cp.x2, cp.y2);
			Paths.add(p);
		}
	}
	private void drawShadowConnections(View v, Widget widget){
		WidgetViewGroup widgetView = null;
		try {
			widgetView = widget.getWidgetView();
		} catch (Exception ex) {
			mConsole.error("Error Drawing shadow connections", ex);
		}
		
		if ((widgetView != null) && (widgetView.getVisibility() == VISIBLE)){
			//find the points of connection
			//float wcViewPortX = mWidgetCanvas.getViewPortX();
			//float wcViewPortY = mWidgetCanvas.getViewPortY();
			
			float wvLeft = widgetView.getLeft();
			float wvTop = widgetView.getTop();
			float wvRight = widgetView.getRight();
			float wvBottom = widgetView.getBottom();
			
			//Left/Top Corner
			Path p = new Path();
			p.moveTo(v.getLeft(), v.getTop());
			p.lineTo(wvLeft, wvTop);
			mShadowConnections.add(p);
			
			//Right/Top Corner
			p = new Path();
			p.moveTo(v.getRight(), v.getTop());
			p.lineTo(wvRight, wvTop);
			mShadowConnections.add(p);
			
			//Left/Bottom Corner
			p = new Path();
			p.moveTo(v.getLeft(), v.getBottom());
			p.lineTo(wvLeft, wvBottom);
			mShadowConnections.add(p);
			
			//Right/Bottom Corner
			p = new Path();
			p.moveTo(v.getRight(), v.getBottom());
			p.lineTo(wvRight, wvBottom);
			mShadowConnections.add(p);
			
		}
	}
	public int getCanvasWidth(){
		return mSizeRect.width();
	}
	public int getCanvasHeight(){
		return mSizeRect.height();
	}
	public ViewPort getViewPort(){
		return mViewPort;
	}
	
	//Dragging
	public void setDragger (DragController dragger){
		mDragger = dragger;
	}
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo){
		return true;
	}
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo){
		
	}
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo){
		
	}
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo){
		
	}
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo)
	{
		//Move is from an external source
		if (source != this){
			onDropExternal (x, y, dragInfo);
		
		}
		//move is internal
		else{
			if (dragInfo instanceof WorkspaceEntity){
				WorkspaceEntity wse = (WorkspaceEntity)dragInfo;
				//moving item
				int[] cellLocationXY = findCellByLocation(x, y);
				wse.setCellXY(cellLocationXY[0], cellLocationXY[1]);
			}
		}

		
	}
	public void onDropCompleted(View target, boolean success)
	{
		target.setVisibility(VISIBLE);
		invalidate();
	}
	private void onDropExternal(int x, int y, Object dragInfo){
		
		WorkspaceEntity wse = null;
		
		if (dragInfo instanceof Widget){
			Widget widgetRef = (Widget)dragInfo;
			Class ref = dragInfo.getClass();
			Object widget = null;
			//ref.getClass().cast(widget);
			//widget = new Widget();

			
			try {
				Class partypes[] = new Class[5];
				partypes[0] = String.class;
				partypes[1] = String.class;
				partypes[2] = Integer.class;
				partypes[3] = Context.class;
				partypes[4] = Boolean.TYPE;
				
				Constructor constructor = ref.getConstructor(partypes);
				if (constructor != null){
					Object arglist[] = new Object[5];
					arglist[0] = new String(widgetRef.getName());
					arglist[1] = new String(widgetRef.getInfo());
					arglist[2] = new Integer(widgetRef.getImage());
					arglist[3] = mActivityContext.getApplicationContext();//this.mActivityContext;
					arglist[4] = new Boolean(true);
					widget = constructor.newInstance(arglist);
				}
				else {
					mConsole.error("Widget Not using default constructor");
				}
				
			} catch (Exception ex) {
				mConsole.error("Error during external drop", ex);

			}
			
			//Widget widget = new Widget(ref.getName(), ref.getInfo(), ref.getImage(), this.getContext(), true);
			wse = new WorkspaceEntity (mActivityContext.getApplicationContext(), (Widget)widget, mViewPort, mWorkspace);
		}
		else {
			wse = new WorkspaceEntity (mActivityContext.getApplicationContext(), (Entity)dragInfo, mViewPort, mWorkspace);
		}
		
		
		//setup the location, and size of the entity
		int[] cellLocationXY = findCellByLocation(x, y);
		
		wse.mLayoutParams.CellX = cellLocationXY[0];
		wse.mLayoutParams.CellY = cellLocationXY[1];
		wse.mLayoutParams.CellHSpan = 1;
		wse.mLayoutParams.CellVSpan = 1;
		wse.mLayoutParams.isDragging = false;
		wse.mLayoutParams.setup(mViewPort);


		//Tell the Workspace that we are adding a new item
		mWorkspace.MessageToRouter(MessageRouter.MESSAGE_TYPE.ADD_WSE, MessageRouter.DIRECTED.DIRECTED, this, mWorkspace, wse);
		
		addView(wse, wse.mLayoutParams);
		//addView(mEntitySupervisor.addItem(wse), wse.layoutParams);
		//addView((WorkspaceEntity)dragInfo);
		
		requestLayout();
		invalidate();
	}

	//Scrolling
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		int deltaX = l - oldl;
		int deltaY = t - oldt;
		
		boolean xFinished = false;;
		boolean yFinished = false;
		
		//extreme cases
		if ((mViewPort.x + deltaX) < 0){
			mViewPort.x = 0;
			xFinished = true;
		}
		if ((mViewPort.x + deltaX) > mViewPort.mMaxX	){
			mViewPort.x = mViewPort.mMaxX;
			xFinished = true;
		}
		
		if ((mViewPort.y + deltaY) < 0){
			mViewPort.y = 0;
			yFinished = true;
		}
		if ((mViewPort.y + deltaY) > mViewPort.mMaxY){
			mViewPort.y = mViewPort.mMaxY;
			yFinished = true;
		}
		
		if (!xFinished){
			mViewPort.x = mViewPort.x + deltaX;
		}
		if (!yFinished){
			mViewPort.y = mViewPort.y + deltaY;
		}
		
		requestLayout();
		invalidate();
		
	}

	//Message End Point
	public void ReceiveMessage(MESSAGE_TYPE messageType,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		switch (messageType){
		case REMOVING_WSE:
			//Remove From View
			if (Data instanceof WorkspaceEntity){
				removeView((WorkspaceEntity)Data);
			}
			break;
		}
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		mWorkspace.MessageToRouter(messageType, directed, from, to, Data);
	}
	public void setMessageRouter(MessageRouter router)
	{
		// mWorkspace is the message router
	}
	
	//Drawing
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		//int visibleWidthMeasureSpec = widthMeasureSpec;
		//int visibleHeightMeasureSpec = this.mViewPort.VRect.height();
		
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mSizeRect.width(), MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(this.mSizeRect.height(), MeasureSpec.EXACTLY);
		//mWidgetCanvas.measure(widthMeasureSpec, heightMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		
		//int visibleWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		//int visibleHeightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		//int maxWidthSpec = widthSpecSize | MeasureSpec.AT_MOST;
		//int maxHeightSpec = heightSpecSize | MeasureSpec.AT_MOST;
		//int maxVisibleWidthSize = visibleWidthSize | MeasureSpec.AT_MOST;
		/*
		final DeleteZone deleteZone = mDeleteZone;
		deleteZone.measure(maxWidthSpec, maxHeightSpec);
		
		final ActionView optionsView = mOptionsView;
		optionsView.measure(maxWidthSpec, maxHeightSpec);
	*/
		//final StatusView statusView = mStatusView;
		//statusView.measure(maxWidthSpec - mOptionsView.getLayoutParams().width, maxHeightSpec);
		/*
		final ActionView manifestView = mManifestView;
		manifestView.measure(maxWidthSpec, maxHeightSpec);
		*/
		int count = getChildCount();
		for (int i = 0; i < count; i++){
			View child = getChildAt(i);
			
			if (!(child instanceof WorkspaceEntity))
			{
				//right now just worried about workspace entities
				if (child instanceof ConnectionPointView){
					child.measure(widthSpecSize | MeasureSpec.AT_MOST, heightSpecSize | MeasureSpec.AT_MOST);
				}
				/*
				if (child instanceof ActionView){
					child.measure(visibleWidthSize | MeasureSpec.AT_MOST, heightSpecSize | MeasureSpec.AT_MOST);
				}
				if (child instanceof StatusView){
					child.measure(maxVisibleWidthSize - mOptionsView.getLayoutParams().width, heightSpecSize | MeasureSpec.AT_MOST);
				}
				*/
				continue;
			}
			
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
			int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			
		}
		
		setMeasuredDimension(widthSpecSize, heightSpecSize);

	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		
		//set the size of the viewport.
		mViewPort.setSize(right - left, bottom - top);
		mViewPort.calculateMaxDimensions(mCanvasCellsX, mCanvasCellsY);
		
		ConnectionPaths.clear();
		
		final int count = getChildCount();
		for (int i = 0; i < count; i++){
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE){
				if (child instanceof WorkspaceEntity){
					Rect cellRect = ((WorkspaceEntity)child).getCellRect();
					child.layout(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom);
					ConnectionPaths.addAll(((WorkspaceEntity)child).updateConnections());
				}
				else{
					/*
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
					*/
					if (child instanceof ConnectionPointView){
						ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams)child.getLayoutParams();
						child.layout(lp.X, lp.Y, lp.X + child.getMeasuredWidth(), lp.Y + child.getMeasuredHeight());
					}
					else{
						child.layout(left, top, right, bottom);
							
					}
				}
			}
		}
		drawConnections();
		//mDeleteZone.bringToFront();
	}
	@Override
	protected void onDraw(Canvas canvas)
	{

		for (int i = 0; i < getChildCount(); i++){
			Path p = null;
			View v = getChildAt(i);
			if (v instanceof ConnectionPointView){
				p = new Path();
				ConnectionPointView cpv = (ConnectionPointView)v;
				ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams)cpv.getLayoutParams();
				p.moveTo(lp.AttachPointEntityX, lp.AttachPointEntityY);
				p.lineTo(lp.AttachPointLabelX, lp.AttachPointLableY);
				canvas.drawPath(p, mPaint);
			}
/*			
			if (v instanceof WorkspaceEntity){
				if (((WorkspaceEntity)v).getEntity() instanceof Widget){
					Widget widget = (Widget) ((WorkspaceEntity)v).getEntity();
					//get the location of the widgetView
					drawShadowConnections(v, widget);
					for (Path sp : mShadowConnections){
						canvas.drawPath(sp, mShadowPaint);
					}
				}
			}
*/			
		}
		
		for (Path p : Paths){
			canvas.drawPath(p, mPaint);
		}
		
		super.onDraw(canvas);
	}
		
	//Layout Parameters For Individual Entities
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
	{
		return p instanceof ControlCanvas.LayoutParams;
	}
	@Override
	protected ControlCanvas.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
	{
		return new ControlCanvas.LayoutParams(p);
	}
	public static class LayoutParams extends ScrollView.MarginLayoutParams{

		public int CellX;
		public int CellY;
		
		public int CellHSpan;
		public int CellVSpan;
		
		public boolean isDragging;
		

		
		public LayoutParams(Context c, AttributeSet attrs)
		{
			super(c, attrs);
			CellHSpan = 1;
			CellVSpan = 1;
		}
		public LayoutParams(ViewGroup.LayoutParams source)
		{
			super(source);
			CellHSpan = 1;
			CellVSpan = 1;
		}
		
		public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan, float zoom){
			super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			CellX = cellX;
			CellY = cellY;
			CellHSpan = cellHSpan;
			CellVSpan = cellVSpan;
		}
		public LayoutParams(){
			super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		public void setup(ViewPort vp){
			final int myCellHSpan = CellHSpan;
			final int myCellVSpan = CellVSpan;
			//final int myCellX = CellX;
			//final int myCellY = CellY;
			
			width = (int) (myCellHSpan * vp.mCellSizeX + ((myCellHSpan - 1) * vp.mPaddingX));
			height = (int) (myCellVSpan * vp.mCellSizeY + ((myCellVSpan - 1) * vp.mPaddingY));
			
		}


	}
	public static class ConnectLabelLayoutParams extends ViewGroup.MarginLayoutParams{

		//public boolean isConnection;
		public boolean isLabelVisible;
		public int PaddingX = 10;
		public int PaddingY = 10;
		public int X;
		public int Y;
		public int AttachPointEntityX = 0;
		public int AttachPointEntityY = 0;
		public int AttachPointLabelX = 0;
		public int AttachPointLableY = 0;
		
		
		public enum IO{
			INPUT,
			OUTPUT
		}
		IO mIO = IO.INPUT;
			
		public ConnectLabelLayoutParams()
		{
			super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			// TODO Auto-generated constructor stub
		}
		
		public void setLocation(int x, int y){
			//isConnection = true;
			X = x;
			Y = y;
		}
		
		public void setAttachPoint(int x1, int y1, int x2, int y2){
			AttachPointEntityX = x1;
			AttachPointEntityY = y1;
			
			AttachPointLabelX = x2;
			AttachPointLableY = y2;
		}
		public void setLabelVisibility(boolean visible){
			isLabelVisible = visible;
		}
		public boolean isLabelVisible(){
			return isLabelVisible;
		}
		public boolean isInput(){
			if (mIO == IO.INPUT){
				return true;
			}
			return false;
		}
		public void setIO(IO io){
			mIO = io;
		}
	}
}
