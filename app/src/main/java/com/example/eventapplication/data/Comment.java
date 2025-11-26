// com/example/eventapplication/data/Comment.java
package com.example.eventapplication.data;

public class Comment {
    public long id;
    public long eventId;
    public String userId;
    public String userName;
    public String text;
    public long createdAt;  // timestamp millis

    public Comment(long id, long eventId, String userId,
                   String userName, String text, long createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.createdAt = createdAt;
    }
}
