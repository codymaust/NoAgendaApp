package us.k117.noagendaapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AudioIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d(getClass().getName(), "Got onRecieve");
		if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			Log.d(getClass().getName(), "Headphones Unplugged");
	          // signal your service to stop playback TODO: Maybe change this to pause audio instead of stop the audio?
			Intent audioStreamService = new Intent (ctx, AudioStreamService.class);
			ctx.stopService(audioStreamService);
		}
	}
}
