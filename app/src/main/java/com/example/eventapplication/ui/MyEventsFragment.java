package com.example.eventapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_my_events, container, false);

        rvMyEvents = v.findViewById(R.id.rvMyEvents);
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

        adapter = new EventAdapter(events, event -> {
            // Open event detail
        });

        rvMyEvents.setAdapter(adapter);
    }
}
