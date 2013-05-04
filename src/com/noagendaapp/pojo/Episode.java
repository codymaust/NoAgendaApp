package com.noagendaapp.pojo;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Episode {

	protected Activity myActivity;
	
	public String id;
	public String title;
	public String subtitle;
	public String link;
	public String position;
	public String length;
	public String dateText;
	public Date date;
	public String episodeNum;
	public String audioUrl;
	protected Boolean seekBarEnabled;
	
	public Episode(Activity activity, String myId) {
		Log.d(getClass().getName(), "Creating Episode Object");
		
		myActivity = activity;
		id = myId;
		
		// Get the Episode details from the sqlite database
		String[] projection = { EpisodeTable.COLUMN_TITLE, EpisodeTable.COLUMN_SUBTITLE, EpisodeTable.COLUMN_LINK, EpisodeTable.COLUMN_POSITION, EpisodeTable.COLUMN_LENGTH, EpisodeTable.COLUMN_DATE, EpisodeTable.COLUMN_EPISODE_NUM };			
		Cursor myCursor = myActivity.getContentResolver().query(EpisodeContentProvider.CONTENT_URI, projection, EpisodeTable.COLUMN_ID + " = ?", new String[] { id }, null);

		// Read the details from the sqlite database
		if (myCursor.moveToFirst()) {
			title = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_TITLE));
			subtitle = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_SUBTITLE));
			link = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_LINK));
			position = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_POSITION));
			length = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_LENGTH));
			dateText = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_DATE));
			episodeNum = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_EPISODE_NUM));			
			audioUrl = GetLocalPath();
		}
		
		//
		// Convert the dateText from the database into a Date object
		//
		//<pubDate>Thu, 25 Apr 2013 15:06:55 -0500</pubDate>
		SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
		
		try {
			date = df.parse(dateText);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			date = new Date();
		}
		
		// Set GUI options
		seekBarEnabled = true;
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
    	myBundle.putString("audioUrl", audioUrl);
    	myBundle.putString("title", title);
    	myBundle.putString("subtitle", subtitle);
      	myBundle.putString("position", position);	
      	
		what = AudioStreamService.MSG_PLAY_FILE;
      	
		SendToAudioStreamService(what, arg1, arg2, myBundle);
		
		//
		// Update the GUI
		//
		
		// Change the play button to the stop icon
		ImageButton play_ImageButton = (ImageButton) myActivity.findViewById(R.id.play_imagebutton);
		play_ImageButton.setImageResource(R.drawable.ic_action_playback_stop_hd);	
		play_ImageButton.setContentDescription(myActivity.getResources().getString(R.string.stop_imagebutton));
		
		// Update the currentPostion & duration text to 00:00:00
		TextView currentPosition_TextView = (TextView) myActivity.findViewById(R.id.currentposition_textview);
		TextView duration_TextView = (TextView) myActivity.findViewById(R.id.duration_textview);
		currentPosition_TextView.setText(String.format("00:00:00"));
		duration_TextView.setText(String.format("00:00:00"));

		// Set the seekBar enabled for downloaded files and disabled for the live stream and set it to 0
		SeekBar audio_SeekBar = (SeekBar) myActivity.findViewById(R.id.audio_seekbar);
		audio_SeekBar.setEnabled(seekBarEnabled);
		audio_SeekBar.setProgress(0);
		audio_SeekBar.setMax(100);
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
		
		// Change to play button to the play icon
		ImageButton myImageButton = (ImageButton) myActivity.findViewById(R.id.play_imagebutton);
		myImageButton.setImageResource(R.drawable.ic_action_playback_play_hd);	
		myImageButton.setContentDescription(myActivity.getResources().getString(R.string.play_imagebutton));
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
	protected void SendToAudioStreamService(int what, int arg1, int arg2, Bundle myBundle) {
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
