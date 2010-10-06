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

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.GpiConstants;
import com.cospandesign.gpi.R;

import dlcm.builder.StructureManager;

public class StructureSelectorActivity extends Activity {
	
	private static int NEW_STRUCTURE_ACTIVITY_ID = 1;
	
	Button mButtonAddStructure;
	ImageView mButtonBack;
	String mStructureName;
	StructureManager mStructureManager;
	ListView mStructureList;
	//StructureAdapter mStructureListAdapter;
	
	//Overrides
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.structure_selector_layout);
		
		mButtonBack = (ImageView) findViewById(R.id.StructureManagerBackView);
		mButtonAddStructure = (Button) findViewById(R.id.AddNewStructureButton);
		mStructureList = (ListView) findViewById(R.id.StructureList);
		mStructureList.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				structureListClick(position);
				
			}
			
		});
		
		mStructureManager = ((GpiApp)this.getApplication()).getStructureManager();
		
		setupArrayAdapter();
		
		//Button Listeners
		mButtonBack.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				backClick(v);
			}
		});
		mButtonAddStructure.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				addStructureClick(v);
			}
		});
		
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_STRUCTURE_ACTIVITY_ID){
			//Get data from intent
			//mStructureName = data.getStringExtra(SubServConstants.STRUCTURE_NAME);
		}
		
		setupArrayAdapter();

	}
	private void setupArrayAdapter(){
		Set<String> keysSet = mStructureManager.StructureMap.keySet();
		String[] keys = new String[keysSet.size()];
		keysSet.toArray(keys);
		
		keys = mStructureManager.StructureMap.keySet().toArray(keys);
		mStructureList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, keys));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)){
			backClick(null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//Button Clicks	
	private void backClick (View v){
		//put all the values needed back in the intent
		Intent intent = getIntent();
		if (mStructureName == null){
			setResult(Activity.RESULT_CANCELED);
			finish();
			return;
		}
		
		intent.putExtra(GpiConstants.STRUCTURE_NAME, mStructureName);
		setResult(RESULT_OK, intent);
		finish();
		
	}
	private void addStructureClick (View v){
		Intent intent = new Intent(this, StructureBuilderActivity.class);
		//set up intent initial values
		startActivityForResult(intent, NEW_STRUCTURE_ACTIVITY_ID);
	}
	private void structureListClick (int position){
		mStructureName = (String) mStructureList.getItemAtPosition(position);
	}
}
