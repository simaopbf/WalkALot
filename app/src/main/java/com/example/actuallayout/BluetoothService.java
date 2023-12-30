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
    public static boolean isConn = false;
    private BioLib.DataACC dataACC = null;
    private byte accSensibility = 1;    // NOTE: 2G= 0, 4G= 1
    private String accConf = "";
    private TextView textACC;
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
        setContentView(R.layout.activity_main_intermediate);

        dbHelper = new DatabaseHelper(this);
        openDatabase();  // Open the database when the activity is created

        Log.d("BluetoothService", "BluetoothService onCreate");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*text = findViewById(R.id.lblStatus);
        text.setText("");*/

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

              /*  case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;
                    // textHR.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");

                    dbHelper.addQRSData(qrs);
                    break; */

             /*   case BioLib.MESSAGE_ACC_UPDATED:
                    dataACC = (BioLib.DataACC)msg.obj;

                    if (accConf == "")
                        textACC.setText("ACC:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
                    else
                        textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);

                    break;

                    */


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

        lib = null;
    }
    boolean isBluetoothConnected() {
        return isConn;  // Assuming isConn is a boolean variable that indicates the Bluetooth connection status
    }
}