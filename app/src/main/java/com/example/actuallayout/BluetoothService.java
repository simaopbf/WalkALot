package com.example.actuallayout;

import static android.app.Service.START_REDELIVER_INTENT;
import static android.app.Service.START_STICKY;
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

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import Bio.Library.namespace.BioLib;

public class BluetoothService extends AppCompatActivity {

    private BioLib lib = null;
    private String address = "";
    private String macaddress = "";
    private String mConnectedDeviceName = "";
    private BluetoothDevice deviceToConnect;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private TextView text;
    private Button buttonConnect;
    private Button mainButton;
    public static boolean isConn = false;
    private BioLib.DataACC dataACC = null;
    private byte accSensibility = 1;    // NOTE: 2G= 0, 4G= 1
    private String accConf = "";
    private TextView textACC;
    private long mUserId;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private MainActivity mainActivity;
    private static final int MY_BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

    //Fui buscar
    // ACC data variables

    private static Double[] dadosAnteriores = {0.0,0.0,0.0};
    private static double agregadoMagnitudes = 0.0;
    private static Double kcalTotais;
    private static int status = 0;
    private static Integer stepCount = 0;
    private static Integer distCount = 0;
    private static Integer timeCount = 0;
    private static Integer sec = 0;
    private static Integer min = 0;
    private static Integer hour = 0;
    private int runningCounter = 0;
    private int walkingCounter = 0;
    private int notMovingCounter = 0;


    // DB

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

//acabei buscar





    // Open the database connection
    private void openDatabase() {
        mDatabase = dbHelper.getWritableDatabase();
    }

    // Close the database connection
    private void closeDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_intermediate);

        dbHelper = new DatabaseHelper(this);
        openDatabase();  // Open the database when the activity is created

        initPreferencesAndGoals();

        Log.d("BluetoothService", "BluetoothService onCreate");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        text = findViewById(R.id.lblStatus);
        text.setText("");

        // MACADDRESS:
        address = "00:23:FE:00:0B:34";

        try {
            lib = new BioLib(this, mHandler);
            text.append("Init BioLib \n");
        } catch (Exception e) {
            text.append("Error to init BioLib \n");
            e.printStackTrace();
        }



        buttonConnect = findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Connect();
            }

            private void Connect() {
                try {
                    Log.d("BluetoothService", "Connect button clicked");
                    Toast.makeText(getApplicationContext(), "Connect button clicked", Toast.LENGTH_SHORT).show();

                    try {
                        deviceToConnect = lib.mBluetoothAdapter.getRemoteDevice(address);
                        Reset();
                        text.setText("");
                        lib.Connect(address, 5);

                    } catch (Exception e) {
                        text.setText("Error to connect device: " + address);
                        e.printStackTrace();
                    }
                    } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            });

        buttonConnect.setEnabled(false);  // Disable the button initially

        mainButton = findViewById(R.id.buttonMain);
        mainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

        // Start MainActivity and pass the user ID
        Intent intent = new Intent(BluetoothService.this, MainActivity.class);
        // Retrieve userId from Intent
        mUserId = getIntent().getLongExtra("userId", -1);
        intent.putExtra("userId", mUserId);
        startActivity(intent);}
            });
    }

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

        dbHelper = new DatabaseHelper(this);
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

    public int onStartCommand(Intent intent, int flags, int startId) {

        // Time

        DateFormat format = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String now = format.format(Calendar.getInstance().getTime());
        min = Integer.valueOf(now.substring(3, 5));
        hour = Integer.valueOf(now.substring(0, 2));
        sec = Integer.valueOf(now.substring(6, 8));

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

        dbHelper = new DatabaseHelper(this);
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
        return START_STICKY;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BioLib.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled
                Toast.makeText(getApplicationContext(), "Bluetooth is now enabled!", Toast.LENGTH_SHORT).show();
                text.append("Bluetooth is now enabled \n");
                text.append("Macaddress selected: " + address + " \n");
                buttonConnect.setEnabled(true);
            } else {
                // User declined to enable Bluetooth, handle accordingly
                Toast.makeText(getApplicationContext(), "Bluetooth not enabled!", Toast.LENGTH_SHORT).show();
                text.append("Bluetooth not enabled \n");
                isConn = false;
                buttonConnect.setEnabled(false);
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
             /*   case BioLib.MESSAGE_READ:
                    textDataReceived.setText("RECEIVED: " + msg.arg1);
                    break;
*/
                case BioLib.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    text.append("Connected to " + mConnectedDeviceName + " \n");
                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
                    Toast.makeText(getApplicationContext(), "Bluetooth NOT supported. Aborting! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth NOT supported. Aborting! \n");
                    isConn = false;
                    break;

                case BioLib.MESSAGE_BLUETOOTH_ENABLED:
                    Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth is now enabled \n");
                    text.append("Macaddress selected: " + address + " \n");
                    buttonConnect.setEnabled(true);

                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_ENABLED:
                    Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth not enabled \n");
                    isConn = false;
                    break;

                case BioLib.REQUEST_ENABLE_BT:
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, BioLib.REQUEST_ENABLE_BT);
                    text.append("Request bluetooth enable \n");
                    break;

                case BioLib.STATE_CONNECTING:
                    text.append("   Connecting to device ... \n");
                    break;

                case BioLib.STATE_CONNECTED:
                    Toast.makeText(getApplicationContext(), "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show();
                    text.append("   Connect to " + deviceToConnect.getName() + " \n");
                    isConn = true;

                    buttonConnect.setEnabled(false);

                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Toast.makeText(getApplicationContext(), "Unable to connect device! ", Toast.LENGTH_SHORT).show();
                    text.append("   Unable to connect device \n");
                    isConn = false;

                    buttonConnect.setEnabled(true);


                    break;

                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    Toast.makeText(getApplicationContext(), "Device connection was lost", Toast.LENGTH_SHORT).show();
                    text.append("   Disconnected from " + deviceToConnect.getName() + " \n");
                    isConn = false;

                    buttonConnect.setEnabled(true);
                    break;

                /*case BioLib.MESSAGE_PUSH_BUTTON:
                    DATETIME_PUSH_BUTTON = (Date)msg.obj;
                    numOfPushButton = msg.arg1;
                    textPUSH.setText("PUSH-BUTTON: [#" + numOfPushButton + "]" + DATETIME_PUSH_BUTTON.toString());
                    break;

                case BioLib.MESSAGE_RTC:
                    DATETIME_RTC = (Date)msg.obj;
                    textRTC.setText("RTC: " + DATETIME_RTC.toString());
                    break;

                case BioLib.MESSAGE_TIMESPAN:
                    DATETIME_TIMESPAN = (Date)msg.obj;
                    textTimeSpan.setText("SPAN: " + DATETIME_TIMESPAN.toString());
                    break;

                case BioLib.MESSAGE_DATA_UPDATED:
                    BioLib.Output out = (BioLib.Output)msg.obj;
                    BATTERY_LEVEL = out.battery;
                    textBAT.setText("BAT: " + BATTERY_LEVEL + " %");
                    PULSE = out.pulse;
                    textPULSE.setText("HR: " + PULSE + " bpm     Nb. Leads: " + lib.GetNumberOfChannels());
                    break;

                case BioLib.MESSAGE_SDCARD_STATE:
                    SDCARD_STATE = (int)msg.arg1;
                    if (SDCARD_STATE == 1)
                        textSDCARD.setText("SD CARD STATE: ON");
                    else
                        textSDCARD.setText("SD CARD STATE: OFF");
                    break;

                case BioLib.MESSAGE_RADIO_EVENT:
                    textRadioEvent.setText("Radio-event: received ... ");

                    typeRadioEvent = (byte)msg.arg1;
                    infoRadioEvent = (byte[]) msg.obj;

                    String str = "";
                    try {
                        str = new String(infoRadioEvent, "UTF8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    textRadioEvent.setText("Radio-event: " + typeRadioEvent + "[" + str + "]");
                    break;

                case BioLib.MESSAGE_FIRMWARE_VERSION:
                    // Show firmware version in device VitalJacket ...
                    firmwareVersion = (String)msg.obj;
                    break;

                case BioLib.MESSAGE_DEVICE_ID:
                    deviceId = (String)msg.obj;
                    textDeviceId.setText("Device Id: " + deviceId);
                    break; */

                case BioLib.MESSAGE_ACC_SENSIBILITY:
                    accSensibility = (byte)msg.arg1;
                    accConf = "4G";
                    switch (accSensibility)
                    {
                        case 0:
                            accConf = "2G";
                            break;

                        case 1:
                            accConf = "4G";
                            break;
                    }

                    textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
                    break;

              case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;
                    break;

                case BioLib.MESSAGE_ACC_UPDATED:
                    dataACC = (BioLib.DataACC)msg.obj;
                    dataReady();
                    break;


               /* case BioLib.MESSAGE_ECG_STREAM:
                    try {
                        textECG.setText("ECG received");
                        ecg = (byte[][]) msg.obj;
                        int nLeads = ecg.length;
                        nBytes = ecg[0].length;
                        //   textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);

                        // Store ECG data in SQLite database
                        // Convert ECG data to a suitable format (e.g., String)

                        String ecgDataString = convertECGDataToString(ecg);
                        handleECGDataReceived(ecg);

                        // Store ECG data in SQLite database*/


                   /* } catch (Exception ex) {
                        //textECG.setText("ERROR in ecg stream");
                    }
                    break;*/

                case BioLib.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private void Reset() {
        try {
            text.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (lib.mBluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                lib.mBluetoothAdapter.cancelDiscovery();
            }
        }
        if(dataACC != null) {
            adddata();
        }

        lib = null;
    }
    boolean isBluetoothConnected() {
        return isConn;  // Assuming isConn is a boolean variable that indicates the Bluetooth connection status
    }

    private void dataReady()
    {

        stepCounter();
        calculateKcal();

        sendDataToActivity(stepCount, kcalTotais, distCount,timeCount, status);

        createNotificationChannel();

        if(stepCount == Steps)
        {
            goalsNotification("Steps",R.drawable.age);
            //mudar para steps_icon
        }
        else if (Math.round(kcalTotais) == Calories)
        {
            goalsNotification("Calories",R.drawable.age);
            //mudar para calicon
        }
        else if (distCount == Distance)
        {
            goalsNotification("Distance",R.drawable.age);
            //mudar para distanceicon
        }

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

    private void adddata() { // Add data to dataset
        dbHelper.insert(stepCount, kcalTotais, distCount, timeCount, currentTime.toString());
    }

    private void goalsNotification(String message, int icon){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID2);
        builder.setContentTitle("Congratulations!");
        builder.setContentTitle("You have accomplished your " + message + " goal!");
        builder.setAutoCancel(true); // True for swipe.
        builder.setSmallIcon(icon);
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(2,builder.build());

    }












    /* ---------------------------------------------------------- Data processing ----------------------------------------------------------------------*/

    private void stepCounter() {

        DateFormat format = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String date = format.format(Calendar.getInstance().getTime());
        int newmin = Integer.valueOf(date.substring(3,5));
        int newhour = Integer.valueOf(date.substring(0,1));
        int newsec = Integer.valueOf(date.substring(6,8));

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