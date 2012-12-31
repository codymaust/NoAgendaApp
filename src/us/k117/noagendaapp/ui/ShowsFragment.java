package us.k117.noagendaapp.ui;


import java.io.File;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import us.k117.noagendaapp.R;

import us.k117.noagendaapp.db.ShowsContentProvider;
import us.k117.noagendaapp.db.ShowsTable;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.DownloadManager.Query;

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
		
		String downloadURI = null;
		String destinationDirectory = Environment.getExternalStorageDirectory() + "/Music/NoAgendaApp";
		String fileName = null;
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		
		String[] projection = { ShowsTable.COLUMN_LINK };			
		Cursor myCursor = getActivity().getContentResolver().query(ShowsContentProvider.CONTENT_URI, projection, ShowsTable.COLUMN_ID + " = ?", new String[] { Long.toString(info.id) }, null);

		if (myCursor.moveToFirst()) {
			downloadURI = myCursor.getString(myCursor.getColumnIndex(ShowsTable.COLUMN_LINK));
			Log.d(getClass().getName(), downloadURI);
		}
		
		fileName = Uri.parse(downloadURI).getLastPathSegment();
		
		File myDestinationFile = new File(destinationDirectory + "/" + fileName);

		if (myDestinationFile.exists()) {
			menu.add(0, PLAY_ID, 0, R.string.shows_context_play);
			menu.add(0, DELETE_ID, 1, R.string.shows_context_delete);		
		} else {
			menu.add(0, DOWNLOAD_ID, 0, R.string.shows_context_download);			
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case PLAY_ID:
			Log.d(getClass().getName(), "PLAY_ID");
			return true;
		case DOWNLOAD_ID:
			Log.d(getClass().getName(), "DOWNLOAD_ID");
			
			String downloadURI = null;
			String destinationDirectory = Environment.getExternalStorageDirectory() + "/Music/NoAgendaApp";
			String fileName = null;
			
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

			String[] projection = { ShowsTable.COLUMN_LINK };			
			Cursor myCursor = getActivity().getContentResolver().query(ShowsContentProvider.CONTENT_URI, projection, ShowsTable.COLUMN_ID + " = ?", new String[] { Long.toString(info.id) }, null);
			
    		if (myCursor.moveToFirst()) {
    			downloadURI = myCursor.getString(myCursor.getColumnIndex(ShowsTable.COLUMN_LINK));
    			Log.d(getClass().getName(), downloadURI);
   			}
    		
    		fileName = Uri.parse(downloadURI).getLastPathSegment();
    		
    		File myDestinationFile = new File(destinationDirectory + "/" + fileName);
 
    		if (!myDestinationFile.exists()) {
    			
    			File myDestinationDirectory = new File(destinationDirectory);
    			if (!myDestinationDirectory.exists()) {
    				myDestinationDirectory.mkdir();
    			}
    		
	        	DownloadManager myDownloadManager = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
	        	Request myRequest = new Request(Uri.parse(downloadURI));
	        	myRequest.setDestinationUri(Uri.parse("file://" + destinationDirectory + "/" + fileName));
	        	long enqueue = myDownloadManager.enqueue(myRequest);
    		}
    		else
    		{
    			Toast.makeText(getActivity(), "Show has already been downloaded", Toast.LENGTH_SHORT).show();
    		}
	        return true;
		case DELETE_ID:
			Log.d(getClass().getName(), "DELETE_ID");
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
