package com.example.eventapplication.ui;

import android.content.Intent;
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
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.BookingsRepository;
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;

import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private RecyclerView rvMyEvents;
    private EventAdapter adapter;
    private BookingsRepository repo;
    private EventDao dao;
    private TextView tvMyEventsCount;
    private TextView tvEmptyMyEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_my_events, container, false);

        rvMyEvents = v.findViewById(R.id.rvMyEvents);
        tvMyEventsCount = v.findViewById(R.id.tvMyEventsCount);
        tvEmptyMyEvents = v.findViewById(R.id.tvEmptyMyEvents);
        rvMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        SessionManager session = new SessionManager(getContext());
        repo = new BookingsRepository(getContext(), session.getEmail());
        dao = new EventDao(getContext());

        loadBookedEvents();

        return v;
    }

    private void loadBookedEvents() {
        List<Long> ids = repo.getAllBookedIds();
        List<Event> events = new ArrayList<>();

        for (long id : ids) {
            Event e = dao.getById(id);
            if (e != null) events.add(e);
        }

        // Update count label
        if (tvMyEventsCount != null) {
            int count = events.size();
            String label = count + (count == 1 ? " event booked" : " events booked");
            tvMyEventsCount.setText(label);
        }

        // Empty state vs list visibility
        if (tvEmptyMyEvents != null && rvMyEvents != null) {
            if (events.isEmpty()) {
                tvEmptyMyEvents.setVisibility(View.VISIBLE);
                rvMyEvents.setVisibility(View.GONE);
            } else {
                tvEmptyMyEvents.setVisibility(View.GONE);
                rvMyEvents.setVisibility(View.VISIBLE);
            }
        }

        adapter = new EventAdapter(events, event -> {
            if (event == null) return;
            Intent i = new Intent(requireContext(), EventDetailActivity.class);
            i.putExtra("event_id", event.id);
            startActivity(i);
        });

        rvMyEvents.setAdapter(adapter);
    }
}
