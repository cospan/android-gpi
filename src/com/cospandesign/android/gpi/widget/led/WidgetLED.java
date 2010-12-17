package com.cospandesign.android.gpi.widget.led;

import java.util.Set;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class WidgetLED extends Widget {

	RadioButton mRadioButton;
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	//Channel Name
	static final String INPUT_BOOLEAN_CHANNEL_NAME = "LED in";
	
	//Property
	static final String LED_STATUS_PROPERTY = "LED Status";
	
	public WidgetLED(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		addProperty(LED_STATUS_PROPERTY, new Boolean(false), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Do you really need more description?", false);
		
		addInputDataChannel(INPUT_BOOLEAN_CHANNEL_NAME, Boolean.class);

	}
	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		mRadioButton = new RadioButton(this.mContext);
		mRadioButton.setChecked(false);
		AddView(mRadioButton);
		setDefualtViewDimensions(50, 50);
		
		mRadioButton.setOnCheckedChangeListener(new OnCheckedChangeListener () {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				setProperty(LED_STATUS_PROPERTY, new Boolean(isChecked));				
			}
		});
	}
	
	@Override
	public void propertiesUpdate() {
		super.propertiesUpdate();
		boolean checked = (Boolean) getPropertyData(LED_STATUS_PROPERTY);
		mRadioButton.setChecked(checked);
	}
	@Override
	public void newDataAvailable(Set<String> inputChannels) {
		super.newDataAvailable(inputChannels);
		if (inputChannels.contains(INPUT_BOOLEAN_CHANNEL_NAME)){
				boolean checked = (Boolean)(dequeueInputData(INPUT_BOOLEAN_CHANNEL_NAME));
				mRadioButton.setChecked(checked);	
		}
	}		
}
