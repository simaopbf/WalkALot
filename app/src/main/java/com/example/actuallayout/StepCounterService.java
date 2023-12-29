package com.example.actuallayout;

import android.app.IntentService;
import android.content.Intent;

import com.example.actuallayout.SQLiteManager;
import Bio.Library.namespace.BioLib;
import java.util.Calendar;

public class StepCounterService extends IntentService {

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

    public StepCounterService() {
        super("StepCounterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("AccelerationData")) {
            BioLib.DataACC dataACC = intent.getParcelableExtra("AccelerationData");
            handleAccelerationData(dataACC);
        }
    }

    private void handleAccelerationData(BioLib.DataACC dataACC) {
        if (dataACC != null) {
            double x = dataACC.X;
            double y = dataACC.Y;
            double z = dataACC.Z;

            // Count steps
            stepCounter(x, y, z);

            // Other processing if needed

            // Insert acceleration data into SQLite
            insertAccelerationData(x, y, z);
        }
    }

    private void insertAccelerationData(double xAxis, double yAxis, double zAxis) {
        sqLiteManager = new SQLiteManager(this);
        sqLiteManager.open();
        sqLiteManager.insertAccelerationData(xAxis, yAxis, zAxis);
        sqLiteManager.close();
    }

    private void stepCounter(double accelerationX, double accelerationY, double accelerationZ) {
        double magnitudePrevious = Math.sqrt(previousData[0] * previousData[0]
                + previousData[1] * previousData[1] + previousData[2] * previousData[2]);
        double magnitude = Math.sqrt(accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ);
        double magnitudeDelta = magnitude - magnitudePrevious;
        aggregatedMagnitudes += Math.abs(magnitudeDelta);

        if (magnitudeDelta >= STEP_THRESHOLD_LOW && magnitudeDelta <= STEP_THRESHOLD_HIGH) {
            stepCount++;
            notMovingCounter = 0;
            status = 1; // Standing still
        } else if (magnitudeDelta > STEP_THRESHOLD_HIGH) {
            stepCount++;
            notMovingCounter = 0;
            status = 2; // Walking
        } else {
            notMovingCounter++;
            walkingCounter = 0;
            if (notMovingCounter >= NOT_MOVING_THRESHOLD) {
                status = 0; // Not moving
            }
        }

        // Store current data for the next iteration
        encherDados(accelerationX, accelerationY, accelerationZ);
    }

    private void encherDados(double x, double y, double z) {
        previousData[0] = x;
        previousData[1] = y;
        previousData[2] = z;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sqLiteManager != null) {
            sqLiteManager.close();
        }
    }
}