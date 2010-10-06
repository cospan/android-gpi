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


package com.cospandesign.android.gpi.properties;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cospandesign.android.gpi.entity.Entity;

public class PropertyLinearView extends LinearLayout
{
	ImageView EntityIcon;
	TextView EntityNameProperty;
	
	LinearLayout ImageNameLayout;
	LinearLayout PropertyLinearLayout;
	
	Entity mEntity;

	public PropertyLinearView(Context context)
	{
		super(context);
		initializeViews();
	}

	public PropertyLinearView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initializeViews();
	}
	
	
	private void initializeViews(){
		
		//Icon, and Name
		EntityIcon = new ImageView(this.getContext());
		EntityNameProperty = new TextView(this.getContext());
		
		ImageNameLayout = new LinearLayout(this.getContext());
		ImageNameLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		ImageNameLayout.setLayoutParams(lp);
		
		//Setup View Specifics
		EntityIcon.setLayoutParams(iconLp);
		EntityNameProperty.setLayoutParams(nameLp);
		EntityNameProperty.setLines(2);
		
		ImageNameLayout.addView(EntityIcon, 0);
		ImageNameLayout.addView(EntityNameProperty, 1);
		
		//Properties
		
		PropertyLinearLayout = new LinearLayout(getContext());
		lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		
		
		
		addView(ImageNameLayout);
		addView(PropertyLinearLayout);
	}
	public void setEntity(Entity entity){
		mEntity = entity;
		setupViews();
	}
	private void setupViews (){
		EntityIcon.setImageResource(mEntity.getImage());

		String name = mEntity.getName();
		String info = mEntity.getInfo();
		
		EntityNameProperty.setText(
		"Name: " + name + "\n" +		
		"Info: " + info	);
		
		//go through each of the properties, and add them to the PropertyLinearLayout
		//Skip the first two because thats the name, and info
		for (int i = 2; i < mEntity.getEntityProperties().size(); i++){
			//String propertyName = mEntity.getProperties().keySet()
		}
	}

}
