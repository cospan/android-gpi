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
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SlidingDrawer;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.connections.ConnectionAnalyzer;
import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.android.gpi.properties.PropertyManagerActivity;
import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetGrid;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetView;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas.LayoutParams;
import com.cospandesign.gpi.R;

public class WorkspaceActivity extends Activity implements DragController.DragListener, MessageRouter, MessageEndPoint, OnClickListener, OnLongClickListener
{
	
	//Workspace State
	
	
	Controller mGpiController;
	//Manage Workspace Entities
	public ArrayList<WorkspaceEntity> mWses;
	//TODO Implement workspace image views as an arrays as apposed to declare different ones all the time
	ArrayList<ActionView> mWavs;

	//Workspace States
	private boolean DRAGGING = false;
	private boolean WIDGET_CANVAS_VISIBLE = false;
	private boolean CONNETION_MODE = false;
	private boolean CONNECT_THROUGH_CANVAS = false;
	private boolean CONNECT_THROUGH_LIST = false;
//	private boolean WSE_SELECTED = false;
//	private boolean ME_SELECTED = false;

	
	public int WorkspaceStatus = 0;
	
	
	DragLayer mDragLayer;
	ControlCanvas mControlCanvas;
	SlidingDrawer mEntityDrawer;
	SlidingDrawer mWidgetDrawer;
	EntityGrid mEntityGrid;
	WidgetGrid mWidgetGrid;
	ActionView mHandle;
	ActionView mWidgetHandle;
	ActionView mDeleteZone;
	//ActionView mInfoView;
	ActionView mOptionsView;
	StatusView mStatusView;
	WidgetCanvas mWidgetCanvas;
	ViewPort mViewPort;
	ActionView mManifestView;
	ActionView mConnectCanvasView;
	ActionView mConnectListView;
	
	CanvasScrollView mWidgetCanvasScroller;
	CanvasScrollView mControlCanvasScroller;
	WorkspaceLayout mWorkspaceLayout;
	
	
	Context mContext;
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	
	public WorkspaceEntity mCurrentWse;
	ConnectionPointView mConnectionPointView;
	
	ConnectionAnalyzer mConnectionAnalyzer;
	
	
	//Different Menus
	Menu mWorkspaceMenu; //Control Canvas, nothing selected
	ArrayList<Integer> mMenuItems;

	//Transition Drawable
//	TransitionDrawable mHandleIcon;
	
	HashMap<String, Entity> mEntityMap;
	ArrayList<Entity> mEntityArray;
	ArrayList<Widget> mWidgets;
	
	
	//Initialization
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		
		//set up display
		try {
			setContentView(R.layout.workspace_layout);
		}
		catch (Exception ex){
			mGpiConsole.error(ex.getMessage());
		}

		//get context to modify menus, animations, etc...
		mContext = getApplication().getApplicationContext();
		
		//set up items
		mGpiController = (Controller)((GpiApp)getApplication()).getControlTree().getController();
		mWidgets = ((GpiApp)getApplication()).getWidgets();
		mEntityMap = new HashMap<String, Entity>();
		setupViews();
		mapData();
		
		mEntityGrid.setAdapter(new entityGridAdapter(this));
		mWidgetGrid.setAdapter(new widgetGridAdpater(this));
		//mWses = new ArrayList<WorkspaceEntity>();
		mWses = ((GpiApp)getApplication()).getActiveWses();
		mConnectionAnalyzer = new ConnectionAnalyzer();
		mCurrentWse = null;
		mConnectionPointView = null;
		
		mMenuItems = new ArrayList<Integer>();
		
		if (mWses.size() > 0){
			restoreWorkspaceEntities();
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mWorkspaceMenu = menu;
		mWorkspaceMenu.add(0, R.id.itemSave, Menu.NONE, "Save");
		mWorkspaceMenu.add(0, R.id.itemLoad, Menu.NONE, "Load");
		mWorkspaceMenu.add(0, R.id.itemDeviceActivity, Menu.NONE, "Device View");
		mWorkspaceMenu.add(0, R.id.itemWorkspaceOptions, Menu.NONE, "Options");
		//MenuInflater inflator = this.getMenuInflater();
		//inflator.inflate(R.menu.workspace_menu, mWorkspaceMenu);
		return true;
	}	
	private void mapData()
	{
		
		//The workspace only needs Mediums, Devices, and Widgets
		for (Medium medium: mGpiController.getMediums())
		{

			mEntityMap.put(medium.getName(), medium);
			
			for (Device device: medium.getDevices())
			{
				mEntityMap.put(device.getName(), device);
			}
		}
		mEntityArray = (ArrayList<Entity>) new ArrayList<Entity>(mEntityMap.values());

	}
	private void setupViews(){
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
		final DragLayer dragLayer = mDragLayer;
		
		mControlCanvasScroller = (CanvasScrollView) findViewById(R.id.ControlCanvasScrollView);
		final CanvasScrollView controlScroller = mControlCanvasScroller;
		
		mControlCanvas = (ControlCanvas) findViewById(R.id.ControlCanvasLayout);
		final ControlCanvas controlCanvas = mControlCanvas;
		
		mWorkspaceLayout = (WorkspaceLayout) findViewById(R.id.WorkspaceLayout);
		final WorkspaceLayout workspaceLayout = mWorkspaceLayout;
		
		mEntityDrawer = (SlidingDrawer) findViewById(R.id.EntityDrawer);
		final SlidingDrawer entityDrawer = mEntityDrawer; 
		
		mEntityGrid = (EntityGrid) findViewById(R.id.entity_drawer_content);
		final EntityGrid entityGrid = mEntityGrid;
		
		mHandle = (ActionView) findViewById(R.id.entity_handle);
		final ActionView handle = mHandle;
		
		mWidgetDrawer = (SlidingDrawer) findViewById(R.id.WidgetDrawer);
		final SlidingDrawer widgetDrawer = mWidgetDrawer;
		
		mWidgetGrid = (WidgetGrid) findViewById(R.id.widget_drawer_content);
		final WidgetGrid widgetGrid = mWidgetGrid;
		
		mWidgetCanvasScroller = (CanvasScrollView) findViewById(R.id.WidgetCanvasScrollView);
		final CanvasScrollView widgetScroller = mWidgetCanvasScroller;
		
		mWidgetHandle = (ActionView) findViewById(R.id.widget_handle);
		final ActionView widgetHandle = mWidgetHandle;
		
		mDeleteZone = (DeleteZone) findViewById(R.id.delete_zone);
		final DeleteZone deleteZone = (DeleteZone) mDeleteZone;
		
		//mManifestView = (ActionView) findViewById(R.id.manifest_view);
		//final ActionView manifestView = mManifestView;
		
		mOptionsView = (ActionView) findViewById(R.id.options_view);
		final ActionView optionsView = mOptionsView;
		
		mStatusView = (StatusView) findViewById(R.id.status_view);
		final StatusView statusView = mStatusView;

		mWidgetCanvas = (WidgetCanvas) findViewById(R.id.WidgetCanvas);
		final WidgetCanvas widgetCanvas = mWidgetCanvas;
		
		mManifestView = new ActionView(this);
		final ActionView manifestView = mManifestView;
		
		mConnectCanvasView = new ActionView(this);
		final ActionView connectCanvas = mConnectCanvasView;
		
		mConnectListView = new ActionView(this);
		final ActionView connectList = mConnectListView;
		
		ActionView.ActionViewLayoutParams avlp;
		
		//Setup deleteZone
		avlp = new ActionView.ActionViewLayoutParams(deleteZone.getLayoutParams());
		deleteZone.setupActionView(this, workspaceLayout, R.drawable.trash, R.string.trash_name);
		deleteZone.setupAnimations(R.anim.bottom_enter, R.anim.bottom_exit);
		avlp.DragVisible = true;
		deleteZone.setLayoutParams(avlp);
		deleteZone.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		deleteZone.setLocation(0,0);
		deleteZone.setInternalLayout(true);
		deleteZone.setUseGravity(true);
		
		//Setup Entity Grid
		entityGrid.setWorkspace(this);
		entityGrid.setDragger(dragLayer);
		
		//Setup Widget Grid
		widgetGrid.setWorkspace(this);
		widgetGrid.setDragger(dragLayer);
		
		//Setup ControlCanavas Scroller
		//LayoutParams svlp = controlScroller.getLayoutParams();
		//controlScroller.setFillViewport(true);
		
		workspaceLayout.setWorkspace(this);
		//workspaceLayout.setOptionsView(optionsView);
		//workspaceLayout.setDeleteView(deleteZone);
		//workspaceLayout.setWidgetCanvas(widgetCanvas);
		
		//Setup ControlCanvas
		controlCanvas.setWorkspace(this);
		//controlCanvas.setDeleteZone(deleteZone);
		//controlCanvas.setScrollLayer(controlScroller);
		//controlCanvas.setOptionsView(optionsView);
		//controlCanvas.setStatusView(statusView);
		controlCanvas.setDragger(dragLayer);
		controlCanvas.setWidgetCanvas(widgetCanvas);
		controlCanvas.setupListener();
		
		//Setup WidgetCanvas
		widgetCanvas.setDragLayer(dragLayer);
		widgetCanvas.setWorkspace(this);
		//widgetCanvas.setScrollLayer(widgetScroller);
		widgetCanvas.setupListeners();
		
		//Setup StatusView
		statusView.setControlCanvas(controlCanvas);
		statusView.setWorkspace(this);
		
		//Don't want to drop items within the Drawer Grid
		dragLayer.setIgnoredDropTarget(entityGrid);
		dragLayer.setIgnoredDropTarget(widgetGrid);
		dragLayer.setIgnoredDropTarget(widgetDrawer);
		dragLayer.setIgnoredDropTarget(widgetHandle);
		dragLayer.setIgnoredDropTarget(entityDrawer);
		dragLayer.setIgnoredDropTarget(optionsView);
		dragLayer.setIgnoredDropTarget(statusView);
		dragLayer.setIgnoredDropTarget(manifestView);
		dragLayer.setDragListener(deleteZone);
		dragLayer.setDragListener(this);
		
		//Setup OptionsView
		avlp = new ActionView.ActionViewLayoutParams(optionsView.getLayoutParams());
		avlp.AlwaysVisible = true;
		optionsView.setupActionView(this, workspaceLayout, R.drawable.options, R.string.options_name);
		optionsView.setupAnimations(R.anim.top_enter, R.anim.top_exit);
		optionsView.setLayoutParams(avlp);
		optionsView.setSize((int)getResources().getDimension(R.dimen.ActionViewWidth), (int)getResources().getDimension(R.dimen.ActionViewHeight));
		optionsView.setGravity(Gravity.LEFT | Gravity.TOP);
		optionsView.setLocation(0, 0);
		optionsView.setInternalLayout(true);
		optionsView.setUseGravity(true);
		
		//Setup Manifest Button
		mWorkspaceLayout.addActionView(manifestView);
		avlp = new ActionView.ActionViewLayoutParams(manifestView.getLayoutParams());
		avlp.AlwaysVisible = true;
		manifestView.setupActionView(this, workspaceLayout, R.drawable.manifest_view, R.string.change_canvas);
		manifestView.setupAnimations(R.anim.top_enter, R.anim.top_exit);
		manifestView.setLayoutParams(avlp);
		manifestView.setSize(getResources().getDimension(R.dimen.ActionViewWidth), getResources().getDimension(R.dimen.ActionViewHeight));
		manifestView.setInternalMeasure(true);
		manifestView.setLocation(0,0);
		manifestView.setGravity(Gravity.RIGHT | Gravity.TOP);
		manifestView.setUseGravity(true);
		manifestView.setInternalLayout(true);
		
		//Setup Handle
		avlp = new ActionView.ActionViewLayoutParams(handle.getLayoutParams());
		avlp.ControlVisible = true;
		handle.setupActionView(this, workspaceLayout, R.drawable.handle_horizontal, R.string.handle_name);
		handle.setupAnimations(R.anim.right_enter, R.anim.right_exit);
		handle.setLayoutParams(avlp);
		
		//Setup Widget Handle
		avlp = new ActionView.ActionViewLayoutParams(widgetHandle.getLayoutParams());
		avlp.ControlVisible = true;
		widgetHandle.setupActionView(this, workspaceLayout, R.drawable.handle_vertical, R.string.widget_handle_name);
		widgetHandle.setupAnimations(R.anim.bottom_enter, R.anim.bottom_exit);
		widgetHandle.setLayoutParams(avlp);
		
		//Setup Connect canvas view
		mWorkspaceLayout.addActionView(connectCanvas);
		avlp = new ActionView.ActionViewLayoutParams((int)getResources().getDimension(R.dimen.ActionViewWidth), (int)getResources().getDimension(R.dimen.ActionViewHeight));
		avlp.ControlVisible = true;
		connectCanvas.setupActionView(this, workspaceLayout, R.drawable.connect_control_canvas, R.string.connect_canvas_name);
		connectCanvas.setupAnimations(R.anim.left_enter, R.anim.left_exit);
		connectCanvas.setLayoutParams(avlp);
		connectCanvas.setSize(getResources().getDimension(R.dimen.ActionViewWidth), getResources().getDimension(R.dimen.ActionViewHeight));
		connectCanvas.setLocation(0, (int)getResources().getDimension(R.dimen.ActionViewHeight));
		connectCanvas.setInternalLayout(true);
		connectCanvas.setInternalMeasure(true);
		connectCanvas.setGravity(Gravity.LEFT | Gravity.TOP);
		connectCanvas.setUseGravity(true);
		connectCanvas.setVisibility(View.GONE);

		
		//Setup Connect list view
		mWorkspaceLayout.addActionView(connectList);
		avlp = new ActionView.ActionViewLayoutParams((int)getResources().getDimension(R.dimen.ActionViewWidth), (int)getResources().getDimension(R.dimen.ActionViewHeight));
		avlp.ControlVisible = true;
		connectList.setupActionView(this, workspaceLayout, R.drawable.connect_list, R.string.connect_list_name);
		connectList.setupAnimations(R.anim.left_enter, R.anim.left_exit);
		connectList.setLayoutParams(avlp);
		connectList.setSize(getResources().getDimension(R.dimen.ActionViewWidth), getResources().getDimension(R.dimen.ActionViewHeight));
		connectList.setLocation(0, (int) getResources().getDimension(R.dimen.ActionViewHeight) * 2);
		connectList.setInternalLayout(true);
		connectList.setInternalMeasure(true);
		connectList.setGravity(Gravity.LEFT | Gravity.TOP);
		connectList.setUseGravity(true);
		connectList.setVisibility(View.GONE);
	}
	private void restoreWorkspaceEntities(){
		try {
			for (WorkspaceEntity wse : mWses){
				wse.guiInitialization(mControlCanvas.getViewPort(), this);
				mControlCanvas.addView(wse, wse.mLayoutParams);
				if (wse.getEntity() instanceof Widget){
					wse.addWidgetCanvasView(wse.getWidgetView());
				}
				mControlCanvas.setupIOLabels(wse);

			}
			mControlCanvas.guiInitialization();
			for (WorkspaceEntity outWse : mWses){
				for (ConnectionPointView outCpv : outWse.getOutputConnectionList()){
					for (WorkspaceEntity inWse : mWses){
						for (ConnectionPointView inCpv : inWse.getInputConnectionList()){
							if (mConnectionAnalyzer.isConnected(outCpv, inCpv)){
								outCpv.setState(ConnectionPointView.STATE.CONNECTED);
								inCpv.setState(ConnectionPointView.STATE.CONNECTED);
								
							}
						}
					}
				}
			}
			
		}catch (Exception ex){
			mGpiConsole.error(ex.getMessage());
			//ex.printStackTrace();
		
		}
	}
	
	//User Events
	public void optionsClick(){

		Intent intent = new Intent(this, PropertyManagerActivity.class);
		intent.putExtra("entity", mCurrentWse.getEntity().getName());
		if (mCurrentWse.getEntity() instanceof Widget){
			intent.putExtra("widget", mWidgets.indexOf(mCurrentWse.getEntity()));
		}
		
		startActivity(intent);
	}
	public void onClick(View v)
	{
		if (v.equals(mWidgetCanvas)){
			//Widget Canvas Clicked
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SET_OPTIONS, null, null, null, null);
			DirectMessage(MessageRouter.MESSAGE_TYPE.START_SCROLLING, null, mWorkspaceLayout, null);
			//mWidgetCanvas
		}
		if (v.equals(mControlCanvas)){
			//ControlCanvas
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SELECTED_CONTROL_CANVAS, null, null, null, null);
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SET_OPTIONS, null, null, null, null);
			//wseClick(null);
		}//ControlCanvas clicked
		if (v instanceof WorkspaceEntity){
			//wseClick((WorkspaceEntity)v);
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SELECTED_WSE, null, null, null, ((WorkspaceEntity)v));
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SET_OPTIONS, null, null, null, ((WorkspaceEntity)v));
		}
		if (v instanceof ConnectionPointView){
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SELECTED_CONNECTION_LABEL, null, null, null, ((ConnectionPointView)v));
		}
		if (v instanceof ActionView){
			ActionView av = (ActionView)v;
			if (av.getName().equals(getResources().getString(R.string.options_name))){
				//option Item selected
				optionsClick();
			}
			if (av.getName().equals(getResources().getString(R.string.change_canvas))){
				if (!WIDGET_CANVAS_VISIBLE){
					MessageToRouter(MessageRouter.MESSAGE_TYPE.TO_WIDGET_CANVAS, null, null, null, null);
				}
				else if (WIDGET_CANVAS_VISIBLE){
					MessageToRouter(MessageRouter.MESSAGE_TYPE.TO_CONTROL_CANVAS, null, null, null, null);
				}

			}
			if (av.getName().equals(getResources().getString(R.string.connect_canvas_name))){
				MessageToRouter(MessageRouter.MESSAGE_TYPE.CONNECT_THROUGH_CANVAS, null, null, null, null);
			}
			if (av.getName().equals(getResources().getString(R.string.connect_list_name))){
				MessageToRouter(MessageRouter.MESSAGE_TYPE.CONNECT_THROUGH_LIST, null, null, null, null);
			}
			
		}
		if (v instanceof WidgetView){
			WidgetView wv = (WidgetView) v;
		}
	}
	public boolean onLongClick(View v)
	{
		if (v.equals(mWidgetCanvas)){
			//Widget Canvas Clicked
			//mWidgetCanvas
		}
		if (v.equals(mControlCanvas)){
			//ControlCanvas
		}
		if (v instanceof WorkspaceEntity){
			//Initiating a local drag
			//wseClick(null);
			MessageToRouter(MessageRouter.MESSAGE_TYPE.SELECTED_CONTROL_CANVAS, null, null, null, null);
			mDragLayer.startDrag((WorkspaceEntity)v, mControlCanvas, (WorkspaceEntity)v, DragController.DRAG_ACTION_MOVE);
		}
		if (v instanceof WidgetView){
			mDragLayer.startDrag((WidgetView)v, mWidgetCanvas, (WidgetView)v, DragController.DRAG_ACTION_MOVE);
		}

		return false;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//TODO add actions for each of the menu items
		switch (item.getItemId()){
		case R.id.itemDeviceActivity:
			break;
		case R.id.itemWorkspaceOptions:
			break;
		case R.id.itemSave:
			break;
		case R.id.itemLoad:
			break;
		}
		return false;
	}
	
	//Drawer
	public void closeDrawer()
	{
		//close the slider
		mEntityDrawer.close();
        if (mEntityDrawer.hasFocus()) {
            mControlCanvas.requestFocus();
        }
		//close the widget drawer
		mWidgetDrawer.close();
		if (mWidgetDrawer.hasFocus()){
			mControlCanvas.requestFocus();
		}
	}
	boolean isDrawerUp() {
		return mEntityDrawer.isOpened() && !mEntityDrawer.isMoving();
	}
	boolean isDrawerMoving(){
		return mEntityDrawer.isMoving();
	}
	boolean isDrawerDown() {
		return !mEntityDrawer.isOpened() && !mEntityDrawer.isMoving();
	}
	View getHandleView(){
		return mHandle;
	}
	
	//Drag
	public boolean acceptDropTarget(WorkspaceEntity Target, Entity dragItem)
	{
		// True if the combination can work
		return false;
	}
	public void dragEnterTarget(WorkspaceEntity Target, Entity dragItem, boolean input)
	{
		//Call Logistics to see if these two entities mesh
	
		if (input){
			Target.setStatus(mConnectionAnalyzer.analyzeInputConnection(Target, dragItem));
		}
		else{
			Target.setStatus(mConnectionAnalyzer.analyzeOutputConnection(Target, dragItem));
		}
		
	}
	public void dragExitTarget(WorkspaceEntity Target, Entity dragItem)
	{
		Target.setStatus(WorkspaceEntity.IDLE);
		
	}
	public void dragOverTarget(WorkspaceEntity Target, Entity dragItem, boolean input)
	{
		if (input){
			Target.setStatus(mConnectionAnalyzer.analyzeInputConnection(Target, dragItem));
		}
		else{
			Target.setStatus(mConnectionAnalyzer.analyzeOutputConnection(Target, dragItem));
		}
		
	}
	public void drop(WorkspaceEntity Target, Object dragItem)
	{
/*
		//see if we need to make any connections
		
		Entity entityTarget = Target.getEntity();
		Entity entityDrop;
		
		if (dragItem instanceof Entity){
			entityDrop = (Entity) dragItem;
		}
		else{
			entityDrop = ((WorkspaceEntity)dragItem).getEntity();
		}
		
		
		//Make Connections
*/
	}
	public void onDragEnd()
	{
		MessageToRouter(MessageRouter.MESSAGE_TYPE.STOP_DRAG, null, null, null, null);
	}
	public void onDragStart(View v, DragSource source, Object info,
			int dragAction)
	{
		MessageToRouter(MessageRouter.MESSAGE_TYPE.START_DRAG, null, null, null, null);

	}
	
	//Functions
	public void setupMenu(){
		this.mWorkspaceMenu.clear();
//		mWorkspaceMenu.add(0, R.id)
	}
	public void drawConnections (Canvas canvas){
		
	}
	public void setViewPort (ViewPort viewPort){
		mViewPort = viewPort;
	}
	public void addItem (WorkspaceEntity wse){
		
		//Check to see if we have a widget?
		if (wse.getEntity() instanceof Widget){
			//we have a widget for a ManifestEntity to add to the WidgetCanvas
			
			try {
				wse.addWidgetCanvasView(wse.getWidgetView(), wse.getLocation()[0], wse.getLocation()[1]);
			} catch (Exception e) {
				mGpiConsole.error(e.getMessage());
				e.printStackTrace();
			}
		}
		wse.getEntity().setWorkspaceEntity(wse);
		mWses.add(wse);
	}
	private void setVisibilityOfActionViews(MESSAGE_TYPE mt){
		switch (mt){
	case START_DRAG:
		mManifestView.hide();
		mDeleteZone.show();
		mEntityDrawer.setVisibility(View.GONE);
		mHandle.hide();
		mWidgetDrawer.setVisibility(View.GONE);
		mWidgetHandle.hide();
		break;
	case STOP_DRAG:
		mManifestView.show();
		mDeleteZone.hide();
		mEntityDrawer.setVisibility(View.VISIBLE);
		mHandle.show();
		mWidgetDrawer.setVisibility(View.VISIBLE);
		mWidgetHandle.show();
		break;
	case TO_WIDGET_CANVAS:
		mEntityDrawer.setVisibility(View.GONE);
		mHandle.hide();
		mWidgetDrawer.setVisibility(View.GONE);
		mWidgetHandle.hide();
		break;
	case TO_CONTROL_CANVAS:
		mEntityDrawer.setVisibility(View.VISIBLE);
		mHandle.show();
		mWidgetDrawer.setVisibility(View.VISIBLE);
		mWidgetHandle.show();
		break;
	case SELECTED_WSE:
		mManifestView.hide();
		mOptionsView.show();
		mStatusView.show(-1);
		mConnectCanvasView.hide();
		mConnectListView.hide();
		break;
	case SELECTED_CONTROL_CANVAS:
		mOptionsView.hide();
		mStatusView.hide();
		mManifestView.show();
		mConnectCanvasView.hide();
		mConnectListView.hide();
		break;
	case SELECTED_CONNECTION_LABEL:
		mConnectCanvasView.show();
		mConnectListView.show();
		}
	}
	@Override
	public String toString()
	{
		return "Workspace";
	}
	private void setupOptions(WorkspaceEntity wse){
		//if wse == null set default options
		if (mWorkspaceMenu == null){
			return;
		}
		MenuItem menuItem = null;
		Intent intent = new Intent();
		mWorkspaceMenu.clear();
		
		if (wse == null){	
			mWorkspaceMenu.add(0, R.id.itemSave, Menu.NONE, "Save");
			mWorkspaceMenu.add(0, R.id.itemLoad, Menu.NONE, "Load");
			mWorkspaceMenu.add(0, R.id.itemDeviceActivity, Menu.NONE, "Device View");
			mWorkspaceMenu.add(0, R.id.itemWorkspaceOptions, Menu.NONE, "Options");

		}
		else {
			final Entity entity = wse.getEntity();
			if (entity instanceof Medium){
				menuItem = mWorkspaceMenu.add(0, R.id.itemMediumQuery, Menu.NONE, "Query");
				menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem arg0) {
						((Medium)entity).query();
						return false;
					}
					
				});
			}
			else if (entity instanceof Device){
				menuItem = mWorkspaceMenu.add(0, R.id.itemDeviceStart, Menu.NONE, "Start Device");
				menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){

					public boolean onMenuItemClick(MenuItem item) {
						((Device)entity).start();
						return false;
					}
					
				});
				menuItem = mWorkspaceMenu.add(0, R.id.itemDeviceStop, Menu.NONE, "Stop Device");
				menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener(){

					public boolean onMenuItemClick(MenuItem item) {
						((Device)entity).stop();
						return false;
					}
					
				});
			}
			else if (entity instanceof Widget){
			
			}
		}
	}
	
	//States
	public boolean isDRAGGING(){
		return DRAGGING;
	}
	public boolean isWIDGET_CANVAS_VISIBLE()
	{
		return WIDGET_CANVAS_VISIBLE;
	}
	
	//Drawer Adapter
	public class entityGridAdapter extends BaseAdapter
	{
		private Context mContext;

		public entityGridAdapter (Context c)
		{
			mContext = c;
		}
		
//		@Override
		public int getCount()
		{
			return mEntityArray.size();
		}

//		@Override
		public Object getItem(int position)
		{
			return mEntityArray.get(position);
		}

//		@Override
		public long getItemId(int position)
		{
			return position;
		}

//		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			Entity ent = mEntityArray.get(position);

			ImageView iv = new ImageView(mContext);
			iv.setImageResource(ent.getImage());
			iv.setScaleType(ImageView.ScaleType.CENTER);
			iv.setLayoutParams(new GridView.LayoutParams(85, 85));
			iv.setPadding(8, 8, 8, 8);
			
			return iv;
		}
		
	}
	public class widgetGridAdpater extends BaseAdapter{

		private Context mContext;
		
		public widgetGridAdpater (Context c){
			mContext = c;
		}
		
//		@Override
		public int getCount()
		{
			return mWidgets.size();
		}

//		@Override
		public Object getItem(int position)
		{
			return mWidgets.get(position);
		}

//		@Override
		public long getItemId(int position)
		{
			return position;
		}

//		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Widget w = mWidgets.get(position);
			ImageView iv = new ImageView(mContext);
			iv.setImageResource(w.getImage());
			iv.setScaleType(ImageView.ScaleType.CENTER);
			iv.setLayoutParams(new GridView.LayoutParams(85, 85));
			iv.setPadding(8, 8, 8, 8);
			return iv;
		}
		
	}
	
	//Life Cycle
	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		
		//Called after activity has been stopped, prior to being started again, always followed by onStart()
		//Perhaps to fix things that didnt get a clean stop
		
		//Followed by onStart()
		//Compliment == onStop()
		
		super.onRestart();
	}
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is becoming visible to the user
		
		//Follwed by onResume() is the activity comes to the foreground or onStop() if it becomes hidden
		//Compliment == onStop()
		
		super.onStart();
	}
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity will start interfacing with the user. The activity is at the top of the stack
		
		//Compliment == onPause()
		
		super.onResume();
	}
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is about to start resuming a previous activity
		//Typically used to 
			//commit unsaved changes to persistent data
			//stop animations
			//stop other things that use CPU time
		//This must be a very quick implementation because the next activity will not resume until this method returns
		
		//Followed by either onResume() if the activity returns to the top, or onStop() if it becomes invisible to the user
		//Compliment == onPause()
		super.onPause();
	}
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is no longer visible to the user, because another activyt has been resumed, and is covering this one.
		//This may happen when another activity is being brought to front, or this one is being destroyed
		
		//Followed by
			//onRestart() if this activity is coming back to interact with the user
			//onDestroy() if this activity is goign away
		//Compliment == onRestart(), onStart()
		
		super.onStop();
	}
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is being destroyed because
			//Activity is finishing (Someone called finish() on it)
			//System is temporarily destroying this instance of the activity to save space
		//if finishing you can tell by isFinishing() == true
		
		//Compliment == onCreate()
/*		
		for (WorkspaceEntity wse : mWses){
			if (wse.getEntity() instanceof Widget){
				Widget widget = (Widget)wse.getEntity();
				
			}
		}
*/		
		for (WorkspaceEntity wse : mWses){
			wse.serviceInitialization();
		}
		mWidgetCanvas.removeAllViews();
		mControlCanvas.removeAllViews();
		super.onDestroy();
	}

	//LayoutParams Overrides
	/*
	public static class ActionViewLayoutParams extends ViewGroup.LayoutParams{

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
	*/
	
	public ControlCanvas getControlCanvas() {
		return mControlCanvas;
	}
	public WidgetCanvas getWidgetCanvas() {
		return mWidgetCanvas;
	}
	//Message Router
	public void BroadcastMessage(MESSAGE_TYPE mt, MessageEndPoint from,
			Object Data)
	{
		// TODO Auto-generated method stub
		for (WorkspaceEntity wse : mWses){
			wse.ReceiveMessage(mt, from, null, Data);
		}
		mControlCanvas.ReceiveMessage(mt, from, null, Data);
		mWidgetCanvas.ReceiveMessage(mt, from, null, Data);
		
	}
	public void DirectMessage(MESSAGE_TYPE mt, MessageEndPoint from, MessageEndPoint to,
			Object Data)
	{
		
		to.ReceiveMessage(mt, from, to, Data);
		
	}
	public void MessageToRouter(MESSAGE_TYPE mt, DIRECTED directed, MessageEndPoint from,
			MessageEndPoint to, Object Data)
	{
		
		LogMessage(mt, directed, from, to, Data);
		setVisibilityOfActionViews(mt);
		
		switch (mt){
		case START_DRAG:

			break;
		case STOP_DRAG:

			break;
		case SELECTED_WSE:
			if (mCurrentWse != null){
				mCurrentWse.setStatus(WorkspaceEntity.IDLE);
				mControlCanvas.removeIOLabels();
				CONNECT_THROUGH_CANVAS = false;
				CONNECT_THROUGH_LIST = false;
				CONNETION_MODE = false;
			}
			mCurrentWse = (WorkspaceEntity)Data;
			mCurrentWse.setStatus(WorkspaceEntity.SELECTED);
			mControlCanvas.setupIOLabels((WorkspaceEntity)Data);
			mStatusView.setText(mCurrentWse.getEntity().getName());
			break;
		case SELECTED_CONTROL_CANVAS:
			CONNECT_THROUGH_CANVAS = false;
			CONNECT_THROUGH_LIST = false;
			CONNETION_MODE = false;
			mControlCanvas.removeIOLabels();
			if (mCurrentWse != null)
			{
				mCurrentWse.setStatus(WorkspaceEntity.IDLE);
			}
			if (mConnectionPointView != null){
				mConnectionPointView.setSelected(false);
			}
			mConnectionPointView = null;
			break;
		case SELECTED_CONNECTION_LABEL:
			if (CONNECT_THROUGH_CANVAS){
				ConnectionPointView cpv = (ConnectionPointView)Data;
				int status = mConnectionAnalyzer.analyzeConnection(mConnectionPointView, cpv);
				if (status == WorkspaceEntity.OUTPUT_GOOD){
					mStatusView.setText("Connection Between " + this.mConnectionPointView.toString() + " and " + cpv.toString() + " is good: Connecting");
					if (mConnectionAnalyzer.connect(mConnectionPointView, cpv)){
						mConnectionPointView.setState(ConnectionPointView.STATE.CONNECTED);
						cpv.setState(ConnectionPointView.STATE.CONNECTED);
					}
				}
				else if (status == WorkspaceEntity.OUTPUT_WARNING){
					mStatusView.setText("Connection Between " + this.mConnectionPointView.toString() + " and " + cpv.toString() + " has warnings, but connecting");
				}
				else{
					mStatusView.setText("Connection Between " + this.mConnectionPointView.toString() + " and " + cpv.toString() + " is bad");
				}
				mControlCanvas.postInvalidate();
				break;
			}
			
			CONNETION_MODE = true;
			if (mConnectionPointView != null){
				mConnectionPointView.setSelected(false);
			}
			mConnectionPointView = ((ConnectionPointView)Data);
			mConnectionPointView.setSelected(true);
			mStatusView.setText("Connection Mode: " + ((ConnectionPointView)Data).toString());
			
			break;
		case TO_WIDGET_CANVAS:
			WIDGET_CANVAS_VISIBLE = true;
			mWorkspaceLayout.setWidgetCanvasVisible(true);
			//Switch the Manifest ImageView to back to controlCanvas
			mManifestView.setImageResource(R.drawable.to_control_view);
			break;
		case TO_CONTROL_CANVAS:
			WIDGET_CANVAS_VISIBLE = false;
			mWorkspaceLayout.setWidgetCanvasVisible(false);
			mManifestView.setImageResource(R.drawable.to_manifest_view);
			break;
		case CONNECT_THROUGH_CANVAS:
			//display all connections of the visible views
			if (!CONNECT_THROUGH_CANVAS){
				mControlCanvas.showAllIO();
				CONNECT_THROUGH_CANVAS = true;
			}
			break;
		case CONNECT_THROUGH_LIST:
			//display ConnectionListView so the user can choose an item
			if (!CONNECT_THROUGH_LIST){
				CONNECT_THROUGH_LIST = true;
			}
			break;
		case ADD_WSE:
			CONNECT_THROUGH_CANVAS = false;
			CONNECT_THROUGH_LIST = false;
			CONNETION_MODE = false;
			mControlCanvas.removeIOLabels();
			if (Data instanceof WorkspaceEntity){
				addItem((WorkspaceEntity)Data);
			}
			break;
		case REMOVING_WSE:
			CONNECT_THROUGH_CANVAS = false;
			CONNECT_THROUGH_LIST = false;
			CONNETION_MODE = false;
			mControlCanvas.removeIOLabels();
			//if a workspace entity is being removed
			if (Data instanceof WorkspaceEntity){
				//TODO remove all connections to/from this entity
				
				//Tell Everyone that this entity is being removed
				BroadcastMessage(mt, from, Data);
				//DirectMessage(mt, from, to, null);
				((WorkspaceEntity)Data).getEntity().resetWorkspaceEntity();
				mWses.remove((WorkspaceEntity)Data);
				//DirectMessage(mt, from, mControlCanvas, to);
			}
			break;
		case SET_OPTIONS:
			setupOptions((WorkspaceEntity)Data);
			break;
		case START_SCROLLING:
		case STOP_SCROLLING:
			this.DirectMessage(mt, from, this.mWorkspaceLayout, Data);
			break;
		}
		
	}
	public void LogMessage(MESSAGE_TYPE mt, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		String MessageString = null;
		String DirectedString = null;
		
		switch(mt){
		case START_DRAG:
			MessageString = new String("Start Drag");
			break;
		case STOP_DRAG:
			MessageString = new String("Stop Drag");
			break;
		case TO_WIDGET_CANVAS:
			MessageString = new String("Change to Widget Canvas");
			break;
		case TO_CONTROL_CANVAS:
			MessageString = new String("Change to Control Canvas");
			break;
		case ADD_WSE:
			MessageString = new String("Adding Workspace Entity");
			break;
		case REMOVING_WSE:
			MessageString = new String("Removing Entity");
			break;
		case SELECTED_WSE:
			MessageString = new String("WSE Selected");
			break;
		case SELECTED_CONTROL_CANVAS:
			MessageString = new String("Control Canvas Selected");
			break;
		case SELECTED_CONNECTION_LABEL:
			MessageString = new String("Connection Selected");
			break;
		case CONNECT_THROUGH_CANVAS:
			MessageString = new String("Connecting Using Canvas");
			break;
		case CONNECT_THROUGH_LIST:
			MessageString = new String("Connecting Using List");
			break;
		case RESIZING_WSE:
			MessageString = new String("Removing Workspace Entity");
			break;
		case SET_OPTIONS:
			MessageString = new String("Setting Options");
			break;
		case STOP_SCROLLING:
			MessageString = new String("Requesting Widget Canvas to Stop Scrolling");
			break;
		case START_SCROLLING:
			MessageString = new String ("Requesting Widget Canvas to Start Scrolling");
			break;
		default:
			MessageString = new String ("Unknown Message");
			break;
		}
		if (directed != null){
			switch(directed){
			case DIRECTED:
				DirectedString = new String("Directed");
				break;
			case BROADCAST:
				DirectedString = new String("Broadcast");
				break;
			}
		}
		else{
			DirectedString = new String ("Directed Not Specified");
		}
		
		if (to == null){
			
		}
		this.mGpiConsole.info(	"Message: " + MessageString + 
								": " + DirectedString +
								" to " + ((to==null) ? "NULL" : to.toString()) +
								" from " + ((from==null) ? "NULL" : from.toString()) +
								" data: " + ((Data==null) ? "NULL" : Data.toString()));

	}
	
	//Message End Point
	public void ReceiveMessage(MESSAGE_TYPE messageType, MessageEndPoint from,
			MessageEndPoint to, Object Data)
	{
		// TODO Auto-generated method stub
		
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data)
	{
		MessageToRouter(messageType, directed, from, to, Data);
		
	}
	public void setMessageRouter(MessageRouter router)
	{
		// This is the message router, I can send messages to myself
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
