package com.android_lab_2;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.android_lab_2.Adapter.FeedAdapter;
import com.android_lab_2.DataBase.DBHelper;
import com.android_lab_2.model.TrimmedRSSObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    private RecyclerView recyclerView;
    private List<TrimmedRSSObject> trimmedRSSObjectList = new ArrayList<>();
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

        setUpSearchButton(view);

        // if fragment is visible, refresh the list
        if(getUserVisibleHint()){
            setUpRecyclerView();
        }

        return view;
    }
    // initializes and start a on click listener for the SearchButton
    private void setUpSearchButton(View view) {
        button = view.findViewById(R.id.button_refresh);
        button.setOnClickListener(v -> {
            closeKeyboard();
            String searchTerm = textView.getText().toString();

            Log.d(TAG, "onClick: searchTerm:" + searchTerm);
            // if search i empty, just refresh the list
            if (searchTerm.equals("") || searchTerm.equals(" ")) {
                if (isNetworkAvailable()) {
                    trimmedRSSObjectList.clear();
                    setUpRecyclerView();
                }
            } else {
                Log.d(TAG, "onClick: Refresh button: after check: " + searchTerm);
                filter(searchTerm);
            }
        });

    }

    // if fragment is visible, refresh the list
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            setUpRecyclerView();
        }
    }

    // searches for relevant entries based on a searchTerm
    public void filter(String searchTerm) {
       String noMatches = "no matches";
        Thread thread =new Thread(() -> {

            Log.d(TAG, "filter: run: searchTerm: " + searchTerm);
            List<TrimmedRSSObject> allEntries =  db.getAllTrimmedRSSObjectsWhereSourceIs("VG");
            List<TrimmedRSSObject> temp = new ArrayList<>();
            temp.clear();

             for (TrimmedRSSObject object : allEntries) {
                 if (UtilityClass.searchMatch(object, searchTerm)) {
                     temp.add(object);
                     Log.d(TAG, "filter: run: object: " + object.toString());
                 }
             }

             // if no entries matches the searchTerm, respond with snackbar message.
             if (temp.size() == 0 ) {
                 Log.d(TAG, "filter: run: temp list size:" + temp.size());
                 temp.clear();
                 Snackbar.make(getActivity().findViewById(android.R.id.content
                 ), noMatches,Snackbar.LENGTH_LONG).show();

             }


            getActivity().runOnUiThread(() -> {
                // if no entries matches the searchTerm, do nothing
                if (temp.size() == 0){
                    return;
                }
                // if some entries matches the searchTerm, update the list
                trimmedRSSObjectList.clear();
                Log.d(TAG, "filter: runOnUiThread: before trimmedRSSObjectList: "+ trimmedRSSObjectList.size());
                adapter.notifyDataSetChanged();
                trimmedRSSObjectList.addAll(temp);
                Log.d(TAG, "filter: runOnUiThread: after trimmedRSSObjectList: "+ trimmedRSSObjectList.size());
                adapter.notifyItemRangeChanged(0, temp.size()-1);
            });
        });

        thread.start();
    }


    // updates the recyclerView
    public void setUpRecyclerView() {

        Thread getContent = new Thread(() -> {

           updateDatastructure();
            // it there isn't anything to display, do nothing
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
        });
        getContent.start();
    }

    // updates the primary datastructure for displaying content
    public void updateDatastructure() {
        int counter = 0;
        String numString = readPreferences(R.string.rss_list_length_key);
        Integer numEntries = Integer.parseInt(numString);

        String origin = readPreferences(R.string.rss_source_currently_selected_url);
        // gets all relevant entries from database based on list length and rss source
        trimmedRSSObjectList = db.getAllEntries(numEntries,origin);
        // for debugging
        if (trimmedRSSObjectList.size() > 0) {
            Log.d(TAG, "updateDatastructure: " + trimmedRSSObjectList.get(0).getPubDate() +" "+ trimmedRSSObjectList.get(0).getTitle());
        }
        // if no entries found, wait and see if database has been updated, and try to get entries again
        while (trimmedRSSObjectList.size() == 0 && counter < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            trimmedRSSObjectList = db.getAllEntries(numEntries, origin);
            counter ++;
        }

    }

    // read a value form DefaultSharedPreferences
    public String readPreferences(int key) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = sharedPref.getString(getString(key),"");

        return str;
    }

    // checks if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
