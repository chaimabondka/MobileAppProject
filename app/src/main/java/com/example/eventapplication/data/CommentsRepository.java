// com/example/eventapplication/data/CommentsRepository.java
package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CommentsRepository {

    private final EventDbHelper helper;

    public CommentsRepository(Context ctx) {
        this.helper = new EventDbHelper(ctx.getApplicationContext());
    }

    public void addComment(long eventId, String userId, String userName, String text) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("event_id", eventId);
        cv.put("user_id", userId);
        cv.put("user_name", userName);
        cv.put("text", text);
        cv.put("created_at", System.currentTimeMillis());
        db.insert("comments", null, cv);
    }

    public List<Comment> getCommentsForEvent(long eventId) {
        List<Comment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id, event_id, user_id, user_name, text, created_at " +
                        "FROM comments WHERE event_id=? ORDER BY created_at ASC",
                new String[]{String.valueOf(eventId)}
        );

        while (c.moveToNext()) {
            long id = c.getLong(0);
            long eId = c.getLong(1);
            String uId = c.getString(2);
            String uName = c.getString(3);
            String text = c.getString(4);
            long ts = c.getLong(5);
            list.add(new Comment(id, eId, uId, uName, text, ts));
        }
        c.close();
        return list;
    }
}
