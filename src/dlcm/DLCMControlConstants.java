/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of DLCM.
 *
 *  DLCM is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DLCM is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DLCM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*DLCMConstants.java
 *
 * All Constants associated with DLCM
 *
 * The only LCM structure that must be used is the "control" structure,
 * these constants are used to send, and receive commands from foreign PubSubs
 * 
 *
 */

package dlcm;

/**
 *
 * @author David McCoy and Micheal Shareghi
 * Constants associated with the Control structure of the LCM type control_t
 * used to send control information between publisher/subscribers
 */
public class DLCMControlConstants {

    /**
     * Ping all remote publisher/subscribers, used to get identification
     */
    public static int PING = 1;
    /**
     * Request the structures that a publisher/subscriber contains
     * @see dlcm.builder.StructureManager
     */
    public static int STRUCTURES = 2;
    /**
     * Request a "Channel String"
     * @see dlcm.ChannelParser
     */
    public static int CHANNELS = 3;
    /**
     * Check if a channel name is already in use
     * XXX this functionality has not been tested
     */
    public static int CHANNEL_CHECK = 4;

    
    /**
     * User function 1
     */
    public static int USER_1 = 100;
    /**
     * User function 2
     */
    public static int USER_2 = 101;
    
}
