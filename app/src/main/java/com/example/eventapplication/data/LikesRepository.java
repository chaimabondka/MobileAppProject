// com/example/eventapplication/data/LikesRepository.java
package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LikesRepository {

    private final EventDbHelper helper;

    public LikesRepository(Context ctx) {
        this.helper = new EventDbHelper(ctx.getApplicationContext());
    }

    /** -1 = dislike, 0 = none, 1 = like */
    public int getUserStatus(long eventId, String userId) {
        if (userId == null) return 0;

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT value FROM likes WHERE event_id=? AND user_id=?",
                new String[]{String.valueOf(eventId), userId}
        );
        int status = 0;
        if (c.moveToFirst()) {
            status = c.getInt(0);
        }
        c.close();
        return status;
    }

    public int getLikesCount(long eventId) {
        return getCountForValue(eventId, 1);
    }

    public int getDislikesCount(long eventId) {
        return getCountForValue(eventId, -1);
    }

    private int getCountForValue(long eventId, int value) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM likes WHERE event_id=? AND value=?",
                new String[]{String.valueOf(eventId), String.valueOf(value)}
        );
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    /** status: -1, 0, 1 */
    public void setStatus(long eventId, String userId, int status) {
        if (userId == null) return;

        SQLiteDatabase db = helper.getWritableDatabase();

        if (status == 0) {
            db.delete("likes", "event_id=? AND user_id=?",
                    new String[]{String.valueOf(eventId), userId});
            return;
        }

        // UPSERT: try update, if 0 rows then insert
        ContentValues cv = new ContentValues();
        cv.put("value", status);
        int rows = db.update("likes", cv, "event_id=? AND user_id=?",
                new String[]{String.valueOf(eventId), userId});

        if (rows == 0) {
            cv.put("event_id", eventId);
            cv.put("user_id", userId);
            db.insert("likes", null, cv);
        }
    }
}
