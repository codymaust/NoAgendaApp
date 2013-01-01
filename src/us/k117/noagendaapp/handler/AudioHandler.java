package us.k117.noagendaapp.handler;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class AudioHandler extends Handler {
	
	private Activity myActivity;
	
	public AudioHandler(Activity activity) {
		myActivity = activity;		
	}

	@Override
	public void handleMessage(Message message) {
		if (message.arg1 == Activity.RESULT_OK ) {
			Toast.makeText(myActivity, "Audio Started", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(myActivity, "Audio Start Failed.", Toast.LENGTH_LONG).show();
		}
	}
	
}
