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

package com.cospandesign.android.gpi.router;


public interface MessageRouter
{
	public enum MESSAGE_TYPE{
		ADD_WSE,
		START_DRAG,
		STOP_DRAG,
		DRAGGING,
		DROP,
		ADDING_ME,
		CANT_FIND_ENTITY,
		ENTITY_BUFFER_FULL,
		REMOVING_ME,
		REMOVING_WSE,
		RESIZING_WSE,
		RESIZING_ME,
		SELECTED_WSE,
		SELECTED_CONTROL_CANVAS,
		SELECTED_CONNECTION_LABEL,
		CONNECT_THROUGH_CANVAS,
		CONNECT_THROUGH_LIST,
		TO_WIDGET_CANVAS,
		TO_CONTROL_CANVAS,
		SET_OPTIONS,
		STOP_SCROLLING,
		START_SCROLLING
	}
	public enum DIRECTED{
		BROADCAST,
		DIRECTED
	}
	
	//Receive Message From Message End Points
	public void MessageToRouter(MESSAGE_TYPE mt, DIRECTED directed, MessageEndPoint from, MessageEndPoint to, Object Data);
	//Broadcast Message to all Message End Points
	public void BroadcastMessage(MESSAGE_TYPE mt, MessageEndPoint from, Object Data);
	//Send Message Directly to an item
	public void DirectMessage(MESSAGE_TYPE mt, MessageEndPoint from, MessageEndPoint to, Object Data);
	//helper function for logging data
	public void LogMessage(MESSAGE_TYPE mt, DIRECTED directed, MessageEndPoint from, MessageEndPoint to, Object Data);
}
