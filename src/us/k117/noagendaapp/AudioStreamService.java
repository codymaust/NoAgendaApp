package us.k117.noagendaapp;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class AudioStreamService extends Service implements MediaPlayer.OnPreparedListener {
	//private static final String ACTION_PLAY = "com.example.action.PLAY";
	MediaPlayer mMediaPlayer = null;
    
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
	}
	
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d(getClass().getName(), "Service onStartCommand");
    
        //if (intent.getAction().equals(ACTION_PLAY)) {
          
    		String audioUrl = intent.getStringExtra("audioUrl");
    	
    		mMediaPlayer = new MediaPlayer();
    		
    		if (mMediaPlayer.isPlaying() == false)
    		{
    			Log.d(getClass().getName(), "MediaPlayer is not playing");
    			
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
            
    			mMediaPlayer.setOnPreparedListener(this);
    			mMediaPlayer.prepareAsync(); // prepare async to not block main thread
    		} else {
    			Log.d(getClass().getName(), "MediaPlayer is already playing");
    		}
    		
    	//}
    		
    	return START_STICKY;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
    	Log.d(getClass().getName(), "onPrepared: MediaPlayer is ready");
        player.start();
    }
}
