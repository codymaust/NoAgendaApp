package com.noagendaapp.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EpisodeTable {

	// Database table
	public static final String TABLE_EPISODE = "episode";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_SUBTITLE = "subtitle";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_POSITION = "position";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_EPISODE
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, " 
		+ COLUMN_TITLE + " text not null, " 
		+ COLUMN_SUBTITLE + " text not null," 
		+ COLUMN_LINK + " text not null,"
		+ COLUMN_POSITION + "integer default 0"
		+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.d(EpisodeTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODE);
		onCreate(database);
	}
} 
