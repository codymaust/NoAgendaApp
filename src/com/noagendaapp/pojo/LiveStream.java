/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
