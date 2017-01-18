package com.intellicartask.intellicartask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.github.mikephil.charting.data.Entry;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import adapter.MyPagerAdapter;

public class GraphActivity extends AppCompatActivity {

    MyPagerAdapter adapter;
    ViewPager vpPager;
    ArrayList<String> xData;
    ArrayList<Integer> dates;
    ArrayList<Entry> incomingData;
    ArrayList<Entry> outgoingData;
    ArrayList<Long> startingtime;
    ArrayList<Long> durationlist;
    ArrayList<Long> endingtime;
    ArrayList<Entry> totalData;
    int dateCheck = 0;
    ProgressBar progressBar;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //System.out.println("STATTT : CRE");
        setContentView(R.layout.activity_graph);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            int hasReadCallLogPermission = checkSelfPermission(Manifest.permission.READ_CALL_LOG);
            if (hasReadCallLogPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }else {
                getCallDetails();
            }

        } else {
            getCallDetails();
        }
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });


    }

    public void getCallDetails() {
        xData = new ArrayList<String>();
        dates = new ArrayList<Integer>();
        incomingData = new ArrayList<Entry>();
        outgoingData = new ArrayList<Entry>();
        startingtime = new ArrayList<Long>();
        endingtime = new ArrayList<Long>();
        durationlist = new ArrayList<Long>();
        totalData = new ArrayList<Entry>();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        if (managedCursor.moveToFirst()) {
            Date imtTime = new Date(Long.valueOf(managedCursor.getString(date)));
            dateCheck = imtTime.getDate();
        }


        int outDur = 0;
        int inDur = 0;
        int count = 0;
        while (managedCursor.moveToNext()) {
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String phNumber = managedCursor.getString(number);

            String callDuration = managedCursor.getString(duration);
            if (callDayTime.getDate() == getYesterday()) {
                startingtime.add(callDayTime.getTime());
                endingtime.add(callDayTime.getTime() + ((Integer.parseInt(callDuration) * 1000)));
                durationlist.add(Long.parseLong(String.valueOf(Integer.parseInt(callDuration) * 1000)));
            }


            if (!xData.contains(getMonth(callDayTime.getMonth()) + " " + String.valueOf(callDayTime.getDate()))) {
                xData.add(getMonth(callDayTime.getMonth()) + " " + String.valueOf(callDayTime.getDate()));
                dates.add(callDayTime.getDate());
            }
            int dircode = Integer.parseInt(callType);
            if (callDayTime.getDate() == dateCheck) {
                switch (dircode) {
                    case CallLog.Calls.INCOMING_TYPE:
                        inDur = inDur + Integer.parseInt(callDuration);
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        outDur = outDur + Integer.parseInt(callDuration);
                        break;
                }
                if (managedCursor.isLast()) {
                    incomingData.add(new Entry(inDur / 60, count));
                    outgoingData.add(new Entry(outDur / 60, count));
                    totalData.add(new Entry((inDur + outDur) / 60, count));
                }
            } else {
                incomingData.add(new Entry(inDur / 60, count));
                outgoingData.add(new Entry(outDur / 60, count));
                totalData.add(new Entry((inDur + outDur) / 60, count));
                inDur = 0;
                outDur = 0;
                count = count + 1;
                switch (dircode) {
                    case CallLog.Calls.INCOMING_TYPE:
                        inDur = Integer.parseInt(callDuration);
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        outDur = Integer.parseInt(callDuration);
                        break;
                }
                dateCheck = callDayTime.getDate();
            }
        }
        managedCursor.close();
        ShowData();
    }

    private void ShowData() {
//        progressBar.setVisibility(View.GONE);
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapter);
    }


    private String getMonth(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month.substring(0, 3);
    }


    public ArrayList<String> getxData() {
        return xData;
    }

    public ArrayList<Integer> getDates() {
        return dates;
    }

    public ArrayList<Entry> getIncomingData() {
        return incomingData;
    }

    public ArrayList<Entry> getOutgoingData() {
        return outgoingData;
    }

    public ArrayList<Entry> getTotalData() {
        return totalData;
    }


    public ArrayList<Long> getStartingtime() {
        return startingtime;
    }


    public ArrayList<Long> getEndingtime() {
        return endingtime;
    }

    public ArrayList<Long> getDurationlist() {
        return durationlist;
    }


    public Integer getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        Date d = yesterday.getTime(); // get a Date object
        return yesterday.get(Calendar.DAY_OF_MONTH);
    }

    public Long getYesterdayTime() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        Date d = yesterday.getTime(); // get a Date object
        return d.getTime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCallDetails();
                        }
                    }, 200);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(GraphActivity.this, "Permission denied to read call log", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("STATTT : RES");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //System.out.println("STATTT : PAU");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //System.out.println("STATTT : RESTA");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //System.out.println("STATTT : STO");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //System.out.println("STATTT : STA");
    }
}
