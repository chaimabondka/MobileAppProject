package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventapplication.R;
import com.example.eventapplication.auth.AuthRepository;
import com.example.eventapplication.auth.EmailVerificationManager;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignInActivity extends AppCompatActivity {
    private EditText emailEt, passEt;
    private AuthRepository repo;
    private SessionManager session;


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        repo = new AuthRepository(this);
        session = new SessionManager(this);





        emailEt = findViewById(R.id.etEmail);
        passEt = findViewById(R.id.etPassword);
        Button btn = findViewById(R.id.btnLogin);


        btn.setOnClickListener(v -> doLogin());

        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> resetPassword());

        findViewById(R.id.btnGoSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> resetPassword());

    }


    private void doLogin() {

        String email = emailEt.getText().toString().trim();
        String pass = passEt.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            toast("Enter email and password");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        toast("Login failed: " + task.getException().getMessage());
                        return;
                    }

                    FirebaseUser fUser = auth.getCurrentUser();
                    if (fUser == null) return;

                    // Load full profile from SQLite
                    UserDao userDao = new UserDao(this);
                    User u = userDao.findByEmail(email);

                    if (u == null) {
                        toast("Local user profile missing. Please Sign Up again.");
                        auth.signOut();
                        return;
                    }

                    // Save in Session
                    SessionManager session = new SessionManager(this);
                    session.login(String.valueOf(u.id), u.name, u.email);

                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
        ;
;
    }

    private void resetPassword() {
        String email = emailEt.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Password reset email sent!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(this,
                                "Error: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }



    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}