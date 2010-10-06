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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.cospandesign.android.gpi.medium.Medium;

public class AndroidAccellerometer extends AndroidSensor {

	public static String ACC_X = "X Accelleration";
	public static String ACC_Y = "Y Accelleration";
	public static String ACC_Z = "Z Accelleration";
	
	private volatile float mAccX = 0.0f;
	private volatile float mAccY = 0.0f;
	private volatile float mAccZ = 0.0f;
	

	
	public AndroidAccellerometer(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, Sensor sensor,
			SensorManager sensorManager) {
		super(name, info, image, context, enabled, parent, sensor, sensorManager);
		addOutputDataChannel(ACC_Z, Float.class);
		addOutputDataChannel(ACC_Y, Float.class);
		addOutputDataChannel(ACC_X, Float.class);		
	}
	
	@Override
	protected void AndroidSensorChangedFunction(SensorEvent event) {
		//Message msg = SensorHandler.obtainMessage();
		//Bundle bundle = new Bundle();
		//float[] accFloat = new float[3];// = {event.values[0], event.values[1], event.values[2]};
		//accFloat[0] = event.values[0];
		//accFloat[1] = event.values[1];
		//accFloat[2] = event.values[2];
		//OutputData(ACC_X, event.values[0]);
		//OutputData(ACC_Y, event.values[1]);
		//OutputData(ACC_Z, event.values[2]);
		//bundle.putFloatArray("Accellerometer", accFloat);
		//msg.setData(bundle);
		//SensorHandler.sendMessage(msg);
		
		mAccX = event.values[0];
		mAccY = event.values[1];
		mAccZ = event.values[2];
	}
/*
	@Override
	protected void SensorHandlerHandleMessage(Message msg) {
		float[] accFloat = msg.getData().getFloatArray("Accellerometer");
		OutputData(ACC_X, accFloat[0]);
		OutputData(ACC_Y, accFloat[1]);
		OutputData(ACC_Z, accFloat[2]);
	}
*/
	@Override
	protected void updateSensorFunction() {
		final float accX = mAccX;
		final float accY = mAccY;
		final float accZ = mAccZ;
		outputData(ACC_X, accX);
		outputData(ACC_Y, accY);
		outputData(ACC_Z, accZ);
	}
	
}
