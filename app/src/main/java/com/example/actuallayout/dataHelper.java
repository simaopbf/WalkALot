package com.example.actuallayout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

//creation and connection trough a class derived from SQLiteOpenHelper
public class dataHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StepNCount.db";
    private static final int SCHEMA_VERSION = 1;

    public dataHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override //creation sql statements crate table in database
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Data(id INTEGER PRIMARY KEY AUTOINCREMENT, steps INTEGER, cal REAL , dist INTEGER, time INTEGER, date TEXT UNIQUE)"); //hour INTEGER PRIMARY KEY AUTOINCREMENT, energyE REAL
    }

    //upgrade  This version of the schema should be created in the onCreate() method. In onUpgrade() we should
    //put code needed to convert a database and schema from an oldVersion to a newVersion
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        android.util.Log.w("Data", "Upgrading database, which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS Data");
        onCreate(db);
    }

//put information into a database : Insert data into the database by passing a ContentValues object to the insert() method:

    // Insert a new record into the Events data base.
    public long insert(int steps, double cal, int dist, int time, String date) { //String hour, String energyE
        ContentValues cv = new ContentValues();
        // Create a new map of values, where column names are the keys
        //cv.put("hour", hour);
        cv.put("steps", steps);
        cv.put("cal", cal);
        cv.put("dist", dist);
        cv.put("time", time);
        cv.put("date", date);
        return getWritableDatabase().insert("Data", null, cv); // Result: Insert the new row, returning the primary key value of the new row
    }


//Read information from a database: query method, The results of the query are returned to you in a Cursor object. falta cenas

    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT steps, cal, dist, time, date FROM Data GROUP BY date", null));
    }
}