package com.cospandesign.android.gpi.widget.button;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.view.View.OnTouchListener;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;

public class WidgetButton extends Widget {

	Button mButton;
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	
	//Constants
	int mButtonLongDownTime = 1000; 
	//Channel Name
	static final String OUTPUT_BOOLEAN_CHANNEL_NAME = "Button Press";
	//Property
	static final String BUTTON_NAME_PROPERTY = "Button Name";
	static final String BUTTON_STATUS_PROPERTY = "Button Status";
	static final String BUTTON_LONG_PRESS_TIME = "Long Press Time";
	
	
	
	public WidgetButton(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		
		addProperty(BUTTON_NAME_PROPERTY, new String("Button"), ENTITY_PROPERTY_TYPE.EDIT_BOX, "Name of the button", false);
		addProperty(BUTTON_STATUS_PROPERTY, new Boolean(false), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Status of the button", false);
		addProperty(BUTTON_LONG_PRESS_TIME, new Integer(mButtonLongDownTime), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Amount of time before a long press is detected", false);
		
		addOutputDataChannel(OUTPUT_BOOLEAN_CHANNEL_NAME, Boolean.class);
	}

	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		mButton = new Button(this.mContext);
		mButton.setText((String) getPropertyData(BUTTON_NAME_PROPERTY));
		AddView (mButton);
		setDefualtViewDimensions(100, 50);
		
		mButton.setOnTouchListener( new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				if (event.getAction() == MotionEvent.ACTION_DOWN && (event.getEventTime() > (event.getDownTime() + mButtonLongDownTime))){
					return false;
				}
				
				switch (event.getAction()){
				case (MotionEvent.ACTION_DOWN):
					buttonDown();
					break;
				case (MotionEvent.ACTION_UP):
					buttonUp();
					break;
				case (MotionEvent.ACTION_CANCEL):
					buttonUp();
					break;
				}
				return true;
			}
		});
	}
		
	
	void buttonDown(){
		//button was pressed
		setProperty(BUTTON_STATUS_PROPERTY, new Boolean(true), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Status of the button", false);
		mGpiConsole.verbose("Button " + mButton.getText() + " is pressed");
		outputData(OUTPUT_BOOLEAN_CHANNEL_NAME, true);
		
		
	}
	void buttonUp(){
		//button up
		setProperty(BUTTON_STATUS_PROPERTY, new Boolean(false), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Status of the button", false);
		mGpiConsole.verbose("Button " + mButton.getText() + " is release");
		outputData(OUTPUT_BOOLEAN_CHANNEL_NAME, false);
	}
	
	
	@Override
	public void propertiesUpdate() {
		super.propertiesUpdate();
		mButtonLongDownTime = (Integer) getPropertyData(BUTTON_LONG_PRESS_TIME);
		mButton.setText((String) getPropertyData(BUTTON_NAME_PROPERTY));
		mButton.requestLayout();
		
	}

}
