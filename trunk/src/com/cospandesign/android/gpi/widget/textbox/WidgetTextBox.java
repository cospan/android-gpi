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

package com.cospandesign.android.gpi.widget.textbox;

import java.util.Set;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cospandesign.android.gpi.entity.EntityProperty;
import com.cospandesign.android.gpi.entity.EntityProperty.ENTITY_PROPERTY_TYPE;
import com.cospandesign.android.gpi.widget.Widget;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class WidgetTextBox extends Widget {

	LinearLayout mTextBoxWidget;
	EditText mSendEditBox;
	ImageButton mButtonSend;
	TextView mTextViewRead;
	
	static final String INPUT_STRING_CHANNEL_NAME = "String in";
	static final String OUTPUT_STRING_CHANNEL_NAME = "String Out";
	
	//Property
	static final String ECHO_TEXT_PROPERTY = "Echo Text";
	static final String APPEND_RETURN_PROPERTY = "Append Return";
	
	boolean mEchoFlag = true;
	boolean mAppendReturnFlag = true;
	
	public WidgetTextBox(String name, String info, Integer image, Context c, boolean enabled) {
		
		super(name, info, image, c, enabled);

		addProperty(ECHO_TEXT_PROPERTY, new Boolean(mEchoFlag), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Echo output data on input text", false);
		addProperty(APPEND_RETURN_PROPERTY, new Boolean(mAppendReturnFlag), ENTITY_PROPERTY_TYPE.CHECK_BOX, "Append a return to the incomming string", false);
		
		
		addInputDataChannel(INPUT_STRING_CHANNEL_NAME, String.class);
		addOutputDataChannel(OUTPUT_STRING_CHANNEL_NAME, String.class);
		

	}
	@Override
	public void guiInitialization(WorkspaceEntity workspaceEntity) {
		super.guiInitialization(workspaceEntity);
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTextBoxWidget = (LinearLayout) inflater.inflate(R.layout.textbox_widget_layout, null);
		
		AddView(mTextBoxWidget);
		setDefualtViewDimensions(200, 200);
		mSendEditBox = (EditText) mTextBoxWidget.findViewById(R.id.textboxEditText);
		mButtonSend = (ImageButton) mTextBoxWidget.findViewById(R.id.textboxSendImageButton);
		mTextViewRead = (TextView) mTextBoxWidget.findViewById(R.id.textboxOutputText);
		mTextViewRead.setText("");
		
		mSendEditBox.setOnKeyListener(new OnKeyListener(){

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER){
					String writeString = mSendEditBox.getText().toString();
					mSendEditBox.setText("");
					if (mEchoFlag){
						mTextViewRead.append(writeString);
					}
					sendData(writeString);
				}
				return false;
			}
			
		});
		mButtonSend.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				sendClick();
				
			}
			
		});
	}
	void sendClick (){
		String writeString = mSendEditBox.getText().toString();
		mSendEditBox.setText("");
		if (mEchoFlag){
			mTextViewRead.append(writeString + "\n");
		}
		sendData(writeString);
	}
	void sendData(String data){
		outputData(OUTPUT_STRING_CHANNEL_NAME, data);
	}
	@Override
	public void propertiesUpdate() {
		super.propertiesUpdate();
		mEchoFlag = (Boolean) getPropertyData(ECHO_TEXT_PROPERTY);
		mAppendReturnFlag = (Boolean) getPropertyData(APPEND_RETURN_PROPERTY);
	}

	@Override
	public void newDataAvailable(Set<String> inputChannels) {
		super.newDataAvailable(inputChannels);
		if (inputChannels.contains(INPUT_STRING_CHANNEL_NAME)){
			String inString = new String ((String)dequeueInputData(INPUT_STRING_CHANNEL_NAME));
			mTextViewRead.append(inString);
			if (mAppendReturnFlag){
				mTextViewRead.append("\n");
			}
		}
	}
	
	

}
