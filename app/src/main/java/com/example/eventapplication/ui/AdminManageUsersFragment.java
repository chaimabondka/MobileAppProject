package com.example.eventapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapplication.R;
import com.example.eventapplication.data.User;
import com.example.eventapplication.data.UserDao;

import java.util.List;

public class AdminManageUsersFragment extends Fragment implements AdminUserAdapter.OnRoleChangeListener {

    private RecyclerView rvUsers;
    private UserDao userDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDao = new UserDao(requireContext());

        rvUsers = view.findViewById(R.id.rvAdminUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userDao.getAll();
        AdminUserAdapter adapter = new AdminUserAdapter(users, this);
        rvUsers.setAdapter(adapter);
    }

    @Override
    public void onRoleChange(User user, String newRole) {
        user.role = newRole;
        userDao.update(user);
        loadUsers();
    }

    @Override
    public void onDelete(User user) {
        userDao.delete(user.id);
        loadUsers();
    }
}
