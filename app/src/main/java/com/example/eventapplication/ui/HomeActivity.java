package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Determine if current user is ADMIN
        SessionManager session = new SessionManager(this);
        UserDao userDao = new UserDao(this);

        boolean isAdmin = session.isAdmin();

        // Fallback for older sessions without role stored
        if (!isAdmin) {
            String email = session.getEmail();
            if (email != null) {
                User u = userDao.findByEmail(email);
                isAdmin = (u != null && "ADMIN".equalsIgnoreCase(u.role));
            }
        }

        final boolean isAdminUser = isAdmin;

        // Hide admin item for non-admin users
        Menu menu = bottomNav.getMenu();
        if (!isAdmin) {
            menu.findItem(R.id.nav_admin).setVisible(false);
        }

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new DiscoverFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_discover) {
                loadFragment(new DiscoverFragment());
                return true;
            } else if (id == R.id.nav_my_events) {
                loadFragment(new MyEventsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            } else if (id == R.id.nav_admin && isAdminUser) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.homeFragmentContainer, fragment)
                .commit();
    }
}
