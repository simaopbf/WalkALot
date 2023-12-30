package com.example.actuallayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.actuallayout.DatabaseHelper;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;
    DatabaseHelper dbHelper;
    Button signUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = username.getText().toString();
                String enteredPassword = password.getText().toString();

                Log.d("LoginActivity", "Clicked LoginButton for username: " + enteredUsername);
                if (checkCredentials(enteredUsername, enteredPassword)) {

                    // Retrieve the user ID from your database based on the entered username
                    long userId = dbHelper.getUserIdByUsername(enteredUsername);

                    if (userId != -1) {
                        Log.d("LoginActivity", "Login successful for username: " + enteredUsername);

                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        // Start MainActivity and pass the user ID
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);

                        // Finish LoginActivity to prevent going back with the back button
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LoginActivity", "Login failed for username: " + enteredUsername);
                    Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
                            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredUsername = username.getText().toString();
                String enteredPassword = password.getText().toString();

                if (checkCredentials(enteredUsername, enteredPassword)) {
                    // Log a message before calling getUserIdByUsername
                    Log.d("LoginActivity", "Calling getUserIdByUsername for username: " + enteredUsername);

                    // Retrieve the user ID from your database based on the entered username
                    long userId = dbHelper.getUserIdByUsername(enteredUsername);

                    if (userId != -1) {
                        /*// Start MainActivity and pass the user ID
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);

                        // Finish LoginActivity to prevent going back with the back button
                        finish();*/

                        Log.d("LoginActivity", "Login successful for username: " + enteredUsername);
                        // Check Bluetooth connection
                        if (isBluetoothConnected()) {
                            // Start MainActivity and pass the user ID
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            // Finish LoginActivity to prevent going back with the back button
                            finish();
                        } else {
                            // If Bluetooth is not connected, redirect to BluetoothService
                            Intent intent = new Intent(LoginActivity.this, BluetoothService.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle sign-up button click, navigate to SignUpActivity
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }



    private boolean checkCredentials(String enteredUsername, String enteredPassword) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = { "username", "password" };
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = { enteredUsername, enteredPassword };

        Cursor cursor = db.query("users", projection, selection, selectionArgs, null, null, null);

        boolean result = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return result;
    }

    private boolean isBluetoothConnected() {
        BluetoothService bluetoothService = new BluetoothService();  // Instantiate BluetoothService
        return bluetoothService.isBluetoothConnected();
    }

}
