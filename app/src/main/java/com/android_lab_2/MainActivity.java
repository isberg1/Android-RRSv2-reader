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
       // startRSService();
      //  stopRSService();


    }
    // ensure default values for preferences exits
    private void ensureValuesExits() {
        // try to get values form defaultpreferences
        String listLength = readPreferences(R.string.rss_list_length_key);
        String rrsSources = readPreferences(R.string.rss_source_key);
        String serviceTime = readPreferences(R.string.update_frequency_key);
        String currentlySelectedURL = readPreferences(R.string.rss_source_currently_selected_url);

        // if value does not exist, wright default values to defaultpreferences
        if (listLength.equals("") || listLength.equals(" ")) {
            writePreferences(R.string.rss_list_length_key, getString(R.string.default_value_rss_list_length));
        }
        if (rrsSources.equals("") || rrsSources.equals(" ")) {
            writePreferences(R.string.rss_source_key, getString(R.string.default_value_rss_source));
        }
        if (serviceTime.equals("") || serviceTime.equals(" ")) {
            writePreferences(R.string.update_frequency_key, getString(R.string.default_value_update_frequency));
        }
        if (currentlySelectedURL.equals("") || currentlySelectedURL.equals(" ")) {
            writePreferences(R.string.rss_source_currently_selected_url, getString(R.string.default_value_rss_source));
        }
    }

    // starts the rss update service
    public void startRSService() {
        // configure jobscheduler service
        JobInfo info = makeService();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        // start service
        int result = jobScheduler.schedule(info);

        // check if service is registered
        if (result == JobScheduler.RESULT_SUCCESS){
            Log.d("startService", "jobScheduler successes");
        }
        else {
            Log.d("startService",  "jobScheduler failed");
        }
    }

    // configure jobscheduler service
    public JobInfo makeService() {
        int time = getUpdatefrequency();
        // set service requirements parameters
        ComponentName componentName = new ComponentName(this, Scheduler.class);
        JobInfo info = new JobInfo.Builder(JOB_SERVICE_ID, componentName)
                .setPersisted(true) // run on reboot
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require network connected
                .setPeriodic(time * 60 * 1000)
                .build();  // register service

        return info;
    }

    // get the frequency at witch the service is to run
    public  int getUpdatefrequency() {
        String stringTime = readPreferences(R.string.update_frequency_key);
        Log.d(TAG, "getUpdatefrequency: stringTime: " + stringTime);
        int newTime = PreferencesFragment.convertTimeStringToInt(stringTime);

        return newTime;
    }

    // reads a DefaultSharedPreferences
    public  String readPreferences(int key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String str = sharedPref.getString(getString(key),"");

        return str;
    }

    // wrights a DefaultSharedPreferences
    public  void writePreferences(int key, String  value) {
        SharedPreferences sharedPref = null;
        try {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writePreferences: unable to getPreferences");
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(key), value);
        editor.apply();
    }

    // stops the jobsheduler service from running, used for debugging
    private void stopRSService() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_SERVICE_ID);
        Log.d(TAG, "stopRSService: ");

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
