package com.example.eventapplication.ui;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private RecyclerView rvSlider, rvEvents;
    private EventAdapter verticalAdapter;
    private EventSliderAdapter sliderAdapter;
    private EventDao dao;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();

    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_discover, container, false);

        rvSlider = v.findViewById(R.id.rvSlider);
        rvEvents = v.findViewById(R.id.rvEvents);
        etSearch = v.findViewById(R.id.etSearch);
        FloatingActionButton fabAdd = v.findViewById(R.id.fabAddEvent);

        dao = new EventDao(requireContext());

        // 1) Layout managers
        rvSlider.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Adapters created ONCE with the lists
        sliderAdapter = new EventSliderAdapter(allEvents, this::openEventDetail);
        rvSlider.setAdapter(sliderAdapter);

        verticalAdapter = new EventAdapter(filteredEvents, this::openEventDetail);
        rvEvents.setAdapter(verticalAdapter);

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

        // Re-apply current search filter to filteredEvents
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

        if (q.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            for (Event e : allEvents) {
                if ((e.title != null && e.title.toLowerCase().contains(q)) ||
                        (e.subtitle != null && e.subtitle.toLowerCase().contains(q)) ||
                        (e.location != null && e.location.toLowerCase().contains(q))) {

                    filteredEvents.add(e);
                }
            }
        }
    }

    private void openEventDetail(Event event) {
        if (event == null) return;
        Intent i = new Intent(requireContext(), EventDetailActivity.class);
        i.putExtra("event_id", event.id);
        startActivity(i);
    }
}
