package com.example.eventapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class EventDao {

    private final EventDbHelper helper;

    public EventDao(Context ctx) {
        helper = new EventDbHelper(ctx);
    }

    public long insert(Event e) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", e.title);
        cv.put("date", e.date);
        cv.put("subtitle", e.subtitle);
        cv.put("description", e.description);
        cv.put("location", e.location);
        cv.put("event_timestamp", e.eventTimestamp);
        cv.put("latitude", e.lat);
        cv.put("longitude", e.lng);
        cv.put("max_places", e.maxPlaces);
        cv.put("available_places", e.availablePlaces);
        cv.put("image_uri", e.imageUri);   // NEW

        return db.insert(EventDbHelper.TABLE_EVENTS, null, cv);
    }

    public int update(Event e) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", e.title);
        cv.put("date", e.date);
        cv.put("subtitle", e.subtitle);
        cv.put("description", e.description);
        cv.put("location", e.location);
        cv.put("event_timestamp", e.eventTimestamp);
        cv.put("latitude", e.lat);
        cv.put("longitude", e.lng);
        cv.put("max_places", e.maxPlaces);
        cv.put("available_places", e.availablePlaces);
        cv.put("image_uri", e.imageUri);   // NEW

        return db.update(EventDbHelper.TABLE_EVENTS, cv, "id=?",
                new String[]{String.valueOf(e.id)});
    }

    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(EventDbHelper.TABLE_EVENTS, "id=?",
                new String[]{String.valueOf(id)});
    }

    public Event getById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(EventDbHelper.TABLE_EVENTS, null,
                "id=?", new String[]{String.valueOf(id)},
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

    public List<Event> getAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(
                EventDbHelper.TABLE_EVENTS,
                null,
                null,
                null,
                null,
                null,
                "event_timestamp DESC, date DESC"
        );
        List<Event> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                list.add(fromCursor(c));
            }
        } finally {
            c.close();
        }
        return list;
    }

    // ADMIN HELPERS
    public int countEvents() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + EventDbHelper.TABLE_EVENTS, null);
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

    private Event fromCursor(Cursor c) {
        Event e = new Event();
        e.id = c.getLong(c.getColumnIndexOrThrow("id"));
        e.title = c.getString(c.getColumnIndexOrThrow("title"));
        e.date = c.getString(c.getColumnIndexOrThrow("date"));
        e.subtitle = c.getString(c.getColumnIndexOrThrow("subtitle"));
        e.description = c.getString(c.getColumnIndexOrThrow("description"));
        e.location = c.getString(c.getColumnIndexOrThrow("location"));

        int idxTs = c.getColumnIndex("event_timestamp");
        if (idxTs != -1) e.eventTimestamp = c.getLong(idxTs);
        int idxLat = c.getColumnIndex("latitude");
        if (idxLat != -1) e.lat = c.getDouble(idxLat);
        int idxLng = c.getColumnIndex("longitude");
        if (idxLng != -1) e.lng = c.getDouble(idxLng);
        int idxMax = c.getColumnIndex("max_places");
        if (idxMax != -1) e.maxPlaces = c.getInt(idxMax);
        int idxAvail = c.getColumnIndex("available_places");
        if (idxAvail != -1) e.availablePlaces = c.getInt(idxAvail);
        int idxImg = c.getColumnIndex("image_uri");
        if (idxImg != -1) e.imageUri = c.getString(idxImg);
        return e;
    }
}
