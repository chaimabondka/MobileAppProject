package com.example.eventapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.EventDao;
import com.example.eventapplication.data.EventDbHelper;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public class AdminDashboardFragment extends Fragment implements AdminUserAdapter.OnRoleChangeListener {

    private TextView tvTotalUsers, tvTotalEvents, tvTotalBookings;
    private TextView tvAdmins, tvOrganizers, tvAttendees;
    private RecyclerView rvUsers;
    private UserDao userDao;
    private EventDao eventDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDao = new UserDao(requireContext());
        eventDao = new EventDao(requireContext());

        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        tvAdmins = view.findViewById(R.id.tvAdmins);
        tvOrganizers = view.findViewById(R.id.tvOrganizers);
        tvAttendees = view.findViewById(R.id.tvAttendees);
        rvUsers = view.findViewById(R.id.rvUsers);

        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

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

        int admins = userDao.countByRole("ADMIN");
        int organizers = userDao.countByRole("ORGANIZER");
        int attendees = userDao.countByRole("ATTENDEE");

        tvAdmins.setText(String.valueOf(admins));
        tvOrganizers.setText(String.valueOf(organizers));
        tvAttendees.setText(String.valueOf(attendees));
    }

    private int countBookings() {
        EventDbHelper helper = new EventDbHelper(requireContext());
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
