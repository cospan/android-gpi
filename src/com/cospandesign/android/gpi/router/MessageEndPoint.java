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

public interface MessageEndPoint
{
	//Set the message router
	public void setMessageRouter(MessageRouter router);
	//Send a Message to the Message Router
	public void SendMessage(MessageRouter.MESSAGE_TYPE messageType, MessageRouter.DIRECTED directed, MessageEndPoint from, MessageEndPoint to, Object Data);
	//Receive a Message from the Message Router
	public void ReceiveMessage(MessageRouter.MESSAGE_TYPE messageType, MessageEndPoint from, MessageEndPoint to, Object Data);
}
