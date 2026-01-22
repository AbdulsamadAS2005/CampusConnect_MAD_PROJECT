package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mad_project.auth.AuthManager;
import com.example.mad_project.fragments.HomeFragment;
import com.example.mad_project.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabCreatePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        // Check authentication
        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabCreatePost = findViewById(R.id.fabCreatePost);

        // Set default fragment
        loadFragment(new HomeFragment());

        // Bottom navigation listener
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    fragment = new HomeFragment();
                } else if (item.getItemId() == R.id.nav_profile) {
                    fragment = new ProfileFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            }
        });

        // Create post FAB
        fabCreatePost.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreatePostActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // Exit app on back from main screen
        moveTaskToBack(true);
    }
}