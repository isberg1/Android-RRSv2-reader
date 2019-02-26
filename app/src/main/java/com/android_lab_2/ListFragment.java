package com.android_lab_2;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android_lab_2.Adapter.FeedAdapter;
import com.android_lab_2.DataBase.DBHelper;
import com.android_lab_2.model.Item;
import com.android_lab_2.model.RSSObject;
import com.android_lab_2.model.TrimmedRSSObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
        textView.setFocusable(false);
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textView.setFocusableInTouchMode(true);
                return false;
            }
        });

        db = new DBHelper(getContext(), DATABASE_NAME);

        button = view.findViewById(R.id.button_refresh);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
                    Log.d(TAG, "onClick: Refresh button + network available");
                    getRRS();
                   // adapter.notifyDataSetChanged();
                    //setUpRecyclerView();

                }
            }
        });

        setUpRecyclerView();

        return view;
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
        trimmedRSSObjectList = db.getAllEntries();
        if (trimmedRSSObjectList.size() > 0) {
            Log.d(TAG, "updateDatastructure: " + trimmedRSSObjectList.get(0).getPubDate() +" "+ trimmedRSSObjectList.get(0).getTitle());
        }

        while (trimmedRSSObjectList.size() == 0 && counter < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            trimmedRSSObjectList = db.getAllEntries();
            counter ++;
        }

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



    public void upDateDB(TrimmedRSSObject rss) {
        Log.d(TAG, "doInBackground: " + rss);
        @SuppressLint("StaticFieldLeak") AsyncTask<TrimmedRSSObject,Void,Void >upDateDBAsync = new AsyncTask<TrimmedRSSObject, Void, Void>() {
            @Override
            protected Void doInBackground(TrimmedRSSObject... objects) {
                for (TrimmedRSSObject temp: objects) {
                    db.insert(temp);
                }
                return null;
            }

        };

        upDateDBAsync.execute(rss);
    }


}
