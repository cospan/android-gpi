package com.cospandesign.android.gpi.widget.numberbox;

import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cospandesign.android.gpi.GpiConsole;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class WidgetNumberBox extends Widget {

	
	LinearLayout mNumberBoxLayout;
	Button mNumberSendButton;
	EditText mEditNumberText;
	
	//Channel Names
	static final String INPUT_NUMBER_CHANNEL_NAME = "Number IN";
	static final String OUTPUT_NUMBER_CHANNEL_NAME = "Number OUT";
	
	//Properties
	static final String NUMBER_VALUE_PROPERTY = "Numeric Value";
	
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	boolean mTextBoxBusy = false;


	

	int mValue = 50;

	Integer mTextBoxInteger;
	
	public WidgetNumberBox(String name, String info, Integer image, Context c,
			boolean enabled) {
		super(name, info, image, c, enabled);
		
		addProperty(NUMBER_VALUE_PROPERTY, new Integer(mValue), ENTITY_PROPERTY_TYPE.NUMBER_BOX, "Value", false);
		
		addInputDataChannel(INPUT_NUMBER_CHANNEL_NAME, Integer.class);
		addOutputDataChannel(OUTPUT_NUMBER_CHANNEL_NAME, Integer.class);
		
		mTextBoxInteger = new Integer(0);

	}
	
	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		LayoutInflater inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mNumberBoxLayout = (LinearLayout) inflator.inflate(R.layout.number_box, null);
		
		AddView(mNumberBoxLayout);
		setDefualtViewDimensions(200, 100);
		mEditNumberText = (EditText) mNumberBoxLayout.findViewById(R.id.NumberEditText);
		mNumberSendButton = (Button) mNumberBoxLayout.findViewById(R.id.NumberSendButton);
		mNumberSendButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
				mValue = Integer.parseInt(mEditNumberText.getText().toString());
				setProperty(NUMBER_VALUE_PROPERTY, new Integer(mValue));
				outputData(OUTPUT_NUMBER_CHANNEL_NAME, new Integer(mValue));
				
				}
				catch (Exception ex){
					mGpiConsole.debug("Error attempting to convert integer " + ex.getMessage());
				}
				
			}
			
		});
		
		mNumberBoxLayout.requestLayout();
	}
	@Override
	public void propertiesUpdate() {
		// TODO Auto-generated method stub
		super.propertiesUpdate();
		
		mValue = (Integer) getPropertyData(NUMBER_VALUE_PROPERTY);
		mEditNumberText.setText(mValue);
		mNumberBoxLayout.requestLayout();
		
	}
	@Override
	public void newDataAvailable(Set<String> inputChannels) {
		super.newDataAvailable(inputChannels);
		
		if (mTextBoxBusy){
			if (inputChannels.contains(INPUT_NUMBER_CHANNEL_NAME)){
				dequeueInputData(INPUT_NUMBER_CHANNEL_NAME);
				mGpiConsole.debug("I'm Busy!");
			}
			return;
		}
		//sort of a mutex... so we don't call overwhelm the text box
		mTextBoxBusy = true;
		if (inputChannels.contains(INPUT_NUMBER_CHANNEL_NAME)){
			mValue = (Integer)(dequeueInputData(INPUT_NUMBER_CHANNEL_NAME));
			mTextBoxInteger = mValue;
			mEditNumberText.setText(mTextBoxInteger.toString());
		}	
		mTextBoxBusy = false;
	}	
}
