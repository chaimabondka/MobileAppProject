package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.EmailVerificationManager;
import com.example.eventapplication.auth.SessionManager;

public class VerifyEmailActivity extends AppCompatActivity {

    private String email;
    private EmailVerificationManager evm;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        evm = new EmailVerificationManager(this);
        session = new SessionManager(this);

        email = getIntent().getStringExtra("email");
        if (email == null) email = "";

        TextView tvInfo = findViewById(R.id.tvInfo);
        EditText etCode = findViewById(R.id.etCode);
        Button btnVerify = findViewById(R.id.btnVerify);
        Button btnResend = findViewById(R.id.btnResend);

        tvInfo.setText("A verification code was sent to:\n" + email);

        btnVerify.setOnClickListener(v -> {
            String codeTyped = etCode.getText().toString().trim();
            if (codeTyped.isEmpty()) {
                toast("Enter the code from your email");
                return;
            }

            if (evm.verifyCode(email, codeTyped)) {
                toast("Email verified!");

                // After verification you can log in automatically if you know the user
                // For now we just send to SignInActivity, user logs in normally.
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            } else {
                toast("Invalid or expired code");
            }
        });

        btnResend.setOnClickListener(v -> {
            if (email.isEmpty()) {
                toast("Missing email");
                return;
            }
            String code = evm.generateAndStoreCode(email);

            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(android.net.Uri.parse("mailto:" + email));
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your new verification code");
            mailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Here is your new verification code: " + code);
            try {
                startActivity(Intent.createChooser(mailIntent, "Send verification email"));
            } catch (Exception e) {
                toast("No email app found");
            }
        });
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
