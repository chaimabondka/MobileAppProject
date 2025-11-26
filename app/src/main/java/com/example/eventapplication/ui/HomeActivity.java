package com.example.eventapplication.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.eventapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new DiscoverFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment frag = null;
            int id = item.getItemId();
            if (id == R.id.nav_discover) {
                frag = new DiscoverFragment();
            } else if (id == R.id.nav_my_events) {
                frag = new MyEventsFragment();
            } else if (id == R.id.nav_profile) {
                frag = new ProfileFragment();
            }
            if (frag != null) {
                loadFragment(frag);
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
