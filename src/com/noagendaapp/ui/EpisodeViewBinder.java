package com.noagendaapp.ui;

import java.io.File;

import com.noagendaapp.R;

import android.app.Activity;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.ImageView;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

public class EpisodeViewBinder implements ViewBinder {
	
	protected Activity myActivity;
	
	public EpisodeViewBinder (Activity activity) {
		myActivity = activity;
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		
	    int viewId = view.getId();
	    switch(viewId) {

	    //
	    // Show the play icon if the file is found on the device or show the download icon if it is not found
	    //
	    case R.id.row_action_icon:   		
    		String link = cursor.getString(columnIndex);
    		String myFileName = Uri.parse(link).getLastPathSegment();
			
    		ImageView action_icon = (ImageView) view;
    		
    		File myFile = new File(Environment.getExternalStorageDirectory() + myActivity.getResources().getString(R.string.download_path) + "/" + myFileName);

			if (myFile.exists()) {
				action_icon.setImageResource(R.drawable.ic_action_playback_play_hl);	
				action_icon.setContentDescription(myActivity.getResources().getString(R.string.episode_row_action_icon_play));
			} else {
				action_icon.setImageResource(R.drawable.ic_action_download_hl);	
				action_icon.setContentDescription(myActivity.getResources().getString(R.string.episode_row_action_icon_download));	
			}
			
			return true;			

	    default:	    	
	    	break;
	    }

		return false;
	}
}