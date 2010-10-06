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
import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;

import dlcm.Channel;
import dlcm.Member;
import dlcm.Structure;
import dlcm.dlcm.DLCM;


public class PubSubDevice extends Device {

	private static final String CAN_PUBLISH = "Can Publish";
	private static final String CAN_SUBSCRIBE = "Can Subscribe";
	
	private static final int CLASS_ERROR = 1;
	
	DLCM mDLCM;
	PubSub mPubSub;
	PubSubManager mPubSubManager;
	Channel mChannel;
	Structure mStructure;
	GpiConsole mGpiConsole;
	
	public PubSubDevice(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, 
			PubSub pubSub, PubSubManager pubSubManager, DLCM dlcm) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		
		super(name, info, image, context, enabled, parent);
		
		addProperty("PubSub", pubSub, ENTITY_PROPERTY_TYPE.NO_DISPLAY,
				"PubSub that controlls this channel", true);
		mDLCM = dlcm;
		mPubSub = pubSub;
		mPubSubManager = pubSubManager;
		mChannel = mDLCM.getChannel(name);
		mStructure = mDLCM.getChannelStructure(name);
		mStructure.initialize(dlcm);
		mGpiConsole = GpiConsole.getinstance();
		
		addProperty (CAN_PUBLISH, (new Boolean (mChannel.canPublish())), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Controls publish capability of the channel", true);
		addProperty (CAN_SUBSCRIBE, (new Boolean (mChannel.canSubscribe())), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Controls subscribe capability of the channel", true);
		
		setupIO();
		
	}
	
	private void setupIO() throws ClassNotFoundException{
		//go through each of the input channels
		ArrayList<Member> members = mStructure.getMemberList();
		
		Set<String> InputChannelNames = getInputDataKeySet();
		Set<String> OutputChannelNames = getOutputDataKeySet();
		
		//Remove Previous IO
		for (String channelName : InputChannelNames){
			removeInputDataChannel(channelName);
		}
		for (String channelName : OutputChannelNames){
			removeOutputDataChannel(channelName);
		}
		
		//Setup output
		if (mChannel.canPublish()){
			for (Member member : members){
				addInputDataChannel(member.Name, member.getDataClass());
			}
		}
		//Setup input
		if (mChannel.canSubscribe()){
			for (Member member : members){
				addOutputDataChannel(member.Name, member.getDataClass());
			}
		}
	}

	@Override
	public void propertiesUpdate() {
		Boolean canPublish = mChannel.canPublish();
		Boolean canSubscribe = mChannel.canSubscribe();
		
		Boolean newCanPublish = (Boolean) getPropertyData(CAN_PUBLISH);
		Boolean newCanSubscribe = (Boolean) getPropertyData(CAN_SUBSCRIBE);
		
		if ((canPublish != newCanPublish) || (canSubscribe != newCanSubscribe)){
			try {
				setupIO();
			} catch (ClassNotFoundException e) {
				Message message = uiHandler.obtainMessage(CLASS_ERROR);
				message.obj = e.getMessage();
			}
		}
		
		mChannel.setCanPublish(newCanPublish);
		mChannel.setCanSubscribe(newCanSubscribe);
	}
	
	private final Handler uiHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what){
			case CLASS_ERROR:
				mGpiConsole.error((String)msg.obj);
			}
		}
		
	};

	public void ReceiveDataFromProvider (Structure structure) {
		if (mWorkspaceEntity == null){
			return;
		}
		//Structure s = (Structure) structure.clone();
		for (String outputChannel : getOutputDataKeySet()){
			//OutputData(outputChannel, s.getMember(outputChannel).Data);
			
			try {
				outputData(outputChannel, structure.getMember(outputChannel).getDataClass().cast(structure.getMember(outputChannel).Data));
			} catch (ClassNotFoundException e) {
				Log.e("GPI", e.getMessage());
				e.printStackTrace();
			}
		}
		mWorkspaceEntity.setStatus(mWorkspaceEntity.NEW_DATA);
	}
	
	//Getting a message from somewhere else
	@Override
	public void newDataAvailable(Set<String> inputChannels) {
		for (String inputChannel : inputChannels){
			mStructure.getMember(inputChannel).Data = dequeueInputData(inputChannel);
		}
		mPubSubManager.PublishStructure(mName, mStructure);
	}


	
}
