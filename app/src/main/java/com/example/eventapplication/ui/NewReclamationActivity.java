package com.example.eventapplication.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.Reclamation;
import com.example.eventapplication.data.ReclamationDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

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
        etTitle.setError(null);
        etDescription.setError(null);

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        boolean hasError = false;
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            hasError = true;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            hasError = true;
        }

        if (hasError) {
            showSnack("Please fill in all required fields");
            return;
        }

        String userId = session.getUserId();
        if (userId == null) {
            showSnack("You must be logged in.");
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
            showSnack("Reclamation sent");
            finish();
        } else {
            showSnack("Error while saving");
        }
    }

    private void showSnack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }
}
