package com.example.actuallayout;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.actuallayout.MainActivity;
import com.example.actuallayout.R;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        (new Handler()).postDelayed(this::goToAct, 3000);
    }

    public void goToAct(){
        Intent mainAct = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainAct);
    }



}