package com.example.eventapplication.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.BookingsRepository;
import com.example.eventapplication.data.Comment;
import com.example.eventapplication.data.CommentsRepository;
import com.example.eventapplication.data.Event;
import com.example.eventapplication.data.EventDao;
import com.example.eventapplication.data.EventDbHelper;
import com.example.eventapplication.data.LikesRepository;
import com.example.eventapplication.data.Ticket;
import com.example.eventapplication.data.TicketDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import android.net.Uri;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private long eventId;
    private EventDao dao;
    private BookingsRepository bookingRepo;
    private CommentsRepository commentsRepo;
    private LikesRepository likesRepo;
    private TicketDao ticketDao;
    private Event event;
    private TextView tvCapacity;
    private MapView mapPreview;
    private Marker previewMarker;
    private android.widget.Button btnOpenInMaps;

    private SessionManager session;
    private String currentUserId;
    private String currentUserName;

    private ImageView ivEventImage;
    private ImageView ivTicketQr;
    private TextView tvTicketTitle;
    private MaterialButton btnBook, btnEdit, btnDelete, btnSendComment, btnTicketPdf;
    private TextView tvTitle, tvDate, tvSubtitle, tvLocation, tvDescription;
    private ImageButton btnLike, btnDislike;
    private TextView tvLikesCount, tvDislikesCount;
    private EditText etComment;
    private RecyclerView rvComments;

    private final List<Comment> comments = new ArrayList<>();
    private CommentsAdapter commentsAdapter;

    private int likesCount = 0;
    private int dislikesCount = 0;
    private boolean userLiked = false;
    private boolean userDisliked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Required by osmdroid: set a user agent to avoid being blocked
        Configuration.getInstance().setUserAgentValue(getPackageName());

        dao = new EventDao(this);
        session = new SessionManager(this);

        currentUserId = session.getUserId();
        currentUserName = session.getName();
        if (currentUserName == null) currentUserName = "Anonymous";

        bookingRepo = new BookingsRepository(this, session.getEmail());
        commentsRepo = new CommentsRepository(this);
        likesRepo = new LikesRepository(this);
        ticketDao = new TicketDao(this);

        bindViews();
        setupCommentsList();

        eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            showSnack("Event not found");
            finish();
            return;
        }

        // Open in external Maps app
        btnOpenInMaps.setOnClickListener(v -> {
            if (event == null || event.lat == 0 || event.lng == 0) {
                showSnack("Location not set for this event");
                return;
            }
            String uri = "geo:" + event.lat + "," + event.lng + "?q=" + event.lat + "," + event.lng +
                    "(" + event.title + ")";
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            startActivity(intent);
        });
        // 🔹 END BLOCK 🔹

        // Now load everything
        loadEvent();
        loadLikes();
        loadComments();
        updateButtonState();
        updateTicketQr();

        // Admin-only controls
        final boolean isAdmin = session.isAdmin();
        if (!isAdmin) {
            btnEdit.setVisibility(android.view.View.GONE);
            btnDelete.setVisibility(android.view.View.GONE);
        } else {
            btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(this, AddEditEventActivity.class);
                i.putExtra("event_id", eventId);
                startActivity(i);
            });

            btnDelete.setOnClickListener(v -> confirmDelete());
        }
        btnBook.setOnClickListener(v -> toggleBooking());
        btnLike.setOnClickListener(v -> onLikeClicked());
        btnDislike.setOnClickListener(v -> onDislikeClicked());
        btnSendComment.setOnClickListener(v -> onSendComment());

        if (btnTicketPdf != null) {
            btnTicketPdf.setOnClickListener(v -> generateTicketPdf());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadEvent();
        loadLikes();
        loadComments();
        updateButtonState();
    }

    private void bindViews() {
        ivEventImage = findViewById(R.id.ivEventImage);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDate = findViewById(R.id.tvDetailDate);
        tvSubtitle = findViewById(R.id.tvDetailSubtitle);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDescription = findViewById(R.id.tvDetailDescription);

        btnBook = findViewById(R.id.btnBook);
        btnEdit = findViewById(R.id.btnEditEvent);
        btnDelete = findViewById(R.id.btnDeleteEvent);

        btnLike = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);
        tvLikesCount = findViewById(R.id.tvLikesCount);
        tvDislikesCount = findViewById(R.id.tvDislikesCount);

        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        rvComments = findViewById(R.id.rvComments);

        tvCapacity = findViewById(R.id.tvCapacity);
        btnOpenInMaps = findViewById(R.id.btnOpenInMaps);
        ivTicketQr = findViewById(R.id.ivTicketQr);
        tvTicketTitle = findViewById(R.id.tvTicketTitle);
        btnTicketPdf = findViewById(R.id.btnTicketPdf);
        mapPreview = findViewById(R.id.mapPreview);
        if (ivTicketQr != null) {
            ivTicketQr.setVisibility(android.view.View.GONE);
        }
        if (tvTicketTitle != null) {
            tvTicketTitle.setVisibility(View.GONE);
        }

        if (btnTicketPdf != null) {
            btnTicketPdf.setVisibility(View.GONE);
        }

        if (mapPreview != null) {
            mapPreview.setMultiTouchControls(false);
        }
    }

    private void updateCapacityUi() {
        if (event == null) return;

        // Update label "X / Y places left"
        if (tvCapacity != null) {
            if (event.maxPlaces > 0) {
                String text = event.availablePlaces + " / " + event.maxPlaces + " places left";
                tvCapacity.setText(text);
            } else {
                tvCapacity.setText("No capacity limit");
            }
        }

        // If no more spots AND user not already booked -> disable & show FULL
        boolean isFull = event.maxPlaces > 0 && event.availablePlaces <= 0;
        boolean userAlreadyBooked = bookingRepo.isBooked(eventId);

        if (isFull && !userAlreadyBooked) {
            btnBook.setEnabled(false);
            btnBook.setText("FULL");
        } else {
            // fall back to normal BOOK / BOOKED logic
            updateButtonState();
        }
    }

    /**
     * Create a ticket row for this booking if needed (outside the booking transaction)
     * and generate a QR code bitmap into ivTicketQr.
     */
    private void ensureTicketForCurrentUser() {
        if (ticketDao == null || ivTicketQr == null) return;
        if (currentUserId == null) return;

        // Find the most recent booking id for this user/email & event
        EventDbHelper helper = new EventDbHelper(this);
        android.database.sqlite.SQLiteDatabase db = helper.getReadableDatabase();
        long bookingId = -1L;
        android.database.Cursor c = db.rawQuery(
                "SELECT id FROM " + EventDbHelper.TABLE_BOOKINGS + " WHERE event_id=? AND user_email=? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(eventId), session.getEmail()}
        );
        try {
            if (c.moveToFirst()) {
                bookingId = c.getLong(0);
            }
        } finally {
            c.close();
        }
        if (bookingId == -1L) return;

        Ticket existing = ticketDao.findByBookingId(bookingId);
        String payload;
        if (existing != null) {
            payload = existing.qrPayload;
        } else {
            // Make payload human-readable and friendly for scanners
            payload = "Event ticket" + "\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "User: " + currentUserName + "\n" +
                    "Event: " + event.title;
            ticketDao.insert(bookingId, currentUserId, eventId, payload);
        }

        renderQr(payload);
    }

    /**
     * Update QR visibility based on booking state.
     */
    private void updateTicketQr() {
        if (ivTicketQr == null) return;
        if (!bookingRepo.isBooked(eventId)) {
            ivTicketQr.setVisibility(android.view.View.GONE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.GONE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(View.GONE);
            return;
        }

        // Try to load existing ticket and show QR
        if (ticketDao == null || currentUserId == null) {
            ivTicketQr.setVisibility(android.view.View.GONE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.GONE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(View.GONE);
            return;
        }

        // Look up latest booking
        EventDbHelper helper = new EventDbHelper(this);
        android.database.sqlite.SQLiteDatabase db = helper.getReadableDatabase();
        long bookingId = -1L;
        android.database.Cursor c = db.rawQuery(
                "SELECT id FROM " + EventDbHelper.TABLE_BOOKINGS + " WHERE event_id=? AND user_email=? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(eventId), session.getEmail()}
        );
        try {
            if (c.moveToFirst()) {
                bookingId = c.getLong(0);
            }
        } finally {
            c.close();
        }
        if (bookingId == -1L) {
            ivTicketQr.setVisibility(android.view.View.GONE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.GONE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(View.GONE);
            return;
        }

        Ticket t = ticketDao.findByBookingId(bookingId);
        if (t == null) {
            ivTicketQr.setVisibility(android.view.View.GONE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.GONE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(View.GONE);
            return;
        }

        renderQr(t.qrPayload);
        if (btnTicketPdf != null) btnTicketPdf.setVisibility(View.VISIBLE);
    }

    private void renderQr(String payload) {
        if (ivTicketQr == null) return;
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(payload, BarcodeFormat.QR_CODE, 500, 500);
            ivTicketQr.setImageBitmap(bitmap);
            ivTicketQr.setVisibility(android.view.View.VISIBLE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.VISIBLE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(android.view.View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            ivTicketQr.setVisibility(android.view.View.GONE);
            if (tvTicketTitle != null) tvTicketTitle.setVisibility(android.view.View.GONE);
            if (btnTicketPdf != null) btnTicketPdf.setVisibility(android.view.View.GONE);
        }
    }

    private void generateTicketPdf() {
        if (event == null || currentUserId == null) {
            showSnack("You must be logged in and have a booking to get a ticket PDF.");
            return;
        }

        // Look up latest booking id
        EventDbHelper helper = new EventDbHelper(this);
        android.database.sqlite.SQLiteDatabase db = helper.getReadableDatabase();
        long bookingId = -1L;
        android.database.Cursor c = db.rawQuery(
                "SELECT id FROM " + EventDbHelper.TABLE_BOOKINGS + " WHERE event_id=? AND user_email=? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(eventId), session.getEmail()}
        );
        try {
            if (c.moveToFirst()) {
                bookingId = c.getLong(0);
            }
        } finally {
            c.close();
        }

        if (bookingId == -1L) {
            showSnack("You must book this event to get a ticket PDF.");
            return;
        }

        String fileName = "Ticket_" + bookingId + ".pdf";
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) dir = getFilesDir();
        File file = new File(dir, fileName);

        android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
        android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(22f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(16f);

        int x = 48;
        int y = 80;

        canvas.drawText("Event ticket", x, y, titlePaint);
        y += 40;

        canvas.drawText("Booking ID: " + bookingId, x, y, textPaint); y += 28;
        canvas.drawText("User: " + currentUserName, x, y, textPaint); y += 28;
        canvas.drawText("Event: " + event.title, x, y, textPaint); y += 28;
        if (event.date != null) {
            canvas.drawText("Date: " + event.date, x, y, textPaint); y += 28;
        }
        if (event.location != null) {
            canvas.drawText("Location: " + event.location, x, y, textPaint); y += 28;
        }

        pdf.finishPage(page);

        try (FileOutputStream out = new FileOutputStream(file)) {
            pdf.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
            showSnack("Failed to create PDF ticket");
            pdf.close();
            return;
        }

        pdf.close();

        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(intent, "Open ticket PDF"));
        } catch (Exception e) {
            showSnack("No PDF viewer installed");
        }
    }

    private void setupCommentsList() {
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(comments);
        rvComments.setAdapter(commentsAdapter);
    }

    private void loadEvent() {
        event = dao.getById(eventId);
        if (event == null) {
            showSnack("Event not found");
            finish();
            return;
        }

        tvTitle.setText(event.title);
        tvDate.setText(event.date);
        tvSubtitle.setText(event.subtitle);
        tvLocation.setText(event.location);
        tvDescription.setText(event.description);
        // Prefer URI image picked from gallery, fall back to resource / placeholder
        if (event.imageUri != null && !event.imageUri.isEmpty()) {
            try {
                ivEventImage.setImageURI(android.net.Uri.parse(event.imageUri));
            } catch (Exception ex) {
                ex.printStackTrace();
                if (event.imageResId != 0) {
                    ivEventImage.setImageResource(event.imageResId);
                } else {
                    ivEventImage.setImageResource(R.drawable.ic_event_placeholder);
                }
            }
        } else if (event.imageResId != 0) {
            ivEventImage.setImageResource(event.imageResId);
        } else {
            ivEventImage.setImageResource(R.drawable.ic_event_placeholder);
        }
        updateCapacityUi();
        updateMapMarker();
    }

    private void loadLikes() {
        likesCount = likesRepo.getLikesCount(eventId);
        dislikesCount = likesRepo.getDislikesCount(eventId);
        int status = likesRepo.getUserStatus(eventId, currentUserId);
        userLiked = status == 1;
        userDisliked = status == -1;
        refreshLikeDislikeUi();
    }

    private void loadComments() {
        comments.clear();
        comments.addAll(commentsRepo.getCommentsForEvent(eventId));
        commentsAdapter.notifyDataSetChanged();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dao.delete(eventId);
                    showSnack("Event deleted");
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleBooking() {
        if (bookingRepo.isBooked(eventId)) {
            bookingRepo.unBookEvent(eventId);
            showSnack("Removed from My Events");
        } else {
            boolean ok = bookingRepo.bookEvent(eventId);
            if (!ok) {
                showSnack("No more places available");
                return;
            }
            // Ensure a ticket exists and update QR display
            ensureTicketForCurrentUser();
            showSnack("Event saved!");
        }
        // refresh event from DB to get updated availablePlaces
        event = dao.getById(eventId);
        updateButtonState();
        updateCapacityUi();
        updateTicketQr();
    }

    private void updateButtonState() {
        if (bookingRepo.isBooked(eventId)) {
            btnBook.setText("BOOKED");
            btnBook.setEnabled(false);
        } else {
            btnBook.setText("BOOK");
            btnBook.setEnabled(true);
        }
    }

    // ------- LIKE / DISLIKE --------

    private void onLikeClicked() {
        if (currentUserId == null) {
            showSnack("Please login to like.");
            return;
        }

        int newStatus;
        if (userLiked) {
            newStatus = 0;  // remove like
        } else {
            newStatus = 1;  // like
        }

        likesRepo.setStatus(eventId, currentUserId, newStatus);
        loadLikes();
    }

    private void onDislikeClicked() {
        if (currentUserId == null) {
            showSnack("Please login to dislike.");
            return;
        }

        int newStatus;
        if (userDisliked) {
            newStatus = 0;  // remove dislike
        } else {
            newStatus = -1; // dislike
        }

        likesRepo.setStatus(eventId, currentUserId, newStatus);
        loadLikes();
    }

    private void refreshLikeDislikeUi() {
        tvLikesCount.setText(String.valueOf(likesCount));
        tvDislikesCount.setText(String.valueOf(dislikesCount));

        btnLike.setColorFilter(userLiked ? 0xFF2196F3 : 0xFF888888);
        btnDislike.setColorFilter(userDisliked ? 0xFFF44336 : 0xFF888888);
    }

    // ------- COMMENTS --------

    private void onSendComment() {
        if (currentUserId == null) {
            showSnack("Please login to comment.");
            return;
        }

        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        commentsRepo.addComment(eventId, currentUserId, currentUserName, text);
        etComment.setText("");
        loadComments();
        rvComments.smoothScrollToPosition(comments.size() - 1);
    }

    private void showSnack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }

    // Simple comments adapter showing "user: text"
    private static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentVH> {

        private final List<Comment> items;

        CommentsAdapter(List<Comment> items) {
            this.items = items;
        }

        @Override
        public CommentVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new CommentVH(view);
        }

        @Override
        public void onBindViewHolder(CommentVH holder, int position) {
            Comment c = items.get(position);
            holder.title.setText(c.userName);
            holder.subtitle.setText(c.text);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class CommentVH extends RecyclerView.ViewHolder {
            TextView title;
            TextView subtitle;
            CommentVH(android.view.View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }
    private void updateMapMarker() {
        if (mapPreview == null || event == null) return;

        double lat = event.lat;
        double lng = event.lng;

        // If no coordinates stored yet but we have a textual location, first try to parse "lat, lng"
        if ((lat == 0 && lng == 0) && event.location != null && !event.location.isEmpty()) {
            boolean parsed = false;
            String loc = event.location.trim();
            if (loc.contains(",")) {
                String[] parts = loc.split(",");
                if (parts.length >= 2) {
                    try {
                        double pLat = Double.parseDouble(parts[0].trim());
                        double pLng = Double.parseDouble(parts[1].trim());
                        lat = pLat;
                        lng = pLng;
                        parsed = true;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // If parsing failed, fall back to geocoding the address text
            if (!parsed) {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> results = geocoder.getFromLocationName(event.location, 1);
                    if (results != null && !results.isEmpty()) {
                        Address a = results.get(0);
                        lat = a.getLatitude();
                        lng = a.getLongitude();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Persist back if we found valid coordinates
            if (lat != 0 || lng != 0) {
                event.lat = lat;
                event.lng = lng;
                dao.update(event);
            }
        }

        if (lat != 0 || lng != 0) {
            GeoPoint pos = new GeoPoint(lat, lng);

            if (previewMarker == null) {
                previewMarker = new Marker(mapPreview);
                previewMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapPreview.getOverlays().add(previewMarker);
            }

            previewMarker.setPosition(pos);

            IMapController controller = mapPreview.getController();
            controller.setZoom(14.0);
            controller.setCenter(pos);

            mapPreview.invalidate();
        }
    }

}
