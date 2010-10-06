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

/**
 * 
 */
package com.cospandesign.android.gpi.medium;

import java.util.ArrayList;

import android.content.Context;

import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.Entity;

/**
 * @author David McCoy
 *
 */
public class Medium extends Entity {
	

	protected ArrayList<Device> mDevices;
	
	public Medium(String name, String info, Integer image, Context context, boolean enabled) {
		super(name, info, image, context, enabled);
		
		mDevices = new ArrayList<Device>();
	}
	public ArrayList<Device> getDevices() {
		return mDevices;
	}
	public void setDevices(ArrayList<Device> devices) {
		mDevices = devices;
	}
	public void AddDevice(Device device){
		mDevices.add(device);
	}
	
	public boolean stop(){
		return false;
	}
	public boolean start(){
		//start the medium
		return false;
	}
	public boolean query(){
		return false;
	}

}
