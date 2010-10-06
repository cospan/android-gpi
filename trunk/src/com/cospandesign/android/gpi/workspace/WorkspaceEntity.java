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
import java.util.Hashtable;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas.ConnectLabelLayoutParams;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetView;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas.WidgetCanvasViewPort;
import com.cospandesign.gpi.R;

public class WorkspaceEntity extends View implements DropTarget, MessageEndPoint
{

	public ControlCanvas.LayoutParams mLayoutParams;
	public WidgetCanvas.LayoutParams mWidgetCanvasLayoutParams;
	ViewPort mViewPort;
	ControlCanvas mControlCanvas;
	WidgetCanvas mWidgetCanvas;
	WorkspaceActivity mWorkspace;
	
	
	int mTimeOut = 200; //200 millisecond timeout
	int mLongTimeOut = 5000;
	
	Context mContext;
	private Entity mEntity;
	
	//Icon will always be the same
	Bitmap mIcon;
	Rect mIconRect;
	
	//Widget View
	WidgetView mWidgetView;
	
	//Temporary Bitmap Image for dragging
	Bitmap mDropBitmap;
	CustomDrawableView mCustomDrawableLayer;
	
	//States
	public static final int IDLE = 0;
	public static final int SELECTED = 1;
	public static final int INPUT_GOOD = 2;
	public static final int INPUT_WARNING = 3;
	public static final int INPUT_BAD = 4;
	public static final int OUTPUT_GOOD = 5;
	public static final int OUTPUT_WARNING = 6;
	public static final int OUTPUT_BAD = 7;
	public static final int STATUS_OK = 8;
	public static final int WARNING = 9;
	public static final int ERROR = 10;
	public static final int NEW_DATA = 11;
	public static final int DATA_STATUS_LAYER = 12;
	
	Hashtable<String, ConnectionPointView> mInputViews;
	Hashtable<String, ConnectionPointView> mOutputViews;
	
	LevelListDrawable mStatus;
	int mState = IDLE;
	
	boolean mServiceMode = false;
	
	private Rect mCellRect;
	
	int X;
	int Y;

	//Constructor
	public WorkspaceEntity(Context context, Entity e, ViewPort viewPort, WorkspaceActivity workspace)
	{
		super (context);
		mContext = context;

		mEntity = e;
		mLayoutParams = new ControlCanvas.LayoutParams(0, 0, 1, 1, 1.0f);
		mCellRect = new Rect();
		mIconRect = new Rect();
		
		guiInitialization(viewPort, workspace);
		
	}
	public WorkspaceEntity(Context context, Entity e){
		super (context);
		mContext = context;
		
		mEntity = e;
		mLayoutParams = new ControlCanvas.LayoutParams(0, 0, 1, 1, 1.0f);
		mCellRect = new Rect();
		mIconRect = new Rect();
	}

	//Draw
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom)
	{
		//in ServiceMode there is no drawing
		if (mServiceMode){
			//shouldn't be drawing, but it may get caught in the middle of a transition
			return;
		}
		
		//changed is only different if the values are different
		/*
		 *  the values given are the values of the viewport 
		 *  relative to the canvas, so instead of left being 
		 *  at 0 it may be at whereever the viewport is
		 */
		updateLayout();
	}
	@Override
	public void setLayoutParams(LayoutParams params)
	{
		super.setLayoutParams(params);
		mLayoutParams = (ControlCanvas.LayoutParams)params;
	}
	@Override
	protected void onDraw(Canvas canvas)
	{
		//in ServiceMode there is no drawing
		if (mServiceMode){
			//shouldn't be drawing, but it may get caught in the middle of a transition
			return;
		}
		if (mLayoutParams.isDragging){
			//let the dragger handle this
			return;
		}
		
		Rect vr = new Rect((int)mViewPort.x, (int)mViewPort.y, (int)(mViewPort.x + mViewPort.mSizeX), (int)(mViewPort.y + mViewPort.mSizeY));
		if (!mCellRect.intersect(vr)){
			return;
		}
		 
		Rect r = new Rect(0, 0, mCellRect.width(), mCellRect.height());
		canvas.drawBitmap(mIcon, null, r, null);

	}
	private void updateLayout()
	{
		X = (int)((mLayoutParams.CellX * mViewPort.mCellSizeX) + ((mLayoutParams.CellX + 1) * mViewPort.mPaddingX));
		Y = (int)((mLayoutParams.CellY * mViewPort.mCellSizeY) + ((mLayoutParams.CellY + 1) * mViewPort.mPaddingY));
		mCellRect.set(X, Y,(int) (X + mViewPort.mCellSizeX),(int) (Y + mViewPort.mCellSizeY));
		
		mLayoutParams.width = (int)mViewPort.mCellSizeX;
		mLayoutParams.height = (int)mViewPort.mCellSizeY;
	
	}
	public ArrayList<ConnectionPath> updateConnections(){
		
		ArrayList<ConnectionPath> ConnectionPaths = new ArrayList<ConnectionPath>();
		
		ConnectionPath cp;
		
		for (String outputName : mOutputViews.keySet()){
			//go through all the connection of the connection point view
			ConnectionPointView cpv = mOutputViews.get(outputName);
			if (cpv.isConnected()){
				int[] location = cpv.getConnectionPoint();
				if (!cpv.isConnectionPointVisible()){
					location[0] = this.X + this.getMeasuredWidth();
					location[1] = this.Y + this.getMeasuredHeight()/2;
				}
				
				for (Entity entity: cpv.getEntity().getAllConnectionsForOutputChannel(cpv.getText().toString()).keySet()){
					cp = new ConnectionPath();
					cp.x1 = location[0];
					cp.y1 = location[1];
					//check if cpv is connection to control canvas... if its visible
					cp.OutputName = outputName;
					for (String name : entity.getInputDataKeySet()){
						cp.InputName = name;
						ConnectionPointView icpv = entity.getWorkspaceEntity().getInputConnectionViews().get(name);
						if (icpv == null){
							continue;
						}
						int[] inputLocation = icpv.getConnectionPoint();
						if (!icpv.isConnectionPointVisible())	{
							inputLocation[0] = entity.getWorkspaceEntity().X;
							inputLocation[1] = entity.getWorkspaceEntity().Y + entity.getWorkspaceEntity().getMeasuredHeight()/2;
						}
						cp.x2 = inputLocation[0];
						cp.y2 = inputLocation[1];
						ConnectionPaths.add(cp);

					}					
				}
			}
		}
		return ConnectionPaths;
	}
	public int[] getLocation(){
		updateLayout();
		int[] location = {X, Y};
		return location;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//in ServiceMode there is no drawing
		if (mServiceMode){
			//shouldn't be drawing, but it may get caught in the middle of a transition
			return;
		}
		
		//int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		//int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		//int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		//int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		
		updateLayout();
		
		setMeasuredDimension(mCellRect.width(), mCellRect.height());
	}

	public ArrayList<ConnectionPointView> getInputConnectionList(){
		
		ArrayList<ConnectionPointView> cpv = new ArrayList<ConnectionPointView>();
		
		for (String name: mInputViews.keySet()){
			cpv.add(mInputViews.get(name));
		}
		return cpv;
	}
	public ArrayList<ConnectionPointView> getOutputConnectionList(){
		ArrayList<ConnectionPointView> cpv = new ArrayList<ConnectionPointView>();
		
		for (String name : mOutputViews.keySet()){
			cpv.add(mOutputViews.get(name));
		}
		return cpv;
	}
	public Class getClassFromConnection(ConnectionPointView cpv){
		
		if (cpv.isInput()){
			return mEntity.getInputDataType((String)cpv.getText());
		}
		return mEntity.getOutputDataType((String)cpv.getText());
		
	}
	
	//Functions
	public Entity getEntity()
	{
		return mEntity;
	}
	public ControlCanvas getControlCanvas() {
		return mControlCanvas;
	}
	public WidgetCanvas getWidgetCanvas() {
		return mWidgetCanvas;
	}
	public boolean equals(Entity o)
	{
		if (mEntity.equals(o))
		{
			return true;
		}
		return false;
	}
	public void setStatus (int state){
		
		
		switch (state){
		case IDLE:
			//call toIdle
			//toIdle(mState);
			break;
		case SELECTED:
			//call toSelected
			//toSelected(mState);
			break;
		case INPUT_GOOD:
		case INPUT_WARNING:
		case INPUT_BAD:
			//toInputState(mState, state);
			break;
		case OUTPUT_GOOD:
		case OUTPUT_WARNING:
		case OUTPUT_BAD:
			//toOutputState(mState, state);
			break;
		case STATUS_OK:
			mStatus.setLevel(DATA_STATUS_LAYER);
			mCustomDrawableLayer.setColor(Color.GREEN);
			((TransitionDrawable)mStatus.getCurrent()).startTransition(mTimeOut);
			break;
		case WARNING:
			mStatus.setLevel(DATA_STATUS_LAYER);
			mCustomDrawableLayer.setColor(Color.YELLOW);
			((TransitionDrawable)mStatus.getCurrent()).startTransition(mTimeOut);
			break;
		case ERROR:
			mStatus.setLevel(DATA_STATUS_LAYER);
			mCustomDrawableLayer.setColor(Color.RED);
			((TransitionDrawable)mStatus.getCurrent()).startTransition(mTimeOut);
			break;
		case NEW_DATA:
			mStatus.setLevel(DATA_STATUS_LAYER);
			mCustomDrawableLayer.setColor(Color.WHITE);
			
			
			break;
		}
		mStatus.setLevel(state);
		mState = state;
	}
	public int getState (){
		return mStatus.getLevel();
	}
	public void NotifyRemoval(){
		//Called when Workspace Entity is about to be removed from the Canvas List
		//Remove the widget From the widget canvas
		//TODO remove WidgetView from WidgetCanvas
		mEntity.notifyRemoval();
	}
	@Override
	public String toString(){
		return mEntity.getName();
	}
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Entity){
			return getEntity().equals(o);
		}
		return super.equals(o);
	}

	//Visual Functions
	public void guiInitialization(ViewPort viewPort, WorkspaceActivity workspace){

		mServiceMode = false;
		
		mWorkspace = workspace;
		mViewPort = viewPort;
		
		mControlCanvas = mWorkspace.getControlCanvas();
		mWidgetCanvas = mWorkspace.getWidgetCanvas();
		
		setOnClickListener(mWorkspace);
		setOnLongClickListener(mWorkspace);
		
		setupView();
		//tell the hosted entity to go to guiMode now
		mEntity.guiInitialization(this);
	}
	private void setupView(){
		
		//set up:
		//restore all location and size information
		//control canvas views
		//widget views
		//connection point views
		
		mIcon = BitmapFactory.decodeResource(mContext.getResources(), mEntity.getImage());
		mIconRect.set(0, 0, mIcon.getWidth(), mIcon.getHeight());
		
		mStatus = new LevelListDrawable();
		
		BitmapDrawable bd = null;
		
//TODO Change to drawing on top of the icons as apposed to drawing on the background
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.transparent_background));
		mStatus.addLevel(IDLE, IDLE, bd);
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.blue_outline));
		mStatus.addLevel(SELECTED, SELECTED, bd);
		
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.green_input));
		mStatus.addLevel(INPUT_GOOD, INPUT_GOOD, bd);
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.yellow_input));
		mStatus.addLevel(INPUT_WARNING, INPUT_WARNING, bd);
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.red_input));
		mStatus.addLevel(INPUT_BAD, INPUT_BAD, bd);
		
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.green_output));
		mStatus.addLevel(OUTPUT_GOOD, OUTPUT_GOOD, bd);
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.yellow_output));
		mStatus.addLevel(OUTPUT_WARNING, OUTPUT_WARNING, bd);
		bd = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.red_output));
		mStatus.addLevel(OUTPUT_BAD, OUTPUT_BAD, bd);
		mCustomDrawableLayer = new CustomDrawableView();
		mStatus.addLevel(DATA_STATUS_LAYER, DATA_STATUS_LAYER, mCustomDrawableLayer);
		
		mStatus.setLevel(IDLE);
		
		setBackgroundDrawable(mStatus);
		
		mInputViews = new Hashtable<String, ConnectionPointView>();
		mOutputViews = new Hashtable<String, ConnectionPointView>();
		

		Set<String>InputDataChannelNames = mEntity.getInputDataKeySet();
		Set<String>OutputDataChannelNames = mEntity.getOutputDataKeySet();
	
		for (String channel : InputDataChannelNames){
			ConnectionPointView cpv = new ConnectionPointView(this.getContext(), this, channel, ControlCanvas.ConnectLabelLayoutParams.IO.INPUT, mEntity.getInputDataType(channel));
			//ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams) new ControlCanvas.ConnectLabelLayoutParams();
			//cpv.setLayoutParams(lp);
			ControlCanvas.ConnectLabelLayoutParams lp = (ConnectLabelLayoutParams) cpv.getLayoutParams();
			//cpv.measure(this.mControlCanvas.getMeasuredWidth() | MeasureSpec.AT_MOST, this.mControlCanvas.getMeasuredHeight() | MeasureSpec.AT_MOST);
			//cpv.measure(mViewPort.mSizeX | MeasureSpec.AT_MOST, mViewPort.mSizeY | MeasureSpec.AT_MOST);
			cpv.measure(this.mControlCanvas.getMeasuredWidth() | MeasureSpec.AT_MOST, this.mControlCanvas.getMeasuredHeight() | MeasureSpec.AT_MOST);
			mInputViews.put(channel, cpv);
		}
		for (String channel : OutputDataChannelNames){
			ConnectionPointView cpv = new ConnectionPointView(this.getContext(), this, channel, ControlCanvas.ConnectLabelLayoutParams.IO.OUTPUT, mEntity.getOutputDataType(channel));
			//ControlCanvas.ConnectLabelLayoutParams lp = (ControlCanvas.ConnectLabelLayoutParams) new ControlCanvas.ConnectLabelLayoutParams();
			//cpv.setLayoutParams(lp);
			ControlCanvas.ConnectLabelLayoutParams lp = (ConnectLabelLayoutParams) cpv.getLayoutParams();
			//cpv.measure(mViewPort.mSizeX | MeasureSpec.AT_MOST, mViewPort.mSizeY | MeasureSpec.AT_MOST);
			cpv.measure(this.mControlCanvas.getMeasuredWidth() | MeasureSpec.AT_MOST, this.mControlCanvas.getMeasuredHeight() | MeasureSpec.AT_MOST);
			mOutputViews.put(channel, cpv);
		}
		
		if (mEntity instanceof Widget){
			mWidgetView = new WidgetView(mContext, this);
			mWidgetView.setWidgetCanvas(mWidgetCanvas);
			
			mWidgetView.setDefaultWidth(100);
			mWidgetView.setDefaultHeight(100);
		}
	
	}
	public void addWidgetCanvasView(WidgetView view, int x, int y){
		mWidgetCanvasLayoutParams  = new WidgetCanvas.LayoutParams(x, y, WidgetCanvas.WidgetCanvasViewPort.Zoom, view);
		mWidgetCanvasLayoutParams.setCanvasLocation(x, y);
		mWidgetCanvasLayoutParams.setDefaultSize(view);
		mWidgetCanvasLayoutParams.setZoom(WidgetCanvas.WidgetCanvasViewPort.Zoom);
		view.setLayoutParams(mWidgetCanvasLayoutParams);
		mWidgetCanvas.addWidgetView(view);
	}
	public void addWidgetCanvasView(WidgetView view){
		view.setLayoutParams(mWidgetCanvasLayoutParams);
		mWidgetCanvas.addWidgetView(view);
	}
	public void addWidgetView(View view){
		//TODO add widget view from here, not from the widget
		mWidgetView.addView(view);
	}
	public WidgetView getWidgetView(){
		return mWidgetView;
	}
	public void setDefaultWidgetViewDimensions(int width, int height){
		mWidgetView.setDefaultHeight(height);
		mWidgetView.setDefaultWidth(width);
		Widget widget = (Widget)mEntity;
	}
	
	public void setCellXY (int x, int y){

		mLayoutParams.CellX = x;
		mLayoutParams.CellY = y;

	}
	public Rect getCellRect()
	{
		return mCellRect;
	}	
	public Hashtable<String, ConnectionPointView> getInputConnectionViews(){
		return mInputViews;
	}
	public Hashtable<String, ConnectionPointView> getOutputConnectionViews(){
		return mOutputViews;
	}
	
	//Background Functions
	public void serviceInitialization(){
		mServiceMode = true;
		removeViews();
		//tell the hosted entity to go to service mode too
		mEntity.serviceInitialization();
	}
	private void removeViews(){
		//remove:
		//put all location/size information back
		//control canvas views
		mControlCanvas = null;
		//widget views
		mWidgetCanvas = null;
		//connection point views
		Set<String> keys = mInputViews.keySet();
		for (String key : keys){
			mInputViews.get(key).stop();
			mInputViews.remove(key);
		}
		keys = mOutputViews.keySet();
		for (String key : keys){
			mOutputViews.get(key).stop();
			mOutputViews.remove(key);
		}
		mStatus = null;
		mIcon = null;
		//kill the workspace
		mWorkspace = null;
		if (mEntity instanceof Widget){
			mWidgetView = null;
		}
		
	}
	public boolean isServiceMode(){
		return mServiceMode;
	}
	
	//DragTarget
	public boolean acceptDrop(DragSource source, int x, int y, int offsetX,
			int offsetY, Object dragInfo)
	{
		Entity ent;
		
		if (dragInfo instanceof WorkspaceEntity){
			ent = ((WorkspaceEntity) dragInfo).getEntity();
		}
		else{
			ent = ((Entity)dragInfo);
		}
		
		return mWorkspace.acceptDropTarget(this, ent);
		//return mEntitySupervisor.acceptDropTarget(this, ent) ;
	}
	public void onDragEnter(DragSource source, int x, int y, int offsetX,
			int offsetY, Object dragInfo)
	{
		//compare rectange intersections to see if we are on the input, or output side
		int targetStart = X;
		int targetEnd = X + this.getWidth();
		int targetMiddle = (targetStart + targetEnd)/2;
		
		Entity ent;
		
		if (dragInfo instanceof WorkspaceEntity){
			ent = ((WorkspaceEntity) dragInfo).getEntity();
		}
		else{
			ent = ((Entity)dragInfo);
		}
		mDropBitmap = BitmapFactory.decodeResource(mContext.getResources(), ent.getImage());
	
		int dropStart = x - offsetX;
		int dropEnd = dropStart + mDropBitmap.getWidth();
		int dropMiddle = (dropStart + dropEnd)/2;
		
		boolean input = ((dropMiddle - targetMiddle) < 0);
		
		mWorkspace.dragEnterTarget(this, ent, input);
		//mEntitySupervisor.dragEnterTarget(this, ent, input);
		
	}
	public void onDragExit(DragSource source, int x, int y, int offsetX,
			int offsetY, Object dragInfo)
	{
		Entity ent;
		
		if (dragInfo instanceof WorkspaceEntity){
			ent = ((WorkspaceEntity) dragInfo).getEntity();
		}
		else{
			ent = ((Entity)dragInfo);
		}
		
		mWorkspace.dragExitTarget(this, ent);
//		mEntitySupervisor.dragExitTarget(this, ent);
		mDropBitmap = null;
	}
	public void onDragOver(DragSource source, int x, int y, int offsetX,
			int offsetY, Object dragInfo)
	{
		//compare rectangle intersections to see if we are on the input, or output side
		int targetStart = X;
		int targetEnd = X + this.getWidth();
		int targetMiddle = (targetStart + targetEnd)/2;
		
		Entity ent;
		
		if (dragInfo instanceof WorkspaceEntity){
			ent = ((WorkspaceEntity) dragInfo).getEntity();
		}
		else{
			ent = ((Entity)dragInfo);
		}
		
		if (mDropBitmap == null){

			mDropBitmap = BitmapFactory.decodeResource(mContext.getResources(), ent.getImage());
		}
		
		int dropStart = x - offsetX;
		int dropEnd = dropStart + mDropBitmap.getWidth();
		int dropMiddle = (dropStart + dropEnd)/2;
		
		boolean input = ((dropMiddle - targetMiddle) < 0);
		
		mWorkspace.dragOverTarget(this, ent, input);
//		mEntitySupervisor.dragOverTarget(this, ent, input);
		
	}
	public void onDrop(DragSource source, int x, int y, int offsetX,
			int offsetY, Object dragInfo)
	{
		//mEntitySupervisor.drop(this, dragInfo);
		mWorkspace.drop(this, dragInfo);
		mDropBitmap = null;
	}
	
	//Status Changes
	/*
	public void toIdle(int prevState){
		//if selected remove the Info
		if (prevState == SELECTED){
			//remove info view
		}
	}
	public void toSelected(int prevState){
		//open up the info view
		
	}
	private void toInputState (int prevState, int newState){
		//show the input state
	}
	private void toOutputState (int prevState, int newState){
		//show the output state
	}
*/
	//Message Router
	public void ReceiveMessage(MESSAGE_TYPE messageType,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		// TODO Receive Message
		switch (messageType){
		case REMOVING_WSE:
				if (this.equals(Data)){
					//Called when Workspace Entity is about to be removed from the Canvas List
					//Remove the widget From the widget canvas
					//TODO remove WidgetView from WidgetCanvas
					mEntity.notifyRemoval();
				}
				else{
					mEntity.removeOutputListener(null, (Entity)Data, null);
				}
			break;
		}
		
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		mWorkspace.MessageToRouter(messageType, directed, this, to, Data);
		
	}
	public void setMessageRouter(MessageRouter router)
	{
		//mWorkspace is the router
		
	}
	
	/*
	private void BackgroundTimerTask(){
		setStatus(IDLE);
	}
	*/
	
	//Data Connection
	public void newOutputData(String channel){
		mOutputViews.get(channel).setState(ConnectionPointView.STATE.NEW_DATA);
	}
	public void newInputData(String channel){
		mInputViews.get(channel).setState(ConnectionPointView.STATE.NEW_DATA);
	}
	
	public class ConnectionPath{
		private String OutputName;
		private String InputName;
		
		public int x1 = 0;
		public int y1 = 0;
		public int x2 = 0;
		public int y2 = 0;
		
		public ConnectionPath(){
			OutputName = new String("");
			InputName = new String("");
		}
		
		public void setOutputName(String name){
			OutputName = new String(name);
		}
		public String getOutputName(){
			return OutputName;
		}
		
		public void setInputName(String name){
			InputName = new String(name);	
		}
		public String getInputName(){
			return InputName;
		}		
	}
	public class CustomDrawableView extends BitmapDrawable{

		int mColor = Color.WHITE;

		public void setColor(int color){
			mColor = color;
		}
		

		
		@Override
		public void draw(Canvas canvas)
		{
			canvas.drawColor(mColor);
			super.draw(canvas);
		}
		
	}

}