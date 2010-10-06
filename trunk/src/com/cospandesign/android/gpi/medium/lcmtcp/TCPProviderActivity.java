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

package com.cospandesign.android.gpi.medium.lcmtcp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.cospandesign.android.gpi.GpiApp;
import com.cospandesign.android.gpi.GpiConstants;
import com.cospandesign.android.gpi.controller.Controller;
import com.cospandesign.android.gpi.medium.Medium;
import com.cospandesign.gpi.R;

public class TCPProviderActivity extends Activity{

	static final String TCP_CUSTOM_IP_STRING = "Custom";
	
	
	Controller mSubServController;
	Medium mTCPMedium;
	TextView mStatusTextView;
	Spinner mNetworkInterfaceSpinner;
	CheckBox mTCPServerCheckBox;
	
	volatile HashMap <String, IPStructure> mNetworkIPMap;
	ArrayAdapter<String> mNIArrayAdapter;

	EditText mIP0ET;
	EditText mIP1ET;
	EditText mIP2ET;
	EditText mIP3ET;
	
	EditText mPortNumber;
	Button mConnectButton;
	
	//Multithreaded stuff
	volatile boolean mLoadingNetwork = false;
	String mAsyncError;
	final Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSubServController = (Controller)((GpiApp)getApplication()).getControlTree().getController();

		mAsyncError = null;
		setContentView(R.layout.tcp_provider_layout);

		//Status Text
		mStatusTextView = (TextView) findViewById(R.id.TcpProviderStatusTextView);
		mStatusTextView.setTextColor(Color.BLUE);
		mStatusTextView.setText("Starting");
		
		//Start Server Check Box
		mTCPServerCheckBox = (CheckBox) findViewById(R.id.StartServerCheckBox);
		
		//Setup Network IP map
		mNetworkIPMap = new HashMap<String, IPStructure>();
		
		//Connect button
		mConnectButton = (Button) findViewById(R.id.TCPConnectButton);
		
		//Network Interface Spinner
		mNetworkInterfaceSpinner = (Spinner) findViewById(R.id.NetworkInterfaceSpinner);
		mNIArrayAdapter = new ArrayAdapter<String>(this.getApplicationContext(), android.R.layout.simple_spinner_item);
		mNIArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mNetworkInterfaceSpinner.setAdapter(mNIArrayAdapter);

		
		OnItemSelectedListener spinnerClickHandler = new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				SpinnerClick(position);
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}

		};
		mNetworkInterfaceSpinner.setOnItemSelectedListener(spinnerClickHandler);

		mConnectButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				ConnectClick();
				
			}
			
		});
		
		//IP Text Boxes
		mIP0ET = (EditText) findViewById(R.id.TCPIP0EditText);
		mIP1ET = (EditText) findViewById(R.id.TCPIP1EditText);
		mIP2ET = (EditText) findViewById(R.id.TCPIP2EditText);
		mIP3ET = (EditText) findViewById(R.id.TCPIP3EditText);
		
		//Port number
		mPortNumber = (EditText) findViewById(R.id.TCPPortNumberEditText);
		
		//don't allow the user to edit the ip address for now
		setIPEnable(false);
		
		//launch a sub task which will load the network interfaces so as not to slow down the UI
		ReloadNetworkInterfaces();

	}
	String GenerateProviderString(){
		if (((String)mNetworkInterfaceSpinner.getSelectedItem()).equals("Custom")){
			IPStructure ipstruct = mNetworkIPMap.get(mNetworkInterfaceSpinner.getSelectedItem());
			ipstruct.IP0 = Integer.parseInt(mIP0ET.getText().toString());
			ipstruct.IP1 = Integer.parseInt(mIP1ET.getText().toString());
			ipstruct.IP2 = Integer.parseInt(mIP2ET.getText().toString());
			ipstruct.IP3 = Integer.parseInt(mIP3ET.getText().toString());
		}
		String providerString = "tcpq://";
		providerString += mNetworkIPMap.get(mNetworkInterfaceSpinner.getSelectedItem()).getIPString();
		providerString += ":";
		providerString += mPortNumber.getText();
		return providerString;
	}
	
	//listener
	void SpinnerClick(int position) {

		String selectedItem = (String) mNetworkInterfaceSpinner.getItemAtPosition(position);
		populateIPTextBoxes(selectedItem);
		if (selectedItem.equals(TCP_CUSTOM_IP_STRING)){
			setIPEnable(true);
		}
		else {
			setIPEnable(false);
		}
	
	}
	void ConnectClick(){
		mConnectButton.setEnabled(false);
		//Bundle bundle = new Bundle();
		
		//Intent intent = this.getIntent();
		Intent intent = getIntent();
		intent.putExtra(GpiConstants.PROVIDER_STRING, GenerateProviderString());
		//bundle.putString(new String (SubServConstants.PROVIDER_STRING), new String (GenerateProviderString()));

		//bundle.putInt(SubServConstants.SERVER_PORT_STRING, Integer.parseInt(mPortNumber.getText().toString()));
		intent.putExtra(GpiConstants.SERVER_PORT_STRING, Integer.parseInt(mPortNumber.getText().toString()));
		if (mNetworkIPMap.get(mNetworkInterfaceSpinner.getSelectedItem()).getIPString().equals("127.0.0.1") && mTCPServerCheckBox.isChecked()){
				intent.putExtra(GpiConstants.START_SERVER_STRING, true);
				//bundle.putBoolean(SubServConstants.START_SERVER_STRING, true);
		}
		else {
			//bundle.putBoolean(SubServConstants.START_SERVER_STRING, true);
			intent.putExtra(GpiConstants.START_SERVER_STRING, false);
		}
		
		//intent.putExtras(bundle);
		setResult(RESULT_OK, intent);

		finish();

	}
	
	//IP Functions
	private void populateIPTextBoxes(String networkInterface){
		mIP0ET.setText(mNetworkIPMap.get(networkInterface).getIP0String());
		mIP1ET.setText(mNetworkIPMap.get(networkInterface).getIP1String());
		mIP2ET.setText(mNetworkIPMap.get(networkInterface).getIP2String());
		mIP3ET.setText(mNetworkIPMap.get(networkInterface).getIP3String());
	}
	private void setIPEnable(boolean enable){
		mIP0ET.setEnabled(enable);
		mIP1ET.setEnabled(enable);
		mIP2ET.setEnabled(enable);
		mIP3ET.setEnabled(enable);
	}
	private void setIPEditBoxes(String networkInterface){
		mIP0ET.setText(mNetworkIPMap.get(networkInterface).getIP0String());
		mIP1ET.setText(mNetworkIPMap.get(networkInterface).getIP1String());
		mIP2ET.setText(mNetworkIPMap.get(networkInterface).getIP2String());
		mIP3ET.setText(mNetworkIPMap.get(networkInterface).getIP3String());
	}
	public synchronized void ReloadNetworkInterfaces(){
		if (mLoadingNetwork){
			return;
		}
		Thread t = new Thread(new NetworkLoader());
		t.start();
	}	

	//Inner Classes
	private class IPStructure {
		public int IP0 = 127;
		public int IP1 = 0;
		public int IP2 = 0;
		public int IP3 = 1;
		
		public void setIP(int ip0, int ip1, int ip2, int ip3){
			IP0 = ip0;
			IP1 = ip1;
			IP2 = ip2;
			IP3 = ip3;
		}
		
		public String getIPString(){
			String IPString = "";
			IPString += Integer.toString((Integer) IP0);
			IPString += ".";
			IPString += Integer.toString((Integer) IP1);
			IPString += ".";
			IPString += Integer.toString((Integer) IP2);
			IPString += ".";
			IPString += Integer.toString((Integer) IP3);
			
			return IPString;
		}
		
		public String getIP0String(){
			return Integer.toString((Integer) IP0);
		}
		public String getIP1String(){
			return Integer.toString((Integer) IP1);
		}
		public String getIP2String(){
			return Integer.toString((Integer) IP2);
		}
		public String getIP3String(){
			return Integer.toString((Integer) IP3);
		}
		public void setIPByString(String ipString){
			String [] ips = ipString.split ("\\.");
			IP0 = Integer.parseInt(ips[0]);
			IP1 = Integer.parseInt(ips[1]);
			IP2 = Integer.parseInt(ips[2]);
			IP3 = Integer.parseInt(ips[3]);
		}
	}
	
	//Background Network Loader
	public class NetworkLoader extends Thread {
		public void run(){
			Enumeration<NetworkInterface> interfaces;
			InetAddress address = null;
			Enumeration<InetAddress> addresses;
			
			//Tell the UI thread we are searching for Network Interfaces
			mLoadingNetwork = true;
			mNetworkIPMap.clear();
			mHandler.post(mUpdateNetworkInterfaceRunnable);
			
			Integer count = 0;
			mNetworkIPMap.put(TCP_CUSTOM_IP_STRING, new IPStructure());
			count++;
			//Get an enumeration of the network interfaces of this device
			try {
				interfaces = NetworkInterface.getNetworkInterfaces();
			}
			catch (SocketException ex){
				mAsyncError = ex.getMessage();
				mLoadingNetwork = false;

				return;
			}
			
			//Traverse the interfaces, and add them to the network spinner
			while (interfaces.hasMoreElements()){
				NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
				ni.getDisplayName();
				addresses = ni.getInetAddresses();
				if (!addresses.hasMoreElements()){
					continue;
				}
				//look for an Inet4Address, can't do Inet6Address yet
				while (addresses.hasMoreElements()){
					address = (InetAddress) addresses.nextElement();
					if (address.getClass().equals(Inet4Address.class)){
						break;
					}
				}
				if (!address.getClass().equals(Inet4Address.class)){
					continue;
				}
				mNetworkIPMap.put(ni.getDisplayName(), new IPStructure());
				mNetworkIPMap.get(ni.getDisplayName()).setIPByString(address.getHostAddress());
			}
			
			mLoadingNetwork = false;
			mHandler.post(mUpdateNetworkInterfaceRunnable);

		}
	}
	
	//Handler for callbacks
	final Runnable mUpdateNetworkInterfaceRunnable = new Runnable(){
		public void run() {
			updateNetworkInterfaceResults();
		}
	};
	
	//Asynchronous return call
	private void updateNetworkInterfaceResults(){
		if (mAsyncError != null){
			mStatusTextView.setText(mAsyncError);
			mStatusTextView.setTextColor(Color.RED);
			mAsyncError = null;
			return;
		}
		if (mLoadingNetwork){
			//can't do much so we just update the status
			
			mStatusTextView.setText("Searching for Interfaces...");
			mStatusTextView.setTextColor(Color.BLUE);
			mNetworkInterfaceSpinner.setEnabled(false);

			
		}
		else {
			mNIArrayAdapter.clear();
			for (String string : mNetworkIPMap.keySet()){
				mNIArrayAdapter.add(string);
			}			
			mNIArrayAdapter.notifyDataSetChanged();
			mNetworkInterfaceSpinner.setEnabled(true);
			mStatusTextView.setText("Loaded");
			mStatusTextView.setTextColor(Color.WHITE);
			mNetworkInterfaceSpinner.setSelection(0);
			setIPEditBoxes(TCP_CUSTOM_IP_STRING);

		}
	}

}
