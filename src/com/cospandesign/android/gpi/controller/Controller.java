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

package com.cospandesign.android.gpi.controller;

import java.util.ArrayList;

import android.content.Context;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.medium.Medium;

public class Controller extends Entity
{
	ArrayList<Medium> mMediums;

	public Controller(String name, String info, Integer image, Context c, boolean enabled)
	{
		super(name, info, image, c, enabled);
		mMediums = new ArrayList<Medium>();
	}

	/**
	 * @return the mediums
	 */
	public ArrayList<Medium> getMediums(){
		return mMediums;
	}

	public void addMedium (Medium medium){
		mMediums.add(medium);
	}
	
}
