package com.android_lab_2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android_lab_2.Adapter.FeedAdapter;
import com.android_lab_2.model.RSSObject;
import com.google.gson.Gson;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    private RecyclerView recyclerView;
    private RSSObject rssObject;


    private String RSSLink = "https://www.nrk.no/toppsaker.rss";
    private String RSSToJsonAPI = "https://api.rss2json.com/v1/api.json?rss_url=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_tab,container,false);

        recyclerView = view.findViewById(R.id.feed_recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);


        getRRS();

        
        return view;
    }

    private void getRRS() {
        AsyncTask<String,String,String> getRRSAsync = new AsyncTask<String, String, String>() {

            ProgressDialog dialog = new ProgressDialog(getContext());


            @Override
            protected void onPreExecute() {
                dialog.setMessage("fetching data");
                dialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                String result;
                HTTPGet getter = new HTTPGet();
                result = getter.getData(strings[0]);

                return result.trim();
            }


            @Override
            protected void onPostExecute(String s) {
                dialog.dismiss();
                rssObject = new Gson().fromJson(s, RSSObject.class);
                FeedAdapter adapter = new FeedAdapter(getContext(), rssObject);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
        };

        StringBuilder url = new StringBuilder(RSSToJsonAPI);
        url.append(RSSLink);

        getRRSAsync.execute(url.toString());



    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            getRRS();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
