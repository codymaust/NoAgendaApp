package us.k117.noagendaapp.db;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import us.k117.noagendaapp.db.ShowsDatabaseHelper;
import us.k117.noagendaapp.db.ShowsTable;

public class ShowsContentProvider extends ContentProvider {
	
	// database
	private ShowsDatabaseHelper database;

	// Used for the UriMacher
	private static final int SHOWS = 10;
	private static final int SHOWS_ID = 20;

	private static final String AUTHORITY = "us.k117.noagendaapp.db";

	private static final String BASE_PATH = "shows";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_PATH;

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, SHOWS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SHOWS_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase mySQLiteDatabase = database.getWritableDatabase();
		long id = 0;

		switch (uriType) {
		case SHOWS:
			id = mySQLiteDatabase.insert(ShowsTable.TABLE_SHOWS, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public boolean onCreate() {
		database = new ShowsDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {		

		SQLiteQueryBuilder mySQLiteQueryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		mySQLiteQueryBuilder.setTables(ShowsTable.TABLE_SHOWS);

		int uriType = sURIMatcher.match(uri);
		
		switch (uriType) {
		case SHOWS:
			break;
		case SHOWS_ID:
			// Adding the ID to the original query
			mySQLiteQueryBuilder.appendWhere(ShowsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		SQLiteDatabase db = null;
		
		try
		{
			db = database.getWritableDatabase();
		}
		catch (Exception ex)
		{
			Log.d(getClass().getName(), ex.toString());
			ex.printStackTrace();
		}
		
		Cursor cursor = mySQLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void checkColumns(String[] projection) {
		String[] available = { ShowsTable.COLUMN_TITLE, ShowsTable.COLUMN_SUBTITLE, ShowsTable.COLUMN_LINK, ShowsTable.COLUMN_ID };
		
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
