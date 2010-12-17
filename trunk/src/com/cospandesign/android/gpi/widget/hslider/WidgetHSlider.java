package com.cospandesign.android.gpi.widget.hslider;

import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class WidgetHSlider extends Widget {

	LinearLayout mHSliderLayout;
	SeekBar mHorizontalSlider;
	
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	//Channel Names
	static final String INPUT_NUMBER_CHANNEL_NAME = "Number IN";
	static final String OUTPUT_NUMBER_CHANNEL_NAME = "Number OUT";
	
	//Properties
	static final String NUMBER_VALUE_PROPERTY = "Numeric Value";
	static final String NUMBER_MAX_PROPERTY = "Max Value";
	static final String NUMBER_MIN_PROPERTY = "Min Value";
	static final String CONTINUOUS_READ_PROPERTY = "Continously Read";
	
	int mMax = 100;
	int mMin = 0;
	int mValue = 50;
	boolean mContinuousEnable = false;
	
	int mOffset = 0;
	
	public WidgetHSlider(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		
		addProperty(NUMBER_VALUE_PROPERTY, new Integer(mValue), ENTITY_PROPERTY_TYPE.NUMBER_SLIDER, "Slider Value", false);
		addProperty(NUMBER_MAX_PROPERTY, new Integer(mMax), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Slider Max Value", false);
		addProperty(NUMBER_MIN_PROPERTY, new Integer(mMin), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Slider Min Value", false);
		addProperty(CONTINUOUS_READ_PROPERTY, new Boolean(mContinuousEnable), ENTITY_PROPERTY_TYPE.CHECK_BOX, "True: Continuous | False: only on up", false);
		
		addInputDataChannel(INPUT_NUMBER_CHANNEL_NAME, Integer.class);
		addOutputDataChannel(OUTPUT_NUMBER_CHANNEL_NAME, Integer.class);
	}

	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		LayoutInflater inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHSliderLayout = (LinearLayout) inflator.inflate(R.layout.horizontal_slider, null);
		
		AddView(mHSliderLayout);
		setDefualtViewDimensions(200, 50);
		mHorizontalSlider = (SeekBar) mHSliderLayout.findViewById(R.id.HorizontalSlider);
		mHorizontalSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar sliderBar, int progress, boolean userChanged) {
				mValue = progress + mOffset;
				if (mContinuousEnable){
					outputData(OUTPUT_NUMBER_CHANNEL_NAME, new Integer(mValue));
				}
			}

			public void onStartTrackingTouch(SeekBar sliderBar) {
				
				mGpiConsole.verbose("Starting to track");
			}

			public void onStopTrackingTouch(SeekBar sliderBar) {
				setProperty(NUMBER_VALUE_PROPERTY, new Integer(mValue), ENTITY_PROPERTY_TYPE.NUMBER_SLIDER, "Slider Value", false);
				outputData(OUTPUT_NUMBER_CHANNEL_NAME, new Integer(mValue));
				mGpiConsole.verbose("New Value: " + mValue);
			}
			
		});
		
	}

	@Override
	public void propertiesUpdate() {
		// TODO Auto-generated method stub
		super.propertiesUpdate();
		
		mMax = (Integer) getPropertyData(NUMBER_MAX_PROPERTY);
		mMin = (Integer) getPropertyData(NUMBER_MIN_PROPERTY);
		mValue = (Integer) getPropertyData(NUMBER_VALUE_PROPERTY);
		mContinuousEnable = (Boolean) getPropertyData(CONTINUOUS_READ_PROPERTY);
		
		mOffset = -mMin;
		
		mHorizontalSlider.setMax(mMax + mOffset);
		mHorizontalSlider.setProgress(mValue + mOffset);

		mHSliderLayout.requestLayout();
		
	}

	@Override
	public void newDataAvailable(Set<String> inputChannels) {
		super.newDataAvailable(inputChannels);
		
		if (inputChannels.contains(INPUT_NUMBER_CHANNEL_NAME)){
			mValue = (Integer)(dequeueInputData(INPUT_NUMBER_CHANNEL_NAME));
			if ((mMin <= mValue) && (mValue <= mMax)){
				setProperty(NUMBER_VALUE_PROPERTY, new Integer(mValue), ENTITY_PROPERTY_TYPE.NUMBER_SLIDER, "Slider Value", false);
				mHorizontalSlider.setProgress(mValue + mOffset);
			}
			else {
				mGpiConsole.verbose("Out of Range of Horizontal Slider Min: " + mMin + " Value: " + mValue + " Max: " + mMax );
			}
		}
		
	}
	
	

}
