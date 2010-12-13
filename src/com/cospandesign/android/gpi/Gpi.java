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

package com.cospandesign.android.gpi;

import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TableRow.LayoutParams;

import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.android.gpi.properties.PropertyManagerActivity;
import com.cospandesign.android.gpi.workspace.WorkspaceActivity;
import com.cospandesign.gpi.R;

import dlcm.builder.StructureManager;

public class Gpi extends Activity {
	//public final boolean dbg = true;
	public final int ACTIVITY_REQUEST_CODE = 1;
	
	StructureManager mStructureManager;
	
	Controller mGpiController;
	Entity mCurrentEntity;
	Medium mCurrentMedium;
	
	Gallery mControllerGallery;
	Gallery mMediumGallery;
	Gallery mDeviceGallery;
	TextView mNameLabel;
	TextView mInfoLabel;
	TableLayout mInfoTable;
	//ListView mNameInfoList;
	
	private static final int MENU_WORKSPACE = 0;
	private static String MENU_WORKSPACE_TITLE = "Workspace";
	private static final int MENU_PROPERTIES = 1;
	private static String MENU_PROPERTIES_TITLE = "Properties";
	private static final int MENU_LOAD = 2;
	private static String MENU_LOAD_TITLE = "Load";
	private static final int MENU_SAVE = 3;
	private static String MENU_SAVE_TITLE = "Save";
	private static final int MENU_QUIT = 4;
	private static String MENU_QUIT_TITLE = "Quit";
		
	GpiConsole mConsole = GpiConsole.getinstance();
	
	Menu mDeviceMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//ucs = new Controller(log_name, "Cospan Design", R.drawable.cducst, this);
		mGpiController = (Controller)((GpiApp)getApplication()).getControlTree().getController();
		mStructureManager = ((GpiApp)getApplication()).getStructureManager();
		mStructureManager.addStructureListener(((GpiApp)getApplication()));
		
		mCurrentEntity = mGpiController;
		if (mGpiController.getMediums().size() > 0)
		{
			mCurrentMedium = mGpiController.getMediums().get(0);
		}
		
		setContentView(R.layout.device_view_layout);
		
		mControllerGallery = (Gallery) findViewById(R.id.ControllerGallery);
		mMediumGallery = (Gallery) findViewById(R.id.MediumGallery);
		mDeviceGallery = (Gallery) findViewById(R.id.DeviceGallery);
		mInfoTable = (TableLayout) findViewById(R.id.DeviceViewPropertyTable);
		mNameLabel = (TextView) findViewById(R.id.EntityNameValue);
		mInfoLabel = (TextView) findViewById(R.id.EntityInfoValue);
		
		
		//set custom adapters
		updateAdapters();
		
		mMediumGallery.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			{
				mediumLongClick(parent, v, position, id);
				return false;
			}
		});
		mDeviceGallery.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			{
				deviceLongClick(parent, v, position, id);
				return false;
			}
		});
	
		//set click listener
		mControllerGallery.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				ControlClick(parent, v, position, id);
			}
		});
		mMediumGallery.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{	
				mediumClick(parent, v, position, id);
			}
			
		});
		mDeviceGallery.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				deviceClick(parent, v, position, id);
			}
		});
		
		mControllerGallery.setUnselectedAlpha(0.50f);
		mMediumGallery.setUnselectedAlpha(0.50f);
		mDeviceGallery.setUnselectedAlpha(0.50f);
		
		mControllerGallery.setAdapter(new ControllerGalleryAdapter(this));
		mMediumGallery.setAdapter(new mediumGalleryAdapter(this));
		mDeviceGallery.setAdapter(new deviceGalleryAdapter(this));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		//MenuInflater inflator = this.getMenuInflater();
		//inflator.inflate(R.menu.device_view_menu, mDeviceMenu);
		menu.clear();
		menu.add(0, MENU_LOAD, 0, MENU_LOAD_TITLE);
		menu.add(0, MENU_SAVE, 1, MENU_SAVE_TITLE);
		menu.add(0, MENU_WORKSPACE, 2, MENU_WORKSPACE_TITLE);
		menu.add(0, MENU_PROPERTIES, 3, MENU_PROPERTIES_TITLE);
		menu.add(0, MENU_QUIT, 4, MENU_QUIT_TITLE);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId()){
		case MENU_SAVE:
			return true;
		case MENU_LOAD:
			return true;
		case MENU_WORKSPACE:
			intent = new Intent(this, WorkspaceActivity.class);
			startActivity(intent);
			return true;
		case MENU_PROPERTIES:
			intent = new Intent(this, PropertyManagerActivity.class);
			intent.putExtra("entity", mCurrentEntity.getName());
			
			startActivity(intent);
			return true;
		case MENU_QUIT:
			finish();
			return true;
			
		
		}
		return false;
	}
	//User Events
	private void ControlLongClick(AdapterView<?> parent, View v, int position, long id)
	{
		//configure UCS
		mCurrentEntity = mGpiController;
		updateAdapters();
//		mControllerGallery.setSelection(position, true);
		mMediumGallery.setAdapter(new mediumGalleryAdapter(this));
		mDeviceGallery.setAdapter(new deviceGalleryAdapter(this));
	}
	private void ControlClick(AdapterView<?> parent, View v, int position, long id)
	{
		//show information about UCS
		//currentEntity = ucs;
		//updateAdapters();
		//start the control canvas activity
		
		//intent.putExtra("com.cospandesign.ucs.ucsController", ucs);
		
//		mControllerGallery.setSelection(position, true);
		mCurrentEntity = mGpiController;
		mMediumGallery.setAdapter(new mediumGalleryAdapter(this));
		mDeviceGallery.setAdapter(new deviceGalleryAdapter(this));
		updateAdapters();
		
	}
	private void mediumLongClick(AdapterView<?> parent, View v, int position, long id)
	{
		//configure selected medium
		mCurrentEntity = mGpiController.getMediums().get(position);
		mCurrentMedium = mGpiController.getMediums().get(position);
		updateAdapters();
//		mMediumGallery.setSelection(position, true);
		mDeviceGallery.setAdapter(new deviceGalleryAdapter(this));
	}
	private void mediumClick(AdapterView<?> parent, View v, int position, long id)
	{
		boolean flag = false;

		//show information about medium
		if (mGpiController.getMediums().get(position).getDevices().size() == 0){
			flag = mGpiController.getMediums().get(position).query();
		}
		mCurrentEntity = mGpiController.getMediums().get(position);
		mCurrentMedium = mGpiController.getMediums().get(position);
		updateAdapters();
//		mMediumGallery.setSelection(position, true);
		mDeviceGallery.setAdapter(new deviceGalleryAdapter(this));
		/*
		if (startActivityFlag){
			Class activityClass = null;
			try{
				if (((Object)mCurrentMedium.GetProperty("Activity")) == null){
					Log.w(SubServConstants.LOG_TAG, "Activity Property == NULL, no activity to launch");
					return;
				}
				activityClass = (Class)((EntityProperty)mCurrentMedium.GetProperty("Activity")).getValue();
			}
			catch (Exception ex){
				Log.e(SubServConstants.LOG_TAG, ex.getMessage());
			}
			if (activityClass != null){
				Intent intent = new Intent (this, activityClass);
				startActivityForResult(intent, TCP_ACTIVITY_REQUEST_CODE);
			}
		}
		*/
	}
	private void deviceLongClick(AdapterView<?> parent, View v, int position, long id)
	{
		//configure device
		if (mCurrentMedium.getDevices().size() == 0) {
			mCurrentEntity = null;
		}
		else{
		mCurrentEntity = mCurrentMedium.getDevices().get(position);
		}
		updateAdapters();
//		mDeviceGallery.setSelection(position, true);
	}
	private void deviceClick(AdapterView<?> parent, View v, int position, long id)
	{
		//show device information
		mCurrentEntity = mCurrentMedium.getDevices().get(position);
		
		updateAdapters();
//		mDeviceGallery.setSelection(position, true);
	}

	//Overrides
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACTIVITY_REQUEST_CODE){
			if (resultCode == RESULT_OK){
				
				mCurrentEntity.setupPropertyFromIntent(data.getStringExtra("KEY"), data);	
			}
		}
		updateNameInfo();
	}	
	
	//Adapters
	public class ControllerGalleryAdapter extends BaseAdapter {
		int mGalleryItemBackGround;
	    private Context mContext;

		public ControllerGalleryAdapter (Context c)
		{
			mContext  = c;			
		}
		
		public int getCount()
		{
			return 1;
		}

		public Object getItem(int position)
		{
			return mGpiController;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView i = new ImageView(mContext);
			i.setImageResource(mGpiController.getImage());
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setLayoutParams(new Gallery.LayoutParams(100, 50));
			return i;
		}
	
	}
	public class mediumGalleryAdapter extends BaseAdapter {

		Context c;
		public mediumGalleryAdapter (Context context){
			c = context;
		}
		public int getCount()
		{
			return mGpiController.getMediums().size();
		}

		public Object getItem(int position)
		{
			if (mGpiController.getMediums().size() <= position)
			{
				return null;
			}
			return mGpiController.getMediums().get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView i = new ImageView(c);
			i.setImageResource(mGpiController.getMediums().get(position).getImage());
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setLayoutParams(new Gallery.LayoutParams(100, 50));
			return i;
		}
		
	}
	public class deviceGalleryAdapter extends BaseAdapter {

		Context c;
		
		public deviceGalleryAdapter (Context context){
			c = context;
		}
		public int getCount()
		{
			return mCurrentMedium.getDevices().size();
		}
		public Object getItem(int position)
		{
			if (mCurrentMedium.getDevices().size() <= position)
			{
				return null;
			}
			return mCurrentMedium.getDevices().get(position);
		}
		public long getItemId(int position)
		{
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mCurrentMedium.getDevices().size() <= position)
			{
				return null;
			}
			ImageView i = new ImageView(c);
			i.setImageResource(mCurrentMedium.getDevices().get(position).getImage());
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			i.setLayoutParams(new Gallery.LayoutParams(100, 50));
			return i;
		}
		
	}
	public class infoViewImageAdapter extends BaseAdapter {
		Context c;
		infoViewImageAdapter (Context context){
			c = context;
		}
		public int getCount()
		{
			if (mCurrentEntity == null)
			{
				return 0;
			}
						
			return mCurrentEntity.getEntityProperties().size();
		}
		public Object getItem(int position)
		{
			if (mCurrentEntity == null)
			{
				return null;
			}
			return mCurrentEntity.getEntityProperties().get(mCurrentEntity.getEntityProperties().keySet().toArray()[position]);
		}
		public long getItemId(int position)
		{
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mCurrentEntity == null)
			{
				return null;
			}
			if (mCurrentEntity.getEntityProperties().size() == 0)
			{
				return null;
			}
			

			LinearLayout.LayoutParams lptv = new LinearLayout.LayoutParams(parent.getMeasuredWidth()/2, 20, .5f);
			LinearLayout.LayoutParams lpvv = new LinearLayout.LayoutParams(parent.getMeasuredWidth()/2, 20, .5f);
			LinearLayout l = new LinearLayout(c);
			TextView tv1 = new TextView(c);
			View v2;
			
			l.setOrientation(LinearLayout.HORIZONTAL);
			
			String name = (String)(mCurrentEntity.getEntityProperties().keySet().toArray()[position]);
			Object object = mCurrentEntity.getPropertyData(name);
			
			tv1.setText(name + " ");
			

			
			tv1.setLayoutParams(lptv);
			l.addView(tv1);
			
			//if button... buttonize!
			
			if (object instanceof String)//type.equals(String.class.toString()))
			{
				v2 = new TextView(c);
				((TextView)v2).setText((String)object);
				v2.setLayoutParams(lpvv);
				l.addView((TextView)v2);
			}
			if (object instanceof Integer)//type.equals(Integer.class.toString()))
			{
				v2 = new TextView(c);
				((TextView)v2).setText(String.valueOf((Integer)object));
				v2.setLayoutParams(lpvv);
				l.addView((TextView)v2);
			}
			
			
			return l;
			
		}
	}
	//Functions
	private void buttonClick(View v){
		this.mCurrentEntity.buttonClick((Integer)v.getTag());
		updateNameInfo();
	}
	private void checkBoxChecked(View v, boolean isChecked){
		CheckBox checkBox = (CheckBox) v;
		String key = (String) checkBox.getTag();
		EntityProperty entityProperty = (EntityProperty) mCurrentEntity.getProperty(key);
		if (!entityProperty.isReadOnly()){
		entityProperty.setData(checkBox.isChecked());
		entityProperty.updateProperties();
		}
		updateNameInfo();
	}
	private void startActivity (View v, Class activity){
		Button b = (Button) v;
		Intent intent = new Intent (this, activity);
		intent.putExtra("KEY", b.getText().toString());
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
	}
	private void updateAdapters () {
//there seems like there should be a better way to do this
		try {
			//Put midgets back in porn
			//Information
			updateNameInfo();
		
//TODO Put back images in the device property table

		}catch (Exception ex){
			mConsole.error("UpdateAdapters(): " +ex.getMessage());
			ex.printStackTrace();
		}
	}
	public void updateNameInfo () {
		
		Set<String> PropertyKeys;
		mInfoTable.removeAllViews();
		mInfoTable.requestLayout();
		
		
		
		if (mCurrentEntity != null){
			mNameLabel.setText(mCurrentEntity.getName());
			mInfoLabel.setText(mCurrentEntity.getInfo());
			
			PropertyKeys = mCurrentEntity.getPropertyKeySet();
			
			for (String key : PropertyKeys){
				
				EntityProperty entityProperty = (EntityProperty) mCurrentEntity.getProperty(key);
				
				if (entityProperty.getType() == ENTITY_PROPERTY_TYPE.NO_DISPLAY){
					continue;
				}
				
			
				TableRow mTableRow = new TableRow(getApplicationContext());
				LayoutParams params = new LayoutParams();
				params.width = LayoutParams.FILL_PARENT;
				mTableRow.setLayoutParams(params);
				mTableRow.setWeightSum(2);

				TextView nameView = new TextView(getApplicationContext());
				params.weight = 1;
				nameView.setText(key);
				nameView.setLayoutParams(params);
				mInfoTable.addView(mTableRow);
				mTableRow.addView(nameView);
			
				//mCurrentEntity.refreshProperty(key);
				
				switch (entityProperty.getType()){
					case CHECK_BOX :
						Boolean checked = (Boolean) entityProperty.getData();
						CheckBox checkBox = new CheckBox(getApplicationContext());
						if (entityProperty.isReadOnly()){
							checkBox.setEnabled(false);
						}
						checkBox.setChecked(checked);
						checkBox.setLayoutParams(params);
						checkBox.setTag(key);
						checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener (){

							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								checkBoxChecked(buttonView, isChecked);
								
							}

						});

						mTableRow.addView(checkBox);
						break;
					case LABEL:
						String string = (String) entityProperty.getData();
						TextView textView = new TextView(getApplicationContext());
						textView.setText(string);
						textView.setLayoutParams(params);
						mTableRow.addView(textView);
						break;
					case EDIT_BOX:
						String editString = (String) entityProperty.getData();
						EditText editBox = new EditText(getApplicationContext());
						editBox.setText(editString);
						editBox.setLayoutParams(params);
						mTableRow.addView(editBox);
						break;
					case SPINNER:
						break;
					case NUMBER_SLIDER:
						break;
					case NUMBER_BOX:
						Object obj = entityProperty.getData();
						TextView textIntegerView = new TextView(getApplicationContext());
						textIntegerView.setText(obj.toString());
						textIntegerView.setLayoutParams(params);
						mTableRow.addView(textIntegerView);
						break;
					case BUTTON:
						Button button = new Button(this);
						button.setText(key);
						button.setTag((Integer)entityProperty.getData());
						button.setLayoutParams(params);
						button.setOnClickListener(new OnClickListener(){

							public void onClick(View v) {
								buttonClick(v);
								
							}
							
						});
						mTableRow.addView(button);
						break;
					case ACTIVITY_BUTTON:
						Button activityButton = new Button (getApplicationContext());
						activityButton.setLayoutParams(params);
						activityButton.setText(key);
						activityButton.setTag(entityProperty.getData());
						activityButton.setOnClickListener(new OnClickListener(){

							public void onClick(View v) {
								startActivity(v, (Class<?>)v.getTag());
								
							}
							
						});
						mTableRow.addView(activityButton);
						break;
					default:
						break;
				}


				
				mInfoTable.requestLayout();
			}
		}
		else{
			mNameLabel.setText("");
			mInfoLabel.setText("");
		}
		
		
	}
	
	//Lifecycle
	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		
		//Called after activity has been stopped, prior to being started again, always followed by onStart()
		//Perhaps to fix things that didnt get a clean stop
		
		//Followed by onStart()
		//Compliment == onStop()
		
		super.onRestart();
	}
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is becoming visible to the user
		
		//Follwed by onResume() is the activity comes to the foreground or onStop() if it becomes hidden
		//Compliment == onStop()
		
		super.onStart();
	}
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity will start interfacing with the user. The activity is at the top of the stack
		
		//Compliment == onPause()
		
		super.onResume();
	}
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is about to start resuming a previous activity
		//Typically used to 
			//commit unsaved changes to persistent data
			//stop animations
			//stop other things that use CPU time
		//This must be a very quick implementation because the next activity will not resume until this method returns
		
		//Followed by either onResume() if the activity returns to the top, or onStop() if it becomes invisible to the user
		//Compliment == onPause()
		super.onPause();
	}
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		
		//Called when the activity is no longer visible to the user, because another activyt has been resumed, and is covering this one.
		//This may happen when another activity is being brought to front, or this one is being destroyed
		
		//Followed by
			//onRestart() if this activity is coming back to interact with the user
			//onDestroy() if this activity is goign away
		//Compliment == onRestart(), onStart()
		
		super.onStop();
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}

}