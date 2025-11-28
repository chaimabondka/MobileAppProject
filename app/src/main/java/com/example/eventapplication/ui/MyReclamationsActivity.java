package com.example.eventapplication.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.Reclamation;
import com.example.eventapplication.data.ReclamationDao;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyReclamationsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvEmpty;
    private ReclamationDao dao;
    private SessionManager session;
    private final List<Reclamation> items = new ArrayList<>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reclamations);

        dao = new ReclamationDao(this);
        session = new SessionManager(this);

        rv = findViewById(R.id.rvMyReclamations);
        tvEmpty = findViewById(R.id.tvEmptyReclamations);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter(items);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        String userId = session.getUserId();
        items.clear();
        if (userId != null) {
            items.addAll(dao.getForUser(userId));
        }
        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            rv.setVisibility(android.view.View.GONE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            rv.setVisibility(android.view.View.VISIBLE);
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.VH> {

        private final List<Reclamation> data;

        MyAdapter(List<Reclamation> data) {
            this.data = data;
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reclamation_user, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Reclamation r = data.get(position);
            holder.title.setText(r.title);
            holder.status.setText(r.status);
            String dateStr = DateFormat.getDateTimeInstance().format(new Date(r.createdAt));
            holder.date.setText(dateStr);
            holder.desc.setText(r.description);

            if (r.response != null && !r.response.trim().isEmpty()) {
                holder.response.setText(r.response);
                holder.response.setVisibility(android.view.View.VISIBLE);
            } else {
                holder.response.setVisibility(android.view.View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, status, date, desc, response;

            VH(android.view.View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvReclTitleUser);
                status = itemView.findViewById(R.id.tvReclStatusUser);
                date = itemView.findViewById(R.id.tvReclDateUser);
                desc = itemView.findViewById(R.id.tvReclDescriptionUser);
                response = itemView.findViewById(R.id.tvReclResponseUser);
            }
        }
    }
}
