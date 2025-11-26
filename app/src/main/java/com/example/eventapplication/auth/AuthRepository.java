package com.example.eventapplication.auth;

import android.content.Context;

import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.example.eventapplication.security.PasswordHasher;

public class AuthRepository {

    private final UserDao userDao;

    public AuthRepository(Context ctx) {
        this.userDao = new UserDao(ctx);
    }

    /**
     * Register a complete user with all fields.
     * This prevents NULL constraint issues (example: role).
     */
    public Result register(
            String name,
            String email,
            char[] password,
            String phone,
            Integer age,
            String address,
            String role,
            String avatarUri
    ) {
        // -------- VALIDATION --------
        if (name == null || name.trim().length() < 2)
            return Result.err("Name must be at least 2 characters");

        if (!isValidEmail(email))
            return Result.err("Invalid email");

        if (password == null || password.length < 6)
            return Result.err("Password must be at least 6 characters");

        if (userDao.findByEmail(email) != null)
            return Result.err("Email already registered");


        // -------- PASSWORD HASHING --------
        byte[] salt = PasswordHasher.newSalt();
        String saltB64 = PasswordHasher.b64(salt);
        String hashB64 = PasswordHasher.b64(PasswordHasher.deriveKey(password, salt));


        // -------- BUILD USER OBJECT --------
        User u = new User();
        u.name = name.trim();
        u.email = email.toLowerCase().trim();
        u.passwordSalt = saltB64;
        u.passwordHash = hashB64;
        u.createdAt = System.currentTimeMillis();

        // NEW FIELDS
        u.phone = phone;
        u.age = age;
        u.address = address;

        // CRITICAL FIX → SQLite requires NOT NULL
        if (role == null || role.trim().isEmpty()) {
            u.role = "ATTENDEE";   // default fallback
        } else {
            u.role = role.trim();
        }

        u.avatarUri = avatarUri;


        // -------- INSERT INTO DATABASE --------
        // -------- INSERT INTO DATABASE --------
        long rawId = userDao.insert(u);

        if (rawId == -1) {
            return Result.err("Failed to create user");
        }

// convert to String if you want id as String
        String id = String.valueOf(rawId);

        u.id = id;

        return Result.ok(u);

    }


    /**
     * Login user (email + password)
     */
    public Result login(String email, char[] password) {
        if (!isValidEmail(email))
            return Result.err("Invalid email");

        User u = userDao.findByEmail(email);
        if (u == null)
            return Result.err("No account with this email");

        boolean ok = PasswordHasher.verify(password, u.passwordSalt, u.passwordHash);

        return ok ? Result.ok(u) : Result.err("Incorrect password");
    }


    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }


    // Lightweight wrapper
    public static class Result {
        public final boolean success;
        public final String error;
        public final User user;

        private Result(boolean success, String error, User user) {
            this.success = success;
            this.error = error;
            this.user = user;
        }

        public static Result ok(User u) { return new Result(true, null, u); }
        public static Result err(String msg) { return new Result(false, msg, null); }
    }
}
