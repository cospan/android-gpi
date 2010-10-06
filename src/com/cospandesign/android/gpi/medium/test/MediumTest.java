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

package com.cospandesign.android.gpi.medium.test;

import android.content.Context;

import com.cospandesign.android.gpi.device.test.TestIntegerDevice;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.gpi.R;

public class MediumTest extends Medium
{
	Context mContext;
	
	boolean IntegerDeviceEnable = false;
	boolean FloatDeviceEnable = false;
	boolean CharacterDeviceEnable = false;
	boolean ImageDeviceEnable = false;
	boolean AudioDeviceEnable = false;
	boolean VideoDeviceEnable = false;

	public MediumTest(String name, String info, Integer image, Context context,
			boolean enabled)
	{
		super(name, info, image, context, enabled);
		
		mContext = context;
		
		if (enabled){
			IntegerDeviceEnable = true;
			FloatDeviceEnable = true;
			CharacterDeviceEnable = true;
			ImageDeviceEnable = true;
			AudioDeviceEnable = true;
			VideoDeviceEnable = true;
		}
		
		AddDevice(
		new TestIntegerDevice(	
				mContext.getString(R.string.test_integer_name), mContext.getString(R.string.test_integer_info), 
				R.drawable.test_int,
				mContext,
				true,
				this
				)
		);

	}

	//Override
	@Override
	public boolean start()
	{
		
		return true;
	}
	
	

}
