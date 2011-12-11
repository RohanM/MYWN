/*
 * Copyright (C) 2008 Google Inc.
 * Modifications to original are copyright (c) 2011 Rohan Mitchell.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.rohanmitchell.mywn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * MYWN TODO list database helper class.
 * Defines basic CRUD methods for tasks and lists.
 */
public class DbAdapter {

    public static final String KEY_TASKS_ID = "_id";
    public static final String KEY_TASKS_LIST_ID = "list_id";
    public static final String KEY_TASKS_DESCRIPTION = "description";
    public static final String KEY_TASKS_IS_COMPLETE = "is_complete";
    public static final String KEY_TASKS_CREATED_AT = "created_at";

    public static final String KEY_LISTS_ID = "_id";
    public static final String KEY_LISTS_NAME = "name";
    public static final String KEY_LISTS_ORDER_NO = "order_no";

    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table tasks (_id integer primary key autoincrement, "
        + "list_id integer not null, description text not null, "
	+ "is_complete tinyint(1) not null, created_at datetime not null default now());"

	+ "create table lists (_id integer primary key autoincrement, "
	+ "name varchar(255) not null, order_no integer not null);";

    /**
     * Database drop sql statement
     */
    private static final String DATABASE_DROP =
	"drop table if exists tasks; drop table if exists lists;";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_TASKS = "tasks";
    private static final String DATABASE_TABLE_LISTS = "lists";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL(DATABASE_DROP);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /********************************************************************************
     * CRUD methods
     ********************************************************************************/

    /**
     * Create a new task using the list and description provided. If the task is
     * successfully created return the new rowId for that task, otherwise return
     * a -1 to indicate failure.
     * 
     * @return rowId or -1 if failed
     */
    public long createTask(int list_id, String description) {
        ContentValues values = new ContentValues();
        values.put(KEY_TASKS_LIST_ID, list_id);
        values.put(KEY_TASKS_DESCRIPTION, description);

        return mDb.insert(DATABASE_TABLE_TASKS, null, values);
    }

    /**
     * Delete the task with the given rowId
     * 
     * @return true if deleted, false otherwise
     */
    public boolean deleteTask(long id) {
        return mDb.delete(DATABASE_TABLE_TASKS, KEY_TASKS_ID + "=" + id, null) > 0;
    }

    /**
     * Return a Cursor over the list of all tasks in the database
     * 
     * @return Cursor over all tasks
     */
    public Cursor fetchAllTasks() {
        return mDb.query(DATABASE_TABLE_TASKS,
			 new String[] {KEY_TASKS_ID, KEY_TASKS_LIST_ID, KEY_TASKS_DESCRIPTION, KEY_TASKS_IS_COMPLETE, KEY_TASKS_CREATED_AT},
			 null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the task that matches the given id
     * 
     * @return Cursor positioned to matching task, if found
     * @throws SQLException if task could not be found/retrieved
     */
    public Cursor fetchTask(long id) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE_TASKS,
		      new String[] {KEY_TASKS_ID, KEY_TASKS_LIST_ID, KEY_TASKS_DESCRIPTION, KEY_TASKS_IS_COMPLETE, KEY_TASKS_CREATED_AT},
		      KEY_TASKS_ID + "=" + id, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the task using the details provided. The task to be updated is
     * specified using the row id, and it is altered to use the values passed in.
     * 
     * @return true if the task was successfully updated, false otherwise
     */
    public boolean updateTask(long id, long list_id, String description, boolean is_complete) {
        ContentValues values = new ContentValues();
        values.put(KEY_TASKS_LIST_ID, list_id);
        values.put(KEY_TASKS_DESCRIPTION, description);
        values.put(KEY_TASKS_IS_COMPLETE, is_complete);

        return mDb.update(DATABASE_TABLE_TASKS, values, KEY_TASKS_ID + "=" + id, null) > 0;
    }
}
