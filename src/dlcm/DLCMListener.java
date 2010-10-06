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

/* DLCMListener.java
 *
 * interface to allow classes to receive DLCM notifications and messages
 *
 */

package dlcm;


/**
 *
 * @author David McCoy
 *
 * DLCM listener callback functions
 */
public interface DLCMListener {

    /**
     * A channel was added to DLCM either locally or through the {@link dlcm.ChannelParser}
     * @param channelName Name of the channel that was added
     */
    public void DLCMChannelAdded(String channelName);

    /**
     * A channel was removed from DLCM, this function is called after a channel is removed
     * @param channelName Name of the channel that was removed
     */
    public void DLCMChannelRemoved(String channelName);

    /**
     * Data was received by DLCM
     * @param channelName Name of channel that data arrived on
     * @param structure an instantiation of a structure with all the members set from a remote provider
     */
    public void DLCMMessageReceived(String channelName, Structure structure);

    /**
     * An error occured in DLCM
     * @param error a String describing the error
     * @param channel name of the channel associated with the error
     */
    public void DLCMError(String error, String channel);
}
