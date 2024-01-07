package com.example.actuallayout;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.animation.ObjectAnimator;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_USER_ID = "userId";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private long mUserId;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2, long userId) {
        HomeFragment fragment = new HomeFragment();
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
            mUserId = -1;

        }
        Log.d("verificarerrohome", "id:" + mUserId);
    }
    private ProgressBar homeProgressBar;
    private ObjectAnimator animatebar;
    private TextView stepsTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the ProgressBar
        homeProgressBar = view.findViewById(R.id.home_progress_bar);

        // Start your animation

        animatebar = ObjectAnimator.ofInt(homeProgressBar, "progress", 0, 75);
        animatebar.setDuration(2000);
        animatebar.start();

        // Find TextView by ID
        stepsTextView = view.findViewById(R.id.stepsTextView);

        // Retrieve steps from the database based on the current date
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        Cursor cursor = databaseHelper.getAll();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                int steps = cursor.getInt(cursor.getColumnIndex("steps"));

                // Check if the date matches the current date
                // You might need to adjust the comparison based on your date format
                if (isCurrentDate(date)) {
                    // Update the TextView with the steps value
                    stepsTextView.setText(steps + "/10000");
                    break; // No need to continue checking other records
                }
            } while (cursor.moveToNext());

            //cursor.close();
        }

        //databaseHelper.close();

        return view;
    }

    private boolean isCurrentDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());

        // Compare the date strings
        return date.equals(currentDate);
    }

}

