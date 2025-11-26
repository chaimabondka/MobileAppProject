package com.example.eventapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> events;
    private final OnEventClickListener listener;

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }
    public void updateData(List<Event> newList) {
        events.clear();
        events.addAll(newList);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event e = events.get(position);
        holder.title.setText(e.title);
        holder.date.setText(e.date);
        holder.subtitle.setText(e.subtitle);
        holder.image.setImageResource(e.imageResId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, date, subtitle;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgEvent);
            title = itemView.findViewById(R.id.tvEventTitle);
            date = itemView.findViewById(R.id.tvEventDate);
            subtitle = itemView.findViewById(R.id.tvEventSubtitle);
        }
    }
}
