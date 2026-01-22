package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.auth.AuthManager;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView signUp, forgotPassword;
    ProgressBar progressBar;
    AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        // Auto-login if already logged in
        if (authManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Bind views
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginBtn = findViewById(R.id.btn_login);
        signUp = findViewById(R.id.tv_sign_up);
        forgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progressBar);


        loginBtn.setOnClickListener(v -> loginUser());

        signUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Use: test@test.com / password123", Toast.LENGTH_LONG).show());
    }

    private void loginUser() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();

        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        authManager.login(e, p, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}