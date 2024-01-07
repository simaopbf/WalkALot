package com.example.actuallayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import android.widget.TextView;
import android.util.Log;
import android.graphics.Color;
import android.widget.CalendarView;
import android.widget.Toast;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
            // Handle the case where user ID is not provided
            // You may want to show an error message or navigate to a different screen

            // mUserId = mUserId;

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

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendarView.setDate(calendar.getTimeInMillis(), true, true);

        // Display the current date in the TextView
        String currentDate = currentDay + "/" + (currentMonth + 1) + "/" + currentYear;
        textViewSelectedDate.setText(currentDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int day) {
                String selectedDate = day + "/" + (month + 1) + "/" + year;
                textViewSelectedDate.setText(selectedDate);

                //Log.d("CalendarFragment", selectedDate);

                fetchDataForSelectedDate(selectedDate);
            }
        });

        return view;
    }
    private void fetchDataForSelectedDate(String selectedDate) {
        // Assuming you have a DatabaseHelper instance named dbHelper
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

        // Use a Cursor to retrieve data from the database
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT SUM(steps) AS steps, SUM(cal) AS cal, SUM(dist) AS dist FROM Data WHERE date = ?", new String[]{selectedDate});

        try {
            // Check if the cursor is not null and move to the first row
            if (cursor != null && cursor.moveToFirst()) {
                // Retrieve values from the cursor
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow("cal"));
                int distance = cursor.getInt(cursor.getColumnIndexOrThrow("dist"));

                // Display the retrieved values in your UI elements (e.g., TextViews)
                // For example:
                textViewSelectedDateSteps.setText("Steps: " + steps);
                textViewSelectedDatekms.setText("Kms: " + distance);
                textViewSelectedDateKcal.setText("Calories: " + calories);
            } else {
                // Handle the case where there is no data for the selected date
                // Clear or set default values in your UI elements
                textViewSelectedDateSteps.setText("Steps: N/A");
                textViewSelectedDatekms.setText("Kms: N/A");
                textViewSelectedDateKcal.setText("Calories: N/A");
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


}



