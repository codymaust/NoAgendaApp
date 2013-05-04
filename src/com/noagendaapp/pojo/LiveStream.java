package com.noagendaapp.pojo;

import java.util.Calendar;

import com.noagendaapp.R;

import android.app.Activity;

public class LiveStream extends Episode {

	public LiveStream(Activity activity, String myId) {
		super(activity, myId);
		
		title = myActivity.getResources().getString(R.string.live_stream_title);
		subtitle = myActivity.getResources().getString(R.string.live_stream_subtitle);
		link = "";
		position = "0";
		length = "0";
		date = Calendar.getInstance();
		episodeNum = "0";
		audioUrl = myActivity.getResources().getString(R.string.live_stream_url);
		
		// Set GUI options
		seekBarEnabled = false;
	}
	
	// Override because the LiveStream cannot Seek
	@Override
	public void SeekTo(int amount) {
	}
	
	// Override because the LiveStream cannot Jump
	@Override 
	public void JumpTo(int position) {
	}
	
	// Override because the LiveStream doesn't have a position to save
	@Override 
	public void SetPosition(int newPosition) {
	}
}
