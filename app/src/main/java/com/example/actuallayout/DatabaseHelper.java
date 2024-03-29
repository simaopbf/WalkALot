package com.example.actuallayout;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import Bio.Library.namespace.BioLib;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "WalkALot.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        // Create the user table
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, gender TEXT, height INTEGER, age INTEGER, weight INTEGER, stepGoal INTEGER, calGoal INTEGER, timeGoal INTEGER, distGoal INTEGER)");
        db.execSQL("CREATE TABLE Data(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER REFERENCES users(id), steps INTEGER, cal REAL , dist INTEGER, time INTEGER, date TEXT)"); //hour INTEGER PRIMARY KEY AUTOINCREMENT, energyE REAL
        db.execSQL("CREATE TABLE ACCDataTable (id INTEGER PRIMARY KEY AUTOINCREMENT, x_axis INTEGER, y_axis INTEGER, z_axis INTEGER, timestamp INTEGER)");
        // Insert default users
        db.execSQL("INSERT INTO users (username, password) VALUES ('user1', 'pass1')");
        db.execSQL("INSERT INTO users (username, password) VALUES ('user2', 'pass2')");

        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (1,1,2,3,'8/1/2024')");
        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (1,4,5,6,'8/1/2024')");
        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (1,7,8,9,'9/1/2024')");

        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (2,10,11,12,'8/1/2024')");
        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (2,13,14,15,'8/1/2024')");
        db.execSQL("INSERT INTO Data (user_id,steps,cal,dist,date) VALUES (2,16,17,18,'9/1/2024')");


        // Add more default users as needed
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS Data");
        db.execSQL("DROP TABLE IF EXISTS ACCDataTable");
        onCreate(db);
    }


    public long insertUser(String username, String password, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        if(isUsernameAvailable(username)==false){
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);

            // Insert the new user into the database
            long newRowId = db.insert("users", null, values);
            // Close the database connection
            db.close();
            return newRowId;

        } else {
            // Username is not available
            Log.e("DatabaseHelper", "Username not available: " + username);
            Toast.makeText(context, "Username already taken. Please pick a different one.", Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    public Boolean isUsernameAvailable(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from users where username = ?", new String[]{username});
        if(cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }


    // Insert a new record into the Events data base.
    public long insert(int steps, double cal, int dist, int time, String date) { //String hour, String energyE
        ContentValues cv = new ContentValues();
        // Create a new map of values, where column names are the keys

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

        return rowsAffected;
    }

    public long updateProfile(long Id,int steps, int cals, int time, int distGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stepGoal", steps);
        values.put("calGoal", cals);
        values.put("timeGoal", time);
        values.put("distGoal", distGoal);

        // Insert the new user into the database
        long rowsAffected = db.update("users", values, "id = ?",new String[]{String.valueOf(Id)});


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


        // Use the content resolver to insert data into the ACCDataTable
        Uri uri = MyContentProvider.CONTENT_URI;
        Uri insertedUri = context.getContentResolver().insert(uri, values);

        if (insertedUri != null) {
            // Data inserted successfully
        } else {
            // Failed to insert data
            Log.e(TAG, "Failed to insert data into ACCDataTable");
        }
        // Return the row ID or -1 if the insertion failed
        return newRowId;
    }

    public long getUserIdByUsername(String username) {

        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1;  // Default value if user is not found

        try {

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

            //cursor.close();
        } catch (Exception e) {
            // Log any exception that occurs
            Log.e("DatabaseHelper", "getUserIdByUsername: Exception", e);
        } finally {

        }

        return userId;
    }
    public Cursor getAll() {
        return (getReadableDatabase().rawQuery("SELECT steps, cal, dist, time, date FROM Data GROUP BY date", null));
    }

    public Cursor Datatable(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT steps, cal, dist, time FROM Data WHERE user_id = ? AND date = ?";
            String[] selectionArgs = {String.valueOf(userId), date};
            cursor = db.rawQuery(query, selectionArgs);


        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error in Datatable: " + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {

            }
        }

        return cursor;
    }
    public int getStepsForUserAndDate(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int steps = 10; // Default value if no match is found

        try {
            String query = "SELECT steps FROM Data WHERE user_id = ? AND date = ?";
            String[] selectionArgs = {String.valueOf(userId), date};

            Cursor cursor = db.rawQuery(query, selectionArgs);


            if (cursor != null && cursor.moveToFirst()) {
                steps = cursor.getInt(cursor.getColumnIndex("steps"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getStepsForUserAndDate: Exception", e);
        }

        return steps;
    }
    public int getDistsForUserAndDate(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int dist = 10; // Default value if no match is found

        try {
            String query = "SELECT dist FROM Data WHERE user_id = ? AND date = ?";
            String[] selectionArgs = {String.valueOf(userId), date};

            Cursor cursor = db.rawQuery(query, selectionArgs);


            if (cursor != null && cursor.moveToFirst()) {
                dist = cursor.getInt(cursor.getColumnIndex("dist"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getStepsForUserAndDate: Exception", e);
        }

        return dist;
    }
    public int targetValue(long userId, String Value) {
        SQLiteDatabase db = this.getReadableDatabase();
        int objective_value=-1;
        Log.d("targetValue", "targetValue:"+Value);

        try {
            String query = "SELECT " + Value + " FROM users WHERE id = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            Log.d("targetValue", "targetValue:"+Value);

            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                objective_value = cursor.getInt(cursor.getColumnIndex(Value));
                Log.d("targetValue", "targetValue:"+objective_value);
            }
        } catch (Exception e) {
            Log.e("targetValue", "targetValue: Exception", e);
        }
        return objective_value;
    }
    public String targetGender(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String objective_value="Male";


        try {
            String query = "SELECT gender FROM users WHERE id = ?";
            String[] selectionArgs = {String.valueOf(userId)};


            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                objective_value = cursor.getString(cursor.getColumnIndex("gender"));
                Log.d("targetValue", "targetValue:"+objective_value);
            }
        } catch (Exception e) {
            Log.e("targetValue", "targetValue: Exception", e);
        }
        return objective_value;
    }
    public int getDistForUserAndDate(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int dist = 0; // Default value if no match is found

        try {
            String query = "SELECT dist FROM Data WHERE user_id = ? AND date = ?";
            String[] selectionArgs = {String.valueOf(userId), date};

            Cursor cursor = db.rawQuery(query, selectionArgs);


            if (cursor != null && cursor.moveToFirst()) {
                dist = cursor.getInt(cursor.getColumnIndex("dist"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getStepsForUserAndDate: Exception", e);
        }

        return dist;
    }
    public int getCalForUserAndDate(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        int cal = 0; // Default value if no match is found

        try {
            String query = "SELECT cal FROM Data WHERE user_id = ? AND date = ?";
            String[] selectionArgs = {String.valueOf(userId), date};

            Cursor cursor = db.rawQuery(query, selectionArgs);


            if (cursor != null && cursor.moveToFirst()) {
                cal = cursor.getInt(cursor.getColumnIndex("cal"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getStepsForUserAndDate: Exception", e);
        }

        return cal;
    }





}
