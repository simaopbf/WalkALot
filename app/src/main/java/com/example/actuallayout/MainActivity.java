package com.example.actuallayout;

//import static android.os.Build.VERSION_CODES.R;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.actuallayout.ConfigActivitySimao.CONFIG_PREFS;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isRunning = isServiceRunningInForeground(this, AcquisitionServiceSimao.class);

        // Configs
        SharedPreferences configPref = getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        boolean firstStart = configPref.getBoolean("firstStart", true); // Second value (true in this case) is always the default value if nothing is saved yet

        Log.d(TAG, "onCreate: ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        if (firstStart) {
            // Checks if it is the first time the user opens the app
            // If so, the user sets the Config inputs

            Intent configAct = new Intent(getApplicationContext(), ConfigActivitySimao.class);
            startActivity(configAct);
        } else {
            if (isRunning) {
                Intent resultAct = new Intent(getApplicationContext(), ResultsActivitySimao.class);
                startActivity(resultAct);
                Log.d(TAG, "onCreate: ###################################################### Service is running! ######################################################");
            } else {
                Intent searchAct = new Intent(getApplicationContext(), SearchDeviceActivitySimao.class);
                startActivity(searchAct);
                Log.d(TAG, "onCreate: ###################################################### Service is not running! ######################################################");
            }

        }
    }
    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }

            }
        }
        return false;
    }



        /*  ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            if(item.getItemId()==R.id.home){
                replaceFragment(new HomeFragment());

            } else if (item.getItemId()==R.id.statistics) {
                replaceFragment(new StatisticsFragment());

            } else if (item.getItemId()==R.id.profile) {
                replaceFragment(new ProfileFragment());

            }else if (item.getItemId()==R.id.Calendar) {
                replaceFragment(new CalendarFragment());

            }else if (item.getItemId()==R.id.settings) {
                replaceFragment(new SettingsFragment());
            }


            return true;
        });

    }*/

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }


}

/*import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            if(item.getItemId()==R.id.home){
                replaceFragment(new HomeFragment());

            } else if (item.getItemId()==R.id.statistics) {
                replaceFragment(new StatisticsFragment());

            } else if (item.getItemId()==R.id.profile) {
                replaceFragment(new ProfileFragment());

            }else if (item.getItemId()==R.id.Calendar) {
                replaceFragment(new CalendarFragment());

            }else if (item.getItemId()==R.id.settings) {
                replaceFragment(new SettingsFragment());
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


}*/