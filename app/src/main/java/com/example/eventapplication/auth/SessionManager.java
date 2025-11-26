package com.example.eventapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "event_app_session";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void login(String userId, String name, String email) {
        prefs.edit()
                .putString("user_id", userId)
                .putString("name", name)
                .putString("email", email)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public String getUserId() {
        return prefs.getString("user_id", null);
    }

    public String getName() {
        return prefs.getString("name", null);
    }

    public String getEmail() {
        return prefs.getString("email", null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }
}
