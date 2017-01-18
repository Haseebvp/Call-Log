package adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

import fragments.LineChartFragment;
import fragments.PieChartFragment;

/**
 * Created by haseeb on 16/1/17.
 */

public class MyPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public MyPagerAdapter(FragmentManager fragmentManager
                          ) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                LineChartFragment tab1 = new LineChartFragment();
                return tab1;


            case 1:

                PieChartFragment tab2 = new PieChartFragment();
                return tab2;

            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        String head;
        if (position == 0){
            head = "Duration in LineChart";
        }
        else {
            head = "Duration in PieChart";
        }
        return head;
    }
}
