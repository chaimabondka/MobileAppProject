package com.example.eventapplication.ui;

import android.content.Intent;
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
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminManageEventsFragment extends Fragment {

    private RecyclerView rvEvents;
    private EventDao eventDao;
    private AdminEventsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_manage_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventDao = new EventDao(requireContext());

        rvEvents = view.findViewById(R.id.rvAdminEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        MaterialButton btnAdd = view.findViewById(R.id.btnAddEventAdmin);
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), AddEditEventActivity.class);
            startActivity(i);
        });

        adapter = new AdminEventsAdapter(new ArrayList<>(), new AdminEventsAdapter.OnEventAdminActionListener() {
            @Override
            public void onEdit(Event event) {
                Intent i = new Intent(requireContext(), AddEditEventActivity.class);
                i.putExtra("event_id", event.id);
                startActivity(i);
            }

            @Override
            public void onDelete(Event event) {
                eventDao.delete(event.id);
                loadEvents();
            }
        });
        rvEvents.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        List<Event> events = eventDao.getAll();
        adapter.updateData(events);
    }
}
