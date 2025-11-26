package com.example.eventapplication.data;
public class Event {
    public long id;
    public String title;
    public String date;          // pretty string: "16 July 2020"
    public String subtitle;
    public String description;
    public String location;      // human-readable address

    // NEW
    public long eventTimestamp;  // real millis since epoch (for sorting/filter)
    public double lat;
    public double lng;
    public int maxPlaces;        // total capacity
    public int availablePlaces;  // remaining spots
    public String imageUri;
    public int imageResId;
    public boolean isBooked = false;

    public Event() {}

    public Event(long id, String title, String date,
                 String subtitle, String description, String location) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.subtitle = subtitle;
        this.description = description;
        this.location = location;
    }
}
