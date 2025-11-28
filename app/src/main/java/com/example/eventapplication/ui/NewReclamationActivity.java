package com.example.eventapplication.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.Reclamation;
import com.example.eventapplication.data.ReclamationDao;
import com.google.android.material.button.MaterialButton;

public class NewReclamationActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private ReclamationDao dao;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reclamation);

        dao = new ReclamationDao(this);
        session = new SessionManager(this);

        etTitle = findViewById(R.id.etReclTitle);
        etDescription = findViewById(R.id.etReclDescription);
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReclamation);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = session.getUserId();
        if (userId == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Reclamation r = new Reclamation();
        r.userId = userId;
        r.title = title;
        r.description = description;
        r.status = Reclamation.STATUS_PENDING;
        r.createdAt = System.currentTimeMillis();
        r.response = "";

        long id = dao.insert(r);
        if (id > 0) {
            Toast.makeText(this, "Reclamation sent", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error while saving", Toast.LENGTH_SHORT).show();
        }
    }
}
