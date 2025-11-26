package com.example.eventapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;
import java.util.Random;

public class EmailVerificationManager {

    private static final String PREF_NAME = "email_verification_prefs";
    private static final String KEY_VERIFIED_PREFIX = "verified_";
    private static final String KEY_CODE_PREFIX = "code_";
    private static final String KEY_TS_PREFIX = "ts_";
    // e.g. 15 minutes
    private static final long EXPIRY_MS = 15 * 60 * 1000L;

    private final SharedPreferences prefs;

    public EmailVerificationManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isVerified(String email) {
        if (email == null) return false;
        return prefs.getBoolean(KEY_VERIFIED_PREFIX + email.toLowerCase(Locale.ROOT), false);
    }

    public void markVerified(String email) {
        if (email == null) return;
        prefs.edit()
                .putBoolean(KEY_VERIFIED_PREFIX + email.toLowerCase(Locale.ROOT), true)
                .remove(KEY_CODE_PREFIX + email.toLowerCase(Locale.ROOT))
                .remove(KEY_TS_PREFIX + email.toLowerCase(Locale.ROOT))
                .apply();
    }

    public String generateAndStoreCode(String email) {
        if (email == null) return null;
        String normalized = email.toLowerCase(Locale.ROOT);

        String code = String.format(Locale.ROOT, "%06d", new Random().nextInt(1_000_000));
        long now = System.currentTimeMillis();

        prefs.edit()
                .putString(KEY_CODE_PREFIX + normalized, code)
                .putLong(KEY_TS_PREFIX + normalized, now)
                .apply();

        return code;
    }

    public boolean verifyCode(String email, String codeTyped) {
        if (email == null || codeTyped == null) return false;

        String normalized = email.toLowerCase(Locale.ROOT);
        String stored = prefs.getString(KEY_CODE_PREFIX + normalized, null);
        long ts = prefs.getLong(KEY_TS_PREFIX + normalized, 0L);

        if (stored == null) return false;
        if (!stored.equals(codeTyped.trim())) return false;
        if (System.currentTimeMillis() - ts > EXPIRY_MS) return false;

        // success → mark verified
        markVerified(email);
        return true;
    }
}
