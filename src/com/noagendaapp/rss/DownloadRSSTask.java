/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.noagendaapp.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.noagendaapp.MainActivity;
import com.noagendaapp.db.EpisodeContentProvider;
import com.noagendaapp.db.EpisodeTable;
import com.noagendaapp.pojo.Episode;
import com.noagendaapp.rss.RSSFeedXmlParser;
import com.noagendaapp.rss.RSSFeedXmlParser.Entry;
import com.noagendaapp.R;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

// Implementation of AsyncTask used to download RSS feed
public class DownloadRSSTask extends AsyncTask<String, Void, String> {

	private MainActivity myMainActivity;
	
	public DownloadRSSTask( MainActivity activity ) {
	    myMainActivity = activity;
	}
	
    @Override
    protected String doInBackground(String... urls) {
        try {
        	// Alert the user that the update has began
        	// doInBackground() runs on a background thread which, since it is not intended to loop, is not connected to a Looper.
        	myMainActivity.runOnUiThread(new Runnable() {
        	    public void run() {
        	    	Toast.makeText(myMainActivity, myMainActivity.getResources().getString(R.string.episode_updating), Toast.LENGTH_SHORT).show();
        	    }
        	});
        	
            return LoadRSSFromNetwork(urls[0]);
        } catch (IOException e) {
        	return myMainActivity.getResources().getString(R.string.connection_error);
        } catch (XmlPullParserException e) {
            return myMainActivity.getResources().getString(R.string.xml_error);
        }
    }

    @Override
    protected void onPostExecute(String result) {
    	Log.d(getClass().getName(), "onPostExecute:");
    	
    	// TODO Fix result == "0" and add case for 1 episode added so it isnt plural  
    	if ( result == "0" ) {
        	Toast.makeText(myMainActivity, myMainActivity.getResources().getString(R.string.episode_up_to_date), Toast.LENGTH_SHORT).show();
    	} else {
        	Toast.makeText(myMainActivity, result + " " + myMainActivity.getResources().getString(R.string.episode_added), Toast.LENGTH_SHORT).show();
    	}
    	
    	
    }

    // Get the RSS feed from the network and update the database if new episode exist
    private String LoadRSSFromNetwork(String urlString) throws XmlPullParserException, IOException {
    	
    	int count = 0;
    	
    	InputStream stream = null;
    	RSSFeedXmlParser myRSSFeedXmlParser = new RSSFeedXmlParser();
    	List<Entry> entries = null;
            
    	try {
    		stream = DownloadUrl(urlString);
    		entries = myRSSFeedXmlParser.parse(stream);
    	// Makes sure that the InputStream is closed after the app is finished using it.
    	} finally {
    		if (stream != null) {
    			stream.close();
    		}
    	}
    
		String title = null;
		String subtitle = null;
		String link = null;
		String length = null;
		String date = null;
		String episodeNum = null;
		
    	for (Entry entry : entries) {
    		title = entry.title;
    		subtitle = entry.summary;
    		link = entry.link;
    		length = entry.length;
    		date = entry.date;
    		episodeNum = entry.episodeNum;
    		
    		// Only save if either summary or description
    		// is available
    		if (title.length() == 0 && link.length() == 0) {
    			break;
    		}

    		ContentValues values = new ContentValues();
    		values.put(EpisodeTable.COLUMN_TITLE, title);
    		values.put(EpisodeTable.COLUMN_SUBTITLE, subtitle);
    		values.put(EpisodeTable.COLUMN_LINK, link);
    		values.put(EpisodeTable.COLUMN_LENGTH, length);
    		values.put(EpisodeTable.COLUMN_DATE, date);
    		values.put(EpisodeTable.COLUMN_EPISODE_NUM, episodeNum);

    		// query the database for the episode link
    		String[] projection = { EpisodeTable.COLUMN_LINK };
    		Cursor myCursor = myMainActivity.getContentResolver().query(EpisodeContentProvider.CONTENT_URI, projection, EpisodeTable.COLUMN_LINK + " = ?", new String[] { link }, null);
    			
    		// if the cursor has data then the episode is already in the database
    		if (myCursor.moveToFirst()) { 
   				Log.d(getClass().getName(), "EPISODE ENTRY EXISTS");
   			} else {
   				Log.d(getClass().getName(), "ADDING NEW EPISODE ENTRY");
   				
   				// Insert the new episode into the database and get back the Uri with the assigned _ID from the database 
   				Uri episodeUri = myMainActivity.getContentResolver().insert(EpisodeContentProvider.CONTENT_URI, values);
   				
   				// Create the episode object and download the episode art
   				Episode myEpisode = new Episode(myMainActivity,	episodeUri.getLastPathSegment());
   				myEpisode.DownloadEpisodeArt();
   				
   				count = count + 1;
   			}
    	}
    
    	return String.valueOf(count);
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream DownloadUrl(String urlString) throws IOException {
    	URL url = new URL(urlString);
    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	conn.setReadTimeout(10000);
    	conn.setConnectTimeout(15000);
    	conn.setRequestMethod("GET");
    	conn.setDoInput(true);
    	
    	// Starts the query
    	conn.connect();
    	InputStream stream = conn.getInputStream();
    	return stream;
    }
}
