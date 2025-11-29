package com.example.eventapplication.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookingsRepository {

    private final EventDbHelper helper;
    private final String userEmail;

    public BookingsRepository(Context ctx, String email) {
        this.helper = new EventDbHelper(ctx.getApplicationContext());
        this.userEmail = email;
    }

    /** BOOK EVENT with capacity check */
    public boolean bookEvent(long eventId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1) Read remaining spots
            Cursor c = db.rawQuery(
                    "SELECT available_places FROM events WHERE id = ?",
                    new String[]{ String.valueOf(eventId) }
            );
            if (!c.moveToFirst()) {
                c.close();
                return false; // event not found
            }
            int available = c.getInt(0);
            c.close();

            if (available <= 0) {
                // no more places
                return false;
            }

            // 2) Insert booking
            db.execSQL("INSERT INTO bookings(event_id, user_email) VALUES(?,?)",
                    new Object[]{eventId, userEmail});

            // 3) Decrement available_places
            db.execSQL("UPDATE events SET available_places = available_places - 1 WHERE id=?",
                    new Object[]{eventId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    /** UNBOOK EVENT and increase capacity */
    public void unBookEvent(long eventId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM bookings WHERE event_id=? AND user_email=?",
                    new Object[]{eventId, userEmail});

            db.execSQL("UPDATE events SET available_places = available_places + 1 WHERE id=?",
                    new Object[]{eventId});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** CHECK */
    public boolean isBooked(long eventId) {
        if (userEmail == null) return false;   // avoid crash

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM bookings WHERE event_id=? AND user_email=? LIMIT 1",
                new String[]{String.valueOf(eventId), userEmail}
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }


    /** GET ALL BOOKED EVENT IDs */
    public List<Long> getAllBookedIds() {
        List<Long> ids = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT event_id FROM bookings WHERE user_email=?",
                new String[]{userEmail}
        );

        while (c.moveToNext()) {
            ids.add(c.getLong(0));
        }
        c.close();

        return ids;
    }
}
