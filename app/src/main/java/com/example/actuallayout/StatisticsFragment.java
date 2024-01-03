package com.example.actuallayout;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
*/
public class StatisticsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private List<String> xValues = Arrays.asList("Sunday","Monday","Tuesday", "Wednesday","Thursday","Friday","Saturday");


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
*/
    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_statistics, container, false);

    }

    private ArrayList<Entry> dataValues()
    {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        dataVals.add(new Entry(0,10000));
        dataVals.add(new Entry(1,4000));
        dataVals.add(new Entry(2,8000));
        dataVals.add(new Entry(3,3000));
        dataVals.add(new Entry(4,5000));
        dataVals.add(new Entry(5,7000));
        dataVals.add(new Entry(6,6000));

        return dataVals;
    }
    LineChart mpLineChart;
    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        // Setup any handles to view objects here
        mpLineChart = (LineChart) getView().findViewById(R.id.chart);

        LineDataSet lineDataSet1 = new LineDataSet(dataValues(),"weekly data");
        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        LineData data = new LineData(dataSets);
        mpLineChart.setData(data);
        mpLineChart.invalidate();
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setCircleRadius(6f);
        // mpLineChart.getAxisRight().setDrawLabels(false);
        // mpLineChart.setDrawGridBackground(false);
        // mpLineChart.getXAxis().setDrawGridLines(false);
        // mpLineChart.getAxisLeft().setDrawGridLines(false);
        //mpLineChart.setDrawBorders(false);
        mpLineChart.setPinchZoom(false);
        mpLineChart.setScaleEnabled(false);
        lineDataSet1.setDrawValues(false);
        YAxis yAxis = mpLineChart.getAxisLeft();
        yAxis.setAxisMaximum(0f);
        yAxis.setAxisMaximum(12000f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);
        mpLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
        mpLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mpLineChart.getXAxis().setGranularity(1f);
        mpLineChart.getLegend().setTextColor(android.R.color.white);
        mpLineChart.getXAxis().setTextColor(Color.WHITE);
        mpLineChart.getXAxis().setGranularityEnabled(true);
        mpLineChart.getDescription().setEnabled(false);
        Legend legend = mpLineChart.getLegend();
        legend.setEnabled(false);
        mpLineChart.animateXY(1000, 1000);


    }
    @Override
    public void onStart() {
        super.onStart();
        getView().post(new Runnable() {
            @Override
            public void run() {
                setupGradient(mpLineChart);
            }
        });
    }

    private void setupGradient(LineChart mChart) {
        Paint paint = mChart.getRenderer().getPaintRender();
        //set value to objective
        int height = mChart.getHeight();

        LinearGradient linGrad = new LinearGradient(0, 0, 0, height,
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_blue_bright),
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
    }

}



