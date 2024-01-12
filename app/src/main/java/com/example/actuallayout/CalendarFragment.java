package com.example.actuallayout;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.Calendar;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class CalendarFragment extends Fragment {

    CalendarView calendarView;
    Calendar calendar;
    TextView textViewSelectedDate;
    TextView textViewSelectedDateSteps;
    TextView textViewSelectedDatekms;
    TextView textViewSelectedDateKcal;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_USER_ID = "userId";
    private String mParam1;
    private String mParam2;
    private long mUserId; // Store the user ID received from SignUpActivity

    private ProgressBar calendarProgressBar;
    private ObjectAnimator animatebar;

    private int savedStepsValue = 0;
    private int stepGoal;
    private int progress;
    private StatisticsFragment statisticsFragment;


    public static CalendarFragment newInstance(String param1, String param2, long userId) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putLong(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mUserId = getArguments().getLong(ARG_USER_ID);
        } else {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        textViewSelectedDate = view.findViewById(R.id.textViewSelectedDate);
        textViewSelectedDateSteps = view.findViewById(R.id.textViewSelectedDateSteps);
        textViewSelectedDatekms = view.findViewById(R.id.textViewSelectedDatekms);
        textViewSelectedDateKcal = view.findViewById(R.id.textViewSelectedDateKcal);
        calendar = Calendar.getInstance();

        stepGoal = fetchStepGoal();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendarView.setDate(calendar.getTimeInMillis(), true, true);

        // Display the current date in the TextView
        calendar.set(currentYear, currentMonth, currentDay); // Set the current date
        String currentDate = formatDate(calendar);
        textViewSelectedDate.setText(currentDate);
        fetchDataForSelectedDate(currentDate);
        animateProgressBar(progress);


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, day);
                String selectedDate = formatDate(selectedCalendar);
                textViewSelectedDate.setText(selectedDate);
                fetchDataForSelectedDate(selectedDate);

                animateProgressBar(progress);
            }
        });



        calendarProgressBar = view.findViewById(R.id.calendar_progress_bar);

        animateProgressBar(progress);
        statisticsFragment = StatisticsFragment.newInstance(mParam1,mParam2,mUserId);
        Calendar currentDateCalendar = Calendar.getInstance();
        currentDateCalendar.set(currentYear, currentMonth, currentDay);



        return view;
    }
    private String formatDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd 00:00:00 'GMT'ZZZZZ yyyy", Locale.UK);
        return sdf.format(calendar.getTime());
    }

    private void animateProgressBar(int progressValue) {
        animatebar = ObjectAnimator.ofInt(calendarProgressBar, "progress", 0, progressValue);
        animatebar.setDuration(1000);
        animatebar.start();
    }
    private void fetchDataForSelectedDate(String selectedDate) {
        // Assuming you have a DatabaseHelper instance named dbHelper
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        String query = "SELECT SUM(steps) AS steps, SUM(cal) AS cal, SUM(dist) AS dist FROM Data WHERE date = ? AND user_id = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{selectedDate, String.valueOf(mUserId)});

        try {
            // Check if the cursor is not null and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve values from the cursor
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));

                int distance = cursor.getInt(cursor.getColumnIndexOrThrow("dist"));
                int calories =(int) ((834/24)*3.80*distance/4000);

                if (steps < stepGoal){
                    double progressFraction = (double) steps / stepGoal;
                    progress = (int) Math.ceil(progressFraction * 100);
                }else{
                    progress = 100;
                }

                textViewSelectedDateSteps.setText(steps + "/" + stepGoal);
                textViewSelectedDatekms.setText(distance+" m");
                textViewSelectedDateKcal.setText(calories+" kcal");
            }
        } catch (Exception e) {
            // Log any exception that occurs
            Log.e("CalendarFragment", "Error fetching data for selected date", e);
        } finally {
            // Close the cursor to release resources
            if (cursor != null) {
                cursor.close();
            }

            // Close the database connection
            dbHelper.close();
        }
    }

    private int fetchStepGoal() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        int stepGoal = 0;

        // Use a Cursor to retrieve data from the database
        String query = "SELECT stepGoal FROM users WHERE id = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{String.valueOf(mUserId)});

        try {
            // Check if the cursor is not null and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve the stepGoal value from the cursor
                stepGoal = cursor.getInt(cursor.getColumnIndexOrThrow("stepGoal"));
            }
        } catch (Exception e) {
            // Log any exception that occurs
            Log.e("CalendarFragment", "Error fetching stepGoal", e);
        } finally {
            // Close the cursor to release resources
            if (cursor != null) {
                cursor.close();
            }

            // Close the database connection
            dbHelper.close();
        }

        return stepGoal;
    }



}



