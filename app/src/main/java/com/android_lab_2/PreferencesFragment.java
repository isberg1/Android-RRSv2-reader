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
    private String selectedSpinnerItem;

    private Spinner spinner= null;
    private TextView serviceTimeDisplay;
    private Button applyChanges;
    private EditText selectListSize;
    private EditText newUrlSource = null;
    ArrayAdapter<String> adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.preferences_tab,container,false);

        spinner = view.findViewById(R.id.spinner_update_options);
        if (spinner == null)
            Log.d(TAG, "onCreateView: spinner is null");


        List<String> entries = new ArrayList<>();
        entries.add(" ");
        String [] temp= getResources().getStringArray(R.array.update_options);
        entries.addAll(Arrays.asList(temp));


        adapter = new ArrayAdapter<String>(view.getContext(),R.layout.spinner_layout,entries);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String str = (String) spinner.getSelectedItem();

                if (str.equals("")|| str.equals(" ")) {
                    selectedSpinnerItem = null;
                    return;
                }

                selectedSpinnerItem= str;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        applyChanges = view.findViewById(R.id.button_preferences_apply_changes);
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

        
        return view;
    }

    private void updateServiceTimeDisplay() {
        String message = "Currently running every:  ";
        message += readPreferences(R.string.update_frequency_key);
        serviceTimeDisplay.setText(message);
    }




    public void applyNewPreferences() {

        setNewRefreshRate();
        setNewNumberOfEntries();
        addNewUrlSource();

    }

    private void addNewUrlSource() {

        String temp = newUrlSource.getText().toString();
        URL url;

        if (temp != null) {
            if (!temp.equals("") && !temp.equals(" ")) {
                try {
                    url = new URL(temp);
                } catch (MalformedURLException e) {
                    closeKeyboard();
                    Toast.makeText(getActivity(),"Malformed URL rejected",Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                    return;
                }

                validateAndUpdateNewUrl(url);
            }
        }


    }

    private  void validateAndUpdateNewUrl(URL url) {

         @SuppressLint("StaticFieldLeak") AsyncTask<URL,Void, Boolean> validateNewUrlAsync = new AsyncTask<URL, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(URL... urls) {
                try {
                    InputStream inputStream = url.openConnection().getInputStream();
                    Scheduler.parseFeed(inputStream);

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

                 if ( aBoolean ) {
                     writeNewUrlToPreferences(url.toString());
                     startRSService();
                     newUrlSource.getText().clear();
                     return;
                 }

                 Toast.makeText(getContext(), "URL not valid RSS feed", Toast.LENGTH_LONG).show();
             }
         };

       validateNewUrlAsync.execute(url);


    }

    private void writeNewUrlToPreferences(String newUrl) {
        String separator =getString(R.string.rss_source_list_separator);
        StringBuilder toPreferences= new StringBuilder();

        String allCurrentUrls = readPreferences(R.string.rss_source_key);

        // ensure idempotent
        if (allCurrentUrls.contains(newUrl)) {
            Log.d(TAG, "writeNewUrlToPreferences: ensure idempotent: all URLs:" + allCurrentUrls);
            return;
        }

        toPreferences.append(allCurrentUrls);
        toPreferences.append(separator);
        toPreferences.append(newUrl);
        writePreferences(R.string.rss_source_key, toPreferences.toString());
    }

    private void setNewRefreshRate() {
        if (selectedSpinnerItem != null ) {
            if ( !selectedSpinnerItem.equals("") && !selectedSpinnerItem.equals(" ")) {
                writePreferences(R.string.update_frequency_key, selectedSpinnerItem);
                updateServiceTimeDisplay();
                startRSService();
                selectedSpinnerItem = null;
                spinner.setSelection(0);
            }
        }

    }

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
            Toast.makeText(getContext(),"Number must be bigger then 2",Toast.LENGTH_LONG);
            return;
        }

        writePreferences(R.string.rss_list_length_key, numEntries.toString());
        Log.d(TAG, "setNewNumberOfEntries: wrote new num entries");
        selectListSize.getText().clear();
    }


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
        editor.putString(getString(key), (String)value);
        editor.apply();
    }

    public String readPreferences(int key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = preferences.getString(getString(key),"");

        return str;
    }


    public void startRSService() {

        JobInfo info = makeService();

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


    public  int getUpdatefrequency() {
        String stringTime = readPreferences(R.string.update_frequency_key);
        Log.d(TAG, "getUpdatefrequency: stringTime: " + stringTime);
        int newTime = convertTimeStringToInt(stringTime);

        return newTime;
    }


    public static int convertTimeStringToInt(String selected) {
        String timeHour = "hour";

        if (selected.equals("") || selected.equals(" ") ||!Pattern.matches("[1-9]*\\s[a-zA-Z]*", selected)) {
            return 15;
        }

        String[] temp = selected.split(" ");

        int toMinutes = 1;

        if (temp[1].equals(timeHour)) {
            toMinutes = 60;
        }


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

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}
