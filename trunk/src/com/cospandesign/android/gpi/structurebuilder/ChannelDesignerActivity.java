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

package com.cospandesign.android.gpi.structurebuilder;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.GpiConstants;
import com.cospandesign.gpi.R;

import dlcm.builder.StructureManager;

public class ChannelDesignerActivity extends Activity{

	private static final int STRUCTURE_ACTIVITY_ID = 1;
	

	
	StructureManager mStructureManager;
	
	Button mStructureChooserButton;
	ImageView mBackButton;
	EditText mName;
	EditText mInfo;
	TextView mStructureNameTextView;
	ImageView mIconView;
	
	String mChannelName;
	String mChannelInfo;
	String mStructureName;
	int mIconId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.channel_designer_layout);
		}catch (Exception ex){
			ex.printStackTrace();
			finish();
		}
		
		//Get handles to everything
		mIconView = (ImageView) findViewById(R.id.ChannelIconImageView);
		mStructureChooserButton = (Button) findViewById(R.id.StructureChooserButton);
		mBackButton = (ImageView) findViewById(R.id.ChannelBackImageView);
		mName = (EditText) findViewById(R.id.ChannelNameEditText);
		mInfo = (EditText) findViewById(R.id.ChannelInfoEditText);
		mStructureNameTextView = (TextView) findViewById(R.id.ChannelStructureNameTextView);
		
		mStructureManager = ((GpiApp)getApplication()).getStructureManager();
		
		setInitialValues(savedInstanceState);

		//Setup Listeners
		mStructureChooserButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				StructureChooserClick(v);
			}
		});
		mBackButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				BackButtonClick(v);
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == STRUCTURE_ACTIVITY_ID && resultCode == RESULT_OK){
			mStructureName = data.getStringExtra(GpiConstants.STRUCTURE_NAME);
			setStructureName();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)){
			BackButtonClick(null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	//Buttons
	private void StructureChooserClick(View v){
		Intent intent = new Intent(this, StructureSelectorActivity.class);
		startActivityForResult(intent, STRUCTURE_ACTIVITY_ID);
	}
	private void BackButtonClick(View v){
		Intent intent = getIntent();
		if (!mStructureManager.containsStructure(mStructureName)){
			setResult(Activity.RESULT_CANCELED, intent);
			finish();
			return;
		}
		intent.putExtra(GpiConstants.CHANNEL_ICON, mIconId);
		intent.putExtra(GpiConstants.CHANNEL_NAME, mName.getText().toString());
		intent.putExtra(GpiConstants.CHANNEL_INFO, mInfo.getText().toString());
		intent.putExtra(GpiConstants.CHANNEL_STRUCTURE, mStructureName);
		
		setResult(RESULT_OK, intent);
		finish();
	}

	//Functions
	private void setInitialValues(Bundle bundle){
		//Get the name of all things in the bundle
		/*
		if (bundle.containsKey(SubServConstants.CHANNEL_NAME)){
			mChannelName = bundle.getString(SubServConstants.CHANNEL_NAME);
		}
		else {
			mChannelName = "Channel" + (new Random()).nextInt(1000);
		}
		if (bundle.containsKey(SubServConstants.CHANNEL_INFO)){
			mChannelInfo = bundle.getString(SubServConstants.CHANNEL_INFO);
		}
		else {
			mChannelInfo = "BoardId" + SubServApp.getBoardId() + ":" + mChannelName;
		}
		if (bundle.containsKey(SubServConstants.CHANNEL_STRUCTURE)){
			mStructureName = bundle.getString(SubServConstants.CHANNEL_STRUCTURE);
		}
		else {
			mStructureName = null;
		}
		if (bundle.containsKey(SubServConstants.CHANNEL_ICON)){
			mIconId = bundle.getInt(SubServConstants.CHANNEL_ICON);
		}
		else {
			mIconId = R.drawable.channel;
		}
		*/
		mChannelName = "Channel" + (new Random()).nextInt(1000);
		mChannelInfo = "BoardId" + GpiApp.getBoardId() + ":" + mChannelName;
		mStructureName = null;
		mIconId = R.drawable.channel;
		
		//Setup Layout based on the bundle information
		mName.setText(mChannelName);
		mInfo.setText(mChannelInfo);
		mIconView.setImageResource(mIconId);
		mStructureChooserButton.setText("Select Structure");
		
		mStructureNameTextView.setText(mStructureName);
	}
	private void setStructureName (){
		mStructureNameTextView.setText(mStructureName);
	}
}
