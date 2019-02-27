package com.android_lab_2;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
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

        ArrayAdapter<String> adapter;
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


        
        return view;
    }

    private void updateServiceTimeDisplay() {
        String message = "Currently running every:  ";
        message += readPreferences(R.string.update_frequency_key);
        serviceTimeDisplay.setText(message);
    }




    public void applyNewPreferences() {

        if (selectedSpinnerItem != null ) {
            if ( !selectedSpinnerItem.equals("") || !selectedSpinnerItem.equals(" ")) {
                writePreferences(R.string.update_frequency_key, selectedSpinnerItem);
                updateServiceTimeDisplay();
                startRSService();
                selectedSpinnerItem = null;
            }

        }


        setNewNumberOfEntries();


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


    }


    public  void writePreferences(int key, String  value) {
        SharedPreferences sharedPref = null;
        try {
            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
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

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String str = sharedPref.getString(getString(key),"");

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




}
