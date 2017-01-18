package fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.intellicartask.intellicartask.GraphActivity;
import com.intellicartask.intellicartask.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import models.TimeDiff;

/**
 * Created by haseeb on 16/1/17.
 */

public class PieChartFragment extends Fragment implements OnChartValueSelectedListener {

    PieChart mChart;
    ArrayList<Entry> times = new ArrayList<Entry>();
    ArrayList<String> labels = new ArrayList<String>();
    ArrayList<Long> startingtime = new ArrayList<Long>();
    ArrayList<Long> endingtime = new ArrayList<Long>();
    ArrayList<Long> durationlist = new ArrayList<Long>();

    long delay = (4 * 3600000);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.piechart_fragment_layout, container, false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mChart = (PieChart) view.findViewById(R.id.piechart);

        startingtime = ((GraphActivity) this.getActivity()).getStartingtime();
        endingtime = ((GraphActivity) this.getActivity()).getEndingtime();
        durationlist = ((GraphActivity) this.getActivity()).getDurationlist();
        makeData();
        ShowData();

        return view;
    }

    private void ShowData() {
        mChart.setUsePercentValues(true);
        mChart.setExtraOffsets(15, 10, 15, 15);

        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setCenterText(getDate() + "'s total duration");
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(30f);
        mChart.setTransparentCircleRadius(21f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        mChart.setDescription("Yesterday's total call Duration");
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);


        Legend l = mChart.getLegend();

        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

    }

    private void setData() {
        PieDataSet dataSet = new PieDataSet(times, "Call Duration");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(labels,dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private void makeData() {
        times.clear();
        labels.clear();
        for (int i=0;i<6;i++) {
            //System.out.println("SUUUU : "+getSum(i)/60000);
            if (getSum(i) != 0 || getSum(i) != 0.0) {
                times.add(new Entry(getSum(i) / 60000, i));
                if (getTimeElapsed(getSum(i)) != null) {
                    labels.add(getTimeElapsed(getSum(i)) + " in " + getLabel(i));
                }
                else {
                    labels.add(getLabel(i));
                }
            }
        }
    }

    private String getLabel(int index){
        String label = null;
        switch (index){
            case 0:
                label = "12 AM to 4 AM";
                break;
            case 1:
                label = "4 AM to 8 AM";
                break;
            case 2:
                label = "8 AM to 12 PM";
                break;
            case 3:
                label = "12 PM to 4 PM";
                break;
            case 4:
                label = "4 PM to 8 PM";
                break;
            case 5:
                label = "8 PM to 12 AM";
                break;

        }
        return label;
    }


    private long getSum(int num) {
        TimeDiff temp = getTimeDiff(num);
        long sum = 0;
        for (int i=0;i<startingtime.size();i++){
            if (temp.getStart() <= startingtime.get(i) && startingtime.get(i) < temp.getEnd()){
                if (endingtime.get(i) <= temp.getEnd()){
                    sum = sum + durationlist.get(i);
                }
                else {
                    sum = sum + (durationlist.get(i) - (endingtime.get(i) - temp.getStart()));
                }

            }
        }
        return sum;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public long getTime() {
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.add(Calendar.DAY_OF_MONTH, -1);
        return date.getTime().getTime();
    }

    public TimeDiff getTimeDiff(int num) {
        TimeDiff diff = new TimeDiff();
        long ti = getTime();
        long start = ti + num * delay;
        long end = ti + (num + 1) * delay;
        diff.setStart(start);
        diff.setEnd(end);
        return diff;
    }

    public String getCheck(long time){
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    private String getTimeElapsed(long milliseconds){
        String data = null;
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
        if (hours != 0 && minutes != 0){
            data = hours + "hrs "+minutes + " mins";
        }
        else if (hours == 0 && minutes != 0){
            data = minutes + " mins";
        }
        else if (hours != 0 && minutes == 0){
            data = hours + "hrs ";
        }
        return data;
    }


    private String getDate(){
        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        date = sdf.format(getTime());
        return date;
    }


    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
