package com.android_lab_2;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class PreferencesFragment extends Fragment {
    private static final String TAG = "PreferencesFragment";
    private String selectedRefreshRateSpinnerItem;
    private String selectedCurrentUrlSpinnerItem;

    private Spinner spinnerRefreshRate = null;
    private Spinner spinnerRssSource = null;
    private TextView serviceTimeDisplay;
    private EditText selectListSize;
    private EditText newUrlSource = null;
    private ArrayAdapter<String> adapter;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.preferences_tab,container,false);

        setUpSpinnerRefreshRate(view);


        Button applyChanges = view.findViewById(R.id.button_preferences_apply_changes);
        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyNewPreferences();
            }
        });


        serviceTimeDisplay = view.findViewById(R.id.text_preferences_currently_selected);
        updateServiceTimeDisplay();
        selectListSize = view.findViewById(R.id.select_list_size);
        newUrlSource = view.findViewById(R.id.new_url_source);

        setUpSpinnerRssSource();
        
        return view;
    }

    // sets up the spinner for selecting rss source
    // the currently selected is always placed on top of the list
    private void setUpSpinnerRssSource() {
        spinnerRssSource = view.findViewById(R.id.select_rss_source);

        // get comma separated list form defaultsharedpreferences
        String urlCSL = readPreferences(R.string.rss_source_key);
        // get currently selected rss source form defaultsharedpreferences
        String currentlySelectedURL = readPreferences(R.string.rss_source_currently_selected_url);
        Log.d(TAG, "setUpSpinnerRssSource: urlCSL: " + urlCSL);

        // ensure the currently selected is always placed on top of the list
        String[] oldUrlList = urlCSL.split(getString(R.string.rss_source_list_separator));
        List<String> newUrlList = new ArrayList<>();
        newUrlList.add(currentlySelectedURL);

        Log.d(TAG, "setUpSpinnerRssSource: currentlySelectedURL: " + currentlySelectedURL);

        for (String str : oldUrlList) {
            if (str.equals(currentlySelectedURL)){
                continue;
            }
            newUrlList.add(str);
        }

        setupAdapterRssSource(view, newUrlList);

    }

    // sets up the adapter for the spinner for selecting rss source
    private void setupAdapterRssSource(View view, List<String> newUrlList ) {
        ArrayAdapter<String> rssSelectionAdapter;
        rssSelectionAdapter = new ArrayAdapter<>(view.getContext(),R.layout.support_simple_spinner_dropdown_item, newUrlList);
        spinnerRssSource.setAdapter(rssSelectionAdapter);
        spinnerRssSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) spinnerRssSource.getSelectedItem();

                if (str.equals("")|| str.equals(" ")) {
                    selectedCurrentUrlSpinnerItem = null;
                    return;
                }

                selectedCurrentUrlSpinnerItem = str;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // sets up the spinner for selecting rss service refresh rate
    private void setUpSpinnerRefreshRate(View view) {
        spinnerRefreshRate = view.findViewById(R.id.spinner_update_options);
        if (spinnerRefreshRate == null)
            Log.d(TAG, "onCreateView: spinnerRefreshRate is null");

        List<String> entries = new ArrayList<>();
        entries.add(" ");
        String [] temp= getResources().getStringArray(R.array.update_options);
        entries.addAll(Arrays.asList(temp));

        setUpAdapterRefreshRate( view, entries );


    }

    // sets up the adapter for the spinner for selecting rss service refresh rate
    private void setUpAdapterRefreshRate(View view, List<String> entries) {

        adapter = new ArrayAdapter<String>(view.getContext(),R.layout.spinner_layout,entries);
        spinnerRefreshRate.setAdapter(adapter);
        spinnerRefreshRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String str = (String) spinnerRefreshRate.getSelectedItem();

                if (str.equals("")|| str.equals(" ")) {
                    selectedRefreshRateSpinnerItem = null;
                    return;
                }

                selectedRefreshRateSpinnerItem = str;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // displays current service refresh rate
    private void updateServiceTimeDisplay() {
        String message = "Currently running every:  ";
        message += readPreferences(R.string.update_frequency_key);
        serviceTimeDisplay.setText(message);
    }

    // is run when the Apply Changes button is pressed
    // tries to update preferences
    public void applyNewPreferences() {

        setNewRefreshRate();
        setNewNumberOfEntries();
        addNewUrlSource();
        setNewCurrentlySelectedURL();

    }
    // validate and store new URL value
    private void setNewCurrentlySelectedURL() {
        if (selectedCurrentUrlSpinnerItem !=null) {
            if (!selectedCurrentUrlSpinnerItem.equals("") && !selectedCurrentUrlSpinnerItem.equals(" ")) {
                writePreferences(R.string.rss_source_currently_selected_url, selectedCurrentUrlSpinnerItem);
            }
        }

    }

    // tries to validate URL as a RSS feed and passes it to the next method for
    // more validation
    private void addNewUrlSource() {

        String temp = newUrlSource.getText().toString();
        URL url;

        if (temp != null) {
            if (!temp.equals("") && !temp.equals(" ")) {
                try {
                    url = new URL(temp);
                } catch (MalformedURLException e) {
                    // print error message if url is not accepted
                    closeKeyboard();
                    Toast.makeText(getActivity(),"Malformed URL rejected",Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                    return;
                }

                validateAndUpdateNewUrl(url);
            }
        }
    }

    // tries to validate URL as a RSS feed, stores is and restarts service
    private  void validateAndUpdateNewUrl(URL url) {

         @SuppressLint("StaticFieldLeak") AsyncTask<URL,Void, Boolean> validateNewUrlAsync = new AsyncTask<URL, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(URL... urls) {
                try {
                    InputStream inputStream = url.openConnection().getInputStream();
                    Scheduler scheduler = new Scheduler();
                    scheduler.parseFeed(inputStream, url.toString());

                } catch (IOException e) {
                    Log.d(TAG, ": new entry failed: IOException: value: " + url.toString());
                    e.printStackTrace();
                    return false;

                } catch (XmlPullParserException e) {
                    Log.d(TAG, ": new entry failed: XmlPullParserException: value: " + url.toString());
                    e.printStackTrace();
                    return false;

                } catch (Exception e) {
                    Log.d(TAG, ": new entry failed: Exception: value: " + url.toString());
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

             @Override
             protected void onPostExecute(Boolean aBoolean) {
                 super.onPostExecute(aBoolean);
                 // if URL paring was successful
                 if ( aBoolean ) {
                     writeNewUrlToPreferences(url.toString());
                     startRSService();
                     newUrlSource.getText().clear();
                     setUpSpinnerRssSource();
                     return;
                 }
                 // respond to rejected url with toast
                 Toast.makeText(getContext(), "URL not valid RSS feed", Toast.LENGTH_LONG).show();
             }
         };

       validateNewUrlAsync.execute(url);


    }


    // wrights a comma separated list of rss source URLs to defaultsharedpreferences
    private void writeNewUrlToPreferences(String newUrl) {
        String separator =getString(R.string.rss_source_list_separator);
        StringBuilder toPreferences= new StringBuilder();

        String allCurrentUrls = readPreferences(R.string.rss_source_key);

        // ensure idempotent( if already in list, do nothing)
        if (allCurrentUrls.contains(newUrl)) {
            Log.d(TAG, "writeNewUrlToPreferences: ensure idempotent: all URLs:" + allCurrentUrls);
            return;
        }

        toPreferences.append(allCurrentUrls);
        toPreferences.append(separator);
        toPreferences.append(newUrl);
        writePreferences(R.string.rss_source_key, toPreferences.toString());
    }

    // validates and updates the rss update service
    private void setNewRefreshRate() {
        if (selectedRefreshRateSpinnerItem != null ) {
            if ( !selectedRefreshRateSpinnerItem.equals("") && !selectedRefreshRateSpinnerItem.equals(" ")) {
                writePreferences(R.string.update_frequency_key, selectedRefreshRateSpinnerItem);
                updateServiceTimeDisplay();
                startRSService();
                selectedRefreshRateSpinnerItem = null;
                spinnerRefreshRate.setSelection(0);
            }
        }

    }

    // updates the defaultsharedpreference for number og entries in RSS list in listFragment
    private void setNewNumberOfEntries() {
        Integer numEntries = 0;
        String numString = selectListSize.getText().toString();

        if (numString.equals("") || numString.equals(" ")) {
            return;
        }
        try {
            numEntries = Integer.parseInt(numString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),"Invalid entry, must be a number",Toast.LENGTH_LONG);
            return;
        }

        if (numEntries < 2){
            Toast.makeText(getContext(),"Number must be bigger then 1",Toast.LENGTH_LONG);
            return;
        }

        writePreferences(R.string.rss_list_length_key, numEntries.toString());
        Log.d(TAG, "setNewNumberOfEntries: wrote new num entries");
        selectListSize.getText().clear();
    }

    // writes a value to defaultsharedpreference
    public  void writePreferences(int key, String  value) {
        SharedPreferences sharedPref = null;
        try {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writePreferences: unable to getPreferences");
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(key), value);
        editor.apply();
    }

    // reads a value from defaultsharedpreference
    public String readPreferences(int key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = preferences.getString(getString(key),"");

        return str;
    }

    // starts the rss update service
    public void startRSService() {
        // configure jobscheduler service
        JobInfo info = makeService();
        // start service
        JobScheduler jobScheduler = (JobScheduler) getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
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
        ComponentName componentName = new ComponentName(getActivity(), Scheduler.class);
        JobInfo info = new JobInfo.Builder(MainActivity.JOB_SERVICE_ID, componentName)
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

    // converts a int value stored as a string to int
    public static int convertTimeStringToInt(String selected) {
        String timeHour = "hour";

        // validates input parameter
        if (selected.equals("") || selected.equals(" ") || !Pattern.matches("[1-9]*\\s[a-zA-Z]*", selected)) {
            // if something is wrong return a default value
            return 15;
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
            return 15;
        }

        int newTime = baseNumber * toMinutes;

        return newTime;
    }

    // closes the virtual keyboard
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}
