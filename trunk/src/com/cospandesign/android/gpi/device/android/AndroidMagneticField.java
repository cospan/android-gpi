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

public class AndroidMagneticField extends AndroidSensor{
	
	public static String MAG_X = "X";
	public static String MAG_Y = "Y";
	public static String MAG_Z = "Z";
	
	private volatile float mMagX = 0.0f;
	private volatile float mMagY = 0.0f;
	private volatile float mMagZ = 0.0f;
	
	public AndroidMagneticField(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, Sensor sensor,
			SensorManager sensorManager) {
		super(name, info, image, context, enabled, parent, sensor, sensorManager);
		
		addOutputDataChannel(MAG_Z, Float.class);
		addOutputDataChannel(MAG_Y, Float.class);
		addOutputDataChannel(MAG_X, Float.class);
	}

	@Override
	protected void AndroidSensorChangedFunction(SensorEvent event) {
		/*
		Message msg = SensorHandler.obtainMessage();
		Bundle bundle = new Bundle();
		float[] accFloat = new float[3];// = {event.values[0], event.values[1], event.values[2]};
		accFloat[0] = event.values[0];
		accFloat[1] = event.values[1];
		accFloat[2] = event.values[2];
		bundle.putFloatArray("Magnetic", accFloat);
		msg.setData(bundle);
		SensorHandler.sendMessage(msg);
		*/
		mMagX = event.values[0];
		mMagY = event.values[1];
		mMagZ = event.values[2];
	}
/*
	@Override
	protected void SensorHandlerHandleMessage(Message msg) {
		float[] accFloat = msg.getData().getFloatArray("Magnetic");
		OutputData(MAG_X, accFloat[0]);
		OutputData(MAG_Y, accFloat[1]);
		OutputData(MAG_Z, accFloat[2]);
	}
*/

	@Override
	protected void updateSensorFunction() {
		final float magX = mMagX;
		final float magY = mMagY;
		final float magZ = mMagZ;
		
		outputData(MAG_X, magX);
		outputData(MAG_Y, magY);
		outputData(MAG_Z, magZ);
	}
	
}
