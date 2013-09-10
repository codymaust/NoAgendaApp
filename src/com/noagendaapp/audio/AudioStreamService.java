/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.noagendaapp.audio;

import java.io.IOException;

import com.noagendaapp.MainActivity;
import com.noagendaapp.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AudioStreamService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

	private static final int NOTIFICATION_ID = 3333;
	
	MediaPlayer myMediaPlayer = null;
	AudioManager myAudioManager = null;
	WifiLock myWifiLock  = null;
    Handler myHandler = new Handler();
	
    private static boolean isRunning = false;
    private static boolean isPlaying = false;
    Messenger myClient = null; // Keeps track of all current registered clients.
    final Messenger myMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    
    //
    // Different types of messages of that the Service and send and receive
    //
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_STOP_AUDIO = 11;
    public static final int MSG_PLAY_FILE = 12;
    public static final int MSG_SEEK_TO = 13;
    public static final int MSG_JUMP_TO = 14;
    public static final int MSG_STOP_GUI = 98;
    public static final int MSG_UPDATE_FRAGMENT_PLAYER = 99;
    
    // Information about the audio from the database
    private static int savedSeekPosition = 0;    
    
	@Override
	public IBinder onBind(Intent intent) {
        return myMessenger.getBinder();
	}
	    	
	@Override
	public void onCreate() {
		Log.d(getClass().getName(), "Service Created");
		
		isRunning = true;
	}

	@Override
	public void onDestroy() {
		Log.d(getClass().getName(), "Service Destroyed");

        isRunning = false;
	}
	
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d(getClass().getName(), "Service onStartCommand");
    	
    	return START_STICKY;
    }
    	
    // MediaPlayer.OnErrorListener 
    public void initMediaPlayer() {
    	Log.d(getClass().getName(), "MediaPlayer: initMediaPlayer");
    	
    	myMediaPlayer = new MediaPlayer();

        myMediaPlayer.setOnErrorListener(this);
    }
   
    // MediaPlayer.OnErrorListener 
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d(getClass().getName(), "MediaPlayer: onError");
		return false;
	}
	
    // Called when MediaPlayer is ready 
    public void onPrepared(MediaPlayer player) {
    	Log.d(getClass().getName(), "onPrepared: MediaPlayer is ready");
    	
        player.start();  
        
		//
		// Jump to the position saved in the database
		//
        if ( savedSeekPosition > 0 ) {
        	// Make sure the savedSeekPosition is valid
        	if (savedSeekPosition < myMediaPlayer.getDuration() && savedSeekPosition > 0) {
        		// Seek to the savedSeekPosition
        		myMediaPlayer.seekTo(savedSeekPosition);
        	}
		}
    }
	
	// AudioManager.OnAudioFocusChangeListener 
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
        case AudioManager.AUDIOFOCUS_GAIN:
            // resume playback
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_GAIN");
        	if (myMediaPlayer == null) {
        		initMediaPlayer();
        	}
            else if (!myMediaPlayer.isPlaying()) {
            	myMediaPlayer.start();
            }
            
        	myMediaPlayer.setVolume(1.0f, 1.0f);
            break;

        case AudioManager.AUDIOFOCUS_LOSS:
            // Lost focus for an unbounded amount of time: stop playback
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS");
        	StopAudio();
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost focus for a short time, but we have to stop
            // playback. We don't release the media player because playback
            // is likely to resume
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
        	if (myMediaPlayer != null) {
	            if (myMediaPlayer.isPlaying()) {
	            	myMediaPlayer.pause();
	            }
        	}
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Lost focus for a short time, but it's ok to keep playing
            // at an attenuated level
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
        	if (myMediaPlayer != null) {
	            if (myMediaPlayer.isPlaying()) {
	            	myMediaPlayer.setVolume(0.1f, 0.1f);
	            }
        	}
            break;
        }
    }
    
    //
    // Handler of incoming messages from clients 
    //
    class IncomingHandler extends Handler { 
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
            	myClient = msg.replyTo;
                break;
            case MSG_UNREGISTER_CLIENT:
            	myClient = null;
                break;
            case MSG_STOP_AUDIO:
            	StopAudio();
            	break;
            case MSG_PLAY_FILE:
            	StartAudio(msg.getData().getString("audioUrl"), msg.getData().getString("title"), msg.getData().getString("subtitle"), Integer.parseInt(msg.getData().getString("position")));
                break;
            case MSG_SEEK_TO:
            	SeekTo(msg.arg1);
            	break;
            case MSG_JUMP_TO:
            	JumpTo(msg.arg1);
            	break;	
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
    
    public static boolean isPlaying() {  	
    	return isPlaying;
    }
               
    private void StartAudio(String audioUrl, String title, String subtitle, int seekPosition)
    {     			
    	// Request audio focus from the AudioManager
    	myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	int result = myAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    	
    	// If AudioManager gives audio focus...
    	if ( result == AudioManager.AUDIOFOCUS_GAIN )
    	{
    		Log.d(getClass().getName(), "Got audio focus");
    	              	
    		if (myMediaPlayer == null) {
    		
    			Log.d(getClass().getName(), "[StartAudio] Initializing new MediaPlayer");
    			// Initialize MediaPlayer
    			myMediaPlayer = new MediaPlayer();
    		
    			// Set a partial CPU wake lock 
    			myMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        
    			// Set a WIFI wake lock if playing streaming audio
    			if (audioUrl.contains("http"))
    			{
    				// need to wifiLock.release(); if you pause or stop the audio
    				myWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
    				myWifiLock.acquire();
    				Log.d(getClass().getName(), "[StartAudio] WIFI lock acquired");
    			}

    			myMediaPlayer.setOnPreparedListener(this);
    		} else {
    			Log.d(getClass().getName(), "[StartAudio] Resetting MediaPlayer");
    			myMediaPlayer.reset();
    		}
    		
    		// Check if the service is already play a file TODO: myMediaPlayer.isPlaying() does't work
    		if (myMediaPlayer.isPlaying() == false)
    		{
    			Log.d(getClass().getName(), "[StartAudio] MediaPlayer is not playing");
    			
    			// Set the DataSource url (local or internet)
    			try {
    				Log.d(getClass().getName(), "[StartAudio] SetDataSource: " + audioUrl);
    				myMediaPlayer.setDataSource(audioUrl);            
    			} catch (IllegalArgumentException ex) {
    				Log.d(getClass().getName(), "Exception: " + ex.toString());
    				ex.printStackTrace();
    			} catch (IllegalStateException ex) {
    				Log.d(getClass().getName(), "Exception: " + ex.toString());
    				ex.printStackTrace();
    			} catch (IOException ex) {
    				Log.d(getClass().getName(), "Exception: " + ex.toString());
    				ex.printStackTrace();
    			} catch (Exception ex) {
    				Log.d(getClass().getName(), "Exception: " + ex.toString());
    				ex.printStackTrace();
    			}    			  			

    			myMediaPlayer.prepareAsync(); // prepare async to not block main thread
    			
    		} else {
    			Log.d(getClass().getName(), "[StartAudio] MediaPlayer is already playing");
    		}
    		
    		// When streaming starts add a foreground Notification
    		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		 Notification notification = new NotificationCompat.Builder(getApplicationContext())
             .setContentTitle(title)
             .setContentText(subtitle)
             .setTicker(getString(R.string.app_name) + ": " + title)
             .setSmallIcon(R.drawable.ic_launcher)
             .setContentIntent(pi)
             .setOngoing(true)
             .build();
    		
    		startForeground(NOTIFICATION_ID, notification);  
    	
    		// Do the following playing a saved file
    		if (audioUrl.contains("http") == false)
    		{   
    			// Set the savedSeekPosition so the player can jumped to the lasted saved position
    			savedSeekPosition = seekPosition;

    			// Start the task to update the gui
    			myHandler.postDelayed(mUpdateTimeTask, 1000);
    		}
            isPlaying = true;
    			    	
    	} else {
    		Log.d(getClass().getName(), "[StartAudio] Could not get audio focus");
    	}
    }
    
    private void StopAudio() {
		Log.d(getClass().getName(), "[StopAudio] Got the Stop Audio command");

    	// if the MediaPlayer has been initialize then release it
    	if ( myMediaPlayer != null ) {
    		
    		// Stop the MediaPlayer if audio is playing
    		if (myMediaPlayer.isPlaying())
    		{
    			myMediaPlayer.stop();
    			Log.d(getClass().getName(), "[StopAudio] issued myMediaPlayer.stop");
    			
    			try {
    				// Send the currentPostion to the client so the position can be saved
    				myClient.send(Message.obtain(null, MSG_STOP_GUI, myMediaPlayer.getCurrentPosition(), 0));
    			} catch (RemoteException e) {
    				Log.w(getClass().getName(), "Exception sending message", e);
    			}
    		}
		
    		// release the MediaPlayer
    		myMediaPlayer.release();
			myMediaPlayer = null;
    	}
		
    	// Remove the service from the foreground
		stopForeground(true);
    	
		// Check if wifiLock is held and release it if so
		if (myWifiLock != null && myWifiLock.isHeld())
		{
			myWifiLock.release();
			Log.d(getClass().getName(), "WIFI lock released");
		}
		
		// Stop the repeating runable thread
		myHandler.removeCallbacks(mUpdateTimeTask);
		
		// Set the isPlaying flag to false
		isPlaying = false;
    }
    
    private void SeekTo(int seekValue) {
    	// Make sure myMediaPlayer has been created
    	if (myMediaPlayer != null) {
    		// Make sure myMediaPlayer is playing
    		if (myMediaPlayer.isPlaying()) {
    			// set the seekPosition; seekValue = time in milliseconds
    			int seekPosition = myMediaPlayer.getCurrentPosition() + seekValue;
    			
    			// Make sure the seekPosition is valid
    			if (seekPosition < myMediaPlayer.getDuration() && seekPosition > 0) {
    				// Seek to the seekPosition
    				myMediaPlayer.seekTo(seekPosition);
    			}
    		}
    	}
    }
    
    private void JumpTo(int seekValue) {
    	// Make sure myMediaPlayer has been created
    	if (myMediaPlayer != null) {
    		// Make sure myMediaPlayer is playing
    		if (myMediaPlayer.isPlaying()) {
    			// set the seekPosition; is the % complete
    			int seekPosition = (int) Math.round(myMediaPlayer.getDuration() * ( seekValue * .01));
    			    			  			
    			// Make sure the seekPosition is valid
    			if (seekPosition < myMediaPlayer.getDuration() && seekPosition > 0) {
    				// Seek to the seekPosition
    				myMediaPlayer.seekTo(seekPosition);
    			}
    		}
    	}
    }
    
    // Background Runnable thread to run every second when audio is play to update the player information
    private Runnable mUpdateTimeTask = new Runnable() {
    	public void run() {
    		if (myMediaPlayer != null) {
    			
        		if (myMediaPlayer.isPlaying()) {
        			try {
        				//Log.d(getClass().getName(), "(mUpdateTimeTask) Get Duration: " + String.valueOf(myMediaPlayer.getDuration()) + " Get Current Position: " + String.valueOf(myMediaPlayer.getCurrentPosition()) );
        				// Send the currentPostion and duration to the client so the player can be updated
        				myClient.send(Message.obtain(null, MSG_UPDATE_FRAGMENT_PLAYER, myMediaPlayer.getCurrentPosition(), myMediaPlayer.getDuration()));
        			} catch (RemoteException e) {
        				Log.w(getClass().getName(), "Exception sending message", e);
        			}
        			
        		} else {
        			Log.d(getClass().getName(), "(mUpdateTimeTask) myMediaPlayer is not playing");
        			
        			// If myMediaPlayer isn't playing then we have most likely reached the end of the file and run
        			// StopAudio to close any other running processes and clean up
        			StopAudio();
        		}
    			
        		// Wait one second before running again
    			myHandler.postDelayed(this, 1000);
    		} else {
    			// Cancel the runnable thread if myMediaPlayer hasn't been initalized
    			myHandler.removeCallbacks(mUpdateTimeTask);
    		}
    	}
    };
}
