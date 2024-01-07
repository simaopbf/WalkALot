package com.example.actuallayout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class SQLiteManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AccelerationDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "AccelerationData";
    private static final String ID_FIELD = "id";
    private static final String X_AXIS_FIELD = "x_axis";
    private static final String Y_AXIS_FIELD = "y_axis";
    private static final String Z_AXIS_FIELD = "z_axis";
    private static final String TIMESTAMP_FIELD = "timestamp";

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            ID_FIELD + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            X_AXIS_FIELD + " REAL NOT NULL, " +
            Y_AXIS_FIELD + " REAL NOT NULL, " +
            Z_AXIS_FIELD + " REAL NOT NULL, " +
            TIMESTAMP_FIELD + " TEXT NOT NULL);";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public SQLiteDatabase open() {
        // Open the database in write mode
        return getWritableDatabase();
    }
    public long insertAccelerationData(double xAxis, double yAxis, double zAxis) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(X_AXIS_FIELD, xAxis);
        values.put(Y_AXIS_FIELD, yAxis);
        values.put(Z_AXIS_FIELD, zAxis);
        values.put(TIMESTAMP_FIELD, getCurrentTimestamp());

        return sqLiteDatabase.insert(TABLE_NAME, null, values);
    }

    public String getAllAccelerationData() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        StringBuilder result = new StringBuilder();

        try {
            if (cursor.moveToFirst()) {
                // Log the column names
                String[] columnNames = cursor.getColumnNames();
                Log.d("ColumnNames", Arrays.toString(columnNames));

                do {
                    // Use getColumnIndexOrThrow to avoid -1 value
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_FIELD));
                    float xAxis = cursor.getFloat(cursor.getColumnIndexOrThrow(X_AXIS_FIELD));
                    float yAxis = cursor.getFloat(cursor.getColumnIndexOrThrow(Y_AXIS_FIELD));
                    float zAxis = cursor.getFloat(cursor.getColumnIndexOrThrow(Z_AXIS_FIELD));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(TIMESTAMP_FIELD));

                    result.append("ID: ").append(id).append(", ")
                            .append("X-Axis: ").append(xAxis).append(", ")
                            .append("Y-Axis: ").append(yAxis).append(", ")
                            .append("Z-Axis: ").append(zAxis).append(", ")
                            .append("Timestamp: ").append(timestamp).append("\n");
                } while (cursor.moveToNext());
            }
        } finally {
            //cursor.close();
            //sqLiteDatabase.close();
        }

        return result.toString();
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Uncomment and integrate this method once you have the conversion logic
    /*
    public long insertSteps(int stepCount, String date) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(STEPS_FIELD, stepCount);
        values.put(DATE_FIELD, date);

        return sqLiteDatabase.insert(TABLE_NAME, null, values);
    }

    // Uncomment and integrate this method once you have the conversion logic
    public String getAllSteps() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        StringBuilder result = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ID_FIELD));
                int stepCount = cursor.getInt(cursor.getColumnIndex(STEPS_FIELD));
                String date = cursor.getString(cursor.getColumnIndex(DATE_FIELD));

                result.append("ID: ").append(id).append(", ")
                        .append("Step Count: ").append(stepCount).append(", ")
                        .append("Date: ").append(date).append("\n");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return result.toString();
    }
    */
}