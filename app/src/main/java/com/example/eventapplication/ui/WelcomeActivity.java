package com.example.eventapplication.ui;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;


public class WelcomeActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        SessionManager sm = new SessionManager(this);
        TextView tv = findViewById(R.id.tvWelcome);
        tv.setText("Hello, " + sm.getName() + " (" + sm.getEmail() + ")");
        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            sm.logout();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }
}