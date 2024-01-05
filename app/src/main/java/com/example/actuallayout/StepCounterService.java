package com.example.actuallayout;

import static com.example.actuallayout.ProfileFragment.CAL_GOAL;
import static com.example.actuallayout.ProfileFragment.DIST_GOAL;
import static com.example.actuallayout.ProfileFragment.DIST_GOAL_U;
import static com.example.actuallayout.ProfileFragment.GOALS_PREFS;
import static com.example.actuallayout.ProfileFragment.STEPS_GOAL;
import static com.example.actuallayout.ProfileFragment.TIME_GOAL;
import static com.example.actuallayout.ProfileFragment.TIME_GOAL_U;
import static com.example.actuallayout.SettingsFragment.CONFIG_PREFS;
import static com.example.actuallayout.SettingsFragment.GENDER;
import static com.example.actuallayout.SettingsFragment.HEIGHT;
import static com.example.actuallayout.SettingsFragment.WEIGHT;
import static java.lang.Math.abs;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.actuallayout.SQLiteManager;
import Bio.Library.namespace.BioLib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class StepCounterService extends Service {

    private static final String TAG = "StepCounterService";
    private final double[] previousData = new double[3];
    private double aggregatedMagnitudes = 0.0;
    private int stepCount = 0;
    private int notMovingCounter = 0;
    private int walkingCounter = 0;
    private int status = 0;

    private SQLiteManager sqLiteManager;
    // Constants for magnitude delta thresholds
    private static final double STEP_THRESHOLD_LOW = 30;
    private static final double STEP_THRESHOLD_HIGH = 90;
    private static final int NOT_MOVING_THRESHOLD = 15;

//Fui buscar
    // ACC data variables

    private static BioLib.DataACC dataACC = null;
    private static Double[] dadosAnteriores = {0.0, 0.0, 0.0};
    private static double agregadoMagnitudes = 0.0;
    private static Double kcalTotais;


    private static Integer distCount = 0;
    private static Integer timeCount = 0;
    private static Integer sec = 0;
    private static Integer min = 0;
    private static Integer hour = 0;
    private int runningCounter = 0;


    // DB

    private DatabaseHelper helper; //dataHelper as our bridge to the database
    private Date currentTime;

    // Config

    private SharedPreferences configPreferences;
    private int Weight;
    private int Height;
    private String Gender;

    // Goals

    private int Steps;
    private int Calories;
    private int Distance;
    private int Time;
    private String TimeU; // U for the string Unit (e.g., h or min)
    private String DistU;
    public static final String CHANNEL_ID2 = "GoalNotification";


    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        StepCounterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StepCounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    // Add your data processing logic here



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Add your data processing logic here
        BluetoothService bluetoothService = BluetoothService.getInstance();
        if (bluetoothService != null) {
            // Now you can use bluetoothService
            // ...
            BioLib.DataACC dataACC = BluetoothService.getDataACC();
            Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);

            // Perform your calculations or other processing
            calculateData(dataACC);

            bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

        } else {
            Log.d(TAG, "wtf ");
        }

        return START_STICKY;
    }
    //PREFERÊNCIAAAAAAAAAS
    private void initPreferencesAndGoals() {
        // Configs
        configPreferences = getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);

        // Goals
        SharedPreferences goalsPreferences = getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);

        Steps = goalsPreferences.getInt(STEPS_GOAL, 10000);
        Calories = goalsPreferences.getInt(CAL_GOAL, 685);
        Distance = goalsPreferences.getInt(DIST_GOAL, 8);
        Time = goalsPreferences.getInt(TIME_GOAL, 1);
        DistU = goalsPreferences.getString(DIST_GOAL_U, "Km");
        TimeU = goalsPreferences.getString(TIME_GOAL_U, "h");

        // Dataset
        stepCount = 0;
        kcalTotais = 0.0;
        distCount = 0;
        timeCount = 0;

        DatabaseHelper = new DatabaseHelper(this);
        Cursor dbData = dbHelper.getAll();

        ArrayList<Integer> steps = new ArrayList<>();
        ArrayList<Double> cal = new ArrayList<>();
        ArrayList<Integer> dist = new ArrayList<>();
        ArrayList<Integer> time = new ArrayList<>();

        if (dbData.getCount() != 0) {
            while (dbData.moveToNext()) {
                steps.add(dbData.getInt(0));
                cal.add(dbData.getDouble(1));
                dist.add(dbData.getInt(2));
                time.add(dbData.getInt(3));
            }

            for (int i = 0; i < steps.size(); i++) {
                stepCount = stepCount + steps.get(i);
                kcalTotais = kcalTotais + cal.get(i);
                distCount = distCount + dist.get(i);
                timeCount = timeCount + time.get(i);
            }
        }

        dbHelper.close();
    }
    private void calculateData(BioLib.DataACC dataACC) {
        stepCounter();
        calculateKcal();

        sendDataToActivity(stepCount, kcalTotais, distCount,timeCount, status);

        createNotificationChannel();

        if(stepCount == Steps)
        {
            goalsNotification("Steps",R.drawable.steps);
        }
        else if (Math.round(kcalTotais) == Calories)
        {
            goalsNotification("Calories",R.drawable.kcal);

        }
        else if (distCount == Distance)
        {
            goalsNotification("Distance",R.drawable.distance);
        }


    }


    // Class for interacting with the main interface of the service
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Bound to BluetoothService, now you can access its public methods
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            BluetoothService bluetoothService = binder.getService();

            // Example: Retrieve data from BluetoothService
            BioLib.DataACC dataACC = bluetoothService.getDataACC();

            // Perform your calculations or other processing
            calculateData(dataACC);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // Handle service disconnect (if needed)
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }



    // OLD CODE
/*@Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        dataReady();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Time

        DateFormat format = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String now = format.format(Calendar.getInstance().getTime());
        min = Integer.valueOf(now.substring(3,5));
        hour = Integer.valueOf(now.substring(0,2));
        sec = Integer.valueOf(now.substring(6,8));

        // Configs

        configPreferences = getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);

        // Goals

        SharedPreferences goalsPreferences = getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);

        Steps = goalsPreferences.getInt(STEPS_GOAL,10000);
        Calories = goalsPreferences.getInt(CAL_GOAL,685);
        Distance = goalsPreferences.getInt(DIST_GOAL, 8);
        Time = goalsPreferences.getInt(TIME_GOAL,1);
        DistU = goalsPreferences.getString(DIST_GOAL_U,"Km");
        TimeU = goalsPreferences.getString(TIME_GOAL_U,"h");

        // Dataset

        stepCount = 0;
        kcalTotais = 0.0;
        distCount = 0;
        timeCount = 0;

        helper = new DatabaseHelper(this);
        Cursor dbData = helper.getAll();

        ArrayList <Integer> steps = new ArrayList<>();
        ArrayList <Double> cal = new ArrayList<>();
        ArrayList <Integer> dist = new ArrayList<>();
        ArrayList <Integer> time = new ArrayList<>();


        if (dbData.getCount() != 0)
        {
            while (dbData.moveToNext())
            {
                steps.add(dbData.getInt(0));
                cal.add(dbData.getDouble(1));
                dist.add(dbData.getInt(2));
                time.add(dbData.getInt(3));
            }

            for (int i = 0; i < steps.size(); i++) {
                stepCount = stepCount + steps.get(i);
                kcalTotais = kcalTotais + cal.get(i);
                distCount = distCount + dist.get(i);
                timeCount = timeCount + time.get(i);
            }
        }

        helper.close();

        return START_REDELIVER_INTENT;
    }


    private void dataReady()
    {

        stepCounter();
        calculateKcal();

        sendDataToActivity(stepCount, kcalTotais, distCount,timeCount, status);

        createNotificationChannel();

        if(stepCount == Steps)
        {
            goalsNotification("Steps",R.drawable.steps);
        }
        else if (Math.round(kcalTotais) == Calories)
        {
            goalsNotification("Calories",R.drawable.kcal);

        }
        else if (distCount == Distance)
        {
            goalsNotification("Distance",R.drawable.distance);
        }

    }
*/
    private void sendDataToActivity(int stepCount, double kcal, int distCount,int timeCount, int status) {
        Intent intent = new Intent("Update UI");
        Bundle b = new Bundle();
        b.putInt("Steps", stepCount);
        b.putDouble("Kcal", kcal);
        b.putInt("Dist",distCount);
        b.putInt("Time",timeCount);
        b.putInt("Status",status);
        intent.putExtra("AppData", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
/*
    private void sendBatteryDataToActivity(int battery) {
        Intent intent = new Intent("Update Battery UI");
        Bundle b = new Bundle();
        b.putInt("Battery", battery);
        intent.putExtra("BatData", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void adddata() { // Add data to dataset
        helper.insert(stepCount, kcalTotais, distCount, timeCount, currentTime.toString());
    }
*/
    private void goalsNotification(String message, int icon){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID2);
        builder.setContentTitle("Congratulations!");
        builder.setContentTitle("You have accomplished your " + message + " goal!");
        builder.setSmallIcon(icon);
        builder.setAutoCancel(true); // True for swipe.

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(2,builder.build());

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel achievementChannel = new NotificationChannel(
                    CHANNEL_ID2, "Goal",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(achievementChannel);
        }
    }

/*
    @Override
    public void onDestroy() { // Destroy service
        super.onDestroy();

        if(dataACC != null) {
            adddata();
        }
        Intent searchAct = new Intent(getApplicationContext(), BluetoothService.class);
        searchAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(searchAct);

        helper.close();

        stopForeground(true);
        stopSelf();
    }*/

    /* ---------------------------------------------------------- Data processing ----------------------------------------------------------------------*/

    private void stepCounter() {

        DateFormat format = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String date = format.format(Calendar.getInstance().getTime());
        int newmin = Integer.valueOf(date.substring(3,5));
        int newhour = Integer.valueOf(date.substring(0,1));
        int newsec = Integer.valueOf(date.substring(6,8));
        Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);

        Double x = (double) dataACC.X;
        Double y = (double) dataACC.Y;
        Double z = (double) dataACC.Z;

        //Count of steps

        double MagnitudePrevious = Math.sqrt(dadosAnteriores[0] * dadosAnteriores[0] + dadosAnteriores[1] * dadosAnteriores[1] + dadosAnteriores[2] * dadosAnteriores[2]);
        double Magnitude = Math.sqrt(x * x + y * y + z * z);
        double MagnitudeDelta = Magnitude - MagnitudePrevious;
        agregadoMagnitudes += abs(MagnitudeDelta);
        encherDados(x, y, z);

        currentTime = Calendar.getInstance().getTime();

        if (MagnitudeDelta >= 30 && MagnitudeDelta <= 90)
        {
            stepCount++;
            distance();
            time(newhour,newmin,newsec);
            notMovingCounter=0;
            status = 1;
        }
        else if (MagnitudeDelta > 90)
        {
            stepCount++;
            distance();
            time(newhour, newmin,newsec);
            status = 2;
            notMovingCounter=0;
        }
        else
        {
            notMovingCounter++;
            min = Integer.valueOf(date.substring(3,5));
            hour = Integer.valueOf(date.substring(0,1));
            sec = Integer.valueOf(date.substring(6,7));
            walkingCounter=0;
            if(notMovingCounter >=15)
            {
                status = 0;
            }
        }
    }

    private void calculateKcal() {
        Weight = configPreferences.getInt(WEIGHT,70);
        final int fs = 10;
        final int adjustment = fs * 60;
        kcalTotais = ((0.001064 * agregadoMagnitudes + 0.087512 * Weight - 5.500229)/fs);
        // Equation for every min so its adjusted
    }

    private void distance(){
        Height = configPreferences.getInt(HEIGHT,165);
        Gender = configPreferences.getString(GENDER,"Male");

        if(Gender.equals("Male"))
        {
            distCount = (int) Math.round((0.415 * Height/100) * stepCount);
        }
        else
        {
            distCount = (int) Math.round((0.413 * Height/100) * stepCount);
        }

        distCount++;
    }

    private void encherDados(Double x, Double y, Double z){
        dadosAnteriores[0] = x;
        dadosAnteriores[1] = y;
        dadosAnteriores[2] = z;
    }

    public void time(int h, int m, int s){

        if(s - sec >= 0)
        {
            timeCount = Math.round(timeCount + (s - sec)/60);
        }
        else
        {
            timeCount = timeCount + s/60;
        }
        if(m - min >= 0)
        {
            timeCount = timeCount + (m- min);
        }
        else
        {
            timeCount = timeCount + m;
        }

        if (h-hour >= 0){
            timeCount = timeCount + (h - hour)*60;
        }
    }


}