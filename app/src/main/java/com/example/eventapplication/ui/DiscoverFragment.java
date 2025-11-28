package com.example.eventapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;
import com.example.eventapplication.data.LikesRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiscoverFragment extends Fragment {

    private RecyclerView rvSlider, rvEvents;
    private EventAdapter verticalAdapter;
    private EventSliderAdapter sliderAdapter;
    private EventDao dao;
    private LikesRepository likesRepo;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();
    private final List<Event> sliderEvents = new ArrayList<>();

    private EditText etSearch;
    private ChipGroup chipContainer;
    private Chip chipAll, chipToday, chipWeek, chipPopular, chipDate;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_TODAY = 1;
    private static final int FILTER_WEEK = 2;
    private static final int FILTER_POPULAR = 3;
    private static final int FILTER_DATE = 4;
    private int currentFilter = FILTER_ALL;

    private long selectedDateStart = 0L;
    private long selectedDateEnd = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_discover, container, false);

        rvSlider = v.findViewById(R.id.rvSlider);
        rvEvents = v.findViewById(R.id.rvEvents);
        etSearch = v.findViewById(R.id.etSearch);
        chipContainer = v.findViewById(R.id.chipContainer);
        chipAll = v.findViewById(R.id.chipAll);
        chipToday = v.findViewById(R.id.chipToday);
        chipWeek = v.findViewById(R.id.chipWeek);
        chipPopular = v.findViewById(R.id.chipPopular);
        chipDate = v.findViewById(R.id.chipDate);
        FloatingActionButton fabAdd = v.findViewById(R.id.fabAddEvent);

        dao = new EventDao(requireContext());
        likesRepo = new LikesRepository(requireContext());

        // 1) Layout managers
        rvSlider.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvEvents.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 2) Adapters created ONCE with the lists
        sliderAdapter = new EventSliderAdapter(sliderEvents, this::openEventDetail);
        rvSlider.setAdapter(sliderAdapter);

        verticalAdapter = new EventAdapter(filteredEvents, this::openEventDetail);
        rvEvents.setAdapter(verticalAdapter);

        // Chips: date / popularity filters for main list
        if (chipAll != null) chipAll.setChecked(true);
        if (chipContainer != null) {
            chipContainer.setOnCheckedStateChangeListener((group, checkedIds) -> {
                int filter = FILTER_ALL;
                int id = checkedIds.isEmpty() ? R.id.chipAll : checkedIds.get(0);
                if (id == R.id.chipToday) filter = FILTER_TODAY;
                else if (id == R.id.chipWeek) filter = FILTER_WEEK;
                else if (id == R.id.chipPopular) filter = FILTER_POPULAR;
                else if (id == R.id.chipDate) filter = FILTER_DATE;
                else filter = FILTER_ALL;

                currentFilter = filter;
                String q = etSearch != null ? etSearch.getText().toString() : "";

                if (filter == FILTER_DATE) {
                    showDateFilterDialog(q);
                } else {
                    // Clear custom date range when leaving date filter
                    selectedDateStart = 0L;
                    selectedDateEnd = 0L;
                    applyFilter(q);
                    verticalAdapter.notifyDataSetChanged();
                }
            });
        }

        // Explicitly open calendar when By date chip is tapped
        if (chipDate != null) {
            chipDate.setOnClickListener(v1 -> {
                chipDate.setChecked(true);
                currentFilter = FILTER_DATE;
                String q = etSearch != null ? etSearch.getText().toString() : "";
                showDateFilterDialog(q);
            });
        }

        // 3) Initial load
        loadEventsFromDb();

        // 4) FAB -> Add new event
        fabAdd.setOnClickListener(view -> {
            Intent i = new Intent(requireContext(), AddEditEventActivity.class);
            startActivity(i);
        });

        // 5) Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // When coming back from Add/Edit, reload from DB
        loadEventsFromDb();
    }

    private void loadEventsFromDb() {
        List<Event> dbEvents = dao.getAll();

        // Update master list
        allEvents.clear();
        allEvents.addAll(dbEvents);

        // Build slider list: only events where likes > dislikes
        sliderEvents.clear();
        for (Event e : allEvents) {
            int likes = likesRepo.getLikesCount(e.id);
            int dislikes = likesRepo.getDislikesCount(e.id);
            if (likes > dislikes) {
                sliderEvents.add(e);
            }
        }

        // Re-apply current search + chip filter to filteredEvents
        String q = etSearch != null ? etSearch.getText().toString() : "";
        applyFilter(q);

        // Notify adapters that data in lists changed
        sliderAdapter.notifyDataSetChanged();
        verticalAdapter.notifyDataSetChanged();
    }

    private void filterEvents(String query) {
        applyFilter(query);
        verticalAdapter.notifyDataSetChanged();
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        filteredEvents.clear();

        long now = System.currentTimeMillis();

        // Normalize "today" to start of day
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfToday = cal.getTimeInMillis();
        long startOfTomorrow = startOfToday + 24L * 60L * 60L * 1000L;
        long endOfWeek = startOfToday + 7L * 24L * 60L * 60L * 1000L;

        for (Event e : allEvents) {
            boolean matchesSearch = q.isEmpty()
                    || (e.title != null && e.title.toLowerCase().contains(q))
                    || (e.subtitle != null && e.subtitle.toLowerCase().contains(q))
                    || (e.location != null && e.location.toLowerCase().contains(q));

            if (!matchesSearch) continue;

            boolean matchesFilter = true;
            long ts = e.eventTimestamp;

            // Skip date-based filters if timestamp is invalid
            boolean hasValidTs = ts > 0L;

            // Normalize event timestamp to start-of-day bucket
            long eventDayStart = 0L;
            if (hasValidTs) {
                Calendar evCal = Calendar.getInstance();
                evCal.setTimeInMillis(ts);
                evCal.set(Calendar.HOUR_OF_DAY, 0);
                evCal.set(Calendar.MINUTE, 0);
                evCal.set(Calendar.SECOND, 0);
                evCal.set(Calendar.MILLISECOND, 0);
                eventDayStart = evCal.getTimeInMillis();
            }

            switch (currentFilter) {
                case FILTER_TODAY:
                    matchesFilter = hasValidTs && (eventDayStart == startOfToday);
                    break;
                case FILTER_WEEK:
                    matchesFilter = hasValidTs && (eventDayStart >= startOfToday && eventDayStart < endOfWeek);
                    break;
                case FILTER_DATE:
                    if (selectedDateStart > 0L && selectedDateEnd > 0L && hasValidTs) {
                        matchesFilter = (eventDayStart >= selectedDateStart && eventDayStart < selectedDateEnd);
                    } else {
                        matchesFilter = true;
                    }
                    break;
                case FILTER_POPULAR:
                    int likes = likesRepo.getLikesCount(e.id);
                    int dislikes = likesRepo.getDislikesCount(e.id);
                    matchesFilter = likes > dislikes;
                    break;
                case FILTER_ALL:
                default:
                    matchesFilter = true;
                    break;
            }

            if (matchesFilter) {
                filteredEvents.add(e);
            }
        }
    }

    private void showDateFilterDialog(String currentQuery) {
        final Calendar cal = Calendar.getInstance();

        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    selectedDateStart = cal.getTimeInMillis();
                    selectedDateEnd = selectedDateStart + 24L * 60L * 60L * 1000L;

                    applyFilter(currentQuery);
                    verticalAdapter.notifyDataSetChanged();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        // Limit selection from today to today + 30 days
        Calendar minCal = Calendar.getInstance();
        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.DAY_OF_YEAR, 30);

        dlg.getDatePicker().setMinDate(minCal.getTimeInMillis());
        dlg.getDatePicker().setMaxDate(maxCal.getTimeInMillis());

        dlg.setOnCancelListener(dialog -> {
            // If user cancels, revert to "All" filter
            currentFilter = FILTER_ALL;
            if (chipAll != null) chipAll.setChecked(true);
            applyFilter(currentQuery);
            verticalAdapter.notifyDataSetChanged();
        });

        dlg.show();
    }

    private void openEventDetail(Event event) {
        if (event == null) return;
        Intent i = new Intent(requireContext(), EventDetailActivity.class);
        i.putExtra("event_id", event.id);
        startActivity(i);
    }
}
