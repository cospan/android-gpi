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


public class AndroidTemperature extends AndroidSensor {

	public static String TEMPERATURE = "Temperature";
	private volatile float mTemperature = 0.0f;
	
	public AndroidTemperature(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, Sensor sensor,
			SensorManager sensorManager) {
		super(name, info, image, context, enabled, parent, sensor, sensorManager);
		addOutputDataChannel(TEMPERATURE, Float.class);
	}
	
	@Override
	protected void AndroidSensorChangedFunction(SensorEvent event) {
		/*
		Message msg = SensorHandler.obtainMessage();
		msg.obj = new Float (event.values[0]);
		SensorHandler.sendMessage(msg);
		*/
		mTemperature = event.values[0];
	}

	@Override
	protected void updateSensorFunction() {
		final float temperature = mTemperature;
		outputData(TEMPERATURE, temperature);
	}

	/*
	@Override
	protected void SensorHandlerHandleMessage(Message msg) {
		OutputData(TEMPERATURE, (Float)msg.obj);
	}
	*/
	

}
