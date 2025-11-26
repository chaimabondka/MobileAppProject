package com.example.eventapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.AuthRepository;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.example.eventapplication.auth.EmailVerificationManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEt, emailEt, passEt, pass2Et, phoneEt, ageEt, addressEt;
    private MaterialSwitch swOrganizer;
    private LinearProgressIndicator pwStrength;
    private ImageView imgAvatar;
    private Uri pickedAvatarUri = null;
    UserDao userDao = new UserDao(this);
    private AuthRepository repo;
    private SessionManager session;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedAvatarUri = uri;
                    imgAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        repo = new AuthRepository(this);
        session = new SessionManager(this);

        imgAvatar = findViewById(R.id.imgAvatar);
        nameEt = findViewById(R.id.etName);
        emailEt = findViewById(R.id.etEmail);
        phoneEt = findViewById(R.id.etPhone);
        ageEt = findViewById(R.id.etAge);
        addressEt = findViewById(R.id.etAddress);
        passEt = findViewById(R.id.etPassword);
        pass2Et = findViewById(R.id.etPassword2);
        swOrganizer = findViewById(R.id.swOrganizer);
        pwStrength = findViewById(R.id.pwStrength);

        findViewById(R.id.btnPickPhoto).setOnClickListener(v -> pickPhoto());
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> doRegister());
        findViewById(R.id.btnGoLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });

        passEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                pwStrength.setProgress(scorePassword(s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void pickPhoto() {
        pickImage.launch("image/*");
    }

    private int scorePassword(String p) {
        int score = 0;
        if (p.length() >= 6) score += 20;
        if (p.length() >= 10) score += 20;
        if (p.matches(".*[A-Z].*")) score += 20;
        if (p.matches(".*[a-z].*")) score += 20;
        if (p.matches(".*[0-9!@#$%^&*].*")) score += 20;
        return score;
    }

    private void doRegister() {
        String name = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String p1 = passEt.getText().toString();
        String p2 = pass2Et.getText().toString();

        if (name.isEmpty() || email.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
            toast("Please fill all required fields");
            return;
        }
        if (!p1.equals(p2)) {
            toast("Passwords do not match");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, p1)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        toast("Signup failed: " + task.getException().getMessage());
                        return;
                    }

                    FirebaseUser fUser = auth.getCurrentUser();
                    if (fUser == null) return;

                    // Build your User
                    User u = new User();
                    u.name = nameEt.getText().toString();
                    u.email = emailEt.getText().toString().trim().toLowerCase();
                    u.passwordHash = "firebase";
                    u.passwordSalt = "firebase";
                    u.createdAt = System.currentTimeMillis();

                    u.role = "ATTENDEE";
                    u.phone = phoneEt.getText().toString();
                    u.address = addressEt.getText().toString();
                    u.age = null;
                    u.avatarUrl = null;
                    u.avatarUri = null;

                    // INSERT INTO SQLITE (critical)
                    long insertedId = userDao.insert(u);

                    if (insertedId == -1) {
                        toast("Failed to save user locally");
                        return;
                    }

                    // convert to String if SessionManager expects String
                    String newId = String.valueOf(insertedId);

                    // Save in Session
                    SessionManager session = new SessionManager(this);
                    session.login(newId, u.name, u.email);

                    // optional email verification
                    fUser.sendEmailVerification();

                    toast("Account created! Please verify email.");
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                });

    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
