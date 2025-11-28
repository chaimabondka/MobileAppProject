package com.example.eventapplication.ui;

import android.net.Uri;
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

public class EventSliderAdapter extends RecyclerView.Adapter<EventSliderAdapter.Holder> {

    public interface OnEventClick {
        void onClick(Event event);
    }

    private final List<Event> list;
    private final OnEventClick listener;

    public EventSliderAdapter(List<Event> list, OnEventClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_slider, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Event e = list.get(position);
        h.bind(e, listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvDate, tvTitle;
        ImageView ivImage;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvSliderDate);
            tvTitle = itemView.findViewById(R.id.tvSliderTitle);
            ivImage = itemView.findViewById(R.id.ivSliderImage);
        }

        void bind(Event e, OnEventClick listener) {
            tvDate.setText(e.date != null ? e.date : "");
            tvTitle.setText(e.title != null ? e.title : "");

            if (e.imageUri != null && !e.imageUri.isEmpty()) {
                try {
                    ivImage.setImageURI(Uri.parse(e.imageUri));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (e.imageResId != 0) {
                        ivImage.setImageResource(e.imageResId);
                    } else {
                        ivImage.setImageResource(R.drawable.ic_event_placeholder);
                    }
                }
            } else if (e.imageResId != 0) {
                ivImage.setImageResource(e.imageResId);
            } else {
                ivImage.setImageResource(R.drawable.ic_event_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(e);
            });
        }
    }
}
