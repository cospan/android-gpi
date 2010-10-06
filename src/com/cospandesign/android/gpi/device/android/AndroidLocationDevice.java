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

package com.cospandesign.android.gpi.device.android;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class AndroidLocationDevice extends Device implements LocationListener

{

	LocationManager mLocationManager;
	LocationProvider mLocationProvider;
	
	private int MinimumTimeBetweenUpdates = 1000;
	private int MinimumDistanceBetweenUpdates = 1;
	
	boolean mEnableProvider = false;
	
	static final private String testLocation = "Test Location";
	
	static final private String locationChannel = "Location";
	static final private String statusChannel = "Status";
	static final private String bundleChannel = "Extras";
	
	static final private String AccuracyString = "Accuracy";
	static final private String AccuracyDescriptionString = "Accuracy of Location";
	
	static final private String PowerString = "Power";
	static final private String PowerDescriptionString = "Power mA used by the device";
	
	static final private String MonetaryString = "Monetary";
	static final private String MonetaryDescriptionString = "Provider requires money";
	
	static final private String RequriesCellString = "Cell Tower";
	static final private String RequiresCellDescriptionString = "Requires a Cell Tower for location";
	
	static final private String NetworkString = "Data Access";
	static final private String NetworkDescriptionString = "Requires access to network for location";
	
	static final private String SataliteString = "Satalites";
	static final private String SatliteDescriptionString = "Requires satalites for navigation";
	
	static final private String AltitudeString = "Altitude";
	static final private String AltitudeDescriptionString = "Provider supports altitude";
	
	static final private String BearingString = "Bearing";
	static final private String BearingDescriptionString = "Provider supports bearing";
	
	static final private String SpeedString = "Speed";
	static final private String SpeedDescriptionString = "Provider supports speed";
	
	static final private String EnableString = "Enable";
	static final private String EnableDescriptionString = "Enable Provider";
	
	static final private String MinTimeString = "Minimum Time";
	static final private String MinTimeDescriptionString = "Minimum time between update";
	
	static final private String MinDistanceString = "Minimum Distance";
	static final private String MinDistanceDescriptionString = "Minimum distance between updates";
	
	public AndroidLocationDevice(String name, String info, Integer image,
			Context context, boolean enabled, Medium parent, LocationManager locationManager)
	{
		super(name, info, image, context, enabled, parent);
		mLocationManager = locationManager;

		//Create a crappy provider
		
//		mLocationManager.addTestProvider(testLocation, false, false, false, false, false, false, false, Criteria.POWER_HIGH, Criteria.ACCURACY_COARSE);
		
		//set up the locationProvider to the best one available
		String providerName = mLocationManager.getBestProvider(((Criteria)new Criteria()), false);
		mLocationProvider = mLocationManager.getProvider(providerName);
		
		
		if (mLocationProvider != null){
			super.setName(mLocationProvider.getName());
			super.setInfo("Location Provider");
			addProperty(AccuracyString, mLocationProvider.getAccuracy(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, AccuracyDescriptionString, true);
			addProperty(PowerString, mLocationProvider.getPowerRequirement(), ENTITY_PROPERTY_TYPE.NUMBER_BOX, PowerDescriptionString, true);
			addProperty(MonetaryString, mLocationProvider.hasMonetaryCost(), ENTITY_PROPERTY_TYPE.CHECK_BOX, MonetaryDescriptionString, true);
			addProperty(RequriesCellString, mLocationProvider.requiresCell(), ENTITY_PROPERTY_TYPE.CHECK_BOX, RequiresCellDescriptionString, true);
			addProperty(NetworkString, mLocationProvider.requiresNetwork(), ENTITY_PROPERTY_TYPE.CHECK_BOX, NetworkDescriptionString, true);
			addProperty(SataliteString, mLocationProvider.requiresSatellite(), ENTITY_PROPERTY_TYPE.CHECK_BOX, SatliteDescriptionString, true);
			addProperty(AltitudeString, mLocationProvider.supportsAltitude(), ENTITY_PROPERTY_TYPE.CHECK_BOX, AltitudeDescriptionString, true);
			addProperty(BearingString, mLocationProvider.supportsBearing(), ENTITY_PROPERTY_TYPE.CHECK_BOX, BearingDescriptionString, true);
			addProperty(SpeedString, mLocationProvider.supportsSpeed(), ENTITY_PROPERTY_TYPE.CHECK_BOX, SpeedDescriptionString, true);
			addProperty(EnableString, mEnableProvider, ENTITY_PROPERTY_TYPE.CHECK_BOX, EnableDescriptionString, false);
			addProperty(MinTimeString, MinimumTimeBetweenUpdates, ENTITY_PROPERTY_TYPE.NUMBER_BOX, MinTimeDescriptionString, false);
			addProperty(MinDistanceString, MinimumDistanceBetweenUpdates, ENTITY_PROPERTY_TYPE.NUMBER_BOX, MinDistanceDescriptionString, false);
		}
		
		addOutputDataChannel(locationChannel, Location.class);
	
	}
	
	public void setLocationProvider(LocationProvider locationProvider){
		mLocationProvider = locationProvider;
	}
	public LocationProvider getLocationProvider(){
		return mLocationProvider;
	}

	//@Override
	public void onLocationChanged(Location location)
	{
		outputData(locationChannel, location);
		mWorkspaceEntity.setStatus(mWorkspaceEntity.NEW_DATA);
		
	}

	//@Override
	public void onProviderDisabled(String provider)
	{
		if (provider.equals(mLocationProvider.getName())){
			if (mWorkspaceEntity != null){
				this.mWorkspaceEntity.setStatus(WorkspaceEntity.ERROR);
			}
		}
		
	}

	//@Override
	public void onProviderEnabled(String provider)
	{
		if (provider.equals(mLocationProvider.getName())){
			if (mWorkspaceEntity != null){
				this.mWorkspaceEntity.setStatus(WorkspaceEntity.STATUS_OK);
			}
		}
	}

	//@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		if (provider.equals(mLocationProvider.getName())){
			if (mWorkspaceEntity != null){
				switch (status){
				case (LocationProvider.OUT_OF_SERVICE):
					this.mWorkspaceEntity.setStatus(WorkspaceEntity.ERROR);
				break;
				case (LocationProvider.TEMPORARILY_UNAVAILABLE):
					this.mWorkspaceEntity.setStatus(WorkspaceEntity.WARNING);
				break;
				case (LocationProvider.AVAILABLE):
					this.mWorkspaceEntity.setStatus(WorkspaceEntity.STATUS_OK);
				break;
				}
			}
		}
		
	}

	@Override
	public void propertiesUpdate()
	{
		
		if (mLocationProvider != null){
			if (mEnableProvider != (Boolean)getPropertyData(EnableString)){
				mEnableProvider = (Boolean)getPropertyData(EnableString);
				if (mEnableProvider){
					mLocationManager.requestLocationUpdates(mLocationProvider.getName(), MinimumTimeBetweenUpdates, MinimumDistanceBetweenUpdates, this);
				}
				else{
					mLocationManager.removeUpdates(this);
				}
			
			}

		}
		
	
		
	}

	
}
