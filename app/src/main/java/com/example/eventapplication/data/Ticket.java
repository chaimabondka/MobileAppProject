package com.example.eventapplication.data;

public class Ticket {
    public long id;
    public long bookingId;
    public String userId;
    public long eventId;
    public String qrPayload;
    public long createdAt;
    public Long checkedInAt; // nullable
}
