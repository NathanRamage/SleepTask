package com.example.nathan.sleeptask.db;

/**
 * Created by nathan on 12/10/16.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    public TaskDbHelper(Context context) {
        super(context, TaskContract.DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTaskTable = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL," +
                TaskContract.TaskEntry.COL_DAY_TITLE + " TEXT NOT NULL," +
                TaskContract.TaskEntry.COL_IMPORTANT + " BOOLEAN NOT NULL DEFAULT 0," +
                TaskContract.TaskEntry.COL_START_TIME + " INT NOT NULL," +
                TaskContract.TaskEntry.COL_DURATION + " INT NOT NULL);";
        db.execSQL(createTaskTable);

        String createDaysTable = "CREATE TABLE " + TaskContract.DayEntry.TABLE + " ( " +
                TaskContract.DayEntry.COL_DAY_TITLE + " TEXT PRIMARY KEY);";
        db.execSQL(createDaysTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.DayEntry.TABLE);
        onCreate(db);
    }
}
