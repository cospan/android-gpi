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
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.cospandesign.android.gpi.entity.Entity;

public class EntityGrid extends GridView implements AdapterView.OnItemLongClickListener,
		OnItemClickListener, DragSource
{
	Context mContext;
	WorkspaceActivity mWorkspace;
	DragController mDragger;

	public EntityGrid(Context context)
	{
		super(context);
		mContext = context;
	}

	public EntityGrid (Context context, AttributeSet attrs) {
		super (context, attrs);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Perhaps display information, or options for this item, but right now don't do anything

	}
//	@Override
	public void onDropCompleted(View target, boolean success)
	{
		//Since things are copied,nothing happens, we always want a list of things
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
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		//parent has the WorkspaceEntity, need to get it, to work with this drag
		Entity ent = (Entity) parent.getItemAtPosition(position);
		//start a dragger
		mDragger.startDrag(view, this, ent, DragController.DRAG_ACTION_COPY);
		//close the drawer
		mWorkspace.closeDrawer();
		return true;
	}

}
