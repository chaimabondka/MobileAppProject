package com.example.eventapplication.data;

public class Reclamation {
    public long id;
    public String userId;      // same type as SessionManager.getUserId()
    public String title;
    public String description;
    public String status;      // "Pending", "In Progress", "Resolved"
    public long createdAt;     // millis
    public String response;    // admin response (optional)

    public Reclamation() {}

    public static final String STATUS_PENDING     = "Pending";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED    = "Resolved";
}
