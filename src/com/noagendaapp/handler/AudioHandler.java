package com.noagendaapp.handler;

import java.util.concurrent.TimeUnit;

import com.noagendaapp.audio.AudioStreamService;
import com.noagendaapp.MainActivity;
import com.noagendaapp.R;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudioHandler extends Handler {
	
	private Activity myActivity;
	
	public AudioHandler(Activity activity) {
		myActivity = activity;		
	}

	@Override
	public void handleMessage(Message message) {
		// Suppress logging MSG_UPDATE_FRAGMENT_PLAYER messages
		if ( message.what != 99 ) {
			Log.d(getClass().getName(), "AudioHandler got a message:" + message.what);
		}
		
        switch (message.what) {
        case Activity.RESULT_OK:
        	// TODO: Currently not used, need to decide if this should be fixed to work or removed completely. Maybe only work for the Live Stream?
			Toast.makeText(myActivity, "Audio Started", Toast.LENGTH_LONG).show();
            break;
        case AudioStreamService.MSG_UPDATE_FRAGMENT_PLAYER:
        	// Only update the player information if the SeekBar isn't being used
        	if (MainActivity.updateSeekBar)
        	{
        		TextView currentPosition_TextView = (TextView) myActivity.findViewById(R.id.currentposition_textview);
        		TextView duration_TextView = (TextView) myActivity.findViewById(R.id.duration_textview);
        	
        		int currentPosition = message.arg1;
        		int duration = message.arg2;
        		
        		// Check the duration to prevent divide by 0 errors
        		if ( duration > 0 ) {
        			// Convert the currentPosition milliseconds to something that could formatted in a human readable display
        			long cHours = TimeUnit.MILLISECONDS.toHours(currentPosition);
        			long cMinutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition) - TimeUnit.HOURS.toMinutes(cHours);
        			long cSeconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.HOURS.toSeconds(cHours) - TimeUnit.MINUTES.toSeconds(cMinutes);
        	
        			// Convert the duration milliseconds to something that could formatted in a human readable display
        			long dHours = TimeUnit.MILLISECONDS.toHours(duration);
        			long dMinutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(dHours);
        			long dSeconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.HOURS.toSeconds(dHours) - TimeUnit.MINUTES.toSeconds(dMinutes);
        	
        			// Update the progress of the SeekBar based on the % of the current position away from the duration
        			SeekBar audio_SeekBar = (SeekBar) myActivity.findViewById(R.id.audio_seekbar);
        			audio_SeekBar.setProgress((currentPosition * 100) / duration);
        			audio_SeekBar.setMax(100);
        	
            		// Update the currentPostion & duration text on the play in the hh:mm:ss format
            		currentPosition_TextView.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
        			duration_TextView.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
        			
        			//
        			// Every 20 seconds save the current position to the database in case something happens
        			//
        			if ( cSeconds == 0 || cSeconds == 20 || cSeconds == 40 ) {
        				if ( MainActivity.activeEpisode != null ) {
        					MainActivity.activeEpisode.SetPosition(currentPosition);
        				}
        			}
        		}
        	} else {
        		Log.d(getClass().getName(), "AudioHandler: SEEKING");
        	}
        	break;
        // TODO: Need to change the name of the Message type	
        case AudioStreamService.MSG_STOP_GUI:
        	int currentPosition = message.arg1;
        	
        	Log.d(getClass().getName(), "AudioHandler: Stoping");
        	
        	// Save the current position to the activeEpisode Object
        	MainActivity.activeEpisode.SetPosition(currentPosition);

        	break;
        default:
            super.handleMessage(message);
        }
	}	
}
