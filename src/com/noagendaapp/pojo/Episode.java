package com.noagendaapp.pojo;

import java.io.File;

import com.noagendaapp.audio.AudioStreamService;
import com.noagendaapp.db.EpisodeContentProvider;
import com.noagendaapp.db.EpisodeTable;
import com.noagendaapp.MainActivity;
import com.noagendaapp.R;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class Episode {

	private Activity myActivity;
	
	public String id;
	public String title;
	public String subtitle;
	public String link;
	public String position;
	
	public Episode(Activity activity, String myId) {
		Log.d(getClass().getName(), "Creating Episode Object");
		
		myActivity = activity;
		id = myId;
		
		// Get the Episode details from the sqlite database
		String[] projection = { EpisodeTable.COLUMN_TITLE, EpisodeTable.COLUMN_SUBTITLE, EpisodeTable.COLUMN_LINK, EpisodeTable.COLUMN_POSITION };			
		Cursor myCursor = myActivity.getContentResolver().query(EpisodeContentProvider.CONTENT_URI, projection, EpisodeTable.COLUMN_ID + " = ?", new String[] { id }, null);

		// Read the details from the sqlite database
		if (myCursor.moveToFirst()) {
			title = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_TITLE));
			subtitle = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_SUBTITLE));
			link = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_LINK));
			position = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_POSITION));
		}
	}
	
	//
	// Play the episode by sending the episode details to the AudioStreamService service
	//
	public void Play() {
		int what = 0;
		int arg1 = 0;
		int arg2 = 0;
		Bundle myBundle = null;
		
		// TODO: Need to reload the object from the database because the position could have changed 
		// since the initial object load (Maybe? Need to test if this is a problem)
    	myBundle = new Bundle();
    	myBundle.putString("audioUrl", GetLocalPath());
    	myBundle.putString("title", title);
    	myBundle.putString("subtitle", subtitle);
      	myBundle.putString("position", position);	
      	
		what = AudioStreamService.MSG_PLAY_FILE;
      	
		SendToAudioStreamService(what, arg1, arg2, myBundle);
	}
	
	//
	// Stop the episode from playing by sending the stop command to the AudioStreamService service
	//
	public void Stop() {
		int what = 0;
		int arg1 = 0;
		int arg2 = 0;
		Bundle myBundle = null;
		
		what = AudioStreamService.MSG_STOP_AUDIO;
		
		SendToAudioStreamService(what, arg1, arg2, myBundle);
	}
	
	//
	// Fast Forward/Rewind the episode playing episode by sending the SeekTo command to the AudioStreamService service
	//
	public void SeekTo(int amount) {
		int what = 0;
		int arg1 = 0;
		int arg2 = 0;
		Bundle myBundle = null;
		
		what = AudioStreamService.MSG_SEEK_TO;
		arg1 = amount;
		
		SendToAudioStreamService(what, arg1, arg2, myBundle);
	}
	
	//
	// Jump to a new position in the episode sending the JumpTo command to the AudioStreamService service
	//
	public void JumpTo(int position) {
		int what = 0;
		int arg1 = 0;
		int arg2 = 0;
		Bundle myBundle = null;
		
		what = AudioStreamService.MSG_JUMP_TO;
		arg1 = position;
		
		SendToAudioStreamService(what, arg1, arg2, myBundle);
	}
	
	//
	// Try to send the command to the AudioStreamService service
	//	
	private void SendToAudioStreamService(int what, int arg1, int arg2, Bundle myBundle) {
		try {  	  
			Message msg = Message.obtain(null, what, arg1, arg2);
			
			if (myBundle != null) {
				msg.setData(myBundle);
			}
			
			MainActivity.myService.send(msg);
		} catch (RemoteException e) {
			Log.w(getClass().getName(), "Exception sending message", e);
		}	
	}
	
	//
	// Download the episode to using Android's built in DownloadManager
	//
	public void Download() {
		String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
		String filename = Uri.parse(link).getLastPathSegment();
		
		// if the file exist don't download it again
		if ( ! FileExists() ) {
			
			// if the destination directory doesn't exist then create it
			File myDirectory = new File(directory);
			if (!myDirectory.exists()) {
				myDirectory.mkdir();
			}
			
            Log.d(getClass().getName(), "Downloading " + link + " to "  + directory + "/" + filename);
			
            // Use DownloadManager to download episode Uri
        	DownloadManager myDownloadManager = (DownloadManager) myActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        	Request myRequest = new Request(Uri.parse(link));
        	myRequest.setDestinationUri(Uri.parse("file://" + directory + "/" + filename));
        	myDownloadManager.enqueue(myRequest);
		}	
	}
	
	//
	// Delete the episode file 
	//
	public void Delete() {		
		String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
		String filename = Uri.parse(link).getLastPathSegment();
		
		// if it exists delete it
		if ( FileExists() ) {
			File myFile = new File(directory + "/" + filename);
			myFile.delete();
		}
	}
	
	//
	// Check if the episode file exists
	//
	public boolean FileExists() {
		String myFileName = Uri.parse(link).getLastPathSegment();
		
		File myFile = new File(Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path) + "/" + myFileName);

		if (myFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	//
	// Return the Uri of the downloaded episode file
	//
	public Uri GetLocalUri() {	
		// if the file doesn't exist then simply return null
		if (FileExists()) {
			String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
			String filename = Uri.parse(link).getLastPathSegment();
			
        	return Uri.parse("file://" + directory + "/" + filename);
		} else {
			return null;
		}		
	}
	
	//
	// Return the path of the downloaded episode file
	//
	public String GetLocalPath() {
		// if the file doesn't exist then simply return null
		if (FileExists()) {
			String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
			String filename = Uri.parse(link).getLastPathSegment();
			
        	return directory + "/" + filename;
		} else {
			return null;
		}		
	}	
	
	//
	// Update the position column in the database with the current position of the audio file
	//
	public void SetPosition(int newPosition) {
		position = Integer.toString(newPosition);
		
    	//
    	// Save the current position into the database so the app can resume where it left off
    	//
		ContentValues values = new ContentValues();
		values.put(com.noagendaapp.db.EpisodeTable.COLUMN_POSITION, position);
		
		// Update the database via the EpisodeContentProvider
		myActivity.getContentResolver().update(Uri.parse(EpisodeContentProvider.CONTENT_URI.toString() + "/" + id), values, null, null);

	}
}
