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


package com.cospandesign.android.gpi.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.device.Device;
import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class PropertyManagerActivity extends Activity
{
	
	public ArrayList<WorkspaceEntity> mWses;
	Controller mSubServ;
	HashMap<String, Entity> mEntityMap;	
	ArrayList<Widget> mWidgets;
	
	View mPropertyView;
	LinearLayout mainPropertyViewGroup;

	
	TextView mNameLabel;
	TextView mInfoLabel;
	
	ImageView mIcon;
	ImageView mBackButton;
	ImageView mSetButton;
	
	TableLayout mPropertyTable;
	ListView mPropertyList;
	
	HashMap<String, Object> mTempPropertiesMap;
	
	Entity mEntity;
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.property_linear_layout);
		
		//mPropertyView = activity.getLayoutInflater().inflate(R.layout.property_linear_layout, mainPropertyViewGroup);
		
		mSubServ = (Controller)((GpiApp)getApplication()).getControlTree().getController();
		mEntityMap = new HashMap<String, Entity>();
		mWidgets = ((GpiApp)getApplication()).getWidgets();
		mWses = ((GpiApp)getApplication()).getActiveWses();
		
		
		
		mainPropertyViewGroup = (LinearLayout) findViewById(R.id.MainPropertyLinearLayout);
		mNameLabel = (TextView) findViewById(R.id.property_Name);
		mInfoLabel = (TextView) findViewById(R.id.property_Info);
		mIcon = (ImageView) findViewById(R.id.IconImageView);
		
		//mPropertyTable = (TableLayout) findViewById(R.id.PropertiesTableLayout);
		mPropertyList = (ListView) findViewById(R.id.PropertiesList);
		
		mBackButton = (ImageView) findViewById(R.id.property_back);
		mSetButton = (ImageView) findViewById(R.id.property_set);
		
		mBackButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v){
				BackClick(v);				
			}
		});
		mSetButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				SetClick(v);
			}
		});
			
		mapData();
		
		Bundle bundle = getIntent().getExtras();
		
		//search the incomming bundle for a key
		//because widget's are cloned at start, we can't use this value
/*		
		if (bundle.containsKey("widget")){
			mEntity = mWidgets.get(bundle.getInt("widget"));
		}

		else if (bundle.containsKey("entity")){
			mEntity = mEntityMap.get(bundle.get("entity"));
		}
		*/		
		if (bundle.containsKey("workspace entity")){
			mEntity = mWses.get(bundle.getInt("workspace entity")).getEntity();
		}
		mNameLabel.setText(mEntity.getName());
		mInfoLabel.setText(mEntity.getInfo());
		mIcon.setImageResource(mEntity.getImage());
		
	
		mPropertyList.setAdapter(new PropertyAdapter(this, mEntity));
		//setupPropertyTable();
		
	}
	private void setupPropertyTable(){
		Set<String> propertyKeys;
		mPropertyTable.removeAllViews();
		mPropertyTable.requestLayout();
		
		if (mEntity != null){
			mNameLabel.setText(mEntity.getName());
			mInfoLabel.setText(mEntity.getInfo());
			EntityProperty entityProperty;
			
			propertyKeys = mEntity.getPropertyKeySet();

			for (String key : propertyKeys){
				entityProperty = (EntityProperty) mEntity.getProperty(key);
				if (entityProperty.getType() == ENTITY_PROPERTY_TYPE.NO_DISPLAY){
					continue;
				}

				
				TableRow tableRow = new TableRow(getApplicationContext());
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				tableRow.setLayoutParams(params);
				tableRow.setWeightSum(2);
				mPropertyTable.addView(tableRow);
				tableRow.requestLayout();
				
/*					
				LinearLayout linearLayout = new LinearLayout(getApplicationContext());
				
				tableRow.addView(linearLayout);
			
				LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(tableRow.getMeasuredHeight()/2, tableRow.getMeasuredHeight());
				lparams.weight = 1;
				linearLayout.setOrientation(LinearLayout.HORIZONTAL);
				linearLayout.setLayoutParams(lparams);
				
				LinearLayout.LayoutParams lptv = new LinearLayout.LayoutParams(tableRow.getMeasuredWidth()/2, LayoutParams.WRAP_CONTENT, .5f);
				LinearLayout.LayoutParams lpvv = new LinearLayout.LayoutParams(tableRow.getMeasuredWidth()/2, LayoutParams.WRAP_CONTENT, .5f);
*/				
				
				TextView tv = new TextView(getApplicationContext());
				//tv.setLayoutParams(lptv);
				LayoutParams tvparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				LayoutParams valueParams = new LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				
				tv.setLines(2);
				tv.setText(key + "\n" + entityProperty.getDescription());
				tv.setLayoutParams(tvparams);
				
				tableRow.addView(tv);
				
				if (entityProperty.getType() == null){
					entityProperty.findDefaultType();
				}
				
				switch (entityProperty.getType()){
				case EDIT_BOX :
					TextEditBox et = new TextEditBox(getApplicationContext(), entityProperty, key);
					et.setLayoutParams(valueParams);
					tableRow.addView(et);
					break;

				case NUMBER_BOX:
					final NumberEditBox nt = new NumberEditBox(getApplicationContext(), entityProperty, key);
					nt.setLayoutParams(valueParams);
					tableRow.addView(nt);
					break;
				
				case NUMBER_SLIDER:
					LinearLayout ll = new LinearLayout(getApplicationContext());
					final TextView v3 = new TextView(getApplicationContext());
					v3.setText(((Integer)entityProperty.getData()).toString());
					final SeekBar v4 = new PropertySeekBar(getApplicationContext(), entityProperty, key, v3);
					v4.setLayoutParams(valueParams);
					v4.setMax(entityProperty.getMax() - entityProperty.getMin());
					v4.setProgress(((Integer)entityProperty.getData()) - entityProperty.getMin());
					ll.setLayoutParams(valueParams);
					ll.setOrientation(LinearLayout.HORIZONTAL);
					ll.addView(v3);
					ll.addView(v4);
					tableRow.addView(ll);		
				break;
				case SPINNER:
					Spinner sp = new Spinner(getApplicationContext());
					sp.setLayoutParams(valueParams);
					PropertySpinnerAdapter psa = new PropertySpinnerAdapter(getApplicationContext(), entityProperty, key);
					sp.setAdapter(psa);
					sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener (){


//						@Override
						public void onItemSelected(AdapterView<?> parent, View v,
								int position, long id)
						{
							final String name = (String)mEntity.getEntityProperties().keySet().toArray()[position];
							final EntityProperty ep = mEntity.getEntityProperties().get(name);
							
							ep.setIndex(position);
							
							
						}

//						@Override
						public void onNothingSelected(AdapterView<?> parent)
						{
							
						}
						
					});
					sp.setSelection(entityProperty.getIndex());
					tableRow.addView(sp);
					break;
				case CHECK_BOX:
					CheckBox cb = new CheckBox(getApplicationContext());
					cb.setLayoutParams(valueParams);
					cb.setChecked((Boolean)entityProperty.getData());
					cb.setTag(new String (key));
					

					cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

//						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked)
						{
							String name = (String) buttonView.getTag();
							EntityProperty ep = mEntity.getEntityProperties().get(name);
						
							ep.setData(isChecked);
							((CheckBox)buttonView).setChecked((Boolean)ep.getData());
							
							
							Toast.makeText(getApplicationContext(), name + "has been changed to: " + ((Boolean)ep.getData()).toString(), Toast.LENGTH_SHORT).show();
							
						} 
					
					
					});
					tableRow.addView(cb);
					break;
				
				}

				mPropertyTable.requestLayout();

				
			}
		}
	}
	private void mapData()
	{
		
		//The workspace only needs Mediums, Devices, and Widgets
		for (Medium medium: mSubServ.getMediums())
		{

			mEntityMap.put(medium.getName(), medium);
			
			for (Device device: medium.getDevices())
			{
				mEntityMap.put(device.getName(), device);
			}
		}
		mEntityMap.put(mSubServ.getName(), mSubServ);

	}
	
	protected void BackClick(View v)
	{
		//Need to hide this view, and go to the previous view
		finish();
	}
	protected void SetClick (View v){
		mTempPropertiesMap = ((PropertyAdapter)mPropertyList.getAdapter()).getmTempProperties();
		EntityProperty entityProperty = null;
		//Go through each of the items in the map, and set the properties
		for (String name : mTempPropertiesMap.keySet()){
			if (mTempPropertiesMap.get(name) == null){
				continue;
			}
			entityProperty = (EntityProperty)mEntity.getProperty(name);
			switch (entityProperty.getType()){
			case EDIT_BOX:
				TextEditBox eb = (TextEditBox) mTempPropertiesMap.get(name);
				entityProperty.setDataQuietly(eb.getText().toString());
				break;
			case CHECK_BOX:
				CheckBox cb = (CheckBox) mTempPropertiesMap.get(name);
				entityProperty.setDataQuietly((Boolean)cb.isChecked());
				break;
			case SPINNER:
				Spinner sp = (Spinner)mTempPropertiesMap.get(name);
				entityProperty.setIndex(sp.getSelectedItemPosition());
				break;
			case NUMBER_SLIDER:
				SeekBar sb = (SeekBar)mTempPropertiesMap.get(name);
				entityProperty.setDataQuietly(((Integer)sb.getProgress() - entityProperty.getMin()));
				break;
			case NUMBER_BOX:
				NumberEditBox nb = (NumberEditBox) mTempPropertiesMap.get(name);
				if (entityProperty.getData() instanceof Float){
					try{
						Float f = Float.valueOf(nb.getText().toString());
						entityProperty.setDataQuietly(f);
					}
					catch (NumberFormatException ex)	{
						Toast.makeText(getApplicationContext(), name + " is not a float text value", Toast.LENGTH_SHORT).show();
					}

				}
				else if (entityProperty.getData() instanceof Integer){
					try{
						Integer integer = Integer.valueOf(nb.getText().toString());
						entityProperty.setDataQuietly(integer);
					}
					catch (NumberFormatException ex){
						Toast.makeText(getApplicationContext(), name + " is not a integer text value", Toast.LENGTH_SHORT).show();
					}

				}
				
				else{
					Toast.makeText(getApplicationContext(), name + " data type is not an Integer, or Float", Toast.LENGTH_LONG).show();
				}
				
				//entityProperty.setValueQuietly(nb.getText().toString());
				break;
			default:
				break;
			}
			
			
		}
		entityProperty.updateProperties();
	}
	public class PropertyAdapter extends BaseAdapter {

		Context mContext;
		Entity mEntity;
		HashMap<String, Object> mTempPropertiesMap;
		
		public PropertyAdapter(Context context, Entity entity)
		{
			
			mContext = context;
			mEntity = entity;
			mTempPropertiesMap = new HashMap<String, Object>();
		}
		public int getCount()
		{
			if (mEntity == null)
			{
				return 0;
			}
			return mEntity.getEntityProperties().size();
		}
		public Object getItem(int position)
		{

			if (mEntity == null){
				return null;
			}
			return mEntity.getProperty((String) mEntity.getPropertyKeySet().toArray()[position]);
		}
		public long getItemId(int position)
		{
			
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mEntity == null){
				return null;
			}
			if (mEntity.getEntityProperties().size() == 0){
				return null;
			}
		
			final int pos = position;
		
			String name = (String)mEntity.getEntityProperties().keySet().toArray()[position];
//			mEntity.refreshProperty(name);
			final EntityProperty ep = mEntity.getEntityProperties().get(name);
			
			LinearLayout.LayoutParams lptv = new LinearLayout.LayoutParams(parent.getMeasuredWidth()/2, LayoutParams.WRAP_CONTENT, .5f);
			LinearLayout.LayoutParams lpvv = new LinearLayout.LayoutParams(parent.getMeasuredWidth()/2, LayoutParams.WRAP_CONTENT, .5f);
			LinearLayout l = new LinearLayout(mContext);
			l.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(mContext);
			tv.setLayoutParams(lptv);
			tv.setLines(2);
			tv.setText(name + "\n" + ep.getDescription());
			l.addView(tv);
			
			//final View v2;
			//This is totally dependent on the type of data we have.
			//Try and get the information from the "Type" field.
			//If it doesn't exist, use the default
			
			if (ep.getType() == null){
				ep.findDefaultType();
			}
			
			switch (ep.getType()){
			case EDIT_BOX:
				TextEditBox et = new TextEditBox(mContext, ep, name);
				et.setLayoutParams(lpvv);
				l.addView(et);
				mTempPropertiesMap.put(name, et);
				break;
			case NUMBER_BOX:
				final NumberEditBox nt = new NumberEditBox(mContext, ep, name);
				nt.setLayoutParams(lpvv);
				l.addView(nt);
				mTempPropertiesMap.put(name, nt);
				break;
			case CHECK_BOX:
				CheckBox cb = new CheckBox(mContext);
				cb.setLayoutParams(lpvv);
				cb.setChecked((Boolean)ep.getData());
				mTempPropertiesMap.put(name, cb);/*
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

//					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						String name = (String)mEntity.getProperties().keySet().toArray()[pos];
						//EntityProperty ep = mEntity.getProperties().get(name);
					
						//ep.setValue(isChecked);
						//((CheckBox)buttonView).setChecked((Boolean)ep.getValue());
						mTempPropertiesMap.put(name, buttonView);
						
						//Toast.makeText(mContext, name + "has been changed to: " + ((Boolean)ep.getValue()).toString(), Toast.LENGTH_SHORT).show();
						
					} 
				
				
				});
				*/
				l.addView(cb);
				break;
			case SPINNER:
				Spinner sp = new Spinner(mContext);
				sp.setLayoutParams(lpvv);
				PropertySpinnerAdapter psa = new PropertySpinnerAdapter(mContext, ep, name);
				sp.setAdapter(psa);
				mTempPropertiesMap.put(name, sp);
				/*
				sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener (){


//					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id)
					{
						final String name = (String)mEntity.getProperties().keySet().toArray()[position];
						final EntityProperty ep = mEntity.getProperties().get(name);
						
						ep.setIndex(position);
						
						
					}

//					@Override
					public void onNothingSelected(AdapterView<?> parent)
					{
						
					}
					
				});
				*/
				sp.setSelection(ep.getIndex());
				l.addView(sp);
				break;
			case NUMBER_SLIDER:
				LinearLayout ll = new LinearLayout(mContext);
				final TextView v3 = new TextView(mContext);
				v3.setText(((Integer)ep.getData()).toString());
				final SeekBar v4 = new PropertySeekBar(mContext, ep, name, v3);
				v4.setLayoutParams(lpvv);
				v4.setMax(ep.getMax() - ep.getMin());
				v4.setProgress(((Integer)ep.getData()) - ep.getMin());
				ll.setLayoutParams(lpvv);
				ll.setOrientation(LinearLayout.HORIZONTAL);
				ll.addView(v3);
				ll.addView(v4);
				l.addView(ll);
				mTempPropertiesMap.put(name, v4);
				break;
	
				/*
			default:
				TextView unknownLabel = new TextView(mContext);
				unknownLabel.setLayoutParams(lpvv);
				unknownLabel.setText("Unknown Type of Data");
				l.addView(unknownLabel);
				break;
				*/
			}
						
			return l;
		}
		public HashMap<String, Object> getmTempProperties() {
			return mTempPropertiesMap;
		}

		
	}
	
	public class PropertySpinnerAdapter implements SpinnerAdapter {

		Context mContext;
		EntityProperty mEntityProperty;
		ArrayList<String> mData;
		String mName;
		
		PropertySpinnerAdapter(Context context, EntityProperty ep, String name){
			mContext = context;
			mEntityProperty = ep;
			mName = name;
			mData = (ArrayList<String>)mEntityProperty.getData();
		}
		
//		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent)
		{
			//TODO how do I fix this??
			TextView tv = new TextView(mContext);
			tv.setText(mData.get(position));
			return tv;
		}

//		@Override
		public int getCount()
		{
			return mData.size();
		}

//		@Override
		public Object getItem(int position)
		{
			//FIXME this might not be correct, it may need a text view returned as apposed to just the text
			return mData.get(position);
			
		}

//		@Override
		public long getItemId(int position)
		{
			return position;
		}

//		@Override
		public int getItemViewType(int position)
		{
			//FIXME I just arbitraily chose a number, cause I think that I will be responsible for the number of different views
			return 1;
		}

//		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView tv = new TextView(mContext);
			tv.setText(mData.get(position));
			return tv;
		}

//		@Override
		public int getViewTypeCount()
		{
			return 1;
		}

//		@Override
		public boolean hasStableIds()
		{
			return true;
		}

//		@Override
		public boolean isEmpty()
		{
			return mData.isEmpty();
		}

//		@Override
		public void registerDataSetObserver(DataSetObserver observer)
		{
			//Our data doesn't change, the only thing that we need is the index of the data
			
		}

//		@Override
		public void unregisterDataSetObserver(DataSetObserver observer)
		{
			//Our data doesn't change we only need the index that the user selected
			
		}
		
	}
	
	public class PropertySeekBar extends SeekBar {

		EntityProperty mEntityProperty;
		String mName;
		Context mContext;
		TextView mStatusView;
		boolean tracking = false;
		
		public PropertySeekBar(Context context, EntityProperty ep, String name, TextView textView)
		{
			super(context);
			mEntityProperty = ep;
			mContext = context;
			mStatusView = textView;
			mName = name;
		
			setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

//				@Override
				public void onProgressChanged(SeekBar seekBar,
						int progress, boolean fromUser)
				{
					//mEntityProperty.setValue(progress - mEntityProperty.Min);
					mStatusView.setText(((Integer)mEntityProperty.getData()).toString());
					//update the text view
					if (tracking == false){
						//Toast t = new Toast(mContext);
						//t.setText(mName + "has been changed to: " + ((Integer)mEntityProperty.getValue()).toString());
						//t.show();
					}
				}

//				@Override
				public void onStartTrackingTouch(SeekBar seekBar)
				{
					tracking = true;

				}

//				@Override
				public void onStopTrackingTouch(SeekBar seekBar)
				{
					tracking = false;
					
				}
				
			});
		}
		
	}
	
	public class NumberEditBox extends EditText {

		Context mContext;
		final EntityProperty mEntityProperty;
		final String mName;
		OnEditorActionListener mEAL;
		boolean invalidFormatFlag = false;
		
		public NumberEditBox(Context context, EntityProperty ep, String name)
		{
			super(context);
			
			mContext = context;
			mEntityProperty = ep;
			mName = name;
			
			setupEditor();
		}
		
		public void setupEditor (){

			setSingleLine();
			setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
			setKeyListener(new NumberKeyListener(){

				@Override
				protected char[] getAcceptedChars()
				{
					if (mEntityProperty.getData() instanceof Integer){
						char[] numberChars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-'};
						return numberChars;
					}
					
					char[] numberChars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.', '-'};
					return numberChars;
				}

//				@Override
				public int getInputType()
				{
					// TODO Auto-generated method stub
					return InputType.TYPE_CLASS_NUMBER;
				}				
			});
			
			
			if (mEntityProperty.getData() instanceof Float){
				this.setText(((Float)mEntityProperty.getData()).toString());
			}
			if (mEntityProperty.getData() instanceof Integer){
				this.setText(((Integer)mEntityProperty.getData()).toString());
			}
	
			addTextChangedListener(new TextWatcher() {

//				@Override
				public void afterTextChanged(Editable s)
				{
					if (invalidFormatFlag){
						invalidFormatFlag = false;
						s.clear();
						s.append("0");
					}
				}

//				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after)
				{
					
				}

				
//				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count)
				{
					//save the data
					
					if (s.length() != 0)
					{
/*						if (mEntityProperty.getValue() instanceof Float){
							try{
								mEntityProperty.setValue(Float.valueOf(s.toString()));
							}
							catch (NumberFormatException ex)	{
								Toast.makeText(mContext, "Invalid Format", Toast.LENGTH_SHORT).show();
								invalidFormatFlag = true;
							}

						}
						else if (mEntityProperty.getValue() instanceof Integer){
							try{
								mEntityProperty.setValue(Integer.valueOf(s.toString()));
							}
							catch (NumberFormatException ex){
								Toast.makeText(mContext, "Invalid Format", Toast.LENGTH_SHORT).show();
								invalidFormatFlag = true;
							}

						}
						
						else{
							Toast.makeText(mContext, "Data Type is invalid", Toast.LENGTH_SHORT).show();
							invalidFormatFlag = true;
						}
*/						if (!((mEntityProperty.getData() instanceof Integer) || (mEntityProperty.getData() instanceof Float))){
							invalidFormatFlag = true;
						}
					}
				}
				
			});

		}
	}

	public class TextEditBox extends EditText {

		Context mContext;
		final EntityProperty mEntityProperty;
		final String mName;
		OnEditorActionListener mEAL;
		boolean invalidFormatFlag = false;
		
		public TextEditBox(Context context, EntityProperty ep, String name)
		{
			super(context);
			
			mContext = context;
			mEntityProperty = ep;
			mName = name;
			
			setupEditor();
		}
		
		public void setupEditor (){

			setSingleLine();
			setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE);
			
			
			if (mEntityProperty.getData() instanceof Float){
				this.setText(((Float)mEntityProperty.getData()).toString());
			}
			if (mEntityProperty.getData() instanceof Integer){
				this.setText(((Integer)mEntityProperty.getData()).toString());
			}
			if (mEntityProperty.getData() instanceof String){
				this.setText((String)mEntityProperty.getData());
			}
	
			addTextChangedListener(new TextWatcher() {

//				@Override
				public void afterTextChanged(Editable s)
				{
					if (invalidFormatFlag){
						invalidFormatFlag = false;
						s.clear();
						s.append("");
					}
				}

//				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after)
				{
					
				}

				
//				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count)
				{
					//save the data
					
					if (s.length() != 0)
					{
						/*
						if (mEntityProperty.getValue() instanceof String){
							mEntityProperty.setValue(s.toString());
						}
						*/
						if (!(mEntityProperty.getData() instanceof String)){
							Toast.makeText(mContext, "Data Type is invalid", Toast.LENGTH_SHORT).show();
							invalidFormatFlag = true;
						}
						
					}
				}
				
			});
			
			/*mEAL = new OnEditorActionListener(){

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
						//save the data
						if (mEntityProperty.getValue() instanceof Float){
							mEntityProperty.setValue(Float.valueOf(((EditText)v).getText().toString()));
						}
						else if (mEntityProperty.getValue() instanceof Integer){
							mEntityProperty.setValue(Integer.valueOf(((EditText)v).getText().toString()));
						}
						else{
						}
					
					}
					return false;
				
				}
			};
			
			setOnEditorActionListener(mEAL);
		*/	
		}
		
	}

}
