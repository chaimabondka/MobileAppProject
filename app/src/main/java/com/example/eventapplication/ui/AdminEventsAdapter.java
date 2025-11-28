package com.example.eventapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Event;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.Holder> {

    public interface OnEventAdminActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    private final List<Event> events;
    private final OnEventAdminActionListener listener;

    public AdminEventsAdapter(List<Event> events, OnEventAdminActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void updateData(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Event e = events.get(position);
        h.tvTitle.setText(e.title);
        h.tvSubtitle.setText(e.subtitle);

        String dateText = e.date;
        if (dateText == null || dateText.isEmpty()) {
            if (e.eventTimestamp > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                dateText = sdf.format(e.eventTimestamp);
            } else {
                dateText = "No date";
            }
        }
        h.tvDate.setText(dateText);

        String capacity;
        if (e.maxPlaces > 0) {
            capacity = e.availablePlaces + " / " + e.maxPlaces + " places left";
        } else {
            capacity = "No capacity limit";
        }
        h.tvCapacity.setText(capacity);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(e);
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(e);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvDate, tvCapacity;
        MaterialButton btnEdit, btnDelete;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminEventTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminEventSubtitle);
            tvDate = itemView.findViewById(R.id.tvAdminEventDate);
            tvCapacity = itemView.findViewById(R.id.tvAdminEventCapacity);
            btnEdit = itemView.findViewById(R.id.btnAdminEditEvent);
            btnDelete = itemView.findViewById(R.id.btnAdminDeleteEvent);
        }
    }
}
