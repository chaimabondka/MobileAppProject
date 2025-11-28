package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private EditText etName, etEmail, etPhone, etAddress, etAge, etRole;
    private ImageView imgAvatar;
    private Button btnSave;

    private UserDao userDao;
    private SessionManager session;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        userDao = new UserDao(requireContext());

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        etAge = view.findViewById(R.id.etAge);
        etRole = view.findViewById(R.id.etRole);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnSave = view.findViewById(R.id.btnSave);
        Button btnSignOut = view.findViewById(R.id.btnSignOut);

        loadUserData();

        btnSave.setOnClickListener(v -> saveChanges());

        btnSignOut.setOnClickListener(v -> {
            // Clear local session
            session.logout();
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Navigate back to sign-in screen and clear back stack
            Intent i = new Intent(requireContext(), SignInActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void loadUserData() {
        String email = session.getEmail();
        currentUser = userDao.findByEmail(email);

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        etName.setText(currentUser.name);
        etEmail.setText(currentUser.email);
        etPhone.setText(currentUser.phone);
        etAddress.setText(currentUser.address);
        etRole.setText(currentUser.role);

        if (currentUser.age != null)
            etAge.setText(String.valueOf(currentUser.age));

    }

    private void saveChanges() {
        if (currentUser == null) return;

        currentUser.name = etName.getText().toString().trim();
        currentUser.phone = etPhone.getText().toString().trim();
        currentUser.address = etAddress.getText().toString().trim();
        currentUser.role = currentUser.role;  // unchanged

        String ageText = etAge.getText().toString().trim();
        currentUser.age = ageText.isEmpty() ? null : Integer.parseInt(ageText);

        int rows = userDao.update(currentUser);

        if (rows > 0) {
            // update session name if changed
            session.login(currentUser.id, currentUser.name, currentUser.email);
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

}
