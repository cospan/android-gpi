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

package com.cospandesign.android.gpi.widget;

import android.content.Context;
import android.view.View;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetViewGroup;

public class Widget extends Entity
{
	static final String WIDTH_STRING = "View Width";
	static final String HEIGHT_STRING = "View Height";
	//public WidgetView mWidgetView;
	
	int mViewWidth = 50;
	int mViewHeight = 50;
	
	public Widget(String name, String info, Integer image, Context c, boolean enabled)
	{
		super(name, info, image, c, enabled);
		
		//mWidgetView = new WidgetView(c, this);
		
		addProperty(WIDTH_STRING, new Integer(mViewWidth), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Set the width of the view", false);
		addProperty(HEIGHT_STRING, new Integer(mViewHeight), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Set the height of the view", false);
		
	}

	
	
	//Widget View
	//can be set by the extended widgets
	protected void setDefualtViewDimensions(int width, int height){
		mWorkspaceEntity.setDefaultWidgetViewDimensions(width, height);
		setProperty(WIDTH_STRING, new Integer(width));
		setProperty(HEIGHT_STRING, new Integer(height));
		//These properties will be overriden
		//addProperty(WIDTH_STRING, new Integer(mWidgetView.getDefaultWidth()), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "default width of the view", false);
		//addProperty(HEIGHT_STRING, new Integer(mWidgetView.getDefaultHeight()), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "default height of the view", false);
	}
	protected void setWidgetViewDimensions (int width, int height){
		setProperty(WIDTH_STRING, new Integer(width));
		setProperty(HEIGHT_STRING, new Integer(height));
		propertiesUpdate();

	}
	public WidgetViewGroup getWidgetView() throws Exception{
		if (mWorkspaceEntity != null){
			return mWorkspaceEntity.getWidgetView();
		}
		throw new Exception ("No WorkspaceEntity");
	}
	/*
	public void setWidgetView(WidgetView widgetView){
		mWidgetView = widgetView;
	}
	*/
	public boolean onWidgetClicked(){
		//Didn't use up the click
		return false;
	}
	public boolean onWidgetLongClicked(){
		//Didn't use up the long click
		return false;
	}
	@Override
	public void propertiesUpdate()
	{
		super.propertiesUpdate();
		mViewWidth = ((Integer) getPropertyData(WIDTH_STRING));
		mViewHeight = ((Integer) getPropertyData(HEIGHT_STRING));
		mWorkspaceEntity.setDefaultWidgetViewDimensions(mViewWidth, mViewHeight);
		mWorkspaceEntity.postInvalidate();
	}
	public void AddView(View view){
		if (mWorkspaceEntity != null){
			mWorkspaceEntity.addWidgetView(view);
		}
	}
	public void RemoveView(View view){
		if (mWorkspaceEntity != null){
			if (mWorkspaceEntity.getWidgetView() != null){
				mWorkspaceEntity.removeWidgetViews();
			}
		}
	}
	public void setWidgetViewSrollEnabled(boolean enabled){
		if (getWorkspaceEntity() == null){
			return;
		}
		WorkspaceEntity wse = getWorkspaceEntity();
/*		
		if (enabled){
			wse.SendMessage(MESSAGE_TYPE.START_SCROLLING, null, wse, null, mWidgetView);
		}
		else {
			wse.SendMessage(MESSAGE_TYPE.STOP_SCROLLING, null, wse, null, mWidgetView);
		}
*/		
	}

	@Override
	public void notifyRemoval() {
		super.notifyRemoval();
		mWorkspaceEntity.removeWidgetViews();
	}
}
