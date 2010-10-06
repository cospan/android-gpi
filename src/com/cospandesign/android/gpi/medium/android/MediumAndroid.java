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

package com.cospandesign.android.gpi.medium.android;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.device.android.AndroidAccellerometer;
import com.cospandesign.android.gpi.device.android.AndroidLight;
import com.cospandesign.android.gpi.device.android.AndroidLocationDevice;
import com.cospandesign.android.gpi.device.android.AndroidMagneticField;
import com.cospandesign.android.gpi.device.android.AndroidOrientation;
import com.cospandesign.android.gpi.device.android.AndroidPreassure;
import com.cospandesign.android.gpi.device.android.AndroidProximity;
import com.cospandesign.android.gpi.device.android.AndroidSensor;
import com.cospandesign.android.gpi.device.android.AndroidTemperature;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.gpi.R;

public class MediumAndroid extends Medium implements SensorEventListener{

	SensorManager sensorManager;
	LocationManager locationManager;
	
	ArrayList<Device> AndroidDevices;
	Hashtable<String, AndroidLocationDevice> AndroidLocationTable;
	Hashtable<String, AndroidSensor> AndroidSensorTable;
	GpiConsole mConsole = GpiConsole.getinstance();
					
		
	public MediumAndroid(String name, String info, Integer image, Context context, boolean enabled) {
		super(name, info, image, context, enabled);
		AndroidDevices = new ArrayList<Device>();
		AndroidSensorTable = new Hashtable<String, AndroidSensor>();
		AndroidLocationTable = new Hashtable<String, AndroidLocationDevice>();
	}
	/* (non-Javadoc)
	 * @see com.cospandesign.ucs.Medium#start()
	 */
	@Override
	public boolean query()
	{
		sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sList = sensorManager.getSensorList(Sensor.TYPE_ALL);
		Device device =	null;
		mDevices.clear();
		for (Sensor sensor:sList)
		{
			
			switch (sensor.getType())
			{
			case (Sensor.TYPE_ACCELEROMETER):
				mConsole.info("Found Accellerometer");
				device = new AndroidAccellerometer (sensor.getName(), "Accellerometer", R.drawable.accelleration, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_LIGHT):
				mConsole.info("Found Light Sensor");
				device = new AndroidLight (sensor.getName(), "Light Sensor Sensor", R.drawable.light, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_MAGNETIC_FIELD):
				mConsole.info("Found Magnetic Field Sensor");
				device = new AndroidMagneticField (sensor.getName(), "Magnetic Field Sensor", R.drawable.magnetic, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_ORIENTATION):
				mConsole.info("Found Orientation Sensor");
				device = new AndroidOrientation (sensor.getName(), "Orientation Sensor", R.drawable.orientation, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_PRESSURE):
				mConsole.info("Found Preassure Sensor");
				device = new AndroidPreassure (sensor.getName(), "Pressure Sensor", R.drawable.pressure, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_PROXIMITY):
				mConsole.info("Found Proximity Sensor");
				device = new AndroidProximity (sensor.getName(), "Proximity Sensor", R.drawable.proximity, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			case (Sensor.TYPE_TEMPERATURE):
				mConsole.info("Found Temperature Sensor");
				device = new AndroidTemperature (sensor.getName(), "Temperature Sensor", R.drawable.temperature, mContext, true, this, sensor, sensorManager);
				//device.start();
				break;
			default:
				if (sensor instanceof Sensor){
					try {
						mConsole.info("Found Unknown Sensor");
						device = new AndroidSensor (sensor.getName(), "Unknown Sensor", R.drawable.unknown, mContext, true, this, sensor, sensorManager);
						mConsole.info("Added " + sensor.getName());
					}
					catch (Exception ex){
						ex.printStackTrace();
						//device = new AndroidSensor ("Unknown", ("Error In Device Properties"), R.drawable.unknown, context, true, this, sensor, sensorManager);
						mConsole.error("Error, failed to add unknown sensor");
					}
				}
				else
				{
					device = new AndroidSensor ("Unknown", ("Device Value is NULL"), R.drawable.unknown, mContext, true, this, sensor, sensorManager);
				}
				break;
			}
			AndroidDevices.add(device);
			mDevices.add(device);
			AndroidSensorTable.put(sensor.getName(), (AndroidSensor)device);
		}
		
		//check for location listener
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		//List<String> lmanagers = locationManager.getAllProviders();
		
		String providerName = locationManager.getBestProvider(((Criteria)new Criteria()), false);
		if (providerName != null){
			mConsole.info("Found a Location Manager");
			device = new AndroidLocationDevice ("Location Manager", "Generic Location Interface", R.drawable.location, mContext, true, this, locationManager);
			AndroidDevices.add(device);
			mDevices.add(device);		
		}
		return true;	
	}

	//Sensor Changed
//	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1)
	{

		
	}
//	@Override
	public void onSensorChanged(SensorEvent event)
	{

		
	}
	
}
