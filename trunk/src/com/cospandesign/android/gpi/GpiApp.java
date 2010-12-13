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

package com.cospandesign.android.gpi;

import java.util.ArrayList;
import java.util.Random;

import android.app.Application;
import android.content.res.Configuration;

import com.cospandesign.android.gpi.controller.android.GpiController;
import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.entity.EntityTree;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.android.MediumAndroid;
import com.cospandesign.android.gpi.medium.lcmtcp.MediumLCMTCP;
import com.cospandesign.android.gpi.medium.test.MediumTest;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.widget.chart.DoubleViewerWidget;
import com.cospandesign.android.gpi.widget.paint.WidgetPainter;
import com.cospandesign.android.gpi.widget.textbox.WidgetTextBox;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

import dlcm.StructureListener;
import dlcm.builder.StructureManager;

public class GpiApp extends Application implements StructureListener{
	
	private EntityTree mEntityTree;
	private ArrayList<Widget> mWidgets;
	private StructureManager mStructureManager;
	private static Integer mBoardId;
	final GpiConsole mConsole = GpiConsole.getinstance();
	private ArrayList<WorkspaceEntity> mActiveWses;
	//private static GpiService mGpiService;


	//Lifecycle
	@Override
	public void onCreate() {
		super.onCreate();
		mBoardId = (new Random().nextInt(1000));
		mStructureManager = new StructureManager();
		SetupControlTree();
		mWidgets = new ArrayList<Widget>();
		SetupWidgets();	
		mActiveWses = new ArrayList<WorkspaceEntity>();
		//mConsole.info("Attempting to start service");
		//this.startService(new Intent(this, GpiService.class));
		//bindService(new Intent(this, GpiService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		//do something with configuration
	}


	private void SetupControlTree ()
	{
		Entity entity;
		mEntityTree = new EntityTree(new GpiController(
				getString(R.string.gpi_name),
				getString(R.string.gpi_info),
				R.drawable.cdgpit, 
				this, 
				true));
		mEntityTree.add(new MediumTest(
				getString(R.string.test_name),
				getString(R.string.test_info),
				R.drawable.test_medium,
				this,
				true));
		mEntityTree.add(new MediumAndroid(
				getString(R.string.android_name),
				getString(R.string.android_info),
				R.drawable.andm,
				this,
				true));
/*		
		mEntityTree.add(new MediumLCMUDP(
				getString(R.string.lcm_udp_name),
				getString(R.string.lcm_udp_info),
				R.drawable.lcmudp,
				this,
				true));
*/				
		entity = new MediumLCMTCP(
				getString(R.string.lcm_tcp_name),
				getString(R.string.lcm_tcp_info),
				R.drawable.lcmtcp,
				this,
				true);
		entity.addProperty(GpiConstants.STRUCTURE_MANAGER_STRING, mStructureManager, ENTITY_PROPERTY_TYPE.NO_DISPLAY, "Structure Manager for DLCM", false);
		mEntityTree.add(entity);
		
	}
	private void SetupWidgets(){
		mWidgets.add(new DoubleViewerWidget(
				getString(R.string.test_integer_name),
				getString(R.string.test_integer_info),
				R.drawable.charter,
				this,
				true));
		
		mWidgets.add(new WidgetTextBox (
				getString(R.string.widget_textbox_name),
				getString(R.string.widget_textbox_info),
				R.drawable.textwidget,
				this,
				true));
		
		mWidgets.add(new WidgetPainter (
				getString(R.string.widget_painter_name),
				getString(R.string.widget_painter_info),
				R.drawable.painterwidget,
				this,
				true));
		
	}	
	public static Integer getBoardId() {
		return mBoardId;
	}

	public EntityTree getControlTree() {
		return mEntityTree;
	}

	public ArrayList<Widget> getWidgets() {
		return mWidgets;
	}

	public StructureManager getStructureManager() {
		return mStructureManager;
	}
	public GpiConsole getConsole(){
		return mConsole;
	}
	public ArrayList<WorkspaceEntity> getActiveWses() {
		return mActiveWses;
	}
	//Analyze the Devices for each structure, and update the gallerys
	public void structureChanged() {
		// TODO Auto-generated method stub
		
	}
	public void structureChanging() {
		// TODO Auto-generated method stub
		
	}
}
