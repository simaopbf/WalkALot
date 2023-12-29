package com.example.actuallayout;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    EditText newUsername;
    EditText newPassword;
    Button registerButton;
    Button loginButtonR;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        newUsername = findViewById(R.id.newUsername);
        newPassword = findViewById(R.id.newPassword);
        registerButton = findViewById(R.id.registerButton);
        loginButtonR = findViewById(R.id.loginButtonR);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle user registration here
                String username = newUsername.getText().toString();
                String password = newPassword.getText().toString();

                // Insert the new user into the database
                long newRowId = dbHelper.insertUser(username, password);

                if (newRowId != -1) {
                    Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Pass userId to MainActivity
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("userId", newRowId);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButtonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle sign-up button click, navigate to registration activity
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}

