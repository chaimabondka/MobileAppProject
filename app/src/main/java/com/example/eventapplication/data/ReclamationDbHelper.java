package com.example.eventapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReclamationDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "reclamations.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_RECLAMATIONS = "reclamations";

    private static final String CREATE_TABLE_RECLAMATIONS =
            "CREATE TABLE " + TABLE_RECLAMATIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT NOT NULL," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "status TEXT NOT NULL," +
                    "created_at INTEGER," +
                    "response TEXT" +
                    ");";

    public ReclamationDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECLAMATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // for now simplest strategy
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECLAMATIONS);
        onCreate(db);
    }
}
