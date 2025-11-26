package com.example.eventapplication.data;

public class User {
    public String id;
    public String name;
    public String email;
    public String passwordHash;
    public String passwordSalt;
    public long createdAt;

    // v2
    public String role;      // ADMIN | ORGANIZER | ATTENDEE
    public String phone;
    public String avatarUrl; // (optional remote URL)

    // v3
    public Integer age;      // nullable
    public String address;   // street/city combined for now
    public String avatarUri; // content://... (picked from gallery)
}