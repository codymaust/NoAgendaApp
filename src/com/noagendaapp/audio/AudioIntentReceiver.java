package com.noagendaapp.audio;

import com.noagendaapp.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class AudioIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d(getClass().getName(), "Got onRecieve");
		if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			Log.d(getClass().getName(), "Headphones Unplugged");
	        // signal your service to stop playback TODO: Maybe change this to pause audio instead of stop the audio?
			try {  	  
				Message msg = Message.obtain(null, AudioStreamService.MSG_STOP_AUDIO, 0, 0);	

				// Make sure the service is running before sending a message to it.
				if (MainActivity.myService != null) {
					MainActivity.myService.send(msg);
				}
			} catch (RemoteException e) {
				Log.w(getClass().getName(), "Exception sending message", e);
			}
		}
	}
}
