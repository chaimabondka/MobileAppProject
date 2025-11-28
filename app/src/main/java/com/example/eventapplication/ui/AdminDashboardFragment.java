package com.example.eventapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.eventapplication.R;
import com.example.eventapplication.ui.ReclamationAdminActivity;
import com.example.eventapplication.data.EventDao;
import com.example.eventapplication.data.EventDbHelper;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.example.eventapplication.data.ReclamationDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalEvents, tvTotalBookings;
    private TextView tvAdmins, tvOrganizers, tvAttendees;
    private UserDao userDao;
    private EventDao eventDao;
    private TextView tvTotalReclamations, tvPendingReclamations;
    private ReclamationDao reclamationDao;

    // Chart views
    private BarChart barChartMonthly;
    private TextView tvCurrentMonth;
    private ImageButton btnPrevMonth, btnNextMonth;
    private Calendar currentMonth;

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
        reclamationDao = new ReclamationDao(requireContext());

        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        tvAdmins = view.findViewById(R.id.tvAdmins);
        tvOrganizers = view.findViewById(R.id.tvOrganizers);
        tvAttendees = view.findViewById(R.id.tvAttendees);

        tvTotalReclamations = view.findViewById(R.id.tvTotalReclamations);
        tvPendingReclamations = view.findViewById(R.id.tvPendingReclamations);

        // Chart + month selector
        barChartMonthly = view.findViewById(R.id.barChartMonthly);
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        View btnManageReclamations = view.findViewById(R.id.btnManageReclamations);
        if (btnManageReclamations != null) {
            btnManageReclamations.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), ReclamationAdminActivity.class);
                startActivity(i);
            });
        }

        // Init month to current
        currentMonth = Calendar.getInstance();

        setupChart();

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentMonth.add(Calendar.MONTH, -1);
                updateMonthAndChart();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentMonth.add(Calendar.MONTH, 1);
                updateMonthAndChart();
            });
        }

        loadStats();
        updateMonthAndChart();
    }

    private void loadStats() {
        int users = userDao.countUsers();
        int events = eventDao.countEvents();
        int bookings = countBookings();

        tvTotalUsers.setText(String.valueOf(users));
        tvTotalEvents.setText(String.valueOf(events));
        tvTotalBookings.setText(String.valueOf(bookings));

        int totalRecl = reclamationDao.countAll();
        int pendingRecl = reclamationDao.countPending();

        if (tvTotalReclamations != null) {
            tvTotalReclamations.setText(String.valueOf(totalRecl));
        }
        if (tvPendingReclamations != null) {
            tvPendingReclamations.setText(String.valueOf(pendingRecl));
        }

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

    private void setupChart() {
        if (barChartMonthly == null) return;

        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.setDrawBarShadow(false);
        barChartMonthly.setPinchZoom(false);

        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(2);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) return "Events";
                if (value == 1f) return "Reservations";
                return "";
            }
        });

        YAxis leftAxis = barChartMonthly.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        barChartMonthly.getAxisRight().setEnabled(false);

        barChartMonthly.getLegend().setEnabled(false);
    }

    private void updateMonthAndChart() {
        if (currentMonth == null || barChartMonthly == null || tvCurrentMonth == null) return;

        String monthName = new DateFormatSymbols().getShortMonths()[currentMonth.get(Calendar.MONTH)];
        int year = currentMonth.get(Calendar.YEAR);
        tvCurrentMonth.setText(monthName + " " + year);

        // Compute start and end of month in millis
        Calendar startCal = (Calendar) currentMonth.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.MONTH, 1);

        long startTs = startCal.getTimeInMillis();
        long endTs = endCal.getTimeInMillis();

        // Count events and reservations for events that fall in this month.
        // We use event_timestamp when available, otherwise we fall back to parsing the "date" string.
        EventDbHelper helper = new EventDbHelper(requireContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        int eventsCount = 0;
        int reservationsCount = 0;

        // Collect all matching event IDs for this month
        Set<Integer> matchingEventIds = new HashSet<>();

        Cursor ce = db.rawQuery(
                "SELECT id, event_timestamp, date FROM " + EventDbHelper.TABLE_EVENTS,
                null);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

            while (ce.moveToNext()) {
                int id = ce.getInt(0);
                long ts = ce.getLong(1);
                String dateStr = ce.getString(2);

                long effectiveTs = ts;
                if (effectiveTs <= 0 && dateStr != null && !dateStr.isEmpty()) {
                    try {
                        effectiveTs = sdf.parse(dateStr).getTime();
                    } catch (ParseException ignored) {
                    }
                }

                if (effectiveTs >= startTs && effectiveTs < endTs) {
                    matchingEventIds.add(id);
                }
            }
        } finally {
            ce.close();
        }

        eventsCount = matchingEventIds.size();

        if (!matchingEventIds.isEmpty()) {
            StringBuilder ids = new StringBuilder();
            for (Integer id : matchingEventIds) {
                if (ids.length() > 0) ids.append(',');
                ids.append(id);
            }
            String inClause = ids.toString();
            Cursor cb = db.rawQuery(
                    "SELECT COUNT(*) FROM " + EventDbHelper.TABLE_BOOKINGS + " WHERE event_id IN (" + inClause + ")",
                    null);
            try {
                if (cb.moveToFirst()) {
                    reservationsCount = cb.getInt(0);
                }
            } finally {
                cb.close();
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, eventsCount));
        entries.add(new BarEntry(1f, reservationsCount));

        BarDataSet dataSet = new BarDataSet(entries, "");
        int[] colors = new int[]{
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorSecondary)
        };
        dataSet.setColors(colors);
        dataSet.setValueTextColor(getResources().getColor(R.color.colorOnSurface));
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartMonthly.setData(data);
        barChartMonthly.setFitBars(true);
        barChartMonthly.invalidate();
        barChartMonthly.animateY(800);
    }

}
