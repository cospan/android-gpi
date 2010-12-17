package com.cospandesign.android.gpi;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.cospandesign.android.gpi.entity.Entity;
import com.cospandesign.android.gpi.workspace.ConnectionPointView;
import com.cospandesign.android.gpi.workspace.WorkspaceEntity;
import com.cospandesign.gpi.R;

public class GpiSaveExternal extends Activity {

	
	Context mContext;
	public ArrayList<WorkspaceEntity> mWses;
	final GpiConsole mGpiConsole = GpiConsole.getinstance();
	
	ImageView mBackButton;
	ImageView mUpDirButton;
	ImageView mNewFolderButton;
	ImageView mSaveButton;
	
	TextView mStatusTextView;
	TextView mDirectoryTextView;
	EditText mFileNameEditText;
	
	ListView mFileListView;
	FileListAdapter mFileListAdapter;
	
	File mFile;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//inflate view
		try {
			setContentView(R.layout.save_activity_layout);
		}
		catch (Exception ex){
			mGpiConsole.error("Failed to inflate layout", ex);
		}	
		//get context to modify menus, animations, etc...
		mContext = getApplication().getApplicationContext();
		mWses = ((GpiApp)getApplication()).getActiveWses();
		
		//get all the view elements
		mBackButton = (ImageView) findViewById(R.id.save_back_button);
		mUpDirButton = (ImageView) findViewById(R.id.save_updir_button);
		mNewFolderButton = (ImageView) findViewById(R.id.save_newfolder_button);
		mSaveButton = (ImageView) findViewById(R.id.save_save_button);
		
		mStatusTextView = (TextView)findViewById(R.id.save_status_text_view);
		mDirectoryTextView = (TextView)findViewById(R.id.save_file_address_text_view);
		mFileNameEditText = (EditText)findViewById(R.id.save_filename_edittext);
		
		mFileListView = (ListView)findViewById(R.id.save_file_list_view);

		
		String filename = new String("Gpi_" + (new Integer(new Random().nextInt(1000))).toString()  + ".gpi" );
		mFileNameEditText.setText(filename);
		
		
		//need to check if the storage exist
		checkMediaAvailibility();
		openExternalStorageDevice();
		populateFileList();
		
		setup_listeners();
	}
	
	//Set Stuff up
	private void setup_listeners (){
		mBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				backClick ();
			}
		});
		mUpDirButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				upDirClick();
			}
		});
		mNewFolderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				newFolderClick();
			}
		});
		mSaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveClick();
			}
		});
		mFileListView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				//Need to get the name, so we can change directory, or select a file
				String filename = (String)adapterView.getItemAtPosition(position);
				fileItemClicked(filename);
			}
		});
	}

	private void backClick (){
		finish();
	}
	private void upDirClick(){
		try {
			mFile = new File(mFile.getParent());
		}
		catch (Exception ex){
			mGpiConsole.error("Couldn't open file", ex);
			openExternalStorageDevice();
			populateFileList();
			return;
		}
		mGpiConsole.info("Up a directory");
	}
	private void newFolderClick(){
		String directory = this.mDirectoryTextView.getText().toString();
		String foldername = this.mFileNameEditText.getText().toString();
		boolean status = false;
		File file = new File (directory + "/" + foldername);
		//File file = new File ("mnt//sdcard//gpi");
		if (!file.exists()){
			status = file.mkdir();
		}
		
		if (status == false){
			mGpiConsole.error("Failed to make folder");
			mStatusTextView.setText("Failed to make folder");
		}
		else {
			try {
				mFile = new File (foldername);
			}
			catch (Exception ex){
				mGpiConsole.error("Couldn't open file", ex);
				openExternalStorageDevice();
				populateFileList();
				return;
			}
			
			populateFileList();
			mStatusTextView.setText("Created new folder");
			mGpiConsole.info("Made new folder at: " + mFile.getName());
		}
	}
	private void saveClick(){
		String dataToWrite = generateSaveString();
		FileWriter fw = null;
		File file = new File(this.mDirectoryTextView.getText().toString() + mFileNameEditText.getText().toString());
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException ex) {
				mGpiConsole.error("Couldn't create file", ex);
				mStatusTextView.setText("Couldn't create file");
				return;
			}
		}
		try {
			fw = new FileWriter (file);
		} catch (IOException ex) {
			mGpiConsole.error("Couldn't open file for writing", ex);
			mStatusTextView.setText("Couldn't open file for writing");
			return;
		}
		try {
			fw.write(dataToWrite);
		} catch (IOException ex) {
			mGpiConsole.error("Failed to write data to file", ex);
			mStatusTextView.setText("Failed to write data to file");
		}
		return;
	}
	private void fileItemClicked(String filename){
		//check to see if the filename exists in the current directory
		File[] files = null;
		SaveFileFilter sff = new SaveFileFilter (filename);

		files = mFile.listFiles(sff);
		
		if (files == null || files.length <= 0){
			mGpiConsole.error("File clicked doesn't exists... wtf?!");
			mStatusTextView.setText ("File clicked doesn't exists... I'm clueless");
			return;
		}
		//there really should only be one file
		mFile = files[0];
		
		//check to see if this si a file, or a directory
		if (mFile.isFile()){
			mFileNameEditText.setText (mFile.getName());
			mStatusTextView.setText("To overwrite select 'Save'");
		}
		else if (mFile.isDirectory()){
			mStatusTextView.setText("Scanning Directory");
			populateFileList ();
		}
		else {
			//Don't know where I am... lets go home
			mStatusTextView.setText("How did I end up there?!");
			openExternalStorageDevice();
			populateFileList();
		}
	}
	private class SaveFileFilter implements FilenameFilter {

		String mName;

		public SaveFileFilter (String name){
			mName = name;
		}
		public boolean accept(File dir, String filename) {
			if (mName == filename){
				return true;
			}
			return false;
		}
		
	}
	
	//File System stuff
	private boolean checkMediaAvailibility(){
		boolean retval = false;
		
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)){
			//we can read and write to the meda
			retval = true;
			mGpiConsole.info("Found External Media, and can write");
			mStatusTextView.setText("External Storage is Available");
		}
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			//we can only read the media
			mGpiConsole.error("External Storage is read only");
			mStatusTextView.setText("External Storage is read only");
		}
		else{
			//somehting got goobered
			mGpiConsole.error("Unable to Read from the state!");
			mStatusTextView.setText("Unable to open the file system!");
		}
		
		return retval;
	}
	private boolean openExternalStorageDevice(){
		mFile =  Environment.getExternalStorageDirectory();
		if (mFile == null){
			mStatusTextView.setText("Couldn't open File handle to directory");
			mGpiConsole.error("Couldn't open File handle to directory");
			return false;
		}
		
		return true;

	}
	private void populateFileList (){
		if (mFile.isDirectory()){
			mDirectoryTextView.setText(mFile.getPath());
			mFile.list();
			mFileListAdapter = new FileListAdapter(mContext);
			for (String filename : mFile.list()){
				if ((filename == null) || (filename.length() <= 0) ){
					continue;
				}
				mFileListAdapter.addFileName(filename);
			}
			mFileListView.setAdapter(mFileListAdapter);
		}
	}
	public class FileListAdapter implements ListAdapter{

		int count = 0;
		Context mContext;
		ArrayList<TextView> mFileList;
		ArrayList<DataSetObserver> mObservers;
		
		FileListAdapter(Context context){
			mContext = context;
			mFileList = new ArrayList<TextView>();
			mObservers = new ArrayList<DataSetObserver>();
		}
		
		public void clearFilenames(){
			mFileList.clear();
		}
		public void addFileName(String name){
			TextView tv = new TextView(mContext);
			tv.setText(name);
			mFileList.add(tv);
		}
		public boolean areAllItemsEnabled() {
			return true;
		}
		public boolean isEnabled(int position) {
			return true;
		}
		public int getCount() {
			return mFileList.size();
		}
		public Object getItem(int position) {
			String text = mFileList.get(position).getText().toString();
			return text;
		}
		public long getItemId(int position) {
			return position;
		}
		public int getItemViewType(int position) {
			return 0;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			return mFileList.get(position);
		}
		public int getViewTypeCount() {
			return 1;
		}
		public boolean hasStableIds() {
			return true;
		}
		public boolean isEmpty() {
			return mFileList.isEmpty();
		}
		public void registerDataSetObserver(DataSetObserver observer) {
			mObservers.add(observer);
		}
		public void unregisterDataSetObserver(DataSetObserver observer) {
			mObservers.remove(observer);
		}
	}

	//pull data out of Workspace Entity to put into the file
	private String generateSaveString(){
		String fileData = new String();
		
		fileData += "GPI save file\n";
		fileData += "filesave version\n";
		fileData += mContext.getString(R.string.file_save_version) + "\n";
		fileData += "\n";
		
		fileData += "Workspace Entities\n";
		fileData += "\n";
		fileData += "\n";
		
		for (WorkspaceEntity wse : mWses){
			int [] location;
			//entity name
			fileData += wse.getEntity().getName() + "\n";
			//properties
			for (String key : wse.getEntity().getPropertyKeySet()){
				fileData += "Property " + key + ":" + wse.getEntity().getPropertyData(key).toString() + "\n";
			}
			//location
			location = wse.getLocation();
			fileData += "Location: " + new Integer(location[0]).toString() + "," + new Integer(location[1]).toString() + "\n"; 
			//Connection
			for (ConnectionPointView ic : wse.getInputConnectionList()){
				fileData += "Input Connection: " + ic.getText() + "\n";
			}
			for (ConnectionPointView ic : wse.getOutputConnectionList()){
				fileData += "Output Connection: " + ic.getText() + "\n";
				Hashtable<Entity, HashSet<String>> lm = wse.getEntity().getAllConnectionsForOutputChannel((String) ic.getText());
				for (Entity entity : lm.keySet()){
					fileData += "\t" + entity.getName() + "\n";
					for (String connection : lm.get(entity)){
						fileData += "\t\t" + connection + "\n";
					}
				}
			}
			fileData += "\n";
		}
		return fileData;
	}


}
