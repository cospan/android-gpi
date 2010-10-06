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

package com.cospandesign.android.gpi;

import android.util.Log;

public class GpiConsole {
	private static GpiConsole instance = null;
	private static String mConsole;
	private boolean mDebugFlag = false;
	
	protected GpiConsole(){
		//Exists only to defeat instantiation
	}
	public static GpiConsole getinstance() {
		if (instance == null){
			instance = new GpiConsole();
			mConsole = "";
		}
		return instance;
	}
	
	
	public void consoleOut(String output){
		mConsole += output + "\n";
	}
	
	public void verbose(String verbose){
		Log.v(GpiConstants.LOG, verbose);
		mConsole += verbose + "\n";
	}
	public void debug(String debug){
		Log.d(GpiConstants.LOG, debug);
		mConsole += debug + "\n";
	}
	public void info(String info){
		Log.i(GpiConstants.LOG, info);
		mConsole += info + "\n";
	}
	public void warning(String warning){
		Log.w(GpiConstants.LOG, warning);
		mConsole += warning + "\n";
	}
	public void error(String error){
		Log.e(GpiConstants.LOG, error);
		mConsole += error + "\n";
	}
	
	public String getConsole(){
		return mConsole;
	}

}
