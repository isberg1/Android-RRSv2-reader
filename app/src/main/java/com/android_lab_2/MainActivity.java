package com.android_lab_2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ViewPager viewPager;

    public static final int  JOB_SERVICE_ID = 111;

    private UtilityClass util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        util = new UtilityClass(getApplicationContext());
        Log.d(TAG, "onCreate: ");
        // ensure default values for preferences exits
        util.ensureValuesExits();
        //ensureValuesExits();
        // used to get and display images in recyclerView, external library: https://github.com/nostra13/Android-Universal-Image-Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // start fragment management
        viewPager = findViewById(R.id.container);
        setUpViewPager(viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // configure jobscheduler service
        util.startRSService();
    }

    // initialize the apps fragments
    private void setUpViewPager(ViewPager viewPager) {
        Log.d(TAG, "setUpViewPager: ");

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ListFragment(), "List");
        adapter.addFragment(new PreferencesFragment(), "Preferences");
        viewPager.setAdapter(adapter);
    }
}

/*

sources:
    Fragments: https://www.youtube.com/watch?v=bNpWGI_hGGg
    RSS + RecyclerView: https://www.youtube.com/watch?v=APInjVO0WkQ
    RSS xml parsing: https://www.androidauthority.com/simple-rss-reader-full-tutorial-733245/
    Service (jobScheduler): https://www.youtube.com/watch?v=3EQWmME-hNA
    Load date in fragment when visible:
        https://viblo.asia/p/my-solution-for-loading-data-when-fragment-visible-using-setuservisiblehint-yMnKM3PDl7P

*/
