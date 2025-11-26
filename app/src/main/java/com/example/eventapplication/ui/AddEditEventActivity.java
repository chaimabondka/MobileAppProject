package com.example.eventapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etTitle, etDate, etSubtitle, etLocation, etDescription, etCapacity;
    private TextView tvFormTitle;
    private Button btnPickLocation, btnPickImage;

    private ImageView ivEventImagePreview;

    private EventDao dao;
    private long eventId = -1;

    private static final int REQ_PICK_LOCATION = 1001;
    private static final int REQ_PICK_IMAGE    = 1002;

    private double eventLat = 0;
    private double eventLng = 0;
    private long eventTimestamp = 0L;
    private String imageUri; // string form of gallery Uri

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        dao = new EventDao(this);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etTitle = findViewById(R.id.etEventTitle);
        etDate = findViewById(R.id.etEventDate);
        etSubtitle = findViewById(R.id.etEventSubtitle);
        etLocation = findViewById(R.id.etEventLocation);
        etDescription = findViewById(R.id.etEventDescription);
        etCapacity = findViewById(R.id.etEventCapacity);

        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnPickImage = findViewById(R.id.btnPickImage);
        Button btnSave = findViewById(R.id.btnSaveEvent);

        ivEventImagePreview = findViewById(R.id.ivEventImagePreview);

        // Load event if editing
        if (getIntent() != null && getIntent().hasExtra("event_id")) {
            eventId = getIntent().getLongExtra("event_id", -1);
            if (eventId != -1) {
                tvFormTitle.setText("Edit Event");
                loadEvent(eventId);
            }
        }

        // DATE PICKER
        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePicker();
        });

        // PICK LOCATION
        btnPickLocation.setOnClickListener(v -> {
            Intent i = new Intent(this, LocationPickerActivity.class);
            startActivityForResult(i, REQ_PICK_LOCATION);
        });

        // PICK IMAGE FROM GALLERY
        btnPickImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            startActivityForResult(pick, REQ_PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth);
                    eventTimestamp = cal.getTimeInMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    etDate.setText(sdf.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    private void loadEvent(long id) {
        Event e = dao.getById(id);
        if (e == null) return;

        etTitle.setText(e.title);
        etDate.setText(e.date);
        etSubtitle.setText(e.subtitle);
        etLocation.setText(e.location);
        etDescription.setText(e.description);

        if (e.maxPlaces > 0) {
            etCapacity.setText(String.valueOf(e.maxPlaces));
        }

        eventLat = e.lat;
        eventLng = e.lng;
        eventTimestamp = e.eventTimestamp;
        imageUri = e.imageUri;

        // 🔐 SAFER IMAGE LOADING (no crash if URI is not readable)
        if (imageUri != null && !imageUri.isEmpty()) {
            try {
                ivEventImagePreview.setImageURI(android.net.Uri.parse(imageUri));
            } catch (Exception ex) {
                ex.printStackTrace();
                ivEventImagePreview.setImageResource(R.drawable.ic_event_placeholder);
            }
        } else {
            ivEventImagePreview.setImageResource(R.drawable.ic_event_placeholder);
        }
    }


    private void saveEvent() {
        String title = etTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String capacityStr = etCapacity.getText().toString().trim();
        int maxPlaces = 0;
        if (!TextUtils.isEmpty(capacityStr)) {
            try {
                maxPlaces = Integer.parseInt(capacityStr);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, "Invalid capacity, using 0", Toast.LENGTH_SHORT).show();
            }
        }

        Event e;
        Event existing = null;
        if (eventId != -1) {
            existing = dao.getById(eventId);
        }
        e = new Event();
        e.id = eventId;
        e.title = title;
        e.date = etDate.getText().toString().trim();
        e.subtitle = etSubtitle.getText().toString().trim();
        e.location = etLocation.getText().toString().trim();
        e.description = etDescription.getText().toString().trim();
        e.lat = eventLat;
        e.lng = eventLng;
        e.eventTimestamp = eventTimestamp;
        e.maxPlaces = maxPlaces;

        // If creating: available = maxPlaces
        // If editing: keep existing remaining places if we have them
        if (eventId == -1) {
            e.availablePlaces = maxPlaces;
        } else if (existing != null) {
            e.availablePlaces = existing.availablePlaces;
        }

        e.imageUri = imageUri;

        if (eventId == -1) {
            long newId = dao.insert(e);
            if (newId > 0) {
                Toast.makeText(this, "Event created", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show();
            }
        } else {
            int updated = dao.update(e);
            if (updated > 0) {
                Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating event", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQ_PICK_LOCATION) {
            eventLat = data.getDoubleExtra("lat", 0);
            eventLng = data.getDoubleExtra("lng", 0);
            String addr = data.getStringExtra("address");

            if (addr != null && !addr.isEmpty()) {
                etLocation.setText(addr);
            } else {
                etLocation.setText(eventLat + ", " + eventLng);
            }

        } else if (requestCode == REQ_PICK_IMAGE) {
            android.net.Uri uri = data.getData();
            if (uri != null) {
                // Persist read permission so we can still load it later (edit screen)
                final int flags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(uri, flags);
                } catch (Exception ignored) {
                    // If it fails, we still try to display it now
                }

                imageUri = uri.toString();
                ivEventImagePreview.setImageURI(uri);
            }
        }
    }

}
