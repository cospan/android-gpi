package com.cospandesign.android.gpi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.cospandesign.gpi.R;

public class GpiService extends Service {
	private static final String TAG = "GPI Service";
	private NotificationManager mNM;
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	private final IBinder mBinder = new LocalBinder();

	
	public class LocalBinder extends Binder {
		public GpiService getService(){
			return GpiService.this;
		}
	}
	
	//Lifecycle functions
	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mGpiConsole.verbose("Creating Service");
	}
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	mGpiConsole.info("LocalService, Received start id " + startId + ": " + intent);
    	showNotification();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.stop_service);
        

        // Tell the user we stopped.
        mGpiConsole.verbose("Destroying Service");
        Toast.makeText(this, R.string.stop_service , Toast.LENGTH_SHORT).show();
    }

    //Fun! ctions
    private void showNotification(){
    	//harSequence text = getText(R.string.start_service);
    	Notification notification =  new Notification(R.drawable.gpi_notification, "Return to GPI", System.currentTimeMillis());
    	//Pending intent to launch our activity if the user selects this notification
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Gpi.class), 0);
    	notification.setLatestEventInfo(this, getText(R.string.gpi_name), "Return to GPI", contentIntent);
    	//send the notification
    	mNM.notify(R.string.start_service, notification);
    }
}
