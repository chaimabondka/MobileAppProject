package com.example.eventapplication.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Reclamation;
import com.example.eventapplication.data.ReclamationDao;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReclamationAdminActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ReclamationDao dao;
    private final List<Reclamation> items = new ArrayList<>();
    private AdminAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamation_admin);

        dao = new ReclamationDao(this);
        rv = findViewById(R.id.rvReclamationsAdmin);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminAdapter(items, this::onReclamationClick);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        items.clear();
        items.addAll(dao.getAll());
        adapter.notifyDataSetChanged();
    }

    private void onReclamationClick(Reclamation r) {
        // Simple dialog to change status + response
        final String[] statuses = {
                Reclamation.STATUS_PENDING,
                Reclamation.STATUS_IN_PROGRESS,
                Reclamation.STATUS_RESOLVED
        };

        int currentIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(r.status)) {
                currentIndex = i;
                break;
            }
        }

        final int[] selectedIndex = {currentIndex};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update reclamation");

        builder.setSingleChoiceItems(statuses, currentIndex,
                (dialog, which) -> selectedIndex[0] = which);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Admin response");
        input.setText(r.response);
        input.setMinLines(3);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newStatus = statuses[selectedIndex[0]];
            String response = input.getText().toString().trim();
            dao.updateStatusAndResponse(r.id, newStatus, response);
            reload();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ------------ Adapter ------------

    private static class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {

        interface OnItemClick {
            void onClick(Reclamation r);
        }

        private final List<Reclamation> data;
        private final OnItemClick listener;

        AdminAdapter(List<Reclamation> data, OnItemClick listener) {
            this.data = data;
            this.listener = listener;
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reclamation_admin, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Reclamation r = data.get(position);
            holder.title.setText(r.title);
            holder.user.setText("User: " + r.userId);
            String dateStr = DateFormat.getDateTimeInstance().format(new Date(r.createdAt));
            holder.status.setText(r.status + " · " + dateStr);
            holder.desc.setText(r.description);

            holder.itemView.setOnClickListener(v -> listener.onClick(r));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, user, status, desc;
            VH(android.view.View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvReclTitle);
                user  = itemView.findViewById(R.id.tvReclUser);
                status= itemView.findViewById(R.id.tvReclStatus);
                desc  = itemView.findViewById(R.id.tvReclDescription);
            }
        }
    }
}
