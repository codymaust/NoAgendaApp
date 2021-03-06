/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.noagendaapp;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import com.noagendaapp.audio.AudioStreamService;
import com.noagendaapp.handler.AudioHandler;
import com.noagendaapp.pojo.Episode;
import com.noagendaapp.pojo.LiveStream;
import com.noagendaapp.rss.DownloadRSSTask;
import com.noagendaapp.ui.EpisodeFragment;
import com.noagendaapp.ui.LiveStreamFragment;
import com.noagendaapp.ui.MyTabListener;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	// Variable for communicating with the Service
    public static Messenger myService = null;
    final Messenger myMessenger = new Messenger(new AudioHandler(this));
    boolean myServiceIsBound;
    
    // Variable for accessing the Seekbar
    SeekBar audio_SeekBar;
    public static boolean updateSeekBar = true;
    
    // Variable for the active episode
    public static Episode activeEpisode = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		// Create Episodes tab
		Tab tab = actionBar
				.newTab()
				.setText("Episodes")
				.setTabListener(new MyTabListener<EpisodeFragment>(this, "episode", EpisodeFragment.class));
		actionBar.addTab(tab);
		
		// Create Live Stream tab
		tab = actionBar
				.newTab()
				.setText("Live Stream")
				.setTabListener(new MyTabListener<LiveStreamFragment>(this, "liveStream", LiveStreamFragment.class));
		actionBar.addTab(tab);	
		
		// Initalize the Seekbar
		audio_SeekBar = (SeekBar) findViewById(R.id.audio_seekbar);
		audio_SeekBar.setOnSeekBarChangeListener(this);

		// Bind to the Service as soon as the application is started
        doBindService();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// If the Service is running bind to it
        CheckIfServiceIsRunning();		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Unbind from the Service 
        doUnbindService();

        // Stop the Service
        Intent intent = new Intent (this, AudioStreamService.class);
		stopService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			DownloadRSSTask myDownloadRSSTask = new DownloadRSSTask(this);
			myDownloadRSSTask.execute(getResources().getString(R.string.rss_feed));
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
    // onProgressChanged, onStartTrackingTouch and onStopTrackingTouch implements SeekBar.OnSeekBarChangeListener
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// If the user touches the SeekBar
		if (fromUser) {
			// Get the current position and duration
    		TextView currentPosition_TextView = (TextView) findViewById(R.id.currentposition_textview);
    		TextView duration_TextView = (TextView) findViewById(R.id.duration_textview);
    		
    		//
    		// Convert the duration into an int 
    		//
    		Date myDurationDate = null;
    		SimpleDateFormat mySimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);  
    		mySimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    		try {
    			myDurationDate = mySimpleDateFormat.parse("1970-01-01 " + String.valueOf(duration_TextView.getText()) + ".000");
    		} catch (Exception e) {
				Log.w(getClass().getName(), "Exception sending message", e);
    		}

    		int duration = 	(int) myDurationDate.getTime();
    		
    		// Calculate the current position by multiplying duration by the Seekbar progress 
			int seekPosition = (int) Math.round(duration * ( progress * .01));

			// Get the hours minutes and seconds
    		long cHours = TimeUnit.MILLISECONDS.toHours(seekPosition);
    		long cMinutes = TimeUnit.MILLISECONDS.toMinutes(seekPosition) - TimeUnit.HOURS.toMinutes(cHours);
    		long cSeconds = TimeUnit.MILLISECONDS.toSeconds(seekPosition) - TimeUnit.HOURS.toSeconds(cHours) - TimeUnit.MINUTES.toSeconds(cMinutes);
    		
    		// Display the time of the current position
        	currentPosition_TextView.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		updateSeekBar = false;	
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateSeekBar = true;
		
		// Tell the activeEpisode to jump to the location of the SeekBar
		if ( AudioStreamService.isPlaying() ) {
			activeEpisode.JumpTo(seekBar.getProgress());
		}
	}
	
	// onClick from Player Controls in fragment_player.xml
	public void onPlayerClick(View view) {
				
		switch(view.getId()) {
		case R.id.play_imagebutton:		
			// play/pause/stop
			if ( AudioStreamService.isPlaying() ) {
				activeEpisode.Stop();
			} else {
				// If the audio isn't playing then check if there is an active 
				// episode and start playing it
				if (activeEpisode != null)
				{
					activeEpisode.Play();
				}
			}
			break;
		case R.id.rewind_imagebutton:
			// Tell the activeEpisode to seek ahead 30 seconds
			if ( AudioStreamService.isPlaying() ) {
				activeEpisode.SeekTo(-30000);
			}
            break;
		case R.id.fastforward_imagebutton:
            // Tell the activeEpisode to seek back 30 seconds
			if ( AudioStreamService.isPlaying() ) {
				activeEpisode.SeekTo(30000);
			}
			break;
		case R.id.live_button:
			// Create a new activeEpisode object for the Live Stream
			Episode myEpisode = new LiveStream(this, "-1");
			activeEpisode = myEpisode;
			// Start the Live Stream
			activeEpisode.Play();	
	        break;
        default:
        	break;
		}		
	}
	
	
	//
	// Functions below used for binding to the Service so the player and Service can communicate with each other 
	//
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            myService = new Messenger(service);
            Log.d(getClass().getName(), "Service Attached");
            try {
                Message msg = Message.obtain(null, AudioStreamService.MSG_REGISTER_CLIENT);
                msg.replyTo = myMessenger;
                myService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            	Log.w(getClass().getName(), "Exception sending message", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            myService = null;
    		Log.d(getClass().getName(), "Service Disconnected");
        }
    };
    
    private void CheckIfServiceIsRunning() {    	
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (AudioStreamService.isRunning()) {
        	Log.d(getClass().getName(), "CheckIfServiceIsRunning() Service is Running");
            doBindService();
        } else {
        	Log.d(getClass().getName(), "CheckIfServiceIsRunning() Service is Not Running");
        }
    }
    
    void doBindService() {
        Log.d(getClass().getName(), "doBindService()");
    	bindService(new Intent(this, AudioStreamService.class), mConnection, Context.BIND_AUTO_CREATE);
        myServiceIsBound = true;
    }
    
    void doUnbindService() {
    	Log.d(getClass().getName(), "doUnbindService()");
    	
        if (myServiceIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (myService != null) {
                try {
                    Message msg = Message.obtain(null, AudioStreamService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = myMessenger;
                    myService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                	Log.w(getClass().getName(), "Exception sending message", e);
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            myServiceIsBound = false;
        }
    }
}
