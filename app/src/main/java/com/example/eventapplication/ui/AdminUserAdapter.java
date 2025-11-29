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
        h.chipUserRole.setText(u.role);

        // Set checked state based on current role
        h.chipAdmin.setChecked(u.role.equals("ADMIN"));
        h.chipOrganizer.setChecked(u.role.equals("ORGANIZER"));
        h.chipAttendee.setChecked(u.role.equals("ATTENDEE"));

        h.chipAdmin.setOnClickListener(v -> {
            if (listener != null) listener.onRoleChange(u, "ADMIN");
        });
        h.chipOrganizer.setOnClickListener(v -> {
            if (listener != null) listener.onRoleChange(u, "ORGANIZER");
        });
        h.chipAttendee.setOnClickListener(v -> {
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
        TextView tvName, tvEmail;
        com.google.android.material.chip.Chip chipUserRole, chipAdmin, chipOrganizer, chipAttendee;
        MaterialButton btnDelete;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            chipUserRole = itemView.findViewById(R.id.chipUserRole);
            chipAdmin = itemView.findViewById(R.id.chipAdmin);
            chipOrganizer = itemView.findViewById(R.id.chipOrganizer);
            chipAttendee = itemView.findViewById(R.id.chipAttendee);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
