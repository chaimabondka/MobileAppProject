package com.example.eventapplication.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.EventDao;
import com.example.eventapplication.data.EventDbHelper;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import com.example.eventapplication.data.ReclamationDao;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements AdminUserAdapter.OnRoleChangeListener {

    private TextView tvTotalUsers, tvTotalEvents, tvTotalBookings;
    private RecyclerView rvUsers;
    private UserDao userDao;
    private EventDao eventDao;
    private TextView tvTotalReclamations, tvPendingReclamations;   // 🔹 NEW
    private ReclamationDao reclamationDao;                          // 🔹 NEW

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        userDao = new UserDao(this);
        eventDao = new EventDao(this);
        reclamationDao = new ReclamationDao(this); // 🔹 NEW

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalEvents = findViewById(R.id.tvTotalEvents);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);

        tvTotalReclamations = findViewById(R.id.tvTotalReclamations);      // 🔹 NEW
        tvPendingReclamations = findViewById(R.id.tvPendingReclamations);  // 🔹 NEW

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Button "Manage" reclamations
        findViewById(R.id.btnManageReclamations).setOnClickListener(v -> {
            Intent i = new Intent(this, ReclamationAdminActivity.class);
            startActivity(i);
        });

        loadStats();
        loadUsers();
    }


    private void loadStats() {
        int users = userDao.countUsers();
        int events = eventDao.countEvents();
        int bookings = countBookings();

        tvTotalUsers.setText(String.valueOf(users));
        tvTotalEvents.setText(String.valueOf(events));
        tvTotalBookings.setText(String.valueOf(bookings));

        // 🔹 Reclamations stats
        int totalRecl = reclamationDao.countAll();
        int pendingRecl = reclamationDao.countPending();

        tvTotalReclamations.setText(String.valueOf(totalRecl));
        tvPendingReclamations.setText(String.valueOf(pendingRecl));
    }

    private int countBookings() {
        EventDbHelper helper = new EventDbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + EventDbHelper.TABLE_BOOKINGS, null);
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            c.close();
        }
    }

    private void loadUsers() {
        List<User> users = userDao.getAll();
        AdminUserAdapter adapter = new AdminUserAdapter(users, this);
        rvUsers.setAdapter(adapter);
    }

    @Override
    public void onRoleChange(User user, String newRole) {
        user.role = newRole;
        userDao.update(user);
        loadUsers();
        loadStats();
    }

    @Override
    public void onDelete(User user) {
        userDao.delete(user.id);
        loadUsers();
        loadStats();
    }
}
