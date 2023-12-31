package com.example.actuallayout;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.NumberPicker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
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

    public static final String CONFIG_PREFS = "configPrefs";
    public static String WEIGHT = "weightPrefs";
    public static String AGE = "birthPrefs";
    public static String HEIGHT = "heightPrefs";
    public static String GENDER = "genderPrefs";
    public static final String TDEE = "TDEEPrefs";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static int weightInp;
    private static int heightInp;
    private static int ageInp;
    private static String genderInp = "Male";
    private long mUserId; // Store the user ID received from SignUpActivity
    private String Gender;
    private int Weight;
    private int Birth;
    private int Height;
    private int tdee;


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

           // mUserId = mUserId;

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
        RadioGroup rg = (RadioGroup) view.findViewById(R.id.genderPick);

        // Load the previously selected gender from SharedPreferences
        SharedPreferences genderPreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        String savedGender = genderPreferences.getString(GENDER, "Male"); // Default to Male if not found

        // Set the appropriate radio button checked based on the saved gender
        if (savedGender.equals("Male")) {
            rg.check(R.id.male_rbtn);
        } else if (savedGender.equals("Female")) {
            rg.check(R.id.female_rbtn);
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.male_rbtn) {
                    Gender="Male";
                }
                else if(checkedId==R.id.female_rbtn) {
                    Gender="Female";
                }

            }
        });
        save_btn2 = view.findViewById(R.id.save_btn2);

        weightPick.setMinValue(30);
        weightPick.setMaxValue(200);
        //weightPick.setValue(60);

        // Load the previously selected age from SharedPreferences -add
        SharedPreferences weightPreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        int savedWeight = weightPreferences.getInt(WEIGHT, 60);
        weightPick.setValue(savedWeight);


        heightPick.setMinValue(100);
        heightPick.setMaxValue(200);
        //heightPick.setValue(150);

        SharedPreferences heightPreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        int savedHeight = heightPreferences.getInt(HEIGHT, 150);
        heightPick.setValue(savedHeight);


        agePick.setMinValue(10);
        agePick.setMaxValue(100);
        //agePick.setValue(25);


        SharedPreferences agePreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        int savedAge = agePreferences.getInt(AGE, 25);
        agePick.setValue(savedAge);

        weightPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker weightPick, int oldValue, int newValue) {

                weightInp = newValue;
                WEIGHT = String.valueOf(weightInp);
                Log.d("verificarerro weight", "id:" + WEIGHT);

                SharedPreferences.Editor editor = weightPreferences.edit();
                editor.putInt(WEIGHT, newValue);
                editor.apply();
            }
        });

        heightPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker heightPick, int oldValue, int newValue) {
                heightInp = newValue;
                HEIGHT= String.valueOf(heightInp);

                SharedPreferences.Editor editor = heightPreferences.edit();
                editor.putInt(HEIGHT, newValue);
                editor.apply();
            }
        });

        agePick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker agePick, int oldValue, int newValue) {
                ageInp = newValue;
                AGE = String.valueOf(ageInp);

                // Save the newly selected age in SharedPreferences -add
                SharedPreferences.Editor editor = agePreferences.edit();
                editor.putInt(AGE, newValue);
                editor.apply();

            }
        });

        save_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserSettings();
                // TDEE calculation

                tdee = (int) Math.round((10 * weightInp) + (6.25 * heightInp) - (5 - ageInp));

                if (genderInp.equals("Male"))
                {tdee = (int) Math.round((tdee + 5) * 1.15);}
                else
                {tdee = (int) Math.round((tdee - 161) * 1.15);}

                //loadData();
            }
        });


    }


    private void saveUserSettings() {

        if (mUserId != -1) {
            Log.d("verificarerro", "id:" + mUserId);
            // Retrieve the values from NumberPickers or other UI elements

            // Update the user settings in the database
            long rowsAffected = dbHelper.updateSettings(mUserId,Gender, heightPick.getValue(), weightPick.getValue(), agePick.getValue());

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

    /*public void loadData(){

        // Load config shared preferences

        SharedPreferences configPreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);

        Gender = configPreferences.getString(GENDER,"Female");
        Weight = configPreferences.getInt(WEIGHT,70);
        Birth = configPreferences.getInt(AGE, 1997);
        Height = configPreferences.getInt(HEIGHT,165);


        weightPick.setValue(Weight);
        agePick.setValue(Birth);
        heightPick.setValue(Height);


    }*/
}


