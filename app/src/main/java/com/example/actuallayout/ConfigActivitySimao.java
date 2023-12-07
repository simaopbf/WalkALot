package com.example.actuallayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

public class ConfigActivitySimao extends AppCompatActivity {

    /* ------------------------------- Shared preferences  ------------------------------- */

    public static final String CONFIG_PREFS = "configPrefs";
    public static final String GENDER = "genderPrefs";
    public static final String WEIGHT = "weightPrefs";
    public static final String BIRTH = "birthPrefs";
    public static final String HEIGHT = "heightPrefs";
    public static final String TDEE = "TDEEPrefs";

    private String Gender;
    private int Weight;
    private int Birth;
    private int Height;
    private int tdee;

    /* ------------------------------- Global variables  ------------------------------- */

    private static String genderInp;
    private static int weightInp;
    private static int birthInp;
    private static int heightInp;

    /* ------------------------------- Layout object variables ------------------------------- */

    NumberPicker genderPck;
    NumberPicker weightPck;
    NumberPicker birthPck;
    NumberPicker heightPck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_config);

        SharedPreferences configPreferences = getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE); // Mode private means no other app can use the saved data
        SharedPreferences.Editor editor = configPreferences.edit();

        /* ------------------------------- Guidelines ------------------------------- */

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;

        Guideline verticalG1 = findViewById(R.id.guideline5);
        if (verticalG1 != null) {
            w = displayMetrics.widthPixels;
            verticalG1.setGuidelineBegin(w / 3); // Puts first guideline at 1/3 of the current running phone width
        }
        Guideline verticalG2 = findViewById(R.id.guideline6);
            if (verticalG2 != null) {
                verticalG2.setGuidelineBegin(Math.round(w * 2 / 3)); // Puts second guideline at 2/3 of the current running phone width
            }


        /* ------------------------------- Number Pickers ------------------------------- */

        // Gender picker

        final String[] gen = {"Male","Female"};

        genderPck = findViewById(R.id.genderPick);
        genderPck.setMinValue(0);
        genderPck.setMaxValue(gen.length-1);
        genderPck.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                String [] auxStr=genderPck.getDisplayedValues();
                genderInp = auxStr[newValue];

            }
        });

        // Weight picker

        weightPck = findViewById(R.id.weightPick);
        weightPck.setMinValue(20);
        weightPck.setMaxValue(250);
        weightPck.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                weightInp = newValue;
            }
        });

        // Age picker

        birthPck = findViewById(R.id.birthPick);
        birthPck.setMinValue(14);
        birthPck.setMaxValue(80);
        birthPck.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                birthInp = newValue;
            }
        });

        // Height picker

        heightPck = findViewById(R.id.heightPick);
        heightPck.setMinValue(60);
        heightPck.setMaxValue(250);
        heightPck.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                heightInp = newValue;
            }
        });

        boolean firstStart = configPreferences.getBoolean("firstStart",true);

        if(firstStart)
        {
            genderPck.setDisplayedValues(gen);
            weightPck.setValue(70);
            birthPck.setValue(25);
            heightPck.setValue(165);

            System.out.println(" ----------------------------------------------------------- First Start ----------------------------------------------------------- ");
        }
        else
        {
            System.out.println(" ---------------------------------------------------------- Another Start ---------------------------------------------------------- ");
            loadData();
        }

        /* ------------------------------- Save Button ------------------------------- */

        Button bt = findViewById(R.id.btn2); //represent object como uma interface view (element in the layout), capture our button from layout
        bt.setOnClickListener(new View.OnClickListener() { //defining a listener: register the onClick listener with the implementation
            //the parameters can be a function that will be called when the user clicks on the button
            @Override
            public void onClick(View view) {

                // Saves info

                String [] auxStr=genderPck.getDisplayedValues();

                genderInp = auxStr[genderPck.getValue()]; // If the user does not choose a new value in the picker, this saves the default value
                weightInp = weightPck.getValue();
                birthInp = birthPck.getValue();
                heightInp = heightPck.getValue();

                editor.putString(GENDER, genderInp);
                editor.putInt(WEIGHT, weightInp);
                editor.putInt(BIRTH, birthInp);
                editor.putInt(HEIGHT, heightInp);
                editor.putBoolean("firstStart",false); // Defined on the main activity

                // TDEE calculation

                tdee = (int) Math.round((10 * weightInp) + (6.25 * heightInp) - (5 - birthInp));

                if (genderInp.equals("Male"))
                {tdee = (int) Math.round((tdee + 5) * 1.15);}
                else
                {tdee = (int) Math.round((tdee - 161) * 1.15);}

                editor.putInt(TDEE, tdee);

                editor.apply();

                Toast.makeText(getApplicationContext(), "Data saved", Toast.LENGTH_SHORT).show();

                // Goes to main activity

                gotoMain();
            }
        });

    }

    public void gotoMain(){
        Intent i = new Intent(ConfigActivitySimao.this, MainActivity.class);
        i.putExtra("Debugging", "Working");
        startActivity(i);
    }

    public void loadData(){

        // Load config shared preferences

        SharedPreferences configPreferences = getSharedPreferences(CONFIG_PREFS, MODE_PRIVATE);

        Gender = configPreferences.getString(GENDER,"Female");
        Weight = configPreferences.getInt(WEIGHT,70);
        Birth = configPreferences.getInt(BIRTH, 1997);
        Height = configPreferences.getInt(HEIGHT,165);

        // Updating views

        String [] genderStrInP = {"",""};
        if (Gender.equals("Female"))
        {
            genderStrInP[0] = Gender;
            genderStrInP[1] = "Male";
        }
        else
        {
            genderStrInP[0] = Gender;
            genderStrInP[1] = "Female";
        }

        genderPck.setDisplayedValues(genderStrInP);
        weightPck.setValue(Weight);
        birthPck.setValue(Birth);
        heightPck.setValue(Height);


    }

}
