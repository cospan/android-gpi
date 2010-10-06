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

public final class GpiConstants {
	static final String LOG = "GPI";
	
	
	static final public int RETURN_ERROR = -1;
	static final public int RETURN_NO_CHANGE = 0;
	static final public int RETURN_SUCCESS = 1;
	
	public final static float MAX_ZOOM = 100.0f;
	public final static float MIN_ZOOM = .01f;
	public final static float DEFAULT_ZOOM = 1.0f;
	
	static final public String PROVIDER_STRING = "Provider String";
	static final public String START_SERVER_STRING = "Start Server";
	static final public String SERVER_PORT_STRING = "Server Port";
	static final public String TCP_SERVICE_CLIENTS_STRING = "TCP Service Clients";
	static final public String ANDROID_PUBSUB_STRING = "Android PubSub";
	static final public String ANDROID_PUBSUB_BOARD_ID = "Android Board Id";
	
	static final public String STRUCTURE_MANAGER_STRING = "Structure Manager";
	
	static final public String STRUCTURE_NAME = "Structure Name";
	static final public String STRUCTURE_INFO = "Structure Info";
	
	public static final String CHANNEL_ICON = "Icon";
	public static final String CHANNEL_NAME = "Name";
	public static final String CHANNEL_INFO = "Info";
	public static final String CHANNEL_STRUCTURE = "Structure";	
}
