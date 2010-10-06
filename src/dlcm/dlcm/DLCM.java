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

/* DLCM.java
 *
 * Main DLCM interface
 *
 * DLCM is to be implemented many times one for each provider.
 * Due to the dynamic nature of DLCM the user code normally doesn't add new
 * channels in code like LCM was designed for, for that reason DLCM will handle
 * more of the receive, instantiation, and removing of structures and channels
 *
 */

package dlcm.dlcm;

import dlcm.DLCMListener;
import dlcm.ChannelParser;
import dlcm.Channel;
import dlcm.Member;
import dlcm.Structure;
import dlcm.builder.StructureManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMEncodable;
import lcm.lcm.LCMSubscriber;

/**
 *
 * @author David McCoy
 *
 * DLCM interface in which all Subscribers/Publishers will interface with This is started after LCM is connected, and takes LCM as an argument
 *
 */
public class DLCM implements LCMSubscriber {

    StructureManager mStructureManager;
    HashMap <String, Channel> ChannelMap;
    ArrayList <DLCMListener> Listeners;
    LCM mLCM;

    /**
     * 
     * Constructs a new DLCM channel
     *
     * @param structureManger The {@link dlcm.builder.StructureManager manager} of the structures for all local subscribers and server
     * @param lcm The LCM class that has already been connected
     * @throws Exception Thrown when user attempts to set up DLCM before the {@link dlcm.builder.StructureManager} has been setup
     */
    public DLCM(StructureManager structureManger, LCM lcm) throws Exception{
        ChannelMap = new HashMap<String, Channel>();
        Listeners = new ArrayList<DLCMListener>();
        mStructureManager = structureManger;
        mLCM = lcm;
        if (mStructureManager == null){
            throw new Exception ("Structure Manger must be set up prior to " +
                    "Channel Manger");
        }
    }

    /**
     *
     * Add a new Listener which will receive callbacks whenever DLCM receives a message, add/remove a channel, and throws an error
     * @see dlcm.DLCMListener
     *
     * @param listener
     */
    public void addDLCMListener(DLCMListener listener){
        Listeners.add (listener);
    }
    private void BroadCastErrors (String message, String channel){
        for (DLCMListener listener : Listeners){
            listener.DLCMError(message, channel);
        }
    }
    private void BroadCastChannelAdded (String channel){
        for (DLCMListener listener : Listeners){
            listener.DLCMChannelAdded(channel);
        }
    }
    private void BroadCastChannelRemoved (String channel){
        for (DLCMListener listener : Listeners){
            listener.DLCMChannelRemoved(channel);
        }
    }
    private void BroadCastMessageReceived (String channelName, Structure structure){
        for (DLCMListener listener : Listeners){
            listener.DLCMMessageReceived(channelName, structure);
        }
    }


    /**
     *Reset DLCM's channel map, removing all local and foreign channels
     */
    public void ResetManager(){
        ChannelMap.clear();
    }

    //Structure Functions
    /**
     *
     * Remove a structure from the channel map
     * Note: this may be erronious
     *
     * @param structureName name of the structure to remove
     */
    public void RemoveStructure(String structureName){
        //look through the map and see if there is a structure that matches, if so remove
        ArrayList <String> removeList = new ArrayList<String>();

        //if the structure names match, add it to the remove list
        for (String name : ChannelMap.keySet()){
            if (ChannelMap.get(name).mStructure.getStructureName().compareTo(structureName) == 0){
                removeList.add(name);
            }
        }

        //remove the list, this method was chosen as apposed to removing in the for
        //loop because the other method can mess up the "for" algorithm
        for (String name : removeList){
            ChannelMap.remove(name);
        }
    }
    /**
     *
     * Searches for the Structure in the structure channels
     *
     * @param structure Name of the structure to query for
     * @return true if structure is found
     */
    public boolean ContainsStructure(Structure structure){
        for (String name : ChannelMap.keySet()){
            if (ChannelMap.get(name).mStructure.getStructureName().compareTo(structure.getStructureName()) == 0){
                return true;
            }
        }
        return false;
    }
    /**
     * Query the structure manager for the given structure,
     * This is useful to determine if a local Publisher/Subscriber can instantiate a class from a foreign Publish/Subscriber
     * without quering the foreign Publish/Subscrber for associated structures
     *
     * @param structureName name of the structure to search for
     * @return true if structure exists in the structure manager
     */
    public boolean StructureExists(String structureName){
        return mStructureManager.containsStructure(structureName);
    }
    /**
     *
     * Clone a structure from the structure manager, this is useful if you want to generate a new Structure to publish
     * due to the fact that much of DLCM is controlled at runtime this function simplifies getting a new Structure
     *
     * @param structureName Name of the structure to get
     * @return a refrence to a new structure specified by structureName, or null if the structure doesn't exists
     */
    public Structure CloneStructure(String structureName){
        if (mStructureManager.containsStructure(structureName)){
            return (Structure) mStructureManager.getStructure(structureName).clone();
        }
        return null;
    }
    /**
     *
     * Parses a LCMGen String, it can come from a local or foreign source, this is a complement to {@link dlcm.builder.LCMStringGenerator#GenerateLCMString() }
     * which will generate a string LCMGen string that is suitable for sending to Foreign Publisher/Subscribers, if there are exceptions while parsing the structure
     * the implemented {@link dlcm.DLCMListener#DLCMError(java.lang.String, java.lang.String) } will be called
     * @see dlcm.builder.LCMStringGenerator
     * @see dlcm.DLCMListener
     *
     * @param structureString String received from foreign Publisher Subscriber
     *
     */
    public void ParseStructure(String structureString) {
        try {
            mStructureManager.parseString(structureString);
        } catch (Exception ex) {
            BroadCastErrors(ex.getMessage(), null);
        }
    }
    /**
     *
     *
     *
     * @param channelString
     * @return
     * @throws Exception
     */
    public boolean UnknownStructures(String channelString) throws Exception{
        ChannelParser parser = new ChannelParser(this);
        return parser.unknownStructures(channelString);
    }

    //Channel Functions
    /**
     *
     * Adds a new channel to DLCM specifying the name of the channel, and the channel itself
     * Upon successfully adding the channel the {@link dlcm.DLCMListener#DLCMChannelAdded(java.lang.String)}
     * function is called
     *
     * @param channelName Name of the channel to add
     * @param channel Wrapper class for a structure, that contains specific information about that channel
     *
     * @see dlcm.Channel
     * @see dlcm.DLCMListener
     *
     * @return true if channels is added
     */
    public boolean AddChannel(String channelName, Channel channel){
        if (ChannelMap.containsKey(channelName)){
            return false;
        }
        ChannelMap.put(channelName, channel);
        mLCM.subscribe(channelName, this);
        this.BroadCastChannelAdded(channelName);
        return true;
    }
    /**
     * Remove an channel from DLCM, upon successfully removing the channel
     * {@link dlcm.DLCMListener#DLCMChannelRemoved(java.lang.String) } function is called
     *
     * @param channelName Name of the channel to remove
     */
    public void RemoveChannel(String channelName){
        if (ChannelMap.containsKey(channelName)){
            ChannelMap.remove(channelName);
            this.BroadCastChannelRemoved(channelName);
        }
    }
    /**
     * Takes a string and generates an Channel, This function is intended to be used with a Publisher/Subscriber in order
     * to map foreign, and local Channels
     *
     * This is a front end for {@link dlcm.ChannelParser}, usually DLCM will mitigate all communication between a Publisher/Subscriber, and ChannelParser
     * @see dlcm.ChannelParser
     *
     * @param channelString String which contains information about a Channel
     *
     * @return a set of strings for each individula channel
     * @throws Exception Error in the channel string was detected
     */
    public String[] ParseChannel(String channelString) throws Exception{
        //Returns the name of the channels added
        ChannelParser parser = new ChannelParser(this);
        return parser.parseChannelString(channelString);
    }
    /**
     *
     * Queries DLCM for a specific Channel
     *
     * @param channelName Name of the channel to search for
     * @return true if the Channel is in DLCM
     *
     */
    public boolean ContainsChannel(String channelName){
        return ChannelMap.keySet().contains(channelName);
    }
    /**
     *
     * Generate a new name in the form "channel###"
     * This function will also check a potential name for conflicts
     *
     * @param requestChannelName requested channel name, can be null
     * 
     * @return requestChannelName if there is no conflict, requestChannelName### if there is a conflict, or channl### if requestChannelName == null
     */
    public String GenerateChannelName(String requestChannelName){
        //either there is no name, and we need a new one, or there is a reference to a name, that wont work
        if (requestChannelName == null){
            //need to create a new name
            return "chanenl" + (new Integer ((new Random().nextInt(1000)))).toString();
        }
        //check if the last of the string contains any numbers
        while (requestChannelName.matches(".*[0-9]")){
            if (requestChannelName.length() < 2){
                return "channel" + (new Integer ((new Random().nextInt(1000)))).toString();
            }
            requestChannelName = requestChannelName.substring(0, requestChannelName.length() - 2);
        }
        return requestChannelName;
    }
    /**
     *
     * A front end for GenerateChannelName, usually used when quering foreign Publisher/Subscriber, and resolving a name
     *
     * @param channelName name of the channel that needs to be modified
     * @return a new channel name that can be tested again for name conflicts
     */
    public String FixChannelName(String channelName){
        String newName = GenerateChannelName(channelName);
        Channel channel = ChannelMap.get(channelName);
        ChannelMap.remove(channelName);
        while (ContainsChannel(newName)){
            newName = GenerateChannelName(channelName);
        }
        channel.setChannelName(newName);
        ChannelMap.put(newName, channel);
        return newName;
    }
    /**
     * Get the channel of a channel, this channel can be used to check the
     * input output direction of a {@link dlcm.Member} within {@link dlcm.Structure}
     * as well as current values of {@link dlcm.Member}
     * 
     * @see dlcm.Channel
     * @see dlcm.Structure
     * @see dlcm.Member
     *
     * @param channelName
     * @return an {@link dlcm.Channel} that corresponds to the channel
     */
    public Channel getChannel(String channelName){
        if (!ChannelMap.containsKey(channelName)){
            return null;
        }
        return ChannelMap.get(channelName);
    }
    /**
     * Returns a clone of the structure associated with an channel, this structure can be populated
     * for a Publish
     *
     * @param channelName
     * @return a clone of the {@link dlcm.Structure} associated with {@link dlcm.Channel}
     *
     * @see dlcm.Channel
     * @see dlcm.Structure
     *
     */
    public Structure getChannelStructure(String channelName)  {
       if (!ChannelMap.containsKey(channelName)){
            return null;
        }
        return CloneStructure(ChannelMap.get(channelName).mStructure.getStructureName());

    }

    /**
     * Generate an Channel String associated with an {@link dlcm.Channel}, this string
     * can be sent to foreign Publisher/Subscribers that will use {@link #ParseChannel(java.lang.String) }
     * to create a proxy for the Channel, in other words creates a string from an channel to share with
     * a foreign Publisher/Subscriber
     *
     * @param channelName Channel that DLCM manages
     * @return String which can be sent to foreign Publisher/Subscribers
     */
    public String GenerateChannelString(String channelName){

        String RawChannelString = null;

        if (!ChannelMap.containsKey(channelName)){
            return null;
        }
        Channel channel = ChannelMap.get(channelName);

        RawChannelString = "";
        RawChannelString += channel.mStructure.getStructureName() + " ";
        RawChannelString += channelName + " ";

        ArrayList<Member> members = channel.mStructure.getMemberList();

        for (Member member : members){
           RawChannelString += member.Name;
           if (member.Constant){
               RawChannelString += ":c:" + member.ConstantData;
               continue;
           }
           if (member.canPublish){
               RawChannelString += ":p";
           }
           if (member.canSubscribe){
               RawChannelString += ":s";
           }
           RawChannelString += " ";

        }
        return RawChannelString + "\n";
    }
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
        if (ChannelMap.keySet().contains(channel)){
            try {
                try {
                    ChannelMap.get(channel).decode(ins);
                } catch (ClassNotFoundException ex) {
                    this.BroadCastErrors(ex.getMessage(), channel);
                    return;
                }
            } catch (IOException ex) {
                this.BroadCastErrors(ex.getMessage(), channel);
                return;
            }
            Structure structure = (Structure) ChannelMap.get(channel).mStructure;
            this.BroadCastMessageReceived(channel, structure);
        }
    }
    /**
     *
     * Generate a new Channel from a channel name
     * structureName should be the name of a strutcture in {@link dlcm.builder.StructureManager}
     *
     * @param channelName Name associated with this channel
     * @param structureName Name of the structure in which to create an channel of
     */
    public void GenerateNewChannel(String channelName, String structureName){
        if (ContainsChannel(channelName)){
            this.BroadCastErrors("Channel already exists", channelName);
            return;
        }
        Structure structure = CloneStructure(structureName);
        try {

                structure.initialize(this);

        } catch (IOException ex) {
            Logger.getLogger(DLCM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DLCM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DLCM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DLCM.class.getName()).log(Level.SEVERE, null, ex);
        }
        Channel channel = new Channel();
        channel.mStructure = structure;
        channel.setChannelName(channelName);
        this.AddChannel(channelName, channel);
        //ChannelMap.put(channelName, channel);
    }

    //LCM
    /**
     *
     * @param channel
     * @param structure
     */
    public void publish(String channel, LCMEncodable structure){
        mLCM.publish(channel, structure);
    }
}