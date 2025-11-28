package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ReclamationDao {

    private final ReclamationDbHelper helper;

    public ReclamationDao(Context ctx) {
        helper = new ReclamationDbHelper(ctx);
    }

    public long insert(Reclamation r) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", r.userId);
        cv.put("title", r.title);
        cv.put("description", r.description);
        cv.put("status", r.status);
        cv.put("created_at", r.createdAt);
        cv.put("response", r.response);
        return db.insert(ReclamationDbHelper.TABLE_RECLAMATIONS, null, cv);
    }

    public int updateStatusAndResponse(long id, String status, String response) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        cv.put("response", response);
        return db.update(ReclamationDbHelper.TABLE_RECLAMATIONS, cv,
                "id=?", new String[]{String.valueOf(id)});
    }

    public List<Reclamation> getAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(ReclamationDbHelper.TABLE_RECLAMATIONS,
                null, null, null, null, null,
                "created_at DESC");
        List<Reclamation> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public List<Reclamation> getForUser(String userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(ReclamationDbHelper.TABLE_RECLAMATIONS,
                null, "user_id=?", new String[]{userId},
                null, null, "created_at DESC");
        List<Reclamation> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public Reclamation getById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(ReclamationDbHelper.TABLE_RECLAMATIONS,
                null, "id=?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
            return null;
        } finally {
            c.close();
        }
    }

    private Reclamation fromCursor(Cursor c) {
        Reclamation r = new Reclamation();
        r.id         = c.getLong(c.getColumnIndexOrThrow("id"));
        r.userId     = c.getString(c.getColumnIndexOrThrow("user_id"));
        r.title      = c.getString(c.getColumnIndexOrThrow("title"));
        r.description= c.getString(c.getColumnIndexOrThrow("description"));
        r.status     = c.getString(c.getColumnIndexOrThrow("status"));
        r.createdAt  = c.getLong(c.getColumnIndexOrThrow("created_at"));
        int idxResp  = c.getColumnIndex("response");
        if (idxResp != -1) {
            r.response = c.getString(idxResp);
        }
        return r;
    }
    public int countAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + ReclamationDbHelper.TABLE_RECLAMATIONS,
                null
        );
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

    public int countPending() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + ReclamationDbHelper.TABLE_RECLAMATIONS +
                        " WHERE status = ?",
                new String[]{Reclamation.STATUS_PENDING}
        );
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

}
