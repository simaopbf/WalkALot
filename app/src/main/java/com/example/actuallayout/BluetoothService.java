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
    private static BioLib.DataACC dataACC = null;
    private byte accSensibility = 1;    // NOTE: 2G= 0, 4G= 1
    private String accConf = "";
    private TextView textACC;
    private long mUserId;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private MainActivity mainActivity;
    private BluetoothAdapter bluetoothAdapter;

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

    // DB

    private Date currentTime;


    private static BluetoothService instance;
    private static final String TAG = "BluetoothService";


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

        instance = this;
        dbHelper = new DatabaseHelper(this);
          // Open the database when the activity is created


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
                    //Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);

                    break;

              case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;
                    break;

                case BioLib.MESSAGE_ACC_UPDATED:
                    dataACC = (BioLib.DataACC)msg.obj;
                    //Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);
                    dbHelper.addACCData(dataACC);
                    //dataReady();
                    break;

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
            Log.d(TAG, "MESSAGE_ACC_UPDATED - DataACC received: " + dataACC.X + ", " + dataACC.Y + ", " + dataACC.Z);
            adddata();
        }

        lib = null;
    }
    boolean isBluetoothConnected() {
        return isConn;
    }

    // Expose a method to get data



    private void adddata() { // Add data to dataset
        openDatabase();
        dbHelper.insert(stepCount, kcalTotais, distCount, timeCount, currentTime.toString());

    }

}