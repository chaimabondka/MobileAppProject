package com.example.eventapplication.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "authapp.db";
    public static final int DB_VERSION = 3;


    public static final String T_USER = "users";
    public static final String C_ID = "id";
    public static final String C_NAME = "name";
    public static final String C_EMAIL = "email";
    public static final String C_HASH = "password_hash";
    public static final String C_SALT = "password_salt";
    public static final String C_CREATED = "created_at";


    private static final String CREATE_USERS =
            "CREATE TABLE " + T_USER + " (" +
                    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    C_NAME + " TEXT NOT NULL, " +
                    C_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    C_HASH + " TEXT NOT NULL, " +
                    C_SALT + " TEXT NOT NULL, " +
                    C_CREATED + " INTEGER NOT NULL" +
                    ")";


    public DbHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }


    @Override public void onCreate(SQLiteDatabase db) { db.execSQL(CREATE_USERS); }


    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN role TEXT NOT NULL DEFAULT 'ATTENDEE'");
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN phone TEXT");
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN avatar_url TEXT");
        }
        if (oldV < 3) {
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN age INTEGER");
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN address TEXT");
            db.execSQL("ALTER TABLE " + T_USER + " ADD COLUMN avatar_uri TEXT"); // local gallery URI
        }
    }
}