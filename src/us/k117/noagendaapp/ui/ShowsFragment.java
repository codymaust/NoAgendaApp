package us.k117.noagendaapp.ui;

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
import android.widget.SimpleCursorAdapter;

import us.k117.noagendaapp.R;

import us.k117.noagendaapp.db.ShowsContentProvider;
import us.k117.noagendaapp.db.ShowsTable;

public class ShowsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

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
		View view = inflater.inflate(R.layout.fragment_shows, container, false);
		return view;
	}
	
	//
	// Required for Context Menu (onCreateContextMenu, onContextitemSeleted)
	//
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, PLAY_ID, 0, R.string.shows_context_play);
			menu.add(0, DOWNLOAD_ID, 1, R.string.shows_context_download);
			menu.add(0, DELETE_ID, 2, R.string.shows_context_delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PLAY_ID:
			Log.d(getClass().getName(), "PLAY");
			return true;
		case DOWNLOAD_ID:
			Log.d(getClass().getName(), "DOWNLOAD");
			return true;
		case DELETE_ID:
			Log.d(getClass().getName(), "DELETE");
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	//
	// Required for LoaderManager.LoaderCallbacks<Cursor> (onCreateLoader, onLoadFinished, onLoaderReset)
	//
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {		
		String orderBy = ShowsTable.COLUMN_TITLE + " DESC";
		
		String[] projection = { ShowsTable.COLUMN_ID, ShowsTable.COLUMN_TITLE, ShowsTable.COLUMN_SUBTITLE };
		CursorLoader cursorLoader = new CursorLoader(getActivity(), ShowsContentProvider.CONTENT_URI, projection, null, null, orderBy);
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
	// Load the show data from the database into the ListFragment
	//
	private void fillData() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { ShowsTable.COLUMN_TITLE, ShowsTable.COLUMN_SUBTITLE };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.title, R.id.subtitle };

		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.shows_row, null, from, to, 0);
		
		setListAdapter(adapter);
	}  
}
