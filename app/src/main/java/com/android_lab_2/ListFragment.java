package com.android_lab_2;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android_lab_2.Adapter.FeedAdapter;
import com.android_lab_2.DataBase.DBHelper;
import com.android_lab_2.model.Item;
import com.android_lab_2.model.RSSObject;
import com.android_lab_2.model.TrimmedRSSObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    private RecyclerView recyclerView;
    private RSSObject rssObject;
    private List<TrimmedRSSObject> trimmedRSSObjectList = new ArrayList<>();

    private String RSSToJsonAPI = "https://api.rss2json.com/v1/api.json?rss_url=";
    private TextView textView;
    private Button button;
    private FeedAdapter adapter;

    public static final String DATABASE_NAME = "RSS_DB";
    public static DBHelper db;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_tab,container,false);

        recyclerView = view.findViewById(R.id.feed_recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        textView = view.findViewById(R.id.list_item_List_Tab);

        db = new DBHelper(getContext(), DATABASE_NAME);

        button = view.findViewById(R.id.button_refresh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard();
                String searchTerm = textView.getText().toString();

                Log.d(TAG, "onClick: searchTerm:" + searchTerm);
                if (searchTerm.equals("") || searchTerm.equals(" ")) {
                    if (isNetworkAvailable()) {
                        trimmedRSSObjectList.clear();
                       // getRRS();
                        setUpRecyclerView();
                    }
                } else {
                    Log.d(TAG, "onClick: Refresh button: after check: " + searchTerm);
                    filter(searchTerm);
                }
            }
        });

        //setUpRecyclerView();

        if(getUserVisibleHint()){ // fragment is visible
            setUpRecyclerView();
        }

        return view;
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) { // fragment is visible and have created
            setUpRecyclerView();
        }
    }

    public void filter(String searchTerm) {
       String noMatches = "no matches";
        Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "filter: run: searchTerm: " + searchTerm);
                List<TrimmedRSSObject> allEntries =  db.getAllTrimmedRSSObjectsWhereSourceIs("VG");
                List<TrimmedRSSObject> temp = new ArrayList<>();
                temp.clear();

                 for (TrimmedRSSObject object : allEntries) {
                     if (searchMatch(object, searchTerm)) {
                         temp.add(object);
                         Log.d(TAG, "filter: run: object: " + object.toString());
                     }
                 }

                 if (temp.size() == 0 ) {
                     Log.d(TAG, "filter: run: temp list size:" + temp.size());
                     temp.clear();
                     Snackbar.make(getActivity().findViewById(android.R.id.content
                     ), noMatches,Snackbar.LENGTH_LONG).show();

                 }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (temp.size() == 0){
                            return;
                        }
                        trimmedRSSObjectList.clear();
                        Log.d(TAG, "filter: runOnUiThread: before trimmedRSSObjectList: "+ trimmedRSSObjectList.size());
                        adapter.notifyDataSetChanged();
                        trimmedRSSObjectList.addAll(temp);
                        Log.d(TAG, "filter: runOnUiThread: after trimmedRSSObjectList: "+ trimmedRSSObjectList.size());
                        adapter.notifyItemRangeChanged(0, temp.size()-1);
                    }
                });
            }
        });

        thread.start();
    }

    private boolean searchMatch(TrimmedRSSObject object, String searchTerm) {

        Log.d(TAG, "searchMatch: object: " + object.toString() + " searchTerm" + searchTerm);
        Log.d(TAG, "searchMatch: bool: " + Pattern.matches(searchTerm, object.toString()));

        Pattern p = Pattern.compile(searchTerm);

        if (p.matcher(object.getTitle()).matches()){
            return true;
        }
        if (p.matcher(object.getPubDate()).matches()){
            return true;
        }
        if (p.matcher(object.getDescription()).matches()){
            return true;
        }
        return false;
    }


    public void setUpRecyclerView() {

        Thread getContent = new Thread(new Runnable() {
            @Override
            public void run() {

               updateDatastructure();

               if (trimmedRSSObjectList.size() == 0) {
                    return;
               }

                // set adapter in UI thread
               getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       adapter = new FeedAdapter(getContext(), trimmedRSSObjectList);
                       recyclerView.setAdapter(adapter);
                       Log.d(TAG, "run:  with a smile  " );
                   }
                });
            }
        });
        getContent.start();
    }

    public void updateDatastructure() {
        int counter = 0;
        String numString = readPreferences(R.string.rss_list_length_key);
        Integer numEntries = Integer.parseInt(numString);

        trimmedRSSObjectList = db.getAllEntries(numEntries);
        if (trimmedRSSObjectList.size() > 0) {
            Log.d(TAG, "updateDatastructure: " + trimmedRSSObjectList.get(0).getPubDate() +" "+ trimmedRSSObjectList.get(0).getTitle());
        }

        while (trimmedRSSObjectList.size() == 0 && counter < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            trimmedRSSObjectList = db.getAllEntries(numEntries);
            counter ++;
        }

    }
    public String readPreferences(int key) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = sharedPref.getString(getString(key),"");

        return str;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getRRS() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,Void> getRSSAsync = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Scheduler scheduler = new Scheduler();
                scheduler.onStartJob(null);
                updateDatastructure();
                Log.d(TAG, "doInBackground: After refresh button");
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "onPostExecute: After refresh button");
            }
        };

        getRSSAsync.execute();
    }


    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
