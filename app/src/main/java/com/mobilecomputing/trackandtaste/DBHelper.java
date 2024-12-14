package com.mobilecomputing.trackandtaste;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "track_and_taste.db";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "saved_locations";
    // columns
    private static final String Column_id = "id";
    public static final String Column_label = "label";
    public static final String Column_lat = "lat";
    public static final String Column_lon = "lon";

    // SQL to create db
    private static final String CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + " ("
            + Column_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Column_label + " TEXT, "
            + Column_lat + " REAL, "
            + Column_lon + " REAL);";

    public DBHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    //insert a new location into the db
    public boolean insertLocation(String label, double lat, double lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column_label, label);
        contentValues.put(Column_lat, lat);
        contentValues.put(Column_lon, lon);
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }


    //fetch all saved locations from the db
    // returns a cursos, so we can manage directly from file and keep this file clean
    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur =  db.query(TABLE_NAME, null, null, null, null, null, null);
        db.close();
        return cur;
    }


}
