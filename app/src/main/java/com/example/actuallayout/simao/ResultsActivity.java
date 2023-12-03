package com.example.actuallayout.simao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.actuallayout.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "Results";
    private final Handler acquisition = new Handler();

    // View variables

    private ProgressBar stepsBar;
    private ProgressBar calsBar;
    private ProgressBar distBar;
    private ProgressBar timeBar;

    // Goals variables

    private int stepGoal;
    private int calGoal;
    private int distGoal;
    private int timeGoal;
    private String timeU; // U for the string Unit (e.g., h or min)
    private String distU;

    // Text views

    private TextView stepsT;
    private TextView calT;
    private TextView distT;
    private TextView timeT;

    private TextView batteryView;
    private TextView statusView;

    // Decimal format

    private DecimalFormat df;

    // Chart

    private GraphDisplay graph;
    private LineDataSet weekSteps;
    private LineChart chart;
    private int xPoint = 6;

    // Color

    private int StepsDataPointsColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        df = new DecimalFormat("#.#");

        batteryView = findViewById(R.id.battery);
        statusView = findViewById(R.id.statusTxt);

        // Goals

        SharedPreferences goalPref = getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);

        stepGoal = goalPref.getInt(STEPS_GOAL, 10000);
        calGoal = goalPref.getInt(CAL_GOAL, 500);
        distGoal = goalPref.getInt(DIST_GOAL, 8);
        timeGoal = goalPref.getInt(TIME_GOAL, 1);
        distU = goalPref.getString(DIST_GOAL_U, "Km");
        timeU = goalPref.getString(TIME_GOAL_U,"h");

        // Importing color scheme from Resource Files: res/values/colors.xml

        StepsDataPointsColor = ContextCompat.getColor(this, R.color.DataPointVal);
        int ProgBarColor = ContextCompat.getColor(this, R.color.MainColor);

        /* ------------------------------- ProgressBar Views ------------------------------- */

        stepsBar = findViewById(R.id.progBarSteps);
        calsBar = findViewById(R.id.progBarCal);
        distBar = findViewById(R.id.progBarDist);
        timeBar = findViewById(R.id.progBarTime);

        /* ------------------------------- Week data ------------------------------- */

        // Steps

        ArrayList<Entry> steps = new ArrayList<>();

        steps.add(new Entry(0, 12));
        steps.add(new Entry(1, 42));
        steps.add(new Entry(2, 90));
        steps.add(new Entry(3,57));
        steps.add(new Entry(4, 54));
        steps.add(new Entry(5, 6));
        steps.add(new Entry(6, 0));

        // Calories

        ArrayList<Entry> calories = new ArrayList<>();

        calories.add(new Entry(0, 1));
        calories.add(new Entry(1, 3));
        calories.add(new Entry(2, 5));
        calories.add(new Entry(3, 2));
        calories.add(new Entry(4, 2));
        calories.add(new Entry(5, 0));
        calories.add(new Entry(6, 0));

        // Distance

        ArrayList<Entry> distance = new ArrayList<>();

        distance.add(new Entry(0,10));
        distance.add(new Entry(1, 35));
        distance.add(new Entry(2, 80));
        distance.add(new Entry(3, 50));
        distance.add(new Entry(4, 45));
        distance.add(new Entry(5, 4));
        distance.add(new Entry(6, (float) 0));

        // Time

        ArrayList<Entry> time = new ArrayList<>();

        time.add(new Entry(0, 1));
        time.add(new Entry(1, 2));
        time.add(new Entry(2, 4));
        time.add(new Entry(3, 2));
        time.add(new Entry(4, 2));
        time.add(new Entry(5, 0));
        time.add(new Entry(6, 0));

        /* ------------------------------- Text Views ------------------------------- */

        stepsT = findViewById(R.id.stepsTxt);
        calT = findViewById(R.id.calTxt);
        distT = findViewById(R.id.distTxt);
        timeT = findViewById(R.id.timeTxt);

        // Setting TextViews and Progress with the latest day values

        int arraySz = steps.size() - 1;

        /* ------------------------------- Weekly Chart ------------------------------- */

        chart = (LineChart) findViewById(R.id.graph);

        graph = new GraphDisplay(this);
        weekSteps = graph.chartSetUp(chart, steps, StepsDataPointsColor, ProgBarColor, 1);
        graph.updateUpperThreshold(0, StepsDataPointsColor);
        graph.graphFade(weekSteps, R.drawable.fade_red, StepsDataPointsColor);

        // Click listener for the value selected on the graph

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                float dayStep = e.getY();

                xPoint = (int) e.getX();


                int idx = 0;
                for (int i = 0; i <= arraySz; i++) {
                    if (dayStep == steps.get(i).getY()) {
                        idx = i;
                    }
                }

                float dayCal = calories.get(idx).getY();
                int dayDist = (int) distance.get(idx).getY();
                int dayTime = (int) time.get(idx).getY();

                stepsT.setText(String.valueOf((int) dayStep));
                calT.setText(dayCal + " Kcal");
                distT.setText(dayDist + " m");
                timeT.setText(dayTime + " min");

                updateBarProgress(dayStep, dayCal, (float) dayDist,(float) dayTime);

            }

            @Override
            public void onNothingSelected() {

            }
        });



        dataStream();
        batteryStream();





        /* ------------------------------- Goal Button ------------------------------- */

        Button goalsButton = findViewById(R.id.goalsBtn);
        goalsButton.setOnClickListener(view -> {
            Intent openGoals = new Intent(getApplicationContext(), GoalsActivity.class);
            startActivity(openGoals);
        });

        /* ------------------------------- Config Button ------------------------------- */

        Button configButton = findViewById(R.id.configBtn);
        configButton.setOnClickListener(view -> {
            Intent openConfig = new Intent(getApplicationContext(), ConfigActivity.class);
            startActivity(openConfig);

        });
    }

    public void dataStream() { //


        BroadcastReceiver dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                // Get data included in the Intent from service

                Bundle b = intent.getBundleExtra("AppData");

                int updatedSteps = b.getInt("Steps", 0);
                double updatedCal = b.getDouble("Kcal", 0);
                int updatedDist = b.getInt("Dist",0);
                int updatedTime = b.getInt("Time",0);

                if (xPoint == 6) { // Receive intent with data and update the Progress only if the nothing is selected or the current's day value is selected

                    stepsT.setText(String.valueOf(updatedSteps));
                    calT.setText(df.format(updatedCal) + " Kcal");
                    distT.setText(updatedDist + " m");
                    timeT.setText(updatedTime + " min");

                    updateBarProgress((float) updatedSteps, (float) updatedCal, (float) updatedDist, (float) updatedTime);
                }

                // Keeps updating Chart

                weekSteps.removeLast();
                weekSteps.addEntry(new Entry(6,updatedSteps));
                weekSteps.notifyDataSetChanged(); // Let the data know a dataSet changed
                graph.updateUpperThreshold(updatedSteps, StepsDataPointsColor);
                chart.notifyDataSetChanged(); // Let the chart know it's data changed
                chart.invalidate(); // Refresh

                // Status detection

                Drawable statusDraw = null;

                // Get data included in the Intent from service

                int st = b.getInt("Status", 0);

                try {
                    if (st == 0)
                    {
                        statusDraw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restingicon);
                    }
                    else if(st == 1)
                    {
                        statusDraw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.walkingicon);
                    }
                    else
                    {
                        statusDraw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.runningicon);
                    }

                    statusView.setCompoundDrawablesWithIntrinsicBounds(null, statusDraw, null,null);

                }catch (Exception ex){System.out.println("Exception: " + ex.getMessage());}
            }
        };

        // Register the service

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                dataReceiver, new IntentFilter("Update UI"));

    }


    public void batteryStream() { // Battery

        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Drawable batLevel = null;

                // Get data included in the Intent from service

                Bundle b = intent.getBundleExtra("BatData");

                int battery = b.getInt("Battery", 0);

                if (battery >= 80 && battery <= 100)
                {
                    batLevel = ContextCompat.getDrawable(getApplicationContext(), R.drawable.highbat);
                }
                else if(battery >= 50 && battery < 80)
                {
                    batLevel = ContextCompat.getDrawable(getApplicationContext(), R.drawable.lowhighbat);
                }
                else if(battery >= 25 && battery < 50)
                {
                    batLevel = ContextCompat.getDrawable(getApplicationContext(), R.drawable.lowmidbat);
                }
                else
                {
                    batLevel = ContextCompat.getDrawable(getApplicationContext(), R.drawable.lowbat);
                }

                // Resize drawable

                Bitmap bitmap = ((BitmapDrawable) batLevel).getBitmap();
                Drawable drawBat = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 45, 20, true));
                batteryView.setCompoundDrawablesWithIntrinsicBounds(null, drawBat, null,null);
                batteryView.setCompoundDrawablePadding(6);
                batteryView.setText("VJ: "+ battery + " %");
            }
        };

        // Register the service

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                batteryReceiver, new IntentFilter("Update Battery UI"));
    }




    public void updateBarProgress(float dayStep, float dayCal, float dayDist, float dayTime){

        // Updates ProgressBars based on previously set goals

        stepsBar.setProgress(Math.round(dayStep/stepGoal * 100));
        calsBar.setProgress(Math.round(dayCal/calGoal * 100));
        if(distU.equals("Km"))
        {
            distBar.setProgress(Math.round((dayDist/(distGoal*1000)) * 100));
        }
        else
        {
            distBar.setProgress(Math.round((dayDist/(distGoal)) * 100));
        }

        if(timeU.equals("h"))
        {
            timeBar.setProgress(Math.round((dayTime/(timeGoal*60)) * 100));
        }
        else
        {
            timeBar.setProgress(Math.round(dayTime/timeGoal * 100));
        }


    }



}