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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.DragController;
import com.cospandesign.android.gpi.workspace.DragSource;
import com.cospandesign.android.gpi.workspace.WorkspaceActivity;

public class WidgetGrid extends GridView implements AdapterView.OnItemLongClickListener, OnItemClickListener,
		DragSource
{

	Context mContext;
	WorkspaceActivity mWorkspace;
	DragController mDragger;
	
	public WidgetGrid(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
	}

	public WidgetGrid(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}
	
//	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Perhaps display information, or options for this item, but right now don't do anything

	}

//	@Override
	public void onDropCompleted(View target, boolean success)
	{

	}

//	@Override
	public void setDragger(DragController dragger)
	{
		mDragger = dragger;

	}
	
	public void setWorkspace(WorkspaceActivity ws){
		mWorkspace = ws;
	}
	
//	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		Widget w = (Widget) parent.getItemAtPosition(position);
		
		mDragger.startDrag(view, this, w, DragController.DRAG_ACTION_COPY);
		mWorkspace.closeDrawer();
		return true;
	}

}
