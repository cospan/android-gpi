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

package com.cospandesign.android.gpi.widget.chart;

import java.util.LinkedList;
import java.util.Set;

import android.content.Context;

import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.widget.chart.FloatGraphView.AddPointStruct;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class DoubleViewerWidget extends Widget
{
	static final String InputChannelName = "Number";
	
	int NumOfPoints = 100;
	int Max = 100;
	int Min = 0;
	boolean mAutoRangeFlag = true;
	LinkedList<Integer> Points;
	
	FloatGraphView mFloatGraphView;
	
	private static final String POINTS_STRING = "Points";
	private static final String MAX_STRING = "Max";
	private static final String MIN_STRING = "Min";
	private static final String AUTO_RANGE_STRING = "Auto Range";
	
	double CurrentPoint = 0;

	public DoubleViewerWidget(String name, String info, Integer image,
			Context c, boolean enabled)
	{
		
		super(name, info, image, c, enabled);
		addProperty(POINTS_STRING, new Integer(100), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Set the number of points to plot", false);
		addProperty(MAX_STRING, new Integer(100), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Set the max value", false);
		addProperty(MIN_STRING, new Integer(0), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Set the min value", false);
		addProperty(AUTO_RANGE_STRING, new Boolean(mAutoRangeFlag), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Graph automatically sets range", false);
		
		
		Points = new LinkedList<Integer>();
		
		//SetDefualtViewDimensions(100, 100);
		//mFloatGraphView = new FloatGraphView(c);
		//this.AddView(mFloatGraphView);
		addInputDataChannel(InputChannelName, Double.class);
		

	}

	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		setDefualtViewDimensions(100, 100);
		mFloatGraphView = new FloatGraphView(mContext);
		AddView(mFloatGraphView);
		//addInputDataChannel(InputChannelName, Double.class);
	}
	

	@Override
	public void serviceInitialization() {
		super.serviceInitialization();
		//Remove ourselves from any listening position
		mFloatGraphView.stop();
		mFloatGraphView = null;
	}

	//Overrides
	public void newDataAvailable(Set<String> inputChannels){
		//TODO fix this, so that its not skipping over the input channel
		if ((mWorkspaceEntity == null) && (mWorkspaceEntity.getWidgetCanvas() == null)){
			return;
		}
		String name = null;
		AddPointStruct[] nps = null;
		
		if (inputChannels.contains(InputChannelName)){
			Object obj = dequeueInputData(InputChannelName);
			name = obj.getClass().getName();
			if (name.matches(Double.class.getName())){
				CurrentPoint = new Double((Double)obj);
			}
			if (name.matches(Float.class.getName())){
				CurrentPoint = new Double((Float)obj);
			}
			if (name.matches(Long.class.getName())){
				CurrentPoint = new Double((Long)obj);
			}
			if (name.matches(Integer.class.getName())){
				CurrentPoint = new Double((Integer)obj);
			}
			if (name.matches(Short.class.getName())){
				CurrentPoint = new Double((Short)obj);
			}
			if (name.matches(Byte.class.getName())){
				CurrentPoint = new Double((Byte)obj);
			}
				/*
			if (obj.getClass().equals(Double.class)){
				CurrentPoint = new Double ((Double)obj);
			}
			if (obj.getClass().equals(Float.class)){
				CurrentPoint = new Float ((Float)obj);
			}
			else if (obj.getClass().equals(Long.class)){
				CurrentPoint = new Double ((Long)obj);
			}
			else if (obj.getClass().equals(Integer.class)){
				CurrentPoint = new Double ((Integer)obj);
			}
			else if (obj.getClass().equals(Short.class)){
				CurrentPoint = new Double ((Short)obj);
			}
			else {
				CurrentPoint = new Double ((Byte)obj);
			}
*/
			AddPointStruct aps = new AddPointStruct(InputChannelName, CurrentPoint, (long)0);
			nps = new AddPointStruct[]{aps};
			
			if (mFloatGraphView != null){
				mFloatGraphView.addData(nps);
			}
			
			
		}
	}
	@Override
	public void propertiesUpdate()
	{
		super.propertiesUpdate();
		NumOfPoints = (Integer) getPropertyData(POINTS_STRING);
		Max = (Integer) getPropertyData(MAX_STRING);
		Min = (Integer) getPropertyData(MIN_STRING);
		mAutoRangeFlag = (Boolean)getPropertyData(AUTO_RANGE_STRING);
		
		mFloatGraphView.setMaxPossibleDataPoints(NumOfPoints);
		mFloatGraphView.setAutoRange(mAutoRangeFlag);
		mFloatGraphView.setGraphMax(Max);
		mFloatGraphView.setGraphMin(Min);
		
	
	}


	

}
