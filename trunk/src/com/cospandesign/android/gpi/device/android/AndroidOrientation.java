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

public class AndroidOrientation extends AndroidSensor {

	public static String AZIMUTH = "Azimuth";
	public static String PITCH = "Pitch";
	public static String ROLL = "Roll";
	
	private volatile float mAzimuth = 0.0f;
	private volatile float mPitch = 0.0f;
	private volatile float mRoll = 0.0f;
	
	public AndroidOrientation(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, Sensor sensor,
			SensorManager sensorManager) {
		super(name, info, image, context, enabled, parent, sensor, sensorManager);
		addOutputDataChannel(ROLL, Float.class);
		addOutputDataChannel(PITCH, Float.class);
		addOutputDataChannel(AZIMUTH, Float.class);
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
		bundle.putFloatArray("Orientation", accFloat);
		msg.setData(bundle);
		SensorHandler.sendMessage(msg);
		*/
		mAzimuth = event.values[0];
		mPitch = event.values[1];
		mRoll = event.values[2];
	}
/*
	@Override
	protected void SensorHandlerHandleMessage(Message msg) {
		float[] accFloat = msg.getData().getFloatArray("Orientation");
		OutputData(AZIMUTH, accFloat[0]);
		OutputData(PITCH, accFloat[1]);
		OutputData(ROLL, accFloat[2]);
	}
*/

	@Override
	protected void updateSensorFunction() {
		final float azimuth = mAzimuth;
		final float pitch = mPitch;
		final float roll = mRoll;
		
		outputData(AZIMUTH, azimuth);
		outputData(PITCH, pitch);
		outputData(ROLL, roll);
	}
	
}
