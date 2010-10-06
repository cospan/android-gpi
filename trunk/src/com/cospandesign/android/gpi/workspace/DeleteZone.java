/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cospandesign.android.gpi.workspace;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.cospandesign.android.gpi.router.MessageRouter;
import com.cospandesign.android.gpi.workspace.controlcanvas.ControlCanvas;

public class DeleteZone extends ActionView implements DropTarget, DragController.DragListener {

    private static final int TRANSITION_DURATION = 250;
    private TransitionDrawable mTransition;

    
    //Constructor
    public DeleteZone(Context context) {
        super(context);
     
    }
    public DeleteZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }
    public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    //Override
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mTransition = (TransitionDrawable) getBackground();
	}
    
    //Drag
//	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo)
	{
		if (!(source instanceof ControlCanvas)){
			return false;
		}
		return true;
	}
//	@Override
	public void onDragEnter(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		mTransition.reverseTransition(TRANSITION_DURATION);
		
	}
//	@Override
	public void onDragExit(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		mTransition.reverseTransition(TRANSITION_DURATION);
	}
//	@Override
	public void onDragOver(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo)
	{
		
	}
//	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo)
	{
		final WorkspaceEntity wse = (WorkspaceEntity)dragInfo;
	
		SendMessage(MessageRouter.MESSAGE_TYPE.REMOVING_WSE, MessageRouter.DIRECTED.DIRECTED, this, null, wse);
	}
//	@Override
	public void onDragEnd()
	{

	}
//	@Override
	public void onDragStart(View v, DragSource source, Object info,
			int dragAction)
	{

	}

	//Functions
	public boolean inDeleteZone(int x, int y){
		Rect rect = new Rect(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
		if (rect.contains(x, y)){
			return true;
		}
		return false;
	}
		
}
