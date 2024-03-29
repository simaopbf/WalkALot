package com.example.actuallayout;

import static android.content.Intent.getIntent;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.example.actuallayout.MyContentProvider.TABLE_NAME;
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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.actuallayout.SQLiteManager;
import Bio.Library.namespace.BioLib;
import Bio.Library.namespace.BioLib.DataACC;

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

    int calrun = 0;
    int distrun = 0;
    int steprun = 0;

    private SQLiteManager sqLiteManager;
    // Constants for magnitude delta thresholds
    private static final double STEP_THRESHOLD_LOW = 30;
    private static final double STEP_THRESHOLD_HIGH = 90;
    private static final int NOT_MOVING_THRESHOLD = 15;

//Fui buscar
    // ACC data variables

    //private static BioLib.DataACC dataACC = null;
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
    private Date timeobj;

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
    private long userId;
    private long suserId;

    private final IBinder binder = new LocalBinder();
    private BioLib.DataACC dataACC;


    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        StepCounterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StepCounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        initPreferencesAndGoals(suserId);
        return binder;
    }

    private ContentObserver accDataObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Trigger your calculations here
            // Retrieve the latest row from ACCDataTable and perform calculations
            int xIntValue = retrieveXIntValueFromDatabase();
            int yIntValue = retrieveYIntValueFromDatabase();
            int zIntValue = retrieveZIntValueFromDatabase();

            double xValue = convertToDouble(xIntValue);
            double yValue = convertToDouble(yIntValue);
            double zValue = convertToDouble(zIntValue);

            calculateData(xValue, yValue, zValue, suserId);
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve userId from the intent
       long userId = intent.getLongExtra("userId", -1);
        suserId= userId;
        initPreferencesAndGoals(suserId);


        // Remove data retrieval logic from onStartCommand
        // Data retrieval and calculations are now handled by accDataObserver
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DatabaseHelper(this);
        Log.d(TAG, "StepCounterService onCreate");
        // Register ContentObserver for ACCDataTable changes
        getContentResolver().registerContentObserver(
                MyContentProvider.CONTENT_URI,
                true,
                accDataObserver
        );


    }
    private int retrieveXIntValueFromDatabase() {
        // Replace with your logic to retrieve X value as integer from the database
        // Example: return dbHelper.retrieveXIntValue();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT x_axis FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 1", null);        int xValue = 0;

        if (cursor.moveToFirst()) {
            xValue = cursor.getInt(cursor.getColumnIndex("x_axis"));
        }

        cursor.close();


        return xValue;
    }

    private int retrieveYIntValueFromDatabase() {
        // Replace with your logic to retrieve Y value as integer from the database

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT y_axis FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 1", null);        int yValue = 0;

        if (cursor.moveToFirst()) {
            yValue = cursor.getInt(cursor.getColumnIndex("y_axis"));
        }

        cursor.close();


        return yValue;
    }

    private int retrieveZIntValueFromDatabase() {
        // Replace with your logic to retrieve Z value as integer from the database
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT z_axis FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 1", null);        int zValue = 0;

        if (cursor.moveToFirst()) {
            zValue = cursor.getInt(cursor.getColumnIndex("z_axis"));
        }

        cursor.close();


        return zValue;
    }

    private double convertToDouble(int intValue) {
        // Convert integer value to double based on your conversion logic
        // Example: return intValue * 1.0; // Simple conversion for demonstration purposes
        return (double) intValue; // Simple conversion for demonstration purposes
    }
    //PREFERÊNCIAAAAAAAAAS
    private void initPreferencesAndGoals(long suserId) {
        DatabaseHelper helper = new DatabaseHelper(this);
        // Configs
        configPreferences = getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);

        // Goals
        SharedPreferences goalsPreferences = getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);



        int goalSteps= helper.targetValue(suserId,"stepGoal");

        Log.d("StepCounterService", "user: " + suserId);
        Log.d("StepCounterService", "NGLKS: " + goalSteps);

        Steps = goalsPreferences.getInt(STEPS_GOAL, goalSteps);
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

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        timeobj = calendar.getTime();
        Log.d("Currenttime", "NGLKS: " + timeobj.toString());



        Cursor dbData = helper.Datatable(suserId,timeobj.toString());

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


    }
    private void calculateData(double xValue, double yValue, double zValue, long userId) {
        int muser = (int) userId;
        stepCounter(xValue,yValue,zValue);
        calculateKcal(currentTime.toString());


        // Insert data into the database
        addDataToDatabase(stepCount, kcalTotais, distCount, timeCount, currentTime.toString(), muser);

        sendDataToActivity(stepCount, kcalTotais, distCount,timeCount, status);


        DatabaseHelper helper = new DatabaseHelper(this);
        int goalsteps = helper.targetValue(suserId,"stepGoal");
        Log.d("goals", "goals: " + goalsteps);
        int goalcals = helper.targetValue(suserId,"calGoal");
        Log.d("goals", "goals: " + goalcals);
        int goaldist = helper.targetValue(suserId,"distGoal");
        Log.d("goals", "goals: " + goaldist);

        createNotificationChannel();



        if(stepCount == goalsteps && steprun==0)
        {
            goalsNotification("Steps",R.drawable.steps);
            steprun=1;
        }
        if (Math.round(kcalTotais) == goalcals && calrun==0)
        {
            goalsNotification("Calories",R.drawable.kcal);
            calrun=1;

        }
        if (distCount == goaldist && distrun==0)
        {
            goalsNotification("Distance",R.drawable.distance);
            distrun=1;
        }


    }




    @Override
    public void onDestroy() {
        super.onDestroy();

    }



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

    private void addDataToDatabase(int steps, double kcal, int dist, int time, String date, int userId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        // Check if a row with the same user_id and date already exists
        Cursor cursor = db.rawQuery("SELECT * FROM Data WHERE user_id = ? AND date = ?", new String[]{String.valueOf(userId), date});

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("steps", steps);
        values.put("cal", kcal);
        values.put("dist", dist);
        values.put("time", time);
        values.put("date", date);

        if (cursor.moveToFirst()) {
            // Row with the same user_id and date exists, update the values
            long rowId = db.update("Data", values, "user_id = ? AND date = ?", new String[]{String.valueOf(userId), date});

            if (rowId != -1) {
                // Log.d(TAG, "Data updated successfully");
            } else {
                Log.e(TAG, "Failed to update data in the database");
            }
        } else {
            // No row with the same user_id and date, insert a new row
            long rowId = db.insert("Data", null, values);

            if (rowId != -1) {
                // Log.d(TAG, "Data inserted successfully");
            } else {
                Log.e(TAG, "Failed to insert data into the database");
            }
        }

        cursor.close();
        // db.close(); // Don't close the database here, as it may cause issues if the cursor is still in use.

    }

    private void adddata() { // Add data to dataset
        helper.insert(stepCount, kcalTotais, distCount, timeCount, currentTime.toString());
    }

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

    /* ---------------------------------------------------------- Data processing ----------------------------------------------------------------------*/

    private void stepCounter(double xValue, double yValue, double zValue) {

        DateFormat format = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String date = format.format(Calendar.getInstance().getTime());
        int newmin = Integer.valueOf(date.substring(3,5));
        int newhour = Integer.valueOf(date.substring(0,1));
        int newsec = Integer.valueOf(date.substring(6,8));
        //Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);

        Double x = xValue;
        Double y = yValue;
        Double z = zValue;

        //Count of steps

        double MagnitudePrevious = Math.sqrt(dadosAnteriores[0] * dadosAnteriores[0] + dadosAnteriores[1] * dadosAnteriores[1] + dadosAnteriores[2] * dadosAnteriores[2]);
        double Magnitude = Math.sqrt(x * x + y * y + z * z);
        double MagnitudeDelta = Magnitude - MagnitudePrevious;
        agregadoMagnitudes += abs(MagnitudeDelta);
        encherDados(x, y, z);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentTime = calendar.getTime();


        if (MagnitudeDelta >= 15 && MagnitudeDelta <= 90)
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

    private void calculateKcal(String date) {
        if (configPreferences != null) {
            Weight = configPreferences.getInt(WEIGHT,70);
        } else {

            Weight = 70; // or any other appropriate default value

        }
        DatabaseHelper helper = new DatabaseHelper(this);

        // Check if a row with the same user_id and date already exists
       int steps = helper.getStepsForUserAndDate(suserId,date);
       int dist= helper.getDistsForUserAndDate(suserId,date);

       int height= helper.targetValue(suserId,"height");
       int weight= helper.targetValue(suserId,"weight");
       int age= helper.targetValue(suserId,"age");
       String gender= helper.targetGender(suserId);
       double bmr;
       if(gender=="Male")
       {
           bmr= (13.75*weight) + (5*height) - (6.76*age) + 66;
       } else if (gender=="Female") {
           bmr= (9.56*weight) + (1.85*height) -(4.68*age)+ 655;
       }
       else{
           bmr= 834;

       }
       double T;
       T=dist/4000;

        kcalTotais = (bmr/24)*3.80*T;
    }

    private void distance(){

        if (configPreferences != null) {
            Height = configPreferences.getInt(HEIGHT, 165);
            // Rest of your code
        } else {
            // Handle the case where configPreferences is null, perhaps by providing a default value or logging an error
            // For example:
            Log.e("YourTag", "configPreferences is null");
            Height = 165; // or any other appropriate default value

        }
        if (configPreferences != null) {
            Gender = configPreferences.getString(GENDER, "Male");
            // Rest of your code
        } else {
            // Handle the case where configPreferences is null, perhaps by providing a default value or logging an error
            // For example:
            String defaultValue = "Male"; // or any other appropriate default value
            Log.e("YourTag", "configPreferences is null");
            // You might want to set a default value or take appropriate action
            // For example:
            Gender = defaultValue;
        }

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
