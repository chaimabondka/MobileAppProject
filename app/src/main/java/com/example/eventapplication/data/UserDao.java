package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class UserDao {
    private final DbHelper helper;


    public UserDao(Context ctx) { this.helper = new DbHelper(ctx); }

    String[] cols = {
            DbHelper.C_ID, DbHelper.C_NAME, DbHelper.C_EMAIL, DbHelper.C_HASH, DbHelper.C_SALT, DbHelper.C_CREATED,
            "role", "phone", "avatar_url", "age", "address", "avatar_uri"
    };


    public User findByEmail(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sel = DbHelper.C_EMAIL + " = ?";
        String[] args = { email.toLowerCase().trim() };
        try (Cursor c = db.query(DbHelper.T_USER, cols, sel, args, null, null, null)) {
            if (c.moveToFirst()) {
                User u = new User();
                u.id = c.getString(0);
                u.name = c.getString(1);
                u.email = c.getString(2);
                u.passwordHash = c.getString(3);
                u.passwordSalt = c.getString(4);
                u.createdAt = c.getLong(5);
                u.role = c.getString(6);
                u.phone = c.getString(7);
                u.avatarUrl = c.getString(8);
                if (!c.isNull(9)) u.age = c.getInt(9);
                u.address = c.getString(10);
                u.avatarUri = c.getString(11);
                return u;
            }
        }
        return null;
    }
    public long insert(User u) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.C_NAME, u.name);
        cv.put(DbHelper.C_EMAIL, u.email.toLowerCase().trim());
        cv.put(DbHelper.C_HASH, u.passwordHash);
        cv.put(DbHelper.C_SALT, u.passwordSalt);
        cv.put(DbHelper.C_CREATED, u.createdAt);

        // new fields
        cv.put("role", u.role);
        cv.put("phone", u.phone);
        cv.put("avatar_url", u.avatarUrl);
        if (u.age != null) cv.put("age", u.age);
        cv.put("address", u.address);
        cv.put("avatar_uri", u.avatarUri);

        return db.insert(DbHelper.T_USER, null, cv);
    }
    public int update(User u) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DbHelper.C_NAME, u.name);
        cv.put(DbHelper.C_EMAIL, u.email);
        cv.put("phone", u.phone);
        cv.put("address", u.address);
        cv.put("role", u.role);
        cv.put("avatar_url", u.avatarUrl);
        cv.put("avatar_uri", u.avatarUri);

        if (u.age != null) cv.put("age", u.age);
        else cv.putNull("age");

        return db.update(DbHelper.T_USER, cv, "id = ?", new String[]{String.valueOf(u.id)});
    }

}