package com.example.eventapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "events.db";
    // BUMP VERSION when schema changes
    public static final int DB_VERSION = 7;

    public static final String TABLE_EVENTS    = "events";
    public static final String TABLE_BOOKINGS  = "bookings";
    public static final String TABLE_COMMENTS  = "comments";
    public static final String TABLE_LIKES     = "likes";
    public static final String TABLE_TICKETS   = "tickets";

    // Full, correct schema for events
    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "date TEXT," +
                    "subtitle TEXT," +
                    "description TEXT," +
                    "location TEXT," +
                    "event_timestamp INTEGER," +
                    "latitude REAL," +
                    "longitude REAL," +
                    "max_places INTEGER DEFAULT 0," +
                    "available_places INTEGER DEFAULT 0," +
                    "image_uri TEXT" +
                    ");";

    private static final String CREATE_TABLE_BOOKINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKINGS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_id INTEGER," +
                    "user_email TEXT" +
                    ");";

    private static final String CREATE_TABLE_TICKETS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TICKETS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "booking_id INTEGER NOT NULL," +
                    "user_id TEXT NOT NULL," +
                    "event_id INTEGER NOT NULL," +
                    "qr_payload TEXT NOT NULL," +
                    "created_at INTEGER," +
                    "checked_in_at INTEGER" +
                    ");";

    private static final String CREATE_TABLE_COMMENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_id INTEGER NOT NULL," +
                    "user_id TEXT NOT NULL," +
                    "user_name TEXT," +
                    "text TEXT NOT NULL," +
                    "created_at INTEGER" +
                    ");";

    private static final String CREATE_TABLE_LIKES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_LIKES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "event_id INTEGER NOT NULL," +
                    "user_id TEXT NOT NULL," +
                    "value INTEGER NOT NULL," +          // 1 = like, -1 = dislike
                    "UNIQUE(event_id, user_id)" +
                    ");";

    public EventDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create ALL tables on fresh install
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_BOOKINGS);
        db.execSQL(CREATE_TABLE_COMMENTS);
        db.execSQL(CREATE_TABLE_LIKES);
        db.execSQL(CREATE_TABLE_TICKETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For development: just drop and recreate everything
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);

        onCreate(db);
    }

}
