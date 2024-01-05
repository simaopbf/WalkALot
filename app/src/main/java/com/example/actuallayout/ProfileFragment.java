package com.example.actuallayout;

import static android.content.Context.MODE_PRIVATE;
import static com.example.actuallayout.SettingsFragment.CONFIG_PREFS;
import static com.example.actuallayout.SettingsFragment.TDEE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

    /* ------------------------------- Shared preferences  ------------------------------- */

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_USER_ID = "userId";
    public static final String GOALS_PREFS = "goalsPrefs";
    public static final String STEPS_GOAL = "stepsPrefs";
    public static final String CAL_GOAL = "calPrefs";
    public static final String TIME_GOAL= "timePrefs";
    public static final String DIST_GOAL = "distPrefs";
    public static final String TIME_GOAL_U = "timeUPrefs";
    public static final String DIST_GOAL_U = "distUPrefs";

    private String mParam1;
    private String mParam2;
    private long mUserId; // Store the user ID received from SignUpActivity
    private static int stepsInp;
    private static int distInp;
    private static int timeInp;
    private static int calsInp;
    private int Steps;
    private int Calories;
    private int Distance;
    private int Time;
    private String TimeU; // U for the string Unit (e.g., h or min)
    private String DistU;

    /* ------------------------------- Layout object variables ------------------------------- */

    private ImageView stepMinus;
    private ImageView stepPlus;
    private TextView stepTxt;

    private ImageView calMinus;
    private ImageView calPlus;
    private TextView calTxt;

    private ImageView distMinus;
    private ImageView distPlus;
    private TextView distTxt;

    private ImageView timeMinus;
    private ImageView timePlus;
    private TextView timeTxt;


    NumberPicker calsGoal;
    NumberPicker stepGoal;
    NumberPicker distGoal;
    NumberPicker timeGoal;

    Button save_btn1;
    DatabaseHelper dbHelper;


    public static ProfileFragment newInstance(String param1, String param2, long userId) {
        ProfileFragment fragment = new ProfileFragment();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TDEE
        Log.d("verificarerroprofile", "id:" + mUserId);
        dbHelper = new DatabaseHelper(requireContext());

       /* SharedPreferences configPreferences = requireContext().getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);
        int tdee = configPreferences.getInt(TDEE,2000);

        TextView TDEEtxt = view.findViewById(R.id.TDEE);
        TDEEtxt.setText("Average TDEE: " + tdee + " Kcal");

        SharedPreferences goalsPreferences = requireContext().getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = goalsPreferences.edit();*/


        /* ------------------------------- Accessing object variables ------------------------------- */

        stepGoal = view.findViewById(R.id.stepGoal);
        calsGoal = view.findViewById(R.id.calsGoal);
        distGoal = view.findViewById(R.id.distGoal);
        timeGoal = view.findViewById(R.id.timeGoal);
       // save_btn1 = view.findViewById(R.id.save_btn1);

        stepGoal.setMinValue(0);
        stepGoal.setMaxValue(200000);
        stepGoal.setValue(10000);

        calsGoal.setMinValue(0);
        calsGoal.setMaxValue(20000);
        calsGoal.setValue(500);

        distGoal.setMinValue(0);
        distGoal.setMaxValue(200000);
        distGoal.setValue(7000);

        timeGoal.setMinValue(0);
        timeGoal.setMaxValue(1440);
        timeGoal.setValue(7000);

        /*stepMinus = view.findViewById(R.id.minusSteps);
        stepPlus  = view.findViewById(R.id.plusSteps);
        stepTxt = view.findViewById(R.id.stepsEditText);

        calMinus = view.findViewById(R.id.minusKcal);
        calPlus  = view.findViewById(R.id.plusKcal);
        calTxt = view.findViewById(R.id.KcalEditText);

        distMinus = view.findViewById(R.id.minusDist);
        distPlus  = view.findViewById(R.id.plusDist);
        distTxt = view.findViewById(R.id.distEditText);

        timeMinus = view.findViewById(R.id.minusTime);
        timePlus  = view.findViewById(R.id.plusTime);
        timeTxt = view.findViewById(R.id.timeEditText);*/

        /* ------------------------------- Changing layout objects ------------------------------- */

        // Steps

        /*stepGoal.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker stepGoal, int oldValue, int newValue) {

                stepsInp = newValue;
                STEPS_GOAL = String.valueOf(stepsInp);
                Log.d("verificarerro weight", "id:" + STEPS_GOAL);


            }
        });*/

       /* stepMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = stepTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                int num = Integer.parseInt(sepStr[0]);
                if (num > 0) {
                    num = num - 500;
                }
                stepTxt.setText(num + " " + sepStr[1]);
            }
        });

        stepPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = stepTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                int num = Integer.parseInt(sepStr[0]);
                num = num + 500;
                stepTxt.setText(num + " " + sepStr[1]);
            }
        });

        // Calories

        int adjCal = (int) Math.round(tdee * 0.25);

        calMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = calTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                int num = Integer.parseInt(sepStr[0]);

                if (num > 0) {
                    if (num <= 100) {
                        num = num - 5;
                    }
                    else
                    {
                        num = num - 50;
                    }
                }
                calTxt.setText(num + " " + sepStr[1]);
            }
        });

        calPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = calTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                int num = Integer.parseInt(sepStr[0]);
                num = num + 50;
                calTxt.setText(num + " " + sepStr[1]);
            }
        });

        // Distance

        distMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = distTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                String aux = sepStr[1];

                int num = Integer.parseInt(sepStr[0]);

                if (num > 0) {
                    if (num <= 1 || checkDist(aux)) {
                        if (num == 1) {
                            num = 1000;
                        }
                        aux = "m";
                        num = num - 100;
                    } else {
                        num = num - 1;
                        aux = "Km";
                    }
                }
                distTxt.setText(num +" "+ aux);
            }
        });

        distPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = distTxt.getText().toString();
                String[] sepStr = extractDigits(numString);

                String aux = sepStr[1];

                int num = Integer.parseInt(sepStr[0]);

                if(num >= 1000 || checkDist(aux) == false){
                    if(num == 1000){num = 1;}
                    aux = "Km";
                    num = num + 1;
                }
                else{
                    num = num + 100;
                    aux = "m";
                }
                distTxt.setText(num +" "+ aux);
            }
        });

        // Time

        timeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = timeTxt.getText().toString();
                String[] sepStr = extractDigits(numString);
                String aux = sepStr[1];

                int num = Integer.parseInt(sepStr[0]);

                if (num > 0) {
                    if (num <= 1 || checkDist(aux)) {
                        if (num == 1) {
                            num = 60;
                        }
                        aux = "min";
                        num = num - 15;
                    } else {
                        num = num - 1;
                        aux = "h";
                    }
                }
                timeTxt.setText(num +" "+ aux);
            }
        });

        timePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String numString = timeTxt.getText().toString();
                String[] sepStr = extractDigits(numString);

                String aux = sepStr[1];

                int num = Integer.parseInt(sepStr[0]);

                if(num >= 60 || checkDist(aux) == false){
                    if(num == 60){num = 1;}
                    aux = "h";
                    num = num + 1;
                }
                else{
                    num = num + 15;
                    aux = "min";
                }
                timeTxt.setText(num +" "+ aux);
            }
        });*/

        save_btn1 = view.findViewById(R.id.save_btn1); //represent object como uma interface view (element in the layout), capture our button fr
        save_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserProfile();

                // Pass the saved goals to main

               /* final String[] auxSteps = extractDigits(stepTxt.getText().toString());
                final int steps = Integer.parseInt(auxSteps[0]);

                final String[] auxCal = extractDigits(calTxt.getText().toString());
                final int cals = Integer.parseInt(auxCal[0]);

                final String[] auxDist = extractDigits(distTxt.getText().toString());
                final int dist = Integer.parseInt(auxDist[0]);

                final String[] auxTime = extractDigits(timeTxt.getText().toString());
                final int time = Integer.parseInt(auxTime[0]);

                final String[] auxDistU = extractDigits(distTxt.getText().toString());
                final String distU = auxDistU[1];

                final String[] auxTimeU = extractDigits(timeTxt.getText().toString());
                final String timeU = auxTimeU[1];

                /*The static keyword means the value is the same for every instance of the class.
                The final keyword means once the variable is assigned a value it can never be changed.
                The combination of static final in Java is how to create a constant value.*/

               /* editor.putInt(STEPS_GOAL, steps);
                editor.putInt(CAL_GOAL, cals);
                editor.putInt(DIST_GOAL, dist);
                editor.putInt(TIME_GOAL, time);
                editor.putString(DIST_GOAL_U, distU);
                editor.putString(TIME_GOAL_U, timeU);

                editor.apply();

                Toast.makeText(requireContext(), "Goals saved", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(requireContext(), MainActivity.class);
                startActivity(i);*/
            }
        });

        //loadData(adjCal);
    }
    private void saveUserProfile() {

        if (mUserId != -1) {
            // Retrieve the values from NumberPickers or other UI elements



            // Update the user settings in the database
            long rowsAffected = dbHelper.updateProfile(mUserId,stepGoal.getValue(), calsGoal.getValue(), timeGoal.getValue(), distGoal.getValue());

            if (rowsAffected > 0) {
                // Settings updated successfully
                Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                // No user found with the given ID, handle this case if needed
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where user ID is not valid
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
        }
    }

    public static String[] extractDigits(String args){ // Substitutes all non-digit characters for empty chars

        String otherStr = args.replaceAll("[0-9]+", "");   // ^ means not within the brackets
        String digitsStr = args.replaceAll("[^0-9]+", ""); // + means more than one occurrence of the same char

        return new String[] {digitsStr.trim(), otherStr.trim()}; //.trim () to remove white spaces
    }

    public static boolean checkDist(String str) {
        if (str.equals("m") || str.equals("min"))
        {
            return true;
        }
        else if (str.equals("Km") || str.equals("h"))
        {
            return false;
        }
        else
        {
            return false;
        }
    }

    /*public void loadData(int calCal){

        // Load goal shared preferences

        SharedPreferences goalPreferences = requireContext().getSharedPreferences(GOALS_PREFS, MODE_PRIVATE);
        Steps = goalPreferences.getInt(STEPS_GOAL,10000);
        Calories = goalPreferences.getInt(CAL_GOAL,calCal);
        Distance = goalPreferences.getInt(DIST_GOAL, 8);
        Time = goalPreferences.getInt(TIME_GOAL,1);
        DistU = goalPreferences.getString(DIST_GOAL_U, "Km");
        TimeU = goalPreferences.getString(TIME_GOAL_U,"h");

        // Updating views

        stepTxt.setText(String.valueOf(Steps));
        calTxt.setText(Calories + " Kcal");
        distTxt.setText(Distance + " " + DistU);
        timeTxt.setText(Time + " " + TimeU);
    }*/



    //antigo
  /*

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}