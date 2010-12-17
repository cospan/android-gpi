package com.cospandesign.android.gpi.controller.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.GpiService;
import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;


public class GpiController extends Controller {

	private static final String SERVICE_MANAGER = "GPI Service";
	//private static GpiService mGpiService;
	private GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	boolean mServiceEnable;
	
	public GpiController(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		//Add Properties
		//mGpiConsole = ((GpiApp)c).getConsole();
		mServiceEnable = false;
//TODO: implement a service with the Android 1.6 library
//addProperty(SERVICE_MANAGER, new Boolean(mServiceEnable), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Background Service", false);
	}

	@Override
	protected void propertiesUpdate() {
		super.propertiesUpdate();
		mServiceEnable = (Boolean) getPropertyData(SERVICE_MANAGER);
	
		if (mServiceEnable){
			//start the service
			mGpiConsole.verbose("Attempting to start service");
			mContext.startService(new Intent(mContext, GpiService.class));
			//mContext.bindService(new Intent(mContext, GpiService.class), mConnection, Context.BIND_AUTO_CREATE);
		}
		else {
			//stop the service
			mGpiConsole.verbose("Attempting to stop service");
			mContext.stopService(new Intent(mContext, GpiService.class));
			//mContext.unbindService(mConnection);
		}
	}
	
/*
	private ServiceConnection mConnection = new ServiceConnection(){
		public void onServiceConnected (ComponentName className, IBinder service){
			//mContext.startService(new Intent(this, GpiService.class));
			mGpiService = ((GpiService.LocalBinder)service).getService();
			mGpiConsole.info("Bound to service");
		}

		public void onServiceDisconnected(ComponentName name) {
			//mGpiService = null;
			mGpiConsole.info("UnBinding from service");
			
		}
	};
*/

	
}
