package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.auth.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, password, confirmPassword;
    Button registerBtn;
    TextView loginText;
    ProgressBar progressBar;

    AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new AuthManager(this);

        // Bind views
        username = findViewById(R.id.et_username);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        registerBtn = findViewById(R.id.btn_register);
        loginText = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progressBar);

        registerBtn.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String u = username.getText().toString().trim();
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();
        String c = confirmPassword.getText().toString().trim();

        if (u.isEmpty() || e.isEmpty() || p.isEmpty() || c.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!p.equals(c)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (p.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerBtn.setEnabled(false);

        authManager.register(u, e, p, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}