package us.k117.noagendaapp;

import android.os.Bundle;
import android.os.Messenger;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;

import us.k117.noagendaapp.audio.AudioStreamService;
import us.k117.noagendaapp.handler.AudioHandler;
import us.k117.noagendaapp.ui.LiveStreamFragment;
import us.k117.noagendaapp.ui.MyTabListener;
import us.k117.noagendaapp.ui.EpisodeFragment;
import us.k117.noagendaapp.rss.DownloadRSSTask;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		
		// Create Episodes tab
		Tab tab = actionBar
				.newTab()
				.setText("Episodes")
				.setTabListener(new MyTabListener<EpisodeFragment>(this, "episode", EpisodeFragment.class));
		actionBar.addTab(tab);
		
		// Create Live Stream tab
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
	
	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			DownloadRSSTask myDownloadRSSTask = new DownloadRSSTask(this);
			myDownloadRSSTask.execute(getResources().getString(R.string.rss_feed));
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
	public void onPlayClick(View view) {
		Intent intent = new Intent (this, AudioStreamService.class);

		//Create a new Messenger for the communication back
		Messenger messenger = new Messenger(new AudioHandler(this));

		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("audioUrl", getResources().getString(R.string.live_stream));
		startService(intent);
	}

	public void onStopClick(View view) {
		Intent intent = new Intent (this, AudioStreamService.class);
		stopService(intent);
	}
}
