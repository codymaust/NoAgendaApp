package com.noagendaapp.handler;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class DownloadHandler extends Handler {
	
	private Activity myActivity;
	
	public DownloadHandler(Activity activity) {
		myActivity = activity;		
	}
	
	public void handleMessage(Message message) {
		Object path = message.obj;
		
		// TODO: Instead of displaying toasts the GUI should be refreshed
		if (message.arg1 == Activity.RESULT_OK && path != null ) {
			Toast.makeText(myActivity, "Downloaded: " + path.toString(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(myActivity, "Download Failed.", Toast.LENGTH_LONG).show();
		}
	}
}
