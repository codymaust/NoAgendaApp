package com.noagendaapp.audio;

import java.io.IOException;

import com.noagendaapp.MainActivity;
import com.noagendaapp.R;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AudioStreamService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
	//private static final String ACTION_PLAY = "com.noagendaapp.action.PLAY";
	private static final int NOTIFICATION_ID = 3333;
	
	MediaPlayer mMediaPlayer = null;
	AudioManager mAudioManager = null;
	WifiLock wifiLock  = null;
	
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(getClass().getName(), "Service Created");
	}

	@Override
	public void onDestroy() {
		Log.d(getClass().getName(), "Service Destroyed");
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		
		// Check if wifiLock is held and release it if so
		if (wifiLock != null && wifiLock.isHeld())
		{
			wifiLock.release();
			Log.d(getClass().getName(), "WIFI lock released");
		}
	}
	
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d(getClass().getName(), "Service onStartCommand");
    	
    	String title = intent.getStringExtra("title");
    	String subtitle = intent.getStringExtra("subtitle");
    
    	// Request audio focus from the AudioManager
    	mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    	
    	// If AudioManager gives audio focus...
    	if ( result == AudioManager.AUDIOFOCUS_GAIN )
    	{
    		Log.d(getClass().getName(), "Got audio focus");
    	
    	
        //if (intent.getAction().equals(ACTION_PLAY)) {
          
    		// Get the url (local or internet) from the start service intent
    		String audioUrl = intent.getStringExtra("audioUrl");
    		Log.d(getClass().getName(), "Got Audio URL: " + audioUrl);
    	
    		// Initialize MediaPlayer
    		mMediaPlayer = new MediaPlayer();
    		
    		// Check if the service is already play a file TODO: mMediaPlayer.isPlaying() does't work
    		if (mMediaPlayer.isPlaying() == false)
    		{
    			Log.d(getClass().getName(), "MediaPlayer is not playing");
    			
    			// Set the DataSource url (local or internet)
    			try {
    				mMediaPlayer.setDataSource(audioUrl);            
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
    			
    			// Set a partial CPU wake lock 
    			mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            
    			// Set a WIFI wake lock if playing streaming audio
    			if (audioUrl.contains("http"))
    			{
    				// need to wifiLock.release(); if you pause or stop the audio
        			wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        			wifiLock.acquire();
        			Log.d(getClass().getName(), "WIFI lock acquired");
    			}
    			
    			mMediaPlayer.setOnPreparedListener(this);
    			mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    			
    		} else {
    			Log.d(getClass().getName(), "MediaPlayer is already playing");
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
    		
    		
    		// Notify the startService call that the result was ok. TODO: Needs work.
    		Bundle extras = intent.getExtras();
    		if (extras != null) {
    			Messenger messenger = (Messenger) extras.get("MESSENGER");
    			Message msg = Message.obtain();
    			msg.arg1 = Activity.RESULT_OK;
    			try {
    				messenger.send(msg);
    			} catch (android.os.RemoteException e1) {
    				Log.w(getClass().getName(), "Exception sending message", e1);
    			}
    		}
    	//}
    	} else {
    		Log.d(getClass().getName(), "Could not get audio focus");
    	}
    	
    	return START_STICKY;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
    	Log.d(getClass().getName(), "onPrepared: MediaPlayer is ready");
        player.start();
    }

    /** MediaPlayer.OnErrorListener **/
    public void initMediaPlayer() {
    	Log.d(getClass().getName(), "MediaPlayer: initMediaPlayer");
    	
    	mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnErrorListener(this);
    }
    
    /** MediaPlayer.OnErrorListener **/
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d(getClass().getName(), "MediaPlayer: onError");
		return false;
	}
	
	/* AudioManager.OnAudioFocusChangeListener */
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
        case AudioManager.AUDIOFOCUS_GAIN:
            // resume playback
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_GAIN");
        	if (mMediaPlayer == null) {
        		initMediaPlayer();
        	}
            else if (!mMediaPlayer.isPlaying()) {
            	mMediaPlayer.start();
            }
            
        	mMediaPlayer.setVolume(1.0f, 1.0f);
            break;

        case AudioManager.AUDIOFOCUS_LOSS:
            // Lost focus for an unbounded amount of time: stop playback and release media player
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS");
        	if (mMediaPlayer != null) {
	            if (mMediaPlayer.isPlaying())
	            {
	            	mMediaPlayer.stop();
	            }
	
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	
	            // Check if wifiLock is held and release it if so
	    		if (wifiLock.isHeld())
	    		{
	    			wifiLock.release();
	    			Log.d(getClass().getName(), "WIFI lock released");
	    		}
        	}
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost focus for a short time, but we have to stop
            // playback. We don't release the media player because playback
            // is likely to resume
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
        	if (mMediaPlayer != null) {
	            if (mMediaPlayer.isPlaying()) {
	            	mMediaPlayer.pause();
	            }
        	}
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Lost focus for a short time, but it's ok to keep playing
            // at an attenuated level
        	Log.d(getClass().getName(), "onAudioFocusChange: AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
        	if (mMediaPlayer != null) {
	            if (mMediaPlayer.isPlaying()) {
	            	mMediaPlayer.setVolume(0.1f, 0.1f);
	            }
        	}
            break;
        }
    }
}
