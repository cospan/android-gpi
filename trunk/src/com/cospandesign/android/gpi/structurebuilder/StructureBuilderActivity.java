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

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.gpi.R;

import dlcm.builder.StructureManager;

public class StructureBuilderActivity extends Activity {

	StructureManager mStructureManager;
	ImageView mButtonBack;
	Button mButtonParse;
	EditText mEditor;
	TextView mStatus;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.lcm_generator_layout);
		mButtonBack = (ImageView)findViewById(R.id.LCMStructureBuilderBackImageView);
		mButtonParse = (Button)findViewById(R.id.LCMStructurGeneratorParseButton);
		mEditor = (EditText)findViewById(R.id.LCMStructureGeneratorEditor);
		mStatus = (TextView)findViewById(R.id.LCMStructureBuilderStatusTextView);
		
		mStructureManager = ((GpiApp)getApplication()).getStructureManager();
		
		mButtonBack.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				backPressed();				
			}
		});
		mButtonParse.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				parsePressed();
			}
		});
		mEditor.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				mStatus.setTextColor(Color.WHITE);
			}
		});
		
		setEditorInitialStructure();

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	setResult(RESULT_OK);
	    	finish();
	        return true;
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	//Buttons
	private void backPressed(){
		setResult(RESULT_OK);
		finish();
	}
	private void parsePressed(){
		try{
			mStructureManager.parseString(mEditor.getText().toString());
		}
		catch (Exception ex){
			setStatus(ex.getMessage(), Color.RED);
			return;
		}
		setStatus("Successfully Parsed!", Color.GREEN);
		
	}
	
	//Functions
	private void setStatus(String status, int color){
		mStatus.setText(status);
		mStatus.setTextColor(color);
	}
	private void setStatus(String status){
		mStatus.setText(status);
		mStatus.setTextColor(Color.WHITE);
	}
	private void setEditorInitialStructure (){
		mEditor.setText(
				
				"struct simple_int \n" +
				"{\n" +
				"    int32_t num;\n" +
				"};"
				
		);
	}	

}
