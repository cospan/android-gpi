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

package com.cospandesign.android.gpi.widget.paint;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class WidgetPainter extends Widget {
	
	ManifestViewPaint mPainterView;
	LinearLayout mPainterLayout;
	
	private boolean mSelected = false;
	
	private static final String WIDTH = "Width";
	private static final String HEIGHT = "Height";
	
	public WidgetPainter(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		

	}

	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		setDefualtViewDimensions(300, 300);
		
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//View view = inflater.inflate(R.layout.paint_widget_layout, null);
		try {
			mPainterLayout = (LinearLayout) inflater.inflate(R.layout.paint_widget_layout, null);
		}
		catch (Exception ex){
			Log.e("GPI", ex.getMessage());
		}
		mPainterView = (ManifestViewPaint) mPainterLayout.findViewById(R.id.paintviewSurfaceCanvas);
		this.AddView(mPainterLayout);
		/*
		mPainterLayout.setFocusable(true);		
		mPainterLayout.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if (!mSelected){
					setWidgetViewSrollEnabled(false);
					mPainterLayout.bringToFront();
				}
				mPainterView.setSelected(true);
		
			}
		});
		mPainterLayout.setOnLongClickListener(new OnLongClickListener(){

			public boolean onLongClick(View v) {
				return true;
			}
			
		});

		mPainterLayout.setOnFocusChangeListener(new OnFocusChangeListener(){

			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus){
					mSelected = false;
					mPainterView.setSelected(false);
					setWidgetViewSrollEnabled(true);
				}
				else {
					mSelected = true;
					setWidgetViewSrollEnabled(false);
					mPainterView.setSelected(true);
				}
			}
		});	
		*/	
	}
	
	@Override
	public boolean onWidgetClicked() {

		return true;
	}

	@Override
	public boolean onWidgetLongClicked() {
		return true;
	}

}
