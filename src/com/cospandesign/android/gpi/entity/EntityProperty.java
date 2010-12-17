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

import com.cospandesign.android.gpi.GpiConsole;

public class EntityProperty {

	private boolean mReadOnly = false;
	public String mDescription;
	private Object mData;
	private Object mDefaultData;
	private ENTITY_PROPERTY_TYPE mType;
	private int mMax = 100;
	private int mMin = 0;
	private int mIndex = 0;
	private Entity mEntity;
	
	//Enumeration of Types
	public enum ENTITY_PROPERTY_TYPE {
		EDIT_BOX,
		LABEL,
		CHECK_BOX,
		SPINNER,
		NUMBER_SLIDER,
		NUMBER_BOX,
		BUTTON,
		ACTIVITY_BUTTON,
		NO_DISPLAY
	}
	
	//Constructor
	public EntityProperty(Entity entity, Object data, ENTITY_PROPERTY_TYPE type, String description, boolean readOnly){
		mEntity = entity;
		mData = data;
		mType = type;
		mDescription = description;
		mReadOnly = readOnly;
		
		if (mType == ENTITY_PROPERTY_TYPE.NUMBER_SLIDER){
			if((Integer)mData > mMax){
				mMax = (Integer)mData;
			}
			if (mMin > (Integer)mData){
				mMin = (Integer)mData;
			}
		}
	}
	
	//Getters/Setters
	public void setRange(int min, int max){
		if (mType == ENTITY_PROPERTY_TYPE.NUMBER_SLIDER){
			if (max <= min){
				throw new IndexOutOfBoundsException ("max( " + ((Integer)(new Integer(max))).toString() + " ) <= min ( " + ((Integer)(new Integer(min))).toString() + " )");
			}
			if (max < ((Integer)mData)){
				mData = (Integer) new Integer(max);
			}
			if ((Integer)mData < min){
				mData = (Integer) new Integer(min);
			}
		}
		
		mMax = max;
		mMin = min;
	}
	public boolean isReadOnly() {
		return mReadOnly;
	}
	public void setReadOnly(boolean mReadOnly) {
		this.mReadOnly = mReadOnly;
	}
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	public Object getData() {
		return mData;
	}
	public void setData(Object mData) {
		this.mData = mData;
	}
	public Object getDefaultData() {
		return mDefaultData;
	}
	public void setDefaultData(Object mDefaultData) {
		this.mDefaultData = mDefaultData;
	}
	public ENTITY_PROPERTY_TYPE getType() {
		return mType;
	}
	public void setType(ENTITY_PROPERTY_TYPE mType) {
		this.mType = mType;
	}
	public int getMax() {
		return mMax;
	}
	public void setMax(int mMax) {
		this.mMax = mMax;
	}
	public int getMin() {
		return mMin;
	}
	public void setMin(int mMin) {
		this.mMin = mMin;
	}
	public int getIndex() {
		return mIndex;
	}
	public void setIndex(int mIndex) {
		this.mIndex = mIndex;
	}
	public void setDataQuietly(Object data){
		mData = data;
	}
	
	public void updateProperties(){
		try {
			mEntity.propertiesUpdate();
		}
		catch (Exception ex){
			GpiConsole.getinstance().error("Unable to update Properties", ex);
		}
	}
	public void findDefaultType(){
		//TODO create data instanceof things...
	}
	//TODO add dependancy... only show one property when there is another property visisble
}
