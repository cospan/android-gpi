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

/* Channel.java
 *
 * This is an instantiation of a structure, it holds the instantion names, and
 * any variables needed to send and recieve information from this structure
 */

package dlcm;

import dlcm.dlcm.DLCM;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import lcm.lcm.LCMEncodable;

/**
 *
 * @author David McCoy and Micheal Shareghi
 *
 * A Channel of data, or this can be thought of as an instantiation of a Structure
 *
 */
public class Channel implements LCMEncodable{
	
    private DLCM mChannelManager;
    private String mChannelName;
    public Structure mStructure;

    //Flags
    private boolean mCanPublish;
    private boolean mCanSubscribe;


    //Constructors/Initializers
    /**
     * This constructure is not generally called
     */
    public Channel(){
            mCanPublish = true;
            mCanSubscribe = true;
    }
    /**
     * The structure usually called
     *
     * @param channelManager a DLCM instance that controls the channels
     * @param structure Structure that will be associated with this channel
     * @param channelName Name of the channel
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Channel(DLCM channelManager, Structure structure, String channelName) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
            this();
            mChannelManager = channelManager;
            mStructure = (Structure) structure.clone();
            mStructure.initialize(mChannelManager);
            mChannelName = new String (channelName);
    }


    //Flags
    /**
     *
     * @param flag
     */
    public void setCanPublish(boolean flag){
            mCanPublish = flag;
    }
    /**
     *
     * @param flag
     */
    public void setCanSubscribe(boolean flag){
            mCanSubscribe = flag;
    }

    /**
     *
     * @return true if this channel can publish
     */
    public boolean canPublish(){
            return mCanPublish;
    }
    /**
     *
     * @return true if this channel can be subscribed to
     */
    public boolean canSubscribe(){
            return mCanSubscribe;
    }


    //Getters/Setters
    /**
     * Sets the name of this channel
     * @param channelName Name of the channel
     */
    public void setChannelName(String channelName){
            mChannelName = new String (channelName);
    }
    /**
     * Sets the channel manager
     * @param dlcm channel manager
     */
    public void setDLCM(DLCM dlcm){
            mChannelManager = dlcm;
    }

    /**
     *
     * @return channel name
     */
    public String getChannelName(){
            return mChannelName;
    }
    /**
     * gets the Channel manager
     * @return Channel manager
     */
    public DLCM getDLCM(){
            return mChannelManager;
    }

    //Encode
    /**
     * encode data for transmitting with LCM
     *
     * @param outs encoded bytes to send
     * @throws IOException
     */
    public void encode(DataOutput outs) throws IOException{
            mStructure.encode(outs);
    }

    //Decode
    /**
     * decode data from an LCM receive
     *
     * @param inps data to decode by channel and the underlying structure
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void decode(DataInput inps) throws IOException, ClassNotFoundException{
            mStructure.decode(inps);
    }


    public void _encodeRecursive(DataOutput d) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param di
     * @throws IOException
     */
    public void _decodeRecursive(DataInput di) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
