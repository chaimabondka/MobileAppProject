package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TicketDao {

    private final EventDbHelper helper;

    public TicketDao(Context ctx) {
        this.helper = new EventDbHelper(ctx.getApplicationContext());
    }

    public long insert(long bookingId, String userId, long eventId, String payload) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("booking_id", bookingId);
        cv.put("user_id", userId);
        cv.put("event_id", eventId);
        cv.put("qr_payload", payload);
        cv.put("created_at", System.currentTimeMillis());
        return db.insert(EventDbHelper.TABLE_TICKETS, null, cv);
    }

    public Ticket findByBookingId(long bookingId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT id, booking_id, user_id, event_id, qr_payload, created_at, checked_in_at " +
                "FROM " + EventDbHelper.TABLE_TICKETS + " WHERE booking_id = ? LIMIT 1";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(bookingId)})) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    public Ticket findByPayload(String payload) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT id, booking_id, user_id, event_id, qr_payload, created_at, checked_in_at " +
                "FROM " + EventDbHelper.TABLE_TICKETS + " WHERE qr_payload = ? LIMIT 1";
        try (Cursor c = db.rawQuery(sql, new String[]{payload})) {
            if (c.moveToFirst()) {
                return fromCursor(c);
            }
        }
        return null;
    }

    public boolean markCheckedIn(long bookingId, long when) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("checked_in_at", when);
        int rows = db.update(EventDbHelper.TABLE_TICKETS, cv, "booking_id = ?",
                new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    private Ticket fromCursor(Cursor c) {
        Ticket t = new Ticket();
        t.id = c.getLong(0);
        t.bookingId = c.getLong(1);
        t.userId = c.getString(2);
        t.eventId = c.getLong(3);
        t.qrPayload = c.getString(4);
        t.createdAt = c.getLong(5);
        if (!c.isNull(6)) t.checkedInAt = c.getLong(6);
        return t;
    }
}
