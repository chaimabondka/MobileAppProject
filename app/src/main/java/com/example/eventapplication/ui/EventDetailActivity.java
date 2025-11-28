package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.eventapplication.data.LikesRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private long eventId;
    private EventDao dao;
    private BookingsRepository bookingRepo;
    private CommentsRepository commentsRepo;
    private LikesRepository likesRepo;
    private Event event;
    private TextView tvCapacity;
    private GoogleMap mMap;
    private android.widget.Button btnOpenInMaps;

    private SessionManager session;
    private String currentUserId;
    private String currentUserName;

    private ImageView ivEventImage;
    private MaterialButton btnBook, btnEdit, btnDelete, btnSendComment;
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

        dao = new EventDao(this);
        session = new SessionManager(this);

        currentUserId = session.getUserId();
        currentUserName = session.getName();
        if (currentUserName == null) currentUserName = "Anonymous";

        bookingRepo = new BookingsRepository(this, session.getEmail());
        commentsRepo = new CommentsRepository(this);
        likesRepo = new LikesRepository(this);

        bindViews();
        setupCommentsList();

        eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 PUT THIS BLOCK HERE 🔹
        // Map fragment setup
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);   // this = EventDetailActivity implements OnMapReadyCallback
        }

        // Open in external Maps app
        btnOpenInMaps.setOnClickListener(v -> {
            if (event == null || event.lat == 0 || event.lng == 0) {
                Toast.makeText(this, "Location not set for this event", Toast.LENGTH_SHORT).show();
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

    private void setupCommentsList() {
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(comments);
        rvComments.setAdapter(commentsAdapter);
    }

    private void loadEvent() {
        event = dao.getById(eventId);
        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleBooking() {
        if (bookingRepo.isBooked(eventId)) {
            bookingRepo.unBookEvent(eventId);
            Toast.makeText(this, "Removed from My Events", Toast.LENGTH_SHORT).show();
        } else {
            boolean ok = bookingRepo.bookEvent(eventId);
            if (!ok) {
                Toast.makeText(this, "No more places available", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Event saved!", Toast.LENGTH_SHORT).show();
        }
        // refresh event from DB to get updated availablePlaces
        event = dao.getById(eventId);
        updateButtonState();
        updateCapacityUi();
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
            Toast.makeText(this, "Please login to like.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Please login to dislike.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Please login to comment.", Toast.LENGTH_SHORT).show();
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
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapMarker();
    }

    private void updateMapMarker() {
        if (mMap == null || event == null) return;

        mMap.clear();
        if (event.lat != 0 || event.lng != 0) {
            LatLng pos = new LatLng(event.lat, event.lng);
            mMap.addMarker(new MarkerOptions().position(pos).title(event.title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
        }
    }

}
