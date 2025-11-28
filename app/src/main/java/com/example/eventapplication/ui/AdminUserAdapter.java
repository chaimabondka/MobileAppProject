package com.example.eventapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.Holder> {

    public interface OnRoleChangeListener {
        void onRoleChange(User user, String newRole);
        void onDelete(User user);
    }

    private final List<User> users;
    private final OnRoleChangeListener listener;

    public AdminUserAdapter(List<User> users, OnRoleChangeListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = users.get(position);
        h.tvName.setText(u.name);
        h.tvEmail.setText(u.email);
        h.tvRole.setText(u.role);

        h.btnMakeAdmin.setOnClickListener(v -> {
            if (listener != null) listener.onRoleChange(u, "ADMIN");
        });
        h.btnMakeOrganizer.setOnClickListener(v -> {
            if (listener != null) listener.onRoleChange(u, "ORGANIZER");
        });
        h.btnMakeAttendee.setOnClickListener(v -> {
            if (listener != null) listener.onRoleChange(u, "ATTENDEE");
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(u);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        MaterialButton btnMakeAdmin, btnMakeOrganizer, btnMakeAttendee, btnDelete;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnMakeAdmin = itemView.findViewById(R.id.btnMakeAdmin);
            btnMakeOrganizer = itemView.findViewById(R.id.btnMakeOrganizer);
            btnMakeAttendee = itemView.findViewById(R.id.btnMakeAttendee);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
