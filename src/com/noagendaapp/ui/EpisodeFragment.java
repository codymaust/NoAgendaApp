package com.noagendaapp.ui;

import com.noagendaapp.audio.AudioStreamService;
import com.noagendaapp.db.EpisodeContentProvider;
import com.noagendaapp.db.EpisodeTable;
import com.noagendaapp.pojo.Episode;
import com.noagendaapp.MainActivity;
import com.noagendaapp.R;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class EpisodeFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	private static final int PLAY_ID = Menu.FIRST + 1;
	private static final int DOWNLOAD_ID = Menu.FIRST + 2;
	private static final int DELETE_ID = Menu.FIRST + 3;
	
	//
	// Required for ListFragment (onCreate, onActivityCreated, onCreateView)
	// 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fillData();		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		// Attach the context menu to the ListFragment
		registerForContextMenu(getListView());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_episode, container, false);
		return view;
	}
	
	//
	// Do something when an item is selected
	//
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Episode myEpisode = new Episode(getActivity(), Long.toString(id));
		
		if (myEpisode.FileExists()) {
			// TODO: If selecting the same episode then stop; if selecting a new episode save the old episode's position
			// Only play the episode if the audio is stopped or the selected episode is different then the active episode
			if ( ! AudioStreamService.isPlaying() || myEpisode.id != MainActivity.activeEpisode.id ) {
				myEpisode.Play();
				// Set the active episode 
				MainActivity.activeEpisode = myEpisode;
			}
			
		} else {
			myEpisode.Download();
		}
	}

	
	//
	// Required for Context Menu (onCreateContextMenu, onContextitemSeleted)
	//
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		
		Episode myEpisode = new Episode(getActivity(), Long.toString(info.id));
		
		if (myEpisode.FileExists()) {
			menu.add(0, PLAY_ID, 0, R.string.episode_context_play);
			menu.add(0, DELETE_ID, 1, R.string.episode_context_delete);		
		} else {
			menu.add(0, DOWNLOAD_ID, 0, R.string.episode_context_download);			
		}		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		Episode myEpisode = new Episode(getActivity(), Long.toString(info.id));

		
		switch (item.getItemId()) {
		case PLAY_ID:
			Log.d(getClass().getName(), "PLAY_ID");
			// TODO: If selecting the same episode then stop; if selecting a new episode save the old episode's position
			// Only play the episode if the audio is stopped or the selected episode is different then the active episode
			if ( ! AudioStreamService.isPlaying() || myEpisode.id != MainActivity.activeEpisode.id ) {
				myEpisode.Play();
				// Set the active episode 
				MainActivity.activeEpisode = myEpisode;
			}
			return true;
		case DOWNLOAD_ID:
			Log.d(getClass().getName(), "DOWNLOAD_ID");
			
			if ( ! myEpisode.FileExists() ) {
				myEpisode.Download();
			}				
	        return true;
		case DELETE_ID:
			Log.d(getClass().getName(), "DELETE_ID");
			
			if ( myEpisode.FileExists() ) {
				myEpisode.Delete();
			}	
			
			Toast.makeText(getActivity(), R.string.episode_deleted, Toast.LENGTH_SHORT).show();
			
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	//
	// Required for LoaderManager.LoaderCallbacks<Cursor> (onCreateLoader, onLoadFinished, onLoaderReset)
	//
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		String orderBy = EpisodeTable.COLUMN_TITLE + " DESC";
		
		String[] projection = { EpisodeTable.COLUMN_ID, EpisodeTable.COLUMN_TITLE, EpisodeTable.COLUMN_SUBTITLE };
		CursorLoader cursorLoader = new CursorLoader(getActivity(), EpisodeContentProvider.CONTENT_URI, projection, null, null, orderBy);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);		
	}
	
	//
	// Load the episode data from the database into the ListFragment
	//
	private void fillData() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { EpisodeTable.COLUMN_TITLE, EpisodeTable.COLUMN_SUBTITLE };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.title, R.id.subtitle };

		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.episode_row, null, from, to, 0);
		
		setListAdapter(adapter);
	}  
}
