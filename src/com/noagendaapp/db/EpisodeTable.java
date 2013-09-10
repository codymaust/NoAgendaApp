/*
Copyright (c) 2013, Kevin Coakley <kevin@k117.us>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
	public static final String COLUMN_LENGTH = "length";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_EPISODE_NUM = "episode_num";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " + TABLE_EPISODE
		+ "(" 
		+ COLUMN_ID + " integer primary key autoincrement, " 
		+ COLUMN_TITLE + " text not null, " 
		+ COLUMN_SUBTITLE + " text not null, " 
		+ COLUMN_LINK + " text not null, "
		+ COLUMN_POSITION + " integer default 0, "
		+ COLUMN_LENGTH + " integer default 0, "
		+ COLUMN_DATE + " text not null, "
		+ COLUMN_EPISODE_NUM + " text not null " 
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
