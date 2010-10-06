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

package com.cospandesign.android.pubsub;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lcm.lcm.LCM;
import lcmtypes.control_t;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.gpi.R;

import dlcm.DLCMListener;
import dlcm.Member;
import dlcm.Structure;
import dlcm.builder.StructureManager;
import dlcm.dlcm.DLCM;

/**
 * @author David McCoy
 *
 * PubSubManager is the connector between the DLCM network endpoints
 * and the user interface, it is responsible for organizing PubSubs,
 * adding channels, and directing information to and from the network
 * to the Devices, or Channels used to send receive data in the Workspace
 * 
 */

public class PubSubManager implements DLCMListener {

	LCM mLCM;
    DLCM mDLCM;
    StructureManager mStructureManager;
    private HashMap <String, PubSub> PubSubMap;
    ArrayList <String> UnparsedInstanceStrings;
    ArrayList<Device> mDevices;
    HashMap<String, PubSubDevice> mDeviceMap;
    Medium mParent;
    Context mContext;
    
	static final int DLCM_ERROR_MESSAGE = 2;
	static final int DLCM_INSTANCE_ADDED = 3;
	static final int DLCM_INSTANCE_REMOVED = 4;    


    /**
     * Default Constructor, initializes Hashmaps, and sets up connections
     * 
     * @param lcm LCM generate when Provider attached to network
     * @param dlcm DLCM generated when Provider attached to the network
     * 	used here to dynamically marshal info to/from channels, and network
     * @param structureManager Application wide structure manager used to control in/out of dlcm structures
     * @param parent Medium provider that is utilizing the PubSubManager
     * @param devices Medium Devices for Android SubServ, and Channels for DLCM
     * @param context Application Context used for Views
     */
    public PubSubManager (LCM lcm, DLCM dlcm, StructureManager structureManager, Medium parent, ArrayList<Device> devices, Context context){
        mLCM = lcm;
        mDLCM = dlcm;
        mStructureManager = structureManager;
        PubSubMap = new HashMap<String, PubSub>();
        mDLCM.addDLCMListener(this);

        UnparsedInstanceStrings = new ArrayList<String>();
        mContext = context;
        mParent = parent;
        mDevices = devices;
        mDeviceMap = new HashMap<String, PubSubDevice>();
    }

    /**
     * Called when the structure manager has changed either added, or removed a structure
     */

    public void StructuresChanged(){
        //Analyze all PubSub's and see if there is something different, if so,
        //fix the connections, and structures

        //compare each pubsub to see if they share the same structure ID, if so,
        //we need to delete all the forign channel associate with the changed
        //structure
    }

    //DLCM Functions
    /* (non-Javadoc)
     * @see dlcm.DLCMListener#DLCMInstanceAdded(java.lang.String)
     */
    public void DLCMChannelAdded(String channelName) {
		Message msg = uiHandler.obtainMessage(this.DLCM_INSTANCE_ADDED, channelName);
		uiHandler.sendMessage(msg);
    }
    /* (non-Javadoc)
     * @see dlcm.DLCMListener#DLCMInstanceRemoved(java.lang.String)
     */
    public void DLCMChannelRemoved(String channelName) {
		Message msg = uiHandler.obtainMessage(this.DLCM_INSTANCE_REMOVED, channelName);
		uiHandler.sendMessage(msg);
    }
    /* (non-Javadoc)
     * @see dlcm.DLCMListener#DLCMMessageReceived(java.lang.String, dlcm.Structure)
     */
    public void DLCMMessageReceived(String channelName, Structure structure) {
    	//mDeviceMap.get(channelName).ReceiveDataFromProvider(structure);
		DLCMReceiveRunnable drr = new DLCMReceiveRunnable();
		drr.channelName = channelName;
		drr.structure = structure;
		uiHandler.post(drr);
    }
    /* (non-Javadoc)
     * @see dlcm.DLCMListener#DLCMError(java.lang.String, java.lang.String)
     */
    public void DLCMError(String error, String channel) {
		Message msg = uiHandler.obtainMessage(this.DLCM_ERROR_MESSAGE);
		Bundle bundle = new Bundle();
		bundle.putString("error", error);
		bundle.putString("channel", channel);
		msg.setData(bundle);
		uiHandler.sendMessage(msg);
    }
    
    //Send To DLCM
    /**
     * Send information to DLCM to publish on the network
     * 
     * @param channelName Channel identifier to send the data
     * @param structure Structure to send
     */
    public void PublishStructure(String channelName, Structure structure){
    	mDLCM.publish(channelName, structure);
    }
    
    //PubSub Functions
    /**
     * Returns a list of PubSubs that PubSubManager is currently Managing
     * 
     * 
     * @return Object[] Object list of data, useful for Swing Lists 
     */
    public Object[] getPubSubNamesListData(){
    	ArrayList<String> PubSubNames = new ArrayList<String>();
    	
    	for (String name : PubSubMap.keySet()){
    		PubSubNames.add(name);
    	}
    	
    	Object[] names = new Object[PubSubNames.size()];
    	names = PubSubNames.toArray();
    	return names;
    }
    /**
     * Returns a list of Channels associated with a PubSub
     * 
     * @param pubSubName PubSub Name used to identify the channels to send
     * @return Object[] a list of the names of channels associated with a PubSub
     */
    public Object[] getChannelsListData(String pubSubName){
    	PubSub psi = PubSubMap.get(pubSubName);
    	ArrayList<String> Channels = new ArrayList<String>();
    	
    	for (String cName : psi.GetChannelNames()){
    		Channels.add(cName);
    	}
    	
    	Object[] channels = new Object[Channels.size()];
    	channels = Channels.toArray();
    	return channels;
    	
    }
    /**
     * Returns a list of Structure Members associated with a channel
     * which in turn is associated with a PubSub
     * 
     * @param pubSubName Name of the PubSub we want to examine
     * @param channelName Name of the channel within the PubSub we want to examine
     * @return Object[] List of Members associated with the channel in the PubSub
     */
    public Object[] getMembersListData (String pubSubName, String channelName){
    	if (mDLCM.ContainsChannel(channelName)){
    		ArrayList<Member> members = mDLCM.getChannelStructure(channelName).getMemberList();
            ArrayList<String> memberNames = new ArrayList<String>();
            for (Member member : members){
                memberNames.add(member.Name);
            }
            Object[] nameObjects = new Object[memberNames.size()];
            nameObjects = memberNames.toArray();
            return nameObjects;
        }
        //TODO Check Static Member
        Object [] debugObs = new Object[0];
        if (!PubSubMap.containsKey(pubSubName)){
            return debugObs;
        }
        Object staticObject = PubSubMap.get(pubSubName).getStaticChannel(channelName);

        Class c = staticObject.getClass();
        Field[] fs = c.getFields();
        ArrayList<String> FieldNames = new ArrayList<String>();
        for (Field f : fs){
            FieldNames.add(f.getName());
        }
        Object[] os = new Object[FieldNames.size()];
        os = FieldNames.toArray();
        return os;   		
    }
    /**
     * Query if the PubSub exists in the PubSubManager
     * 
     * @param pubSubName name of the PubSub
     * @return true if PubSubManager is managing the pubsub
     */
    public boolean contains(String pubSubName){
        return PubSubMap.containsKey(pubSubName);
    }
    /**
     * @param pubSubName
     * @return true = contains, false = not contains
     */
    private PubSub getPubSub(String pubSubName){
        return PubSubMap.get(pubSubName);
    }
    /**
     * Add a PubSub to the manager with a name, and a board identification
     * 
     * @param pubSubName Name of PubSub to add
     * @param boardID Board Identification number
     */
    public void AddPubSub (String pubSubName, Long boardID){
        if (PubSubMap.containsKey(pubSubName)){
            return;
        }
        PubSub psi = new PubSub(pubSubName, boardID, mDLCM);
        PubSubMap.put(pubSubName, psi);
    }
    /**
     * @param pubSubName If the PubSub exists in the PubSub manager remove it
     */
    public void RemovePubSub (String pubSubName){
        if (!PubSubMap.containsKey(pubSubName)){
            return;
        }
        PubSubMap.remove(pubSubName);
    }
    /**
     * Returns a name of all the PubSubs that are currently being managed
     * 
     * @return Set<String> a Set of strings of the names of the PubSubs
     */
    public ArrayList<String> getPubSubNames (){
    	ArrayList<String> PubSubNames = new ArrayList<String>(PubSubMap.keySet());
        return PubSubNames;
    }	

    //Instances/Structures
    public boolean ParseRawInputStrings(control_t message) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException{
    	boolean needStructures = false;
    	
    	for (String channelName : PubSubMap.get(message.from).mChannelNames){
    		removeDevice(channelName);
    	}
    	
    	try {
			PubSubMap.get(message.from).ParseRawInputString(message.control_string);
		} catch (Exception e) {
			needStructures = true;
		} finally {
			if (needStructures){
				PubSubMap.get(message.from).AddUnparsedString(message.control_string);
			}
		}
		
		//Now Add the device
		for (String channelName : PubSubMap.get(message.from).mChannelNames){
			addDevice(message.from, channelName, "Remote Device", mDLCM.getChannelStructure(channelName).getStructureName(), R.drawable.channel);
		}
    	return false;
    }
    public boolean ParseRawInputString(String name, String instanceString) throws Exception{
    	for (String channelName : PubSubMap.get(name).GetChannelNames()){
    		removeDevice(channelName);
    	}
    	PubSubMap.get(name).ParseRawInputString(instanceString);
		for (String channelName : PubSubMap.get(name).GetChannelNames()){
			addDevice(name, channelName, "Channel Device", mDLCM.getChannelStructure(channelName).getStructureName(), R.drawable.channel);
		}
    	return true;
    }
    public boolean AddLocalChannel(String localPubSub, String name, String info, String structureName, Integer iconId) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException{
    	PubSubMap.get(localPubSub).AddChannelName(name);
    	mDLCM.GenerateNewChannel(name, structureName);
    	addDevice(localPubSub, name, "Local Channel", structureName, iconId);
    	return true;
    }
    public void clearUnparsedRawInputStrings(String name){
    	if (!PubSubMap.get(name).getUnparsedInstanceStrings().isEmpty()){
    		PubSubMap.get(name).ClearUnparsedStrings();
    	}
    }
    public void ParseUnparsedInputStrings(control_t message) throws Exception{
    	
        for (String name : PubSubMap.keySet()){
            PubSub ps = PubSubMap.get(name);
            if (ps.isSameBoard(message.board_id)){
                ArrayList<String> uis = ps.getUnparsedInstanceStrings();
                Iterator itr = uis.iterator();
                while (itr.hasNext()){
                    String instanceString = (String)itr.next();
                    //ps.ParseRawInputString(instanceString);
                    ParseRawInputString(name, instanceString);
                    itr.remove();

                }
            }
        }    	
    }
    public String getRawInstanceString(String name){
    	return PubSubMap.get(name).GenerateRawInstanceString();
    }
    //Devices
    /**
     * Starts the process of adding a Android SubServ Device, DLCM Channel, and PubSub Channel
     * if a previous Device exists, this function will remove the device, and add a new one
     * 
     * @param from Name of the originating PubSub
     * @param name Name of the Device/DLCM Channel/PubSubChannel
     * @param info Information about the device, used to assist the user
     * @param structureName Name of the structure used to generate a device
     * @param iconId Identification of the icon used for display in SubServ
     * @return boolean true successfully added
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public boolean addDevice (String from, String name, String info, String structureName, Integer iconId) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException{
    	//DeviceMap
    	if (mDeviceMap.containsKey(name)){
    		removeDevice(name);
    	}
    	//mDLCM.GenerateNewInstance(name, structureName);
    	
    	PubSubDevice device = new PubSubDevice(name, info, iconId, mContext, true, mParent, PubSubMap.get(from), this, mDLCM);
    	mDeviceMap.put(name, device);
    	mDevices.add(device);
    	
    	
    	
    	return true;
    }
    /**
     * Examine the PubSub Devices, and if the device exists remove it
     * 
     * @param channel name of the Channel/Device to remove
     * @return boolean true successfully deleted
     */
    public boolean removeDevice (String channel){
    	if (mDeviceMap.containsKey(channel)){
    		Device d = mDeviceMap.remove(channel);
    		mDevices.remove(d);
    	}
    	if (mDLCM.ContainsChannel(channel)){
    		mDLCM.RemoveChannel(channel);
    	}
    	return true;
    }
	/**
	 * Handler that changes all the non UI thread functions to work in the UI thread
	 * 
	 * Currently Handles DLCM (add instance, remove instance) Receive Message
	 * 
	 */
	final Handler uiHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			switch (msg.what){
/*
			case (DLCM_ERROR_MESSAGE):
				Bundle bundle = msg.getData();
				break;
*/				
			case (DLCM_INSTANCE_ADDED):
				//This could be a response to me, or from a remote device
				String addChannelName = (String) msg.obj;
				break;
			case (DLCM_INSTANCE_REMOVED):
				String removeChannelName = (String) msg.obj;
				removeDevice(removeChannelName);
				break;
			}
			
		}
	};
	/**
	 * @author David McCoy
	 * 
	 * Receive Messages on the UI thread so that DLCM read can access
	 * the UI functions
	 *
	 */
	private class DLCMReceiveRunnable implements Runnable {
		public String channelName;
		public Structure structure;
		public void run() {
			mDeviceMap.get(channelName).ReceiveDataFromProvider(structure);
			
		}
	}
}
