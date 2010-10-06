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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cospandesign.android.gpi.lcm;

/**
 *
 * @author cospan
 */
public interface TCPBackgroundServiceCallback {
    //Notify when a client connects
    public void clientConnecting(int numOfClients);
    //Notify when a client disconnects
    public void clientDisconnecting(int numOfClients);
}
