package com.example.actuallayout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;

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
    private boolean isConn = false;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private MainActivity mainActivity;
    private static final int MY_BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

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
        setContentView(R.layout.test);

        dbHelper = new DatabaseHelper(this);
        openDatabase();  // Open the database when the activity is created

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

                    // Check if Bluetooth is enabled
                    if (!lib.mBluetoothAdapter.isEnabled()) {
                        // Bluetooth is not enabled, request user to enable it
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, BioLib.REQUEST_ENABLE_BT);
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothService.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, MY_BLUETOOTH_PERMISSION_REQUEST_CODE);
                        return;
                    }
                    if (ContextCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        // Request BLUETOOTH permission
                        ActivityCompat.requestPermissions(BluetoothService.this, new String[]{Manifest.permission.BLUETOOTH}, MY_BLUETOOTH_PERMISSION_REQUEST_CODE);
                        return;
                    }
                    deviceToConnect = lib.mBluetoothAdapter.getRemoteDevice(address);
                    if (deviceToConnect == null) {
                        Log.e("BluetoothService", "Bluetooth device is null for address: " + address);
                        Toast.makeText(getApplicationContext(), "Bluetooth device is null", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Reset();
                    text.setText("");
                    Log.d("BluetoothService", "Attempting to connect to device: " + deviceToConnect.getName() + " (" + deviceToConnect.getAddress() + ")");


                    if (ContextCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothService.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_BLUETOOTH_PERMISSION_REQUEST_CODE);
                        return;
                    }
                    lib.mBluetoothAdapter.cancelDiscovery();  // Cancel discovery before connecting
                    lib.Connect(address, 5);
                } catch (Exception e) {
                    Log.e("BluetoothService", "Error connecting to device: " + e.getMessage());
                    text.setText("Error to connect device: " + address);
                    e.printStackTrace();
                }
            }
        });

        buttonConnect.setEnabled(false);  // Disable the button initially
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
                case BioLib.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    text.append("Connected to " + mConnectedDeviceName + " \n");
                    break;

                case BioLib.STATE_CONNECTED:
                    if (ContextCompat.checkSelfPermission(BluetoothService.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Request BLUETOOTH permission
                        ActivityCompat.requestPermissions(BluetoothService.this, new String[]{Manifest.permission.BLUETOOTH}, MY_BLUETOOTH_PERMISSION_REQUEST_CODE);
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show();
                    text.append("   Connect to " + deviceToConnect.getName() + " \n");
                    isConn = true;
                    buttonConnect.setEnabled(false);

                    // Send broadcast to notify MainActivity about successful connection
                    Intent connectionIntent = new Intent("bluetoothConnectionStatus");
                    connectionIntent.putExtra("isConnected", true);
                    sendBroadcast(connectionIntent);
                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Exception connectException = (Exception) msg.obj;
                    if (connectException != null) {
                        connectException.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Unable to connect device! " + connectException.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("BluetoothService", "Connect exception is null");
                        Toast.makeText(getApplicationContext(), "Unable to connect device!", Toast.LENGTH_SHORT).show();
                    }
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
                    buttonConnect.setEnabled(false);
                    break;

                case BioLib.REQUEST_ENABLE_BT:
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, BioLib.REQUEST_ENABLE_BT);
                    text.append("Request Bluetooth enable \n");
                    break;

                case BioLib.MESSAGE_READ:
                    text.append("RECEIVED: " + msg.arg1 + "\n");
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

        lib = null;
    }

}