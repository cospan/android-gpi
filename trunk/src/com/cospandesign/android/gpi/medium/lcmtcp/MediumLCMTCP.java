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

package com.cospandesign.android.gpi.medium.lcmtcp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import lcmtypes.control_t;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.GpiConstants;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.lcm.SubServTCPService;
import com.cospandesign.android.gpi.lcm.TCPBackgroundServiceCallback;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.android.gpi.structurebuilder.ChannelDesignerActivity;
import com.cospandesign.android.pubsub.PubSubManager;
import com.cospandesign.gpi.R;

import dlcm.DLCMControlConstants;
import dlcm.DLCMListener;
import dlcm.Structure;
import dlcm.builder.StructureManager;
import dlcm.dlcm.DLCM;

public class MediumLCMTCP extends Medium implements LCMSubscriber, DLCMListener, TCPBackgroundServiceCallback{
	
	static final String BUTTON_SETUP = "Setup TCP";
	static final int BUTTON_SETUP_ID = 5;
	static final String BUTTON_CHANNELS = "Channels";
	static final int BUTTON_CHANNELS_ID = 6;
	static final String BUTTON_PING = "Ping";
	static final int BUTTON_PING_ID = 7;
	static final String BUTTON_START = "Start TCP Provider";
	static final int BUTTON_START_ID = 1;
	static final String BUTTON_CHAT = "Send Chat";
	static final int BUTTON_CHAT_ID = 8;
	static final String BUTTON_CHANNEL_DESIGNER = "Channel Designer";
	static final int BUTTON_CHANNEL_DESIGNER_ID = 9;
	static final String EDIT_BOX_CHAT = "Chat";
	static final String CHECK_BOX_CONNECTED = "Connected";

	
	static final int DLCM_ERROR_MESSAGE = 2;
	static final int DLCM_INSTANCE_ADDED = 3;
	static final int DLCM_INSTANCE_REMOVED = 4;
	static final int CLIENT_CHANGE = 5;
	
	SubServTCPService mTCPService;
	LCM mLCM;
	DLCM mDLCM;
	String mAndroidPubSubName;
	Integer mBoardId;
	StructureManager mStructureManager;
	PubSubManager mPubSubManager;

//TODO make a abstract class which will call this class when people connect
	TimerTask mTCPServicePoller;
	Timer mTCPServiceTimer;
	int mUpdateRate = 1000;
	volatile Integer mClientCount;
	boolean canStart = false;
	
	
	//Constructor
	public MediumLCMTCP(String name, String info, Integer image, Context context, boolean enabled)
	{
		super(name, info, image, context, enabled);
		addProperty(CHECK_BOX_CONNECTED, (Boolean)false, ENTITY_PROPERTY_TYPE.CHECK_BOX, "TCP Provider Connected", true);
		addProperty(BUTTON_SETUP, TCPProviderActivity.class, ENTITY_PROPERTY_TYPE.ACTIVITY_BUTTON, "TCP Provider Activity", true);
		addProperty("LCM", mLCM, ENTITY_PROPERTY_TYPE.NO_DISPLAY, "LCM Object", true);
		
		Integer RandomNumber = (new Random()).nextInt(1000);
		mAndroidPubSubName = "AndPubSub" + RandomNumber.toString();
		addProperty(GpiConstants.ANDROID_PUBSUB_STRING, mAndroidPubSubName, ENTITY_PROPERTY_TYPE.LABEL, "Android PubSub name", true);
		
		mBoardId = GpiApp.getBoardId();
		addProperty(GpiConstants.ANDROID_PUBSUB_BOARD_ID, mBoardId, ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Android Board Identification", true);
		mClientCount = new Integer(0);
		addProperty(GpiConstants.TCP_SERVICE_CLIENTS_STRING, mClientCount, ENTITY_PROPERTY_TYPE.NUMBER_BOX, "No Clients Connected", true);
		
	}

	//Overriden Entity Methods
	@Override
	public boolean query() {
		return true;
	}
	@Override
	public boolean start() {
		if (!canStart){
			return false;
		}
		String providerString = (String) getPropertyData(GpiConstants.PROVIDER_STRING);
		boolean startServer = (Boolean) getPropertyData(GpiConstants.START_SERVER_STRING);
		Integer serverPort = (Integer) getPropertyData(GpiConstants.SERVER_PORT_STRING);
		if (mStructureManager == null){
			mStructureManager = (StructureManager) getPropertyData(GpiConstants.STRUCTURE_MANAGER_STRING);
		}
		
		if (startServer){
			try {
				mTCPService = new SubServTCPService(serverPort, this);
			} catch (IOException e) {
				return false;
			}
		}
		
		try {
			mLCM = new LCM(providerString);
		} 
		catch (IOException e) {
			return false;
		}
        try {
            logDebug("Starting DLCM");
            mDLCM = new DLCM(mStructureManager, mLCM);
        } catch (Exception ex) {
            logError(ex.getMessage());
            return false;
        }
        mDLCM.addDLCMListener(this);

        try {
            mLCM.subscribe(".*control", this);
            //mLCM.subscribe("control", this)
            mLCM.subscribe("chat", this);
        }
        catch (Exception ex){
        	logDebug(ex.getMessage());
        	return false;
        }
        setProperty(CHECK_BOX_CONNECTED, ((Boolean)true), ENTITY_PROPERTY_TYPE.CHECK_BOX, "TCP Provider Connected", true);
        setProperty(EDIT_BOX_CHAT, ((String)"Hello From Android!"), ENTITY_PROPERTY_TYPE.EDIT_BOX, "Send Messages from Android", false);
        
        addProperty(BUTTON_CHAT, BUTTON_CHAT_ID, ENTITY_PROPERTY_TYPE.BUTTON, "Send Chat Messages to other pubsubs", false);
        addProperty(BUTTON_CHANNELS, BUTTON_CHANNELS_ID, ENTITY_PROPERTY_TYPE.BUTTON, "Request Channels from other PubSubs", false);
        addProperty(BUTTON_CHANNEL_DESIGNER, ChannelDesignerActivity.class, ENTITY_PROPERTY_TYPE.ACTIVITY_BUTTON, "Design a Channel with this Activity", true);
		
        mPubSubManager = new PubSubManager(mLCM, mDLCM, mStructureManager, this, mDevices, mContext);
		mPubSubManager.AddPubSub(mAndroidPubSubName, (long)GpiApp.getBoardId());
        return true;
		
	}
	@Override
	public void setupPropertyFromIntent(String key, Intent returnIntent) {
		if (key.equals(BUTTON_SETUP)){
			addProperty(GpiConstants.PROVIDER_STRING, returnIntent.getStringExtra(GpiConstants.PROVIDER_STRING), ENTITY_PROPERTY_TYPE.EDIT_BOX, "String used to start provider", false);
			addProperty(GpiConstants.START_SERVER_STRING, returnIntent.getBooleanExtra(GpiConstants.START_SERVER_STRING, false), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Start Server", false);
			addProperty(GpiConstants.SERVER_PORT_STRING, returnIntent.getIntExtra(GpiConstants.SERVER_PORT_STRING, 7600), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Server Port Number", false);
			
			addProperty(BUTTON_START, BUTTON_START_ID, ENTITY_PROPERTY_TYPE.BUTTON, "Press to start the TCP Provider", true);
			
			canStart = true;
		}
		else if (key.equals(BUTTON_CHANNEL_DESIGNER)){
//Add a Local Channel!
			try {
				String channelName = returnIntent.getStringExtra(GpiConstants.CHANNEL_NAME);
				String channelInfo = returnIntent.getStringExtra(GpiConstants.CHANNEL_INFO);
				String channelStructure = returnIntent.getStringExtra(GpiConstants.CHANNEL_STRUCTURE);
				Integer iconId = returnIntent.getIntExtra(GpiConstants.CHANNEL_ICON, R.drawable.channel);
				
				mPubSubManager.AddLocalChannel(
						mAndroidPubSubName, 
						channelName, 
						channelInfo, 
						channelStructure, 
						iconId
						);
			} catch (Exception e) {
				logError("Failed to add TCP Provider Device\n" + e.getMessage());
				e.printStackTrace();
			} 
		}
	}

	
	//Medium Functions
	public void StartButtonClick(){
		if (start()){
			logDebug ("Successfully Started DLCM");
		}
		removeProperty(BUTTON_START);
/*		
		if (v.getTag().getClass().equals(Gpi.class)){
			((Gpi)v.getTag()).updateNameInfo();
		}
*/		
		logDebug("Query Button Clicked");
		
	}
	public void PingButtonClick(){
		logDebug ("Sending Ping");
		PingQuery();
	}
	public void sendChatClick(){
		logDebug ("Sending Chat");
		String chatString = (String) getPropertyData(EDIT_BOX_CHAT);
		sendChatString(chatString);
	}
	public void ChannelsButtonClick(){
		ChannelRequest();
	}
    public void sendChatString(String controlString){
        control_t controlOut = new control_t();
        controlOut.control_string = controlString;
        controlOut.from = mAndroidPubSubName;
        controlOut.board_id = mBoardId;
        controlOut.flags = 0;
        controlOut.status = 0;
        publishControl ("chat", controlOut);
    }
    public void publishControl(String channel, control_t controlOut){
        try{
            mLCM.publish(channel, controlOut);
        }
        catch (Exception ex){
        	logError(ex.getMessage());
        }
    }

    //Remote Request from me
    private void PingRequest (control_t response){
    	logDebug("Received a Ping... Responding... Pong!");
        control_t controlOut = new control_t();
        controlOut.control_string = "Pong";
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = 0;
        controlOut.status = DLCMControlConstants.PING;
        controlOut.board_id = mStructureManager.StructureManagerID;
        
        //add this to my ForeignMap
        if (!mPubSubManager.contains(response.from)){
            mPubSubManager.AddPubSub(response.from, response.board_id);
        }

        publishControl(response.from + ".control", controlOut);
    }
    private void StructureRequest (String channel){
    	logDebug("Received a request for structures");
        try {
            control_t controlOut = new control_t();
            controlOut.from = mAndroidPubSubName;
            controlOut.control_string = mStructureManager.getLCMString();
            controlOut.flags = 0;
            controlOut.status = DLCMControlConstants.STRUCTURES;
            controlOut.board_id = mStructureManager.StructureManagerID;

            publishControl("control", controlOut);
        } catch (Exception ex) {
            logError(ex.getMessage());
        }
    }
    private void InstanceRequest (control_t msg){
        logDebug("Received a request for instances");
        control_t controlOut = new control_t();
        controlOut.control_string = mPubSubManager.getRawInstanceString(mAndroidPubSubName);
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = 0;
        controlOut.status = DLCMControlConstants.CHANNELS;
        controlOut.board_id = mStructureManager.StructureManagerID;
        publishControl(msg.from + ".control", controlOut);
    }
    private void ChannelResolveRequest (control_t msg){

        //this could have caused some headaches
        if (msg.from.equals(mAndroidPubSubName)){
            return;
        }

        //if we don't have it, just end
        if (!mDLCM.ContainsChannel(msg.control_string)){
            return;
        }
        
        //we have the same name, respond back
        control_t controlOut = new control_t();
        controlOut.control_string = msg.control_string;
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = 0;
        controlOut.status = DLCMControlConstants.CHANNEL_CHECK;
        controlOut.board_id = mStructureManager.StructureManagerID;
        publishControl(msg.from + ".control", controlOut);

    }
    
    //Local Send Out
    private void ChannelResolveQuery (String channel_name){
        logDebug("Sending a request to all channels for a name resolution");
        control_t controlOut = new control_t();
        controlOut.control_string = channel_name;
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = DLCMControlConstants.CHANNEL_CHECK;
        controlOut.status = 0;
        controlOut.board_id = mStructureManager.StructureManagerID;
        publishControl("control", controlOut);
    }
    private void PingQuery (){
        logDebug("Sending Ping Query");
        control_t controlOut = new control_t();
        controlOut.control_string = "Ping";
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = DLCMControlConstants.PING;
        controlOut.status = 0;
        controlOut.board_id = mBoardId;
        publishControl ("control", controlOut);
    }
    private void ChannelRequest(){
        //go through each of the entries in the subpub list
    	ArrayList<String> pubSubNames = mPubSubManager.getPubSubNames();
    	
        if (pubSubNames == null){
            return;
        }

        //Don't send a request to ourselves
        if (pubSubNames.contains(mAndroidPubSubName)){
            pubSubNames.remove(mAndroidPubSubName);
        }
        
        control_t controlOut = new control_t();
        controlOut.control_string = "";
        controlOut.from = mAndroidPubSubName;
        controlOut.flags = DLCMControlConstants.CHANNELS;
        controlOut.status = 0;
        controlOut.board_id = mBoardId;

        for (String name : pubSubNames){
            publishControl(name + ".control", controlOut);
        }


        //send a request for all their channels, and channel string
    }
    
    //Remote Responded to me
    private void PingResponse (control_t response){
        //add this to my ForeignMap
        logDebug("Got a Pong from a Ping");
        if (!mPubSubManager.contains(response.from)){
            mPubSubManager.AddPubSub(response.from, response.board_id);
        }
//        PubSubListener();
    }
    private void InstanceResponse (control_t response) throws InstantiationException, IllegalAccessException{
        if (!mPubSubManager.contains(response.from)){
            mPubSubManager.AddPubSub(response.from, response.board_id);
        }
        
        try {
			if (!mPubSubManager.ParseRawInputStrings(response)){
				logInfo("Scheduling a request for structures to: " + response.from);
			    delayedStructureRequest delayedStructureRequest = new delayedStructureRequest();
			    delayedStructureRequest.setDestination(response.from);
			    delayedStructureRequest.setFrom(mAndroidPubSubName);
			    Timer structureRequestTimer = new Timer();
			    //half a second
			    structureRequestTimer.schedule(delayedStructureRequest, 500);

			    //mPubSubManager.getPubSub(response.from).AddUnparsedString(response.control_string);
			    instanceTimerToLive instTimerToLive = new instanceTimerToLive();
			    instTimerToLive.setControlValue(response.from);
			    Timer timer = new Timer();
			    //about 5 minutes
			    timer.schedule(instTimerToLive, 300000 );
			}
		} catch (ClassNotFoundException e) {
			logError(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logError(e.getMessage());
			e.printStackTrace();
		}
    }
    public class instanceTimerToLive extends TimerTask {

        private String Name;

        @Override
        public void run() {
        	mPubSubManager.clearUnparsedRawInputStrings(Name);
        	logError("Unparsed Instance strings from " + Name + " have timed out, and will be removed");

        }
        public void setControlValue(String name){
            Name = new String (name);
        }

    }
    public class delayedStructureRequest extends TimerTask {

        public String Name;
        public String From;

        @Override
        public void run() {
            logInfo("Requesting Structures from " + Name);
            control_t controlOut = new control_t();
            controlOut.control_string = "";
            controlOut.board_id = mBoardId;
            controlOut.from = From;
            controlOut.flags = DLCMControlConstants.STRUCTURES;
            controlOut.status = 0;
            publishControl(Name + ".control", controlOut);
        }
        public void setDestination(String name){
            Name = name;
        }
        public void setFrom(String from){
            From = from;
        }
        
    }
    private void structureResponse (control_t response){
        mDLCM.ParseStructure(response.control_string);
        logInfo("Received Structure Response");

        try {
			mPubSubManager.ParseUnparsedInputStrings(response);
		} catch (Exception e) {
			logError(e.getMessage());
		}
    }
    private void channelResolveResponse (control_t response){
        String newName = null;
        if (mDLCM.ContainsChannel(response.control_string)){
            newName = mDLCM.FixChannelName(response.control_string);
        }
        ChannelResolveQuery(newName);
//        PubSubListener();
    }

    //Property Functions
	@Override
	public void buttonClick(int buttonId) {
		
		switch (buttonId){
		case BUTTON_CHAT_ID:
			sendChatClick();
			break;
		case BUTTON_CHANNELS_ID:
			ChannelsButtonClick();
			break;
		case BUTTON_PING_ID:
			PingButtonClick();
			break;
		case BUTTON_START_ID:
			StartButtonClick();
			break;
		case BUTTON_SETUP_ID:
			break;
		default:
			break;
		}
	}

	//LCM Receive
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
    	LCMReceiveRunnable lrb = new LCMReceiveRunnable();
    	lrb.channel = channel;
    	lrb.ins = ins;
    	uiHandler.post(lrb);
		
	}

	//DLCM Listeners
	public void DLCMError(String error, String channel) {
//		LogDebug("Channel " + channel + "Error: " + error);
		Message msg = uiHandler.obtainMessage(this.DLCM_ERROR_MESSAGE);
		Bundle bundle = new Bundle();
		bundle.putString("error", error);
		bundle.putString("channel", channel);
		msg.setData(bundle);
		uiHandler.sendMessage(msg);
		
	}
	public void DLCMChannelAdded(String channelName) {
		Message msg = uiHandler.obtainMessage(this.DLCM_INSTANCE_ADDED, channelName);
		uiHandler.sendMessage(msg);
		
	}
	public void DLCMChannelRemoved(String channelName) {
		Message msg = uiHandler.obtainMessage(this.DLCM_INSTANCE_REMOVED, channelName);
		uiHandler.sendMessage(msg);
	}
	public void DLCMMessageReceived(String channelName, Structure structure) {
		// TODO Send this off to the Device
		/*
		DLCMReceiveRunnable drr = new DLCMReceiveRunnable();
		drr.channelName = channelName;
		drr.structure = structure;
		uiHandler.post(drr);
		*/
	}


	
	//UI Handle/Functions/Runnables
	final Handler uiHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			switch (msg.what){
			case (DLCM_ERROR_MESSAGE):
				Bundle bundle = msg.getData();
				logInfo("From " + bundle.getString("channel") + ": " + "Error: " + bundle.getString("error"));
				Toast.makeText(mContext, "From " + bundle.getString("channel") + ": " + "Error: " + bundle.getString("error") , Toast.LENGTH_LONG).show();
				break;
			case (DLCM_INSTANCE_ADDED):
				logInfo("DLCM Instance " + ((String)msg.obj) + " Added");
				Toast.makeText(mContext, "DLCM Instance " + ((String)msg.obj) + " Added" , Toast.LENGTH_LONG).show();
				break;
			case (DLCM_INSTANCE_REMOVED):
				logInfo("DLCM Instance " + ((String)msg.obj) + " Removed");
				Toast.makeText(mContext, "DLCM Instance " + ((String)msg.obj) + " Removed" , Toast.LENGTH_LONG).show();
				break;
			case (CLIENT_CHANGE):
				mClientCount = ((Integer)msg.obj);
				setProperty(GpiConstants.TCP_SERVICE_CLIENTS_STRING, mClientCount, ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Number of Clients connected to local server", true);
				propertiesUpdate();
				Toast.makeText(mContext, "Clients Connected = " + mClientCount.toString() , Toast.LENGTH_LONG).show();
				break;
			}
			
		}
	};

	private class DLCMReceiveRunnable implements Runnable {
		public String channelName;
		public Structure structure;
		public void run() {
			//Toast.makeText(context, "From " + channelName + ": " + "Structure: " + structure.getStructureName() , Toast.LENGTH_LONG).show();
			
		}
	}
	private class LCMReceiveRunnable implements Runnable {
		
		public LCMDataInputStream ins;
		public String channel;
		
		public void run() {

	        String [] Filters = null;
	        try {

	            Filters = channel.split("\\.");

	            //Control Channel
	            if (channel.matches("chat")){
	                control_t msg = new control_t(ins);
	                if (msg.from.equals(mAndroidPubSubName)){
	                	return;
	                }
//	                LogInfo("From: " + msg.from + ": " + msg.control_string);
	                Toast.makeText(mContext, "From " + msg.from + ": " + msg.control_string , Toast.LENGTH_LONG).show();
	            }
	            if ((channel.equals(mAndroidPubSubName + ".control")) ||(channel.equals("control"))){

	                control_t msg = new control_t(ins);

	                //Don't read control message from self
	                if ((channel.equals("control")) && msg.from.equals(mAndroidPubSubName)){
	                    return;
	                }

	                //Send out messages, when somethings happens
	 //               LogDebug("From: " + msg.from + "Message: " + msg.control_string);


	                //Received a RESPONSE
	                if (msg.status == DLCMControlConstants.STRUCTURES){
	                    structureResponse(msg);
	                }
	                else if (msg.status == DLCMControlConstants.CHANNELS){
	                    InstanceResponse(msg);
	                }
	                else if (msg.status == DLCMControlConstants.PING){
	                    //Received a response to a request for a ping
	                    PingResponse(msg);
//	                    UpdatePubSubsList();
	                }
	                else if (msg.status == DLCMControlConstants.CHANNEL_CHECK){
	                    channelResolveResponse(msg);
	                }


	                //Received a REQUEST
	                if (msg.flags == DLCMControlConstants.STRUCTURES){
	                    //send the structure data
	                    StructureRequest(msg.control_string);
	                }
	                else if (msg.flags == DLCMControlConstants.CHANNELS){
	                    //send instance data
	                    InstanceRequest(msg);
	                }
	                else if (msg.flags == DLCMControlConstants.PING){
	                    PingRequest(msg);
//	                    UpdatePubSubsList();
	                }
	                else if (msg.flags == DLCMControlConstants.CHANNEL_CHECK){
	                    ChannelResolveRequest(msg);
	                }

	               
	            }
	            //TODO Look for other static lcm channels here
	        }

	        catch (Exception ex) {
//	        	LogError(ex.getMessage());
	            return;
	        }
			
		}
		
	}
	public void clientConnecting(int numOfClients) {
		Message msg = uiHandler.obtainMessage(this.CLIENT_CHANGE, (new Integer(numOfClients)));
		uiHandler.sendMessage(msg);
	
	}

	public void clientDisconnecting(int numOfClients) {
		Message msg = uiHandler.obtainMessage(this.CLIENT_CHANGE, (new Integer(numOfClients)));
		uiHandler.sendMessage(msg);
		
	}
	



}
