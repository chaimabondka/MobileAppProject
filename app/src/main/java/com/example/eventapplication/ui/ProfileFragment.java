package com.example.eventapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import com.example.eventapplication.R;
import com.example.eventapplication.auth.SessionManager;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;
import com.google.firebase.auth.FirebaseAuth;
import com.example.eventapplication.ui.NewReclamationActivity;
import com.example.eventapplication.ui.MyReclamationsActivity;

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
        Button btnNewReclamation = view.findViewById(R.id.btnNewReclamation);
        Button btnMyReclamations = view.findViewById(R.id.btnMyReclamations);

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

        btnNewReclamation.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), NewReclamationActivity.class);
            startActivity(i);
        });

        btnMyReclamations.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), MyReclamationsActivity.class);
            startActivity(i);
        });
    }

    private void loadUserData() {
        String email = session.getEmail();
        currentUser = userDao.findByEmail(email);

        if (currentUser == null) {
            showSnack("User not found");
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

        etName.setError(null);
        etAge.setError(null);

        String nameText = etName.getText().toString().trim();
        String phoneText = etPhone.getText().toString().trim();
        String addressText = etAddress.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();

        boolean hasError = false;

        if (nameText.isEmpty()) {
            etName.setError("Name is required");
            hasError = true;
        }

        Integer ageValue = null;
        if (!ageText.isEmpty()) {
            try {
                int parsed = Integer.parseInt(ageText);
                if (parsed < 10 || parsed > 120) {
                    etAge.setError("Age must be between 10 and 120");
                    hasError = true;
                } else {
                    ageValue = parsed;
                }
            } catch (NumberFormatException e) {
                etAge.setError("Invalid age");
                hasError = true;
            }
        }

        if (hasError) {
            showSnack("Please correct the highlighted fields");
            return;
        }

        currentUser.name = nameText;
        currentUser.phone = phoneText;
        currentUser.address = addressText;
        currentUser.role = currentUser.role;  // unchanged
        currentUser.age = ageValue;

        int rows = userDao.update(currentUser);

        if (rows > 0) {
            // update session name if changed
            session.login(currentUser.id, currentUser.name, currentUser.email);
            showSnack("Profile updated");
        } else {
            showSnack("Failed to update profile");
        }
    }

    private void showSnack(String msg) {
        View root = getView();
        if (root != null) {
            Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

}
