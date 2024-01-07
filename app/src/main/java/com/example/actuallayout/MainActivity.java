package com.example.actuallayout;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.actuallayout.R;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.actuallayout.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private long mUserId; // Add a member variable to store the userId
    private String mParam1;
    private String mParam2;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve userId from Intent
        mUserId = getIntent().getLongExtra("userId", -1);
        if (mUserId == -1) {
            // If userId is not found in the intent, try getting it from the savedInstanceState
            if (savedInstanceState != null) {
                mUserId = savedInstanceState.getLong("userId", -1);
            }
        }
        Log.d("verificarerromain", "id:" + mUserId);

        // Start the StepCounterService
        startService(new Intent(this, StepCounterService.class));

        // Pass userId to the all fragments
        replaceFragment(HomeFragment.newInstance(mParam1, mParam2, mUserId));
        replaceFragment(SettingsFragment.newInstance(mParam1, mParam2, mUserId));
        replaceFragment(StatisticsFragment.newInstance(mParam1, mParam2, mUserId));
        replaceFragment(ProfileFragment.newInstance(mParam1, mParam2, mUserId));
        replaceFragment(CalendarFragment.newInstance(mParam1, mParam2, mUserId));

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            if(item.getItemId()==R.id.home){
                replaceFragment(  HomeFragment.newInstance(mParam1,mParam2,mUserId));

            } else if (item.getItemId()==R.id.statistics) {
                replaceFragment(StatisticsFragment.newInstance(mParam1,mParam2,mUserId));

            } else if (item.getItemId()==R.id.profile) {
                replaceFragment(ProfileFragment.newInstance(mParam1,mParam2,mUserId));

            }else if (item.getItemId()==R.id.Calendar) {
                replaceFragment(CalendarFragment.newInstance(mParam1,mParam2,mUserId));

            }else if (item.getItemId()==R.id.settings) {
                replaceFragment(SettingsFragment.newInstance(mParam1,mParam2,mUserId));
            }


            return true;
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("userId", mUserId);
    }

}
    /*private void startBluetoothService() {
        Intent bluetoothServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
        bluetoothServiceIntent.putExtra("userId", mUserId);

        // Use startActivity for starting activities
        startActivity(bluetoothServiceIntent);

        // Add this log statement to check that the BluetoothService is started with userId
        Log.d("MainActivity", "startBluetoothService: userId = " + mUserId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the BroadcastReceiver when the activity is destroyed
        unregisterReceiver(bluetoothConnectionReceiver);
    }*/
