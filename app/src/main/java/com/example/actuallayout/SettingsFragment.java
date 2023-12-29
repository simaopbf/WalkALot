package com.example.actuallayout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import android.widget.NumberPicker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_USER_ID = "userId";

    private static final String STATE_WEIGHT = "state_weight";
    private static final String STATE_HEIGHT = "state_height";
    private static final String STATE_AGE = "state_age";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static int weightInp;
    private static int heightInp;
    private static int ageInp;
    private long mUserId; // Store the user ID received from SignUpActivity
    NumberPicker weightPick;
    NumberPicker heightPick;
    NumberPicker agePick;
    Button save_btn2;
    DatabaseHelper dbHelper;


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2, long userId) {
        SettingsFragment fragment = new SettingsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        dbHelper = new DatabaseHelper(requireContext());

        weightPick = view.findViewById(R.id.weightPick);
        heightPick = view.findViewById(R.id.heightPick);
        agePick = view.findViewById(R.id.agePick);
        save_btn2 = view.findViewById(R.id.save_btn2);

        weightPick.setMinValue(30);
        weightPick.setMaxValue(200);
        weightPick.setValue(60);

        heightPick.setMinValue(100);
        heightPick.setMaxValue(200);
        heightPick.setValue(150);


        agePick.setMinValue(10);
        agePick.setMaxValue(100);
        agePick.setValue(25);


        weightPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker weightPick, int oldValue, int newValue) {
                weightInp = newValue;

            }
        });


        heightPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker heightPick, int oldValue, int newValue) {
                heightInp = newValue;
            }
        });


        agePick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker agePick, int oldValue, int newValue) {
                ageInp = newValue;
            }
        });

        save_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserSettings();
            }
        });


    }


    private void saveUserSettings() {
        if (mUserId != -1) {
            // Retrieve the values from NumberPickers or other UI elements
            String genderInp = "Male"; // Replace with your logic to get the gender

            // Update the user settings in the database
            int rowsAffected = (int) dbHelper.insertSettings(genderInp, heightInp, weightInp, ageInp);

            if (rowsAffected > 0) {
                // Settings updated successfully
                Toast.makeText(requireContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                // No user found with the given ID, handle this case if needed
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where user ID is not valid
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
        }
    }
}


