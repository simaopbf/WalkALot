package com.example.actuallayout;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import Bio.Library.namespace.BioLib;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "WalkALot.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the user table
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, gender TEXT, height INTEGER, age INTEGER, weight INTEGER)");
        db.execSQL("CREATE TABLE Data(id INTEGER PRIMARY KEY AUTOINCREMENT, steps INTEGER, cal REAL , dist INTEGER, time INTEGER, date TEXT UNIQUE)"); //hour INTEGER PRIMARY KEY AUTOINCREMENT, energyE REAL

        // Insert default users
        db.execSQL("INSERT INTO users (username, password) VALUES ('user1', 'pass1')");
        db.execSQL("INSERT INTO users (username, password) VALUES ('user2', 'pass2')");
        // Add more default users as needed
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS Data");
        onCreate(db);
    }

    public long insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        // Insert the new user into the database
        long newRowId = db.insert("users", null, values);

        // Close the database connection
        db.close();

        return newRowId;
    }

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

    public long updateSettings(long Id,String gender, int height, int weight, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("gender", gender);
        values.put("height", height);
        values.put("weight", weight);
        values.put("age", age);

        // Insert the new user into the database
        long rowsAffected = db.update("users", values, "id = ?",new String[]{String.valueOf(Id)});


        // Close the database connection
        db.close();

        return rowsAffected;
    }

    // Add ACC data to the database
    public long addACCData(BioLib.DataACC dataACC) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("timestamp", System.currentTimeMillis());  // You might want to store a timestamp for each data point
        values.put("x_axis", dataACC.X);
        values.put("y_axis", dataACC.Y);
        values.put("z_axis", dataACC.Z);

        // Insert the ACC data into the database
        long newRowId = db.insert("ACCDataTable", null, values);

        // Close the database connection
        db.close();

        // Return the row ID or -1 if the insertion failed
        return newRowId;
    }

    public long getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;  // Default value if user is not found

        try {
            Log.d("DatabaseHelper", "Querying user ID for username: " + username);

            String[] projection = { "id" };
            String selection = "username = ?";
            String[] selectionArgs = { username };

            Cursor cursor = db.query("users", projection, selection, selectionArgs, null, null, null);

            int columnIndex = cursor.getColumnIndexOrThrow("id");

            if (cursor.moveToFirst() && columnIndex != -1) {
                userId = cursor.getLong(columnIndex);
            } else {
                // Log a message if the cursor is empty or columnIndex is -1
                Log.e("DatabaseHelper", "getUserIdByUsername: Cursor is empty or columnIndex is -1");
            }

            cursor.close();
        } catch (Exception e) {
            // Log any exception that occurs
            Log.e("DatabaseHelper", "getUserIdByUsername: Exception", e);
        } finally {
            db.close();
        }

        return userId;
    }
    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT steps, cal, dist, time, date FROM Data GROUP BY date", null));
    }

}


