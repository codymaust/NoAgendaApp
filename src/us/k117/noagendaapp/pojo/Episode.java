package us.k117.noagendaapp.pojo;

import java.io.File;

import us.k117.noagendaapp.R;
import us.k117.noagendaapp.db.EpisodeContentProvider;
import us.k117.noagendaapp.db.EpisodeTable;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Episode {

	private Activity myActivity;
	
	public String id;
	public String title;
	public String subtitle;
	public String link;
	
	public Episode(Activity activity, String myId) {
		Log.d(getClass().getName(), "Creating Episode Object");
		
		myActivity = activity;
		id = myId;
		
		String[] projection = { EpisodeTable.COLUMN_TITLE, EpisodeTable.COLUMN_SUBTITLE, EpisodeTable.COLUMN_LINK };			
		Cursor myCursor = myActivity.getContentResolver().query(EpisodeContentProvider.CONTENT_URI, projection, EpisodeTable.COLUMN_ID + " = ?", new String[] { id }, null);

		if (myCursor.moveToFirst()) {
			title = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_TITLE));
			subtitle = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_SUBTITLE));
			link = myCursor.getString(myCursor.getColumnIndex(EpisodeTable.COLUMN_LINK));
		}
	}
	
	public void Download() {
		
		String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
		String filename = Uri.parse(link).getLastPathSegment();
		
		if ( ! FileExists() ) {
			
			File myDirectory = new File(directory);
			if (!myDirectory.exists()) {
				myDirectory.mkdir();
			}
			
        	DownloadManager myDownloadManager = (DownloadManager) myActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        	Request myRequest = new Request(Uri.parse(link));
        	myRequest.setDestinationUri(Uri.parse("file://" + directory + "/" + filename));
        	long enqueue = myDownloadManager.enqueue(myRequest);
		}	
	}
	
	public void Delete() {
		
		String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
		String filename = Uri.parse(link).getLastPathSegment();
		
		if ( FileExists() ) {
			File myFile = new File(directory + "/" + filename);
			myFile.delete();
		}
	}
	
	public boolean FileExists() {
		String myFileName = Uri.parse(link).getLastPathSegment();
		
		File myFile = new File(Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path) + "/" + myFileName);

		if (myFile.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	public Uri GetLocalUri() {
		
		if (FileExists()) {
			String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
			String filename = Uri.parse(link).getLastPathSegment();
			
        	return Uri.parse("file://" + directory + "/" + filename);
		} else {
			return null;
		}		
	}
	
	public String GetLocalPath() {
		
		if (FileExists()) {
			String directory = Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path);
			String filename = Uri.parse(link).getLastPathSegment();
			
        	return directory + "/" + filename;
		} else {
			return null;
		}		
	}
	
}
