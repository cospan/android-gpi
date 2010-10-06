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

package com.cospandesign.android.gpi.device.test;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;

public class TestIntegerDevice extends Device
{

	private static final int CONSTANT = 0;
	private static final int RANDOM = 1;
	private static final int EQUATION = 2;
	
	private static final int SINE_FUNCTION = 0;
	private static final int TRIANGLE_FUNCTION = 1;
	
	private static final String UPDATE_RATE_STRING = "Update Rate";
	private static final String FUNCTION_STRING = "Function";
	private static final String CONSTANT_VALUE_STRING = "Constant Value";
	private static final String EQUATION_TYPE_STRING = "Equation Types";
	private static final String EQUATION_STEP_STRING = "Equation Steps";
	private static final String EQUATION_MAX_STRING = "Equation Max";
	private static final String EQUATION_MIN_STRING = "Equation Min";
	private static final String OUTPUT_QUEUE_SIZE_STRING = "Output Queue Size";
	
	final static String outputChannelName = "Integers";
	
	Timer mTimer;
	TimerTask mTimerTask;
	
	
	int Count = 0;
	int Steps = 100;
	int Max = 100;
	int Min = 0;
	int UpdateRate = 0;
	int FunctionType = EQUATION;
	int EquationType = 0;
	int OutputQueueSize = 1;
	int ConstantValue = 1;
	
	boolean mStopFlag = true;
	
	//LinkedList<Integer> OutputIntegerQueue;
	
	public TestIntegerDevice(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent)
	{
		super(name, info, image, context, enabled, parent);
		
		mTimer = new Timer();
		mTimerTask = new TimerTask(){
			public void run(){
				Tick();
			}		
		};	
		
		
		ArrayList<String> functionTypes = new ArrayList<String>();
		functionTypes.add("Constant");
		functionTypes.add("Random");
		functionTypes.add("Equation");

		ArrayList<String> equationTypes = new ArrayList<String>();
		equationTypes.add("Sine");
		equationTypes.add("Triangle");
		
		
		
		//Setup the initial properties
		addProperty(UPDATE_RATE_STRING, new Integer(100), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Rate (Milliseconds) which data outputs", false);
		addProperty(FUNCTION_STRING, functionTypes, ENTITY_PROPERTY_TYPE.SPINNER, "Choose between output options", false);
		((EntityProperty)getProperty(FUNCTION_STRING)).setIndex(2);
		addProperty(CONSTANT_VALUE_STRING, new Integer(1), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Constant Value", false);
		addProperty(EQUATION_TYPE_STRING, equationTypes, ENTITY_PROPERTY_TYPE.SPINNER, "Select the type of equation", true);
		addProperty(EQUATION_STEP_STRING, new Integer(100), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Number of steps for equation", false);
		addProperty(EQUATION_MAX_STRING, new Integer (100), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Max value of equation", false);
		addProperty(EQUATION_MIN_STRING, new Integer (0), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Min value of equation", false);
		addProperty(OUTPUT_QUEUE_SIZE_STRING, new Integer (1), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Size of the outptu queue", false);
		
		SetupIO();
		//Schedule the timer based on the default properties
		mStopFlag = false;
		//mTimer.schedule(mTimerTask, 100);
		mTimer.scheduleAtFixedRate(mTimerTask, 1000, 100);
		
		//For right now we have this running all the time
		//start();
		
	}
	
	private void SetupIO (){
		//OutputIntegerQueue = new LinkedList<Integer>();
		addOutputDataChannel(outputChannelName, Integer.class);
	}

	
	public void Tick(){

		switch (FunctionType){
		case CONSTANT:
			FunctionConstant();
			break;
		case RANDOM:
			FunctionRandom();
			break;
		case EQUATION:
			FunctionEquation();
			break;
		}
		
		//Check setup the timing
		if (mStopFlag == false){
			//mTimer.sschedule(mTimerTask, UpdateRate);
		}
	}
	
	private void FunctionConstant(){
		OutputInt(ConstantValue);
	}
	private void FunctionRandom(){
		Random random = new Random();
		OutputInt (random.nextInt());
	}
	private void FunctionEquation(){
		switch(EquationType){
		case SINE_FUNCTION:
			Count++;
			if (Count > Steps){
				Count = 0;
			}
			int SineValue = (int)(((Math.sin((2 * Math.PI * (double)Count)/((double)Steps))) + 1)*((Max - Min)/2) + Min);
			OutputInt (SineValue);
			break;
		case TRIANGLE_FUNCTION:
			Count++;
			if (Count > Steps){
				Count = 0;
			}
			int size = Steps;
			int midpoint = size/2;
			int triangleValue = 0;
			float height = Max - Min;
			float percentHeight = 0;
			if (Count < midpoint){
				percentHeight = ((float)Count) / ((float)midpoint);
				triangleValue = (int) ((percentHeight * height) + Min); 
			}
			else {
				percentHeight = 1.0f - ((float)(Count - midpoint) / ((float)midpoint));
				triangleValue = (int) ((percentHeight * height) + Min);
			}
			OutputInt(triangleValue);
			
			break;
		}
	}


	@Override
	public boolean start()
	{
		//Check setup the timing
		mTimer.schedule(mTimerTask, UpdateRate);
		mStopFlag = false;
		return true;
	}
	@Override
	public boolean stop()
	{
		mStopFlag = true;
		return true;
	}
	@Override
	public void propertiesUpdate()
	{
		Steps = (Integer)getPropertyData(EQUATION_STEP_STRING);
		Max = (Integer)getPropertyData(EQUATION_MAX_STRING);
		Min = (Integer)getPropertyData(EQUATION_MIN_STRING);
		UpdateRate = (Integer)getPropertyData(UPDATE_RATE_STRING);
		FunctionType = (Integer)getProperty(FUNCTION_STRING).getIndex();
		EquationType = (Integer)getProperty(EQUATION_TYPE_STRING).getIndex();
		OutputQueueSize = (Integer) getPropertyData(OUTPUT_QUEUE_SIZE_STRING);
		ConstantValue = (Integer)getPropertyData(CONSTANT_VALUE_STRING);

	}
	private void OutputInt(int num){
		outputData(outputChannelName, num);
		if (this.mWorkspaceEntity != null){
			mWorkspaceEntity.setStatus(mWorkspaceEntity.NEW_DATA);
		}
	}	
	
}
