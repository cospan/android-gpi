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

package com.cospandesign.android.gpi.entity;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import android.content.Context;
import android.content.Intent;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class Entity {
	
	
	//Entity
	protected Integer mImage;
	protected Context mContext;
	protected boolean mEnable;
	protected String mName;
	protected String mInfo;
	protected boolean mServiceMode = false;
	
	//Properties
	Hashtable<String, EntityProperty> mEntityProperties;
	
	//Input/Output
	Hashtable<String, Class> mDataOutputTypes;
	Hashtable<String, Class> mDataInputTypes;
	Hashtable<String, Object> mDataInputTable;
	Hashtable<String, Hashtable<Entity, HashSet<String>>> mListenerMap;
	
	//WorkspaceEntity
	protected WorkspaceEntity mWorkspaceEntity;
	
	private GpiConsole mGpiConsole;
	
	//Constructor
	public Entity (String name, String info, Integer image, Context context, boolean enable){
		mName = name;
		mInfo = info;
		mImage = image;
		mContext = context;
		mEnable = enable;
		
		mListenerMap = new Hashtable<String, Hashtable<Entity, HashSet<String>>>();
		
		mDataOutputTypes = new Hashtable<String, Class>();
		mDataInputTypes = new Hashtable<String, Class>();
		mDataInputTable = new Hashtable<String, Object>();
		mEntityProperties = new Hashtable<String, EntityProperty>();
		
		mGpiConsole = GpiConsole.getinstance();
	}

	//Getters/Setters
	public Integer getImage() {
		return mImage;
	}
	public void setImage(Integer mImage) {
		this.mImage = mImage;
	}
	public boolean isEnable() {
		return mEnable;
	}
	public void setEnable(boolean mEnable) {
		this.mEnable = mEnable;
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	public String getInfo() {
		return mInfo;
	}
	public void setInfo(String mInfo) {
		this.mInfo = mInfo;
	}
	public Hashtable<String, EntityProperty> getEntityProperties() {
		return mEntityProperties;
	}
	public void setEntityProperties(
			Hashtable<String, EntityProperty> mEntityProperties) {
		this.mEntityProperties = mEntityProperties;
	}
	public WorkspaceEntity getWorkspaceEntity() {
		return mWorkspaceEntity;
	}
	public void setWorkspaceEntity(WorkspaceEntity mWorkspaceEntity) {
		this.mWorkspaceEntity = mWorkspaceEntity;
	}
	public void resetWorkspaceEntity(){
		mWorkspaceEntity = null;
	}
	public boolean isWorkspaceEntity(){
		return (mWorkspaceEntity != null);
	}
	
	//GUI Functions
	public void guiInitialization(WorkspaceEntity workspaceEntity){
		mWorkspaceEntity = workspaceEntity;
		mServiceMode = false;
	}
	//Background Service Functions
	public void serviceInitialization(){
		mServiceMode = true;
	}
	public boolean isServiceMode(){
		return mServiceMode;
	}
	
	//Properties
	public void addProperty(String name, Object data, ENTITY_PROPERTY_TYPE type, String description, boolean readOnly){
		mEntityProperties.put(name, new EntityProperty(this, data, type, description, readOnly));
	}
	public boolean isPropertyReadOnly(String name){
		return mEntityProperties.get(name).isReadOnly();
	}
	public void setProperty(String name, Object data, ENTITY_PROPERTY_TYPE type, String description, boolean readOnly){
		EntityProperty entityProperty = null;
		if (mEntityProperties.keySet().contains(name)){
			entityProperty = mEntityProperties.get(name);
			entityProperty.setData(data);
			entityProperty.setType(type);
			entityProperty.setDescription(description);
			entityProperty.setReadOnly(readOnly);
		}
		else{
			mEntityProperties.put(name, new EntityProperty(this, data, type, description, readOnly));
		}
		propertiesUpdate();
	}
	public EntityProperty getProperty(String name){
		return mEntityProperties.get(name);
	}
	public Object getPropertyData(String name){
		return mEntityProperties.get(name).getData();
	}
	public void removeProperty(String name){
		mEntityProperties.remove(name);
	}
	public Set<String> getPropertyKeySet(){
		return mEntityProperties.keySet();
	}

	//Connections
 	public int canConnect(Entity inputEntity){
		//Look at all the possible output types that we have
		Set<String> outputKeys = mDataOutputTypes.keySet();
		int retValue = WorkspaceEntity.OUTPUT_BAD;
		for (String outputKey : outputKeys){
			Set<String> inputKeys = inputEntity.getInputDataKeySet();
			for (String inputKey : inputKeys){
				retValue = canConnect (inputEntity.getInputDataType(inputKey), mDataOutputTypes.get(outputKey));
				if ((retValue == WorkspaceEntity.OUTPUT_GOOD) || (retValue == WorkspaceEntity.OUTPUT_WARNING)){
					retValue = WorkspaceEntity.OUTPUT_GOOD;
					break;
				}
			}
		}
		return retValue;
	}
	public int canConnect(Entity inputEntity, String inputChannel){
		//look at all the possible output typaes that we have
		Set<String> outputKeys = mDataOutputTypes.keySet();
		
		for (String outputKey : outputKeys){
			if (canConnect(inputEntity.getInputDataType(inputChannel), mDataOutputTypes.get(outputKey)) != mWorkspaceEntity.OUTPUT_GOOD){
				return mWorkspaceEntity.OUTPUT_GOOD;
			}
		}
		return mWorkspaceEntity.OUTPUT_BAD;
	}
	public int canConnect(Entity inputEntity, String inputChannel, String outputChannel){
		//look at all the possible output types that we have
		return canConnect (inputEntity.getInputDataType(inputChannel), mDataOutputTypes.get(outputChannel));
	}
	private int canConnect (Class<?> inClass, Class<?> outClass){
		String strInClass = inClass.getName();
		String strOutClass = outClass.getCanonicalName();
		int returnValue = WorkspaceEntity.OUTPUT_BAD;
		
		if (inClass.toString().matches(outClass.toString())){
			returnValue = WorkspaceEntity.OUTPUT_GOOD;
		}
		else {
			if (strInClass.matches(Double.class.getName())){
				if (strOutClass.matches(Float.class.getName()) || 
					strOutClass.matches(Long.class.getName()) ||
					strOutClass.matches(Integer.class.getName()) ||
					strOutClass.matches(Short.class.getName()) ||
					strOutClass.matches(Byte.class.getName())){
					returnValue = WorkspaceEntity.OUTPUT_WARNING;
				}
			}
			if (strInClass.matches(Float.class.getName())){
				if (strOutClass.matches(Long.class.getName()) ||
					strOutClass.matches(Integer.class.getName()) ||
					strOutClass.matches(Short.class.getName()) ||
					strOutClass.matches(Byte.class.getName())){
					returnValue = WorkspaceEntity.OUTPUT_WARNING;
				}
			}
			if (strInClass.matches(Long.class.getName())){
				if (strOutClass.matches(Integer.class.getName()) ||
					strOutClass.matches(Short.class.getName()) ||
					strOutClass.matches(Byte.class.getName())){
					returnValue = WorkspaceEntity.OUTPUT_WARNING;
				}
			}
			if (strInClass.matches(Integer.class.getName())){
				if (strOutClass.matches(Short.class.getName()) ||
					strOutClass.matches(Byte.class.getName())){
					returnValue = WorkspaceEntity.OUTPUT_WARNING;
				}
			}
			if (strInClass.matches(Short.class.getName())){
				if (strOutClass.matches(Byte.class.getName())){
					returnValue = WorkspaceEntity.OUTPUT_WARNING;
				}
			}

		}
		

		return returnValue;
	}
	public int canConnectInput(Entity entity){
		return WorkspaceEntity.INPUT_BAD;
	}
	public boolean isConnected(String outputChannel, Entity inputEntity, String inputChannel){
		//check if the output map for that channel is null
		if (mListenerMap.get(outputChannel) == null){
			//not connected
			return false;
		}
		//there is a map associated with this channel
		Hashtable<Entity, HashSet<String>> entityTable = mListenerMap.get(outputChannel);
		
		//check if the output table has the entity in it
		if (entityTable.get(inputEntity) == null){
			//output table doesn't have entity in it
			return false;
		}
		
		HashSet<String> inputChannelSet = entityTable.get(inputEntity);
		
		//check if the inputChannelSet has the channel in it
		if (inputChannelSet.contains(inputChannel)){
			//all the conditions are met
			return true;
		}
		
		return false;
	}
	public void removeInputDataChannel(String name){
		mDataInputTable.remove(name);
		mDataInputTypes.remove(name);
	}
	public boolean enqueueInputData(Entity targetEntity, HashSet<String>inputChannels, Object data, int connectionColor){
		if (inputChannels != null){
			for (String inputChannel : inputChannels){
				targetEntity.getDataInputTable().put(inputChannel, data);
				if (targetEntity.mWorkspaceEntity != null){
					targetEntity.mWorkspaceEntity.getInputConnectionViews().get(inputChannel).setConnectionColor(connectionColor);
				}
			}
			return true;
		}
		return false;
	}
	public Object dequeueInputData(String name){
		return mDataInputTable.get(name);
	}
	
	//Data Channels
	public Class<?> getInputDataType(String channel){
		return mDataInputTypes.get(channel);
	}
	public Hashtable<String, Object>getDataInputTable(){
		return mDataInputTable;
	}
	public Set<String> getInputDataKeySet(){
		return mDataInputTable.keySet();
	}	
	public <T> void addInputDataChannel(String name, Class<T> clazz){
		mDataInputTypes.put(name, clazz);
		mDataInputTable.put(name, new Object());
	}
	public <T> void addOutputDataChannel(String name, Class<T> clazz){
		mDataOutputTypes.put(name, clazz);
	}
	public Class<?> getOutputDataType(String channel){
		return this.mDataOutputTypes.get(channel);
	}
	public void removeOutputDataChannel(String name){
		mDataOutputTypes.remove(name);
	}
	public Hashtable<Entity, HashSet<String>> getAllConnectionsForOutputChannel(String outputChannelName){
		return mListenerMap.get(outputChannelName);
	}
	public boolean outputData(String outputChannel, Object data){
		int connectionColor = 0;
		
		if (mWorkspaceEntity != null){
			mWorkspaceEntity.newOutputData(outputChannel);
			connectionColor = mWorkspaceEntity.getOutputConnectionViews().get(outputChannel).getConnectionColor();
		}
		
		//get a set of the entities that we will call to send data to
		if (mListenerMap.get(outputChannel) != null){
			Set<Entity> entities = mListenerMap.get(outputChannel).keySet();
			Hashtable<Entity, HashSet<String>> entityTable = mListenerMap.get(outputChannel);
			for (Entity entity : entities){
				HashSet<String> inputChannels = entityTable.get(entity);
				if (enqueueInputData(entity, inputChannels, data, connectionColor) == false){
					//TODO send a message that there is nothing to send it to
				}
				else {
					//TODO show data was passed through here
					//because we waited until here to notify the output that there is new data available, we can set up
					//animation on the host side tht says data passed out
					entity.newDataAvailable(inputChannels);
				}
			}
		}
		else {
			//nothing listening
			return false;
		}
		return true;
	}
	
	//Listener
	public Set<String> getOutputDataKeySet(){
		//get a list of channels, so we can see the cast values
		return mDataOutputTypes.keySet();
	}
	public boolean requestToBeOutputListener(String outputChannel, Entity entity, String inputChannel){
		//if there isn't a table conencted to the output channel
		if (mListenerMap.get(outputChannel) == null){
			HashSet<String> inputChannelSet = new HashSet<String>();
			inputChannelSet.add(inputChannel);
			Hashtable<Entity, HashSet<String>> entityTable = new Hashtable<Entity, HashSet<String>>();
			entityTable.put(entity, inputChannelSet);
			mListenerMap.put(outputChannel, entityTable);
		}
		
		//if there is a hashtable that is connected to the output channel, then find out if there is an entry for the entity we want
		else if (mListenerMap.get(outputChannel).get(entity) == null){
			HashSet<String> inputChannelSet = new HashSet<String>();
			inputChannelSet.add(inputChannel);
			mListenerMap.get(outputChannel).put(entity, inputChannelSet);
		}
		//There is already an outputChannel associated with a sub hashtable. the hashtable also has a entry names entity
		else {
			mListenerMap.get(outputChannel).get(entity).add(inputChannel);
		}
		return true;
	}
	public void removeOutputListener(String outputChannel, Entity entity, String inputChannel){
		//if we are given the outputChannel, entity, and iput channel, remove just the inputChannel
		if ((outputChannel != null) && (entity != null) && (inputChannel != null)){
			if ((mListenerMap.get(outputChannel) != null) && (mListenerMap.get(outputChannel).get(entity) != null) && (mListenerMap.get(outputChannel).get(entity).contains(inputChannel))){
				mListenerMap.get(outputChannel).get(entity).remove(inputChannel);
			}
		}
		//If we are given the output channel, and the ENtity, but not an input channel, then just remove the entity
		else if ((outputChannel != null) && (entity != null) && (inputChannel == null)){
			if ((mListenerMap.get(outputChannel) != null) && (mListenerMap.get(outputChannel).get(entity) != null)){
				mListenerMap.get(outputChannel).remove(entity);
			}
		}
		//If we are only given the output channel we remove every thing
		else if ((outputChannel != null) && (entity == null) && (inputChannel == null)){
			if (mListenerMap.get(outputChannel) != null){
				mListenerMap.remove(outputChannel);
			}
		}
		//remove an entity
		else if ((outputChannel == null) && (entity != null) && (inputChannel == null)){
			Set<String> outputChannels = mListenerMap.keySet();
			for (String outputChannelString : outputChannels){
				if (mListenerMap.get(outputChannelString).containsKey(entity)){
					mListenerMap.get(outputChannelString).remove(entity);
				}
			}
		}
	}
	
	//Console Out
	public void logDebug(String string){
		mGpiConsole.debug(mName + " " + string);
	}
	public void logVerbose(String string){
		mGpiConsole.verbose(mName + " " + string);
	}
	public void logInfo(String string){
		mGpiConsole.info(mName + " " + string);
	}
	public void logWarning(String string){
		mGpiConsole.warning(mName + " " + string);
	}
	public void logError(String string){
		mGpiConsole.error(mName + " " + string);
	}

	//Override Me
	public void setupPropertyFromIntent(String key, Intent returnIntent){
		
	}
	public void newDataAvailable(Set<String> inputChannels){
		//Override me to tell the bottom class that new data on all the input channels listend
		if (mWorkspaceEntity != null){
			for (String channel : inputChannels){
				mWorkspaceEntity.newInputData(channel);
			}
		}
	}
	protected void propertiesUpdate(){
		//Called whenever property is changed
	}
	public void notifyRemoval(){
		//called when being removed from canvas
	}
	public void buttonClick(int buttonId){
		//Override Me
	}
}
