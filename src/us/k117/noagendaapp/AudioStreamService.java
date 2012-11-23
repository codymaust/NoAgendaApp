package us.k117.noagendaapp;

import java.io.IOException;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.util.Log;

public class AudioStreamService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
	//private static final String ACTION_PLAY = "com.example.action.PLAY";
	MediaPlayer mMediaPlayer = null;
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
		
		wifiLock.release();
	}
	
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d(getClass().getName(), "Service onStartCommand");
    
        //if (intent.getAction().equals(ACTION_PLAY)) {
          
    		// Get the url (local or internet) from the start service intent
    		String audioUrl = intent.getStringExtra("audioUrl");
    	
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
    			}
    			
    			mMediaPlayer.setOnPreparedListener(this);
    			mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    			
    		} else {
    			Log.d(getClass().getName(), "MediaPlayer is already playing");
    		}
    		
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
}
