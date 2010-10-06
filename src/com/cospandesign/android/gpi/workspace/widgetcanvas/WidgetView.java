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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.cospandesign.android.gpi.router.MessageEndPoint;
import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.router.MessageRouter.DIRECTED;
import com.cospandesign.android.gpi.router.MessageRouter.MESSAGE_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.DragSource;
import com.cospandesign.android.gpi.workspace.DropTarget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class WidgetView extends ViewGroup implements MessageEndPoint
{
	
	MessageRouter mRouter;
	WidgetCanvas mWidgetCanvas;
	WorkspaceEntity mWorkspaceEntity;
	Widget mWidget;
	WidgetCanvas.LayoutParams mParentLayoutParams;
	
	public int mDefaultWidth = 50;
	int mDefaultHeight = 50;
	
	
	//Widget View States
	private boolean SHOW_LEVEL = false;
	private boolean FULL_SCREEN = false;
	
	//Constructor
	public WidgetView(Context context, Widget widget){
		super(context);
		mWidget = widget;
		setOnClickListener(new OnClickListener() { 
			public void onClick(View v){
				Click(v);				
			}});
		setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v)
			{
				//use up the long click listener
				return LongClick(v);
			}
			
		});
		setBackgroundColor(Color.BLACK);
	}
	public WidgetView(Context context, WorkspaceEntity parent){
		super(context);
		if ((parent.getEntity()) instanceof Widget){
			mWidget = (Widget)parent.getEntity();
		}

		setOnClickListener(new OnClickListener() { 
			public void onClick(View v){
				Click(v);				
			}});
		setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v)
			{
				//use up the long click listener
				return LongClick(v);
			}
			
		});
		setBackgroundColor(Color.BLACK);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom){

//TODO add the ability to add Labels, buttons, go to full screen, etc...
		//Display Widget Views
		for (int i = 0; i < this.getChildCount(); i++){
			//WidgetCanvas.LayoutParams lp = (WidgetCanvas.LayoutParams)getChildAt(i).getLayoutParams();
			getChildAt(i).layout(0, 0, mParentLayoutParams.width, mParentLayoutParams.height);
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		//Call the child Views Layouts
		for (int i = 0; i < this.getChildCount(); i++){
			getChildAt(i).measure(mParentLayoutParams.width | MeasureSpec.EXACTLY, mParentLayoutParams.height | MeasureSpec.EXACTLY);
		}

		setMeasuredDimension(mParentLayoutParams.width, mParentLayoutParams.height);
	}

	//Functions
	public void setDefaultWidth(int width){
		mDefaultWidth = width;
	}
	public int getDefaultWidth(){
		return mDefaultWidth;
	}
	public void setDefaultHeight(int height){
		mDefaultHeight = height;
	}
	public int getDefaultHeight(){
		return mDefaultHeight;
	}
	
	public void Click(View v){
		//Clicked
		//tell the widget we were clicked
		if (!mWidget.onWidgetClicked()){
			//tell the canvas we were clicked
			mWidget.getWorkspaceEntity().getWidgetCanvas().onWidgetClick(this, mWidget);
		}
	}
	public boolean LongClick(View v){
		//Long Clicked
		
		//tell the widget we were clicked
		if (!mWidget.onWidgetLongClicked()){
			//tell the canvas we were clicked
			if (!this.getWidget().getWorkspaceEntity().getWidgetCanvas().onWidgetLongClick(this, mWidget)){
				return false; 
			}
		}
		return true;
	}
	public void setWidgetCanvas(WidgetCanvas widgetCanvas){
		mWidgetCanvas = widgetCanvas;
	}
	public Widget getWidget(){
		return mWidget;
	}
	public void setWidget(Widget widget){
		mWidget = widget;
	}
	public void setWorkspaceEntity(WorkspaceEntity wse){
		mWorkspaceEntity = wse;
	}
	public WorkspaceEntity getWorkspaceEntity(){
		return mWorkspaceEntity;
	}
	public void setLayoutParams(WidgetCanvas.LayoutParams params){
		mParentLayoutParams = params;
	}
	public com.cospandesign.android.gpi.workspace.widgetcanvas.WidgetCanvas.LayoutParams getWidgtViewLayoutParams() {
		return mParentLayoutParams;
	}
	@Override
	public String toString(){
		return mWidget.getName();
	}
	
	//Layout Parameters For Individual Entities
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p){
		return p instanceof WidgetView.LayoutParams;
	}
	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs){
		return new WidgetView.LayoutParams(getContext(), attrs);
	}
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p){
		return new WidgetView.LayoutParams(p);
	}
	public static class LayoutParams extends ViewGroup.LayoutParams{

		int X;
		int Y;
				
		public LayoutParams(Context c, AttributeSet attrs){
			super(c, attrs);
		}
		public LayoutParams(ViewGroup.LayoutParams source){
			super(source);
		}
		public LayoutParams(int x, int y){
			super(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			X = x;
			Y = y;
		}
		
				
	}
	
	//Message Receiver
	public void ReceiveMessage(MESSAGE_TYPE messageType,
			MessageEndPoint from, MessageEndPoint to, Object Data){
		// TODO Receive Message
	}
	public void SendMessage(MESSAGE_TYPE messageType, DIRECTED directed,
			MessageEndPoint from, MessageEndPoint to, Object Data){
		mRouter.MessageToRouter(messageType, directed, from, to, Data);
	}
	public void setMessageRouter(MessageRouter router){
		mRouter = router;
	}
	
}
