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

package com.cospandesign.android.gpi.device.android;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;

public class AndroidSensor extends Device
{

	static final private String MaximumRangeString = "Maximum Range";
	static final private String MaximuRangeDescription = "Maximum Range of the Sensor";
	
	static final private String PowerString = "Power Usage mA";
	static final private String PowerDescription = "Power used by the device";
	
	static final private String ResolutionString = "Resolution";
	static final private String ResolutionDescription = "Maximum Resolution of Sensor";
	
	static final private String VersionString = "Version";
	static final private String VersionDescription = "Version Number of Sensor";
	
	static final private String VendorString = "Vendor";
	static final private String VendorDescription = "Vendor Name";
	
	protected static final String RateString = "Sensor Read Rate";
	static final private String RateDescription = "Rate of Sensor Reading";
	
	protected static final String UpdateRateString = "Output Rate";
	static final private String UpdateRateDescription = "Rate of Device Output (ms)";
	protected int UpdateRate = 100;
	
	boolean mSensorConnected = false;
	
	Sensor mSensor;
	SensorManager mSensorManager;
	
	SensorTask mSensorTask;
	Timer mSensorTimer;
	
	public AndroidSensor(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, Sensor sensor, SensorManager sensorManager)
	{
		super(name, info, image, context, enabled, parent);
		
		mSensor = sensor;
		mSensorManager = sensorManager;
		
		addProperty(MaximumRangeString, sensor.getMaximumRange(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, MaximuRangeDescription, true);
		addProperty(PowerString, sensor.getPower(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, PowerDescription, true);
		addProperty(ResolutionString, sensor.getResolution(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, ResolutionDescription, true);
		addProperty(VersionString, sensor.getVersion(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, VersionDescription, true);
		addProperty(VendorString, sensor.getVendor(), ENTITY_PROPERTY_TYPE.EDIT_BOX, VendorDescription, true);
		addProperty(RateString, SensorManager.SENSOR_DELAY_UI, ENTITY_PROPERTY_TYPE.NUMBER_BOX, RateDescription, true);
		addProperty(UpdateRateString, (Integer)UpdateRate, ENTITY_PROPERTY_TYPE.NUMBER_BOX, UpdateRateDescription, false);
	}
	
	public void setSensor(Sensor sensor){
		mSensor = sensor;
	}
	public Sensor getSensor(){
		return mSensor;
	}
	
	
	
	@Override
	public boolean start() {
		if (!mSensorConnected){
			mSensorConnected = mSensorManager.registerListener(mSensorEventListener, mSensor, (Integer) ((EntityProperty)getProperty(AndroidSensor.RateString)).getData());
			mSensorTask = new SensorTask();
			mSensorTimer = new Timer();
			mSensorTimer.scheduleAtFixedRate(mSensorTask, 0, UpdateRate);
		}
		return true;
	}
	@Override
	public boolean stop() {
		if (mSensorConnected == true){
			mSensorManager.unregisterListener(mSensorEventListener);
			mSensorConnected = false;
		}
		if (mSensorTask != null){
			mSensorTask.cancel();
		}
		if (mSensorTimer != null){
			mSensorTimer.cancel();
			mSensorTimer.purge();
		}
		return true;
	}

	private class SensorTask extends TimerTask {

		@Override
		public void run() {
			updateSensorFunction();	
		}	
	}
	protected void updateSensorFunction (){
		
	}

	final protected SensorEventListener mSensorEventListener = new SensorEventListener (){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			AndroidSensorChangedFunction(event);
			
		}
	};
	protected void AndroidSensorChangedFunction(SensorEvent event){
		return;
	}
	final protected Handler SensorHandler = new Handler (){

		@Override
		public void handleMessage(Message msg) {
			SensorHandlerHandleMessage(msg);
		}
		
	};
	protected void SensorHandlerHandleMessage (Message msg){
		
	}

}
