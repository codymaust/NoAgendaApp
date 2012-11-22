package us.k117.noagendaapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import us.k117.noagendaapp.MyTabListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		
		Tab tab = actionBar
				.newTab()
				.setText("Shows")
				.setTabListener(new MyTabListener<ShowsFragment>(this, "shows", ShowsFragment.class));
		actionBar.addTab(tab);
		
		tab = actionBar
				.newTab()
				.setText("Live Stream")
				.setTabListener(new MyTabListener<LiveStreamFragment>(this, "liveStream", LiveStreamFragment.class));
		actionBar.addTab(tab);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			/*
			Object path = message.obj;
			if (message.arg1 == RESULT_OK && path != null ) {
				Toast.makeText(MainActivity.this, "Downloaded: " + path.toString(), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MainActivity.this, "Download Failed.", Toast.LENGTH_LONG).show();
			}
			*/
			Toast.makeText(MainActivity.this, "Message Recieved", Toast.LENGTH_LONG).show();
		}
	};
	
	
	public void onClick(View view) {
		Intent intent = new Intent (this, AudioStreamService.class);
		//Create a new Messenger for the communication back
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);
		//intent.setData(Uri.parse("http://courses.ucsd.edu/default.aspx"));
		intent.putExtra("audioUrl", "http://stream1.fralnet.com:8000");
		startService(intent);
	}

	public void onStopClick(View view) {
		Intent intent = new Intent (this, AudioStreamService.class);
		stopService(intent);
	}
}
