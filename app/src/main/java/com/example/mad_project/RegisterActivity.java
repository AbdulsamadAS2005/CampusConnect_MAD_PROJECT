package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, password, confirmPassword;
    Button registerBtn;
    TextView loginText;
    ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        // Bind views (MATCH XML IDs)
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

        progressBar.setVisibility(View.VISIBLE);
        registerBtn.setEnabled(false);

        auth.createUserWithEmailAndPassword(e, p)
                .addOnSuccessListener(authResult -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // go back to Login
                })
                .addOnFailureListener(e1 -> {
                    progressBar.setVisibility(View.GONE);
                    registerBtn.setEnabled(true);
                    Toast.makeText(this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
