package com.android_lab_2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.regex.Pattern;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.android_lab_2.MainActivity.JOB_SERVICE_ID;

/**
 * helper class
 * reads and writes to DefaultSharedPreferences
 * starts and stops service
 */
public class UtilityClass {
    private static final String TAG = "UtilityClass";
    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public UtilityClass(Context context) {
        this.context = context;
        sharedPref= PreferenceManager.getDefaultSharedPreferences(this.context);
        editor = sharedPref.edit();
    }

    // tries to match a regex pattern with a searchTerm, and return the boolean of the result
    public static boolean searchMatch(TrimmedRSSObject object, String searchTerm) {

        Log.d(TAG, "searchMatch: object: " + object.toString() + " searchTerm" + searchTerm);
      //  Log.d(TAG, "searchMatch: bool: " + Pattern.matches(searchTerm, object.toString()));

        Pattern p = null;
        try {
            p = Pattern.compile(searchTerm);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // if title matches
        if (p.matcher(object.getTitle()).matches()){
            return true;
        }
        // if pubDate matches
        if (p.matcher(object.getPubDate()).matches()){
            return true;
        }
        // if description matches
        return p.matcher(object.getDescription()).matches();
    }



    // starts the rss update service
    public void startRSService() {
        // configure jobscheduler service
        JobInfo info = makeService();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
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
        ComponentName componentName = new ComponentName(context, Scheduler.class);
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
        int newTime = convertTimeStringToInt(stringTime);

        return newTime;
    }


    // converts an int value stored as a string to int
    public int convertTimeStringToInt(String selected) {
        String timeHour = "hour";

        int defaultTime = context.getResources().getInteger(R.integer.default_time_service);

        // validates input parameter
        if (selected.equals("") || selected.equals(" ") || !Pattern.matches("\\d+\\s[a-zA-Z]*", selected)) {
            // if something is wrong return a default value
            Log.d(TAG, "convertTimeStringToInt: bad value reurning defaultTime " + selected);
            return defaultTime;
        }
        // a correct paramater may be "15 min", value is split in order to get only the int part
        String[] temp = selected.split(" ");

        int toMinutes = 1;
        // if second part of parameter is "hour" convert it to minuets
        if (temp[1].equals(timeHour)) {
            toMinutes = 60;
        }

        // convert string to int
        int baseNumber;
        try {
            Log.d(TAG, "convertTimeStringToInt: temp[0]: " + temp[0]);
            baseNumber = Integer.parseInt(temp[0].trim());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "convertTimeStringToInt: converting string to Integer failed");
            return defaultTime;
        }

        int newTime = baseNumber * toMinutes;

        return newTime;
    }




    // reads a DefaultSharedPreferences
    public  String readPreferences(int key) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String keyName = context.getResources().getString(key);
        String str = sharedPref.getString(keyName,"");

        return str;
    }

    // wrights a DefaultSharedPreferences
    public  void writePreferences(int key, String  value) {
        String keyName = context.getResources().getString(key);
        editor.putString(keyName, value);
        editor.apply();
    }

    // stops the jobsheduler service from running, used for debugging
    private void stopRSService() {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_SERVICE_ID);
        Log.d(TAG, "stopRSService: ");

    }

    // ensure default values for preferences exits
    public void ensureValuesExits() {
        // try to get values form defaultpreferences
        String listLength = readPreferences(R.string.rss_list_length_key);
        String rrsSources = readPreferences(R.string.rss_source_key);
        String serviceTime = readPreferences(R.string.update_frequency_key);
        String currentlySelectedURL = readPreferences(R.string.rss_source_currently_selected_url);

        // if value does not exist, wright default values to defaultpreferences
        if (listLength.equals("") || listLength.equals(" ")) {
            String temp = context.getResources().getString(R.string.default_value_rss_list_length);
            writePreferences(R.string.rss_list_length_key, temp);
        }
        if (rrsSources.equals("") || rrsSources.equals(" ")) {
            String temp = context.getResources().getString(R.string.default_value_rss_source);
            writePreferences(R.string.rss_source_key,temp );
        }
        if (serviceTime.equals("") || serviceTime.equals(" ")) {
            String temp = context.getResources().getString(R.string.default_value_update_frequency);
            writePreferences(R.string.update_frequency_key, temp);
        }
        if (currentlySelectedURL.equals("") || currentlySelectedURL.equals(" ")) {
            String temp = context.getResources().getString(R.string.default_value_rss_source);
            writePreferences(R.string.rss_source_currently_selected_url, temp);
        }
    }

}
