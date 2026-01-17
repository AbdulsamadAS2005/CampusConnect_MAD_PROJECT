package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView signUp, forgotPassword;
    ProgressBar progressBar;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Bind views using correct frontend IDs
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
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
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

        auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener(authResult -> {
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e1 -> {
                    progressBar.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);
                    Toast.makeText(LoginActivity.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
