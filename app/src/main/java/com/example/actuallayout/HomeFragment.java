package com.example.actuallayout;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.animation.ObjectAnimator;
import android.widget.TextView;

import android.os.PersistableBundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.actuallayout.databinding.ActivityMainBinding;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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


    private Date currentTime;
    private int fractionGoal;
    private int steps;

    private int targetSteps=1;

   private KonfettiView konfettiView = null;


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
    //private FrameLayout HomeFragmentLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize the ProgressBar
        homeProgressBar = view.findViewById(R.id.home_progress_bar);


        // Start your animation

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentTime = calendar.getTime();
        // Find TextView by ID
        stepsTextView = view.findViewById(R.id.stepsTextView);


        // Retrieve steps from the database based on the current date
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        Cursor cursor = databaseHelper.getAll();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // String date = cursor.getString(cursor.getColumnIndex("date"));
                steps = databaseHelper.getStepsForUserAndDate(mUserId,currentTime.toString());

                Log.d("stepsverify", "steps" + steps);
                break;

            } while (cursor.moveToNext());


        }

        targetSteps= databaseHelper.targetValue(mUserId,"stepGoal");
        stepsTextView.setText( steps + "/"+ targetSteps);

        if(targetSteps==0)
        {
            targetSteps=10000;
        }
        Log.d("stepsverifytarget", "target" + targetSteps);
        fractionGoal= (steps*100/targetSteps);
        Log.d("stepsverifytarget", "fracao" + fractionGoal);
        animatebar = ObjectAnimator.ofInt(homeProgressBar, "progress", 0,fractionGoal);
        animatebar.setDuration(2000);
        animatebar.start();

        //Confetti animation
        konfettiView = view.findViewById(R.id.konfettiView);
        //HomeFragmentLayout = view.findViewById(R.id.HomeFragmentLayout);

        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
        Party party =
                new PartyFactory(emitterConfig)
                        .angle(270)
                        .spread(90)
                        .setSpeedBetween(1f, 5f)
                        .timeToLive(2000L)
                        .sizes(new Size(12, 5f, 0.2f))
                        .position(0.0, 0.0, 1.0, 0.0)
                        .build();
        // konfettiView.setOnClickListener(view -> konfettiView.start(party));


        if (fractionGoal == homeProgressBar.getMax()) {
            konfettiView.start(party);

        }


        return view;
    }

    private boolean isCurrentDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("HH/mm/ss", Locale.UK);
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());

        // Compare the date strings
        return date.equals(currentDate);
    }

}