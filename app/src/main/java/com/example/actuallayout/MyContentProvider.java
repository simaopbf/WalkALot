package com.example.actuallayout;

import android.content.ContentProvider;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
public class MyContentProvider extends ContentProvider {

    private static final String TAG = "MyContentProvider";
    // Authority for this content provider
    public static final String AUTHORITY = "com.example.actuallayout.provider";

    // Define a constant for the table name
    public static final String TABLE_NAME = "ACCDataTable";

    // Content URI
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);


    // Define the MIME type for a directory of items
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.actuallayout.provider." + TABLE_NAME;

    // Define the MIME type for a single item
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.actuallayout.provider." + TABLE_NAME;

    // URI matcher code for the content URI for the ACCDataTable table
    private static final int ACC_DATA_TABLE = 1;

    // URI matcher object to match a content URI to a corresponding code
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer
    static {
        // Add content URI patterns to the matcher
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME, ACC_DATA_TABLE);
    }

    // Database helper instance
    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
     //   Log.d(TAG, "esta a funcionar");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ACC_DATA_TABLE:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
       // Log.d(TAG, "insert: Insert method in MyContentProvider called");
       // Log.d(TAG, "insert method called. Uri: " + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ACC_DATA_TABLE:
                long id = db.insert(TABLE_NAME, null, values);
                if (id == -1) {
                    Log.e(TAG, "Failed to insert data into ACCDataTable");
                    return null;
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                Log.e(TAG, "Unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // Implement the update method if needed
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Implement the delete method if needed
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ACC_DATA_TABLE:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
