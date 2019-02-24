package com.android_lab_2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.PkRSS;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SectionsPageAdapter sectionsPageAdapter;

    private ViewPager viewPager;

    public static final int  JOB_SERVICE_ID = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.container);
        setUpViewPager(viewPager);


        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // set starting position to 1 right of default.
        viewPager.setCurrentItem(viewPager.getCurrentItem() +1);


        startRSService();

       // stopRSService();



    }

    private void startRSService() {

        int time = getUpdatefrequency();
        // set service requirements parameters
        ComponentName componentName = new ComponentName(this, Scheduler.class);
        JobInfo info = new JobInfo.Builder(JOB_SERVICE_ID, componentName)
                .setPersisted(true) // run on reboot
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require network connected
                .setPeriodic(time * 60 * 1000)
                .build();  // register service

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(info);

        // check if service is registered
        if (result == JobScheduler.RESULT_SUCCESS){
            Log.d("startService", "jobScheduler successes");
        }
        else {
            Log.d("startService",  "jobScheduler failed");
        }
    }

    private void stopRSService() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_SERVICE_ID);
        Log.d(TAG, "stopRSService: ");

    }

    private int getUpdatefrequency() {
        return 15;
    }

    private void setUpViewPager(ViewPager viewPager) {
        Log.d(TAG, "setUpViewPager: ");

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new SourceFragment(), "Source");
        adapter.addFragment(new ListFragment(), "List");
        adapter.addFragment(new FavoriteFragment(), "Favorite");
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

/*

sources:
    Fragments: https://www.youtube.com/watch?v=bNpWGI_hGGg
    RSS + RecyclerView: https://www.youtube.com/watch?v=APInjVO0WkQ
    RSS xml parsing: https://www.androidauthority.com/simple-rss-reader-full-tutorial-733245/
    Service (jobScheduler): https://www.youtube.com/watch?v=3EQWmME-hNA
*/
