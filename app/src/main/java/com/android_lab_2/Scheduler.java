package com.android_lab_2;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import com.android_lab_2.DataBase.DBHelper;
import com.android_lab_2.model.TrimmedRSSObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scheduler extends JobService {
    private static final String TAG = "Scheduler";
    private boolean jobCancelled = false;
    private DBHelper db;
    private SharedPreferences  sharedPreferences;
    private String [] rssSourceList;
    private List<String> getURLs = new ArrayList<>();

    // this method is run when the service stats
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: ");
        // read settings form DefaultSharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temp = sharedPreferences.getString(getString(R.string.rss_source_key), getString(R.string.default_value_rss_source));
        rssSourceList = temp.split(getString(R.string.rss_source_list_separator));
        Log.d(TAG, "onStartJob: read rss_source_key: value: " + temp);
        getURLs.addAll(Arrays.asList(rssSourceList));

        // ensure only 1 instance of the database exists
        if (ListFragment.db == null) {
            db = new DBHelper(this, ListFragment.DATABASE_NAME);
        } else {
            db = ListFragment.db;
        }
        // gets new rss articles
        getNewRRS(params);

        return true;
    }

    // this method is run when the service stops or is canceled
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: ");
        jobCancelled = true;
        // retry later if canceled
        return true;
    }

    // gets new rss articles
    private void getNewRRS(final JobParameters params) {

        new Thread(() -> {

            List<TrimmedRSSObject> parseFeedList;
            // for all rss sources
            for (String currentURL : getURLs) {
                if (jobCancelled) {
                    Log.d(TAG, "run: jobCancelled == true");
                    return;
                }
                // do som validation
                if (currentURL == null) {
                    continue;
                }
                if (currentURL.equals("") || currentURL.equals(" ")) {
                    continue;
                }

                try {
                    // do som validation
                    if(!currentURL.startsWith("http://") && !currentURL.startsWith("https://"))
                        currentURL = "http://" + currentURL;

                    URL url = new URL(currentURL);
                    InputStream inputStream = url.openConnection().getInputStream();
                    // pars url stream
                    parseFeedList = parseFeed(inputStream, currentURL);

                    for (TrimmedRSSObject temp : parseFeedList){
                        if (jobCancelled) {
                            Log.d(TAG, "run: jobCancelled == true");
                            return;
                        }
                        // update database
                        upDateDB(temp);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error", e);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "Error", e);
                }
            }

        }).start();

    }

    // parses a url stream, and tries to get rss objects from itt
    public List<TrimmedRSSObject> parseFeed(InputStream inputStream, String currentURL) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        String pubDate = null;
        boolean isItem = false;
        List<TrimmedRSSObject> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {

                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }
                // XML input validation
                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                } else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                } else if (name.equalsIgnoreCase("link")) {
                    if (title != null || description != null) {
                        link = result;
                    }

                }
                // if everything is OK, add item to list
                if (title != null && link != null && description != null && pubDate !=null) {
                    if(isItem) {
                        TrimmedRSSObject item = new TrimmedRSSObject(title, pubDate, link, description, currentURL);
                        Log.d(TAG, "parseFeed: pubDate: "+ pubDate + " link: " + link +" title:" +title + " description: " + description);
                        items.add(item);

                    }

                    // reset values for next iteration
                    title = null;
                    link = null;
                    description = null;
                    pubDate = null;
                    isItem = false;
                }

            }

            return items;
        } finally {
            inputStream.close();
        }
    }

    // update the database with new rss objects
    public void upDateDB(TrimmedRSSObject rss) {
        Log.d(TAG, "upDateDB: date: " + rss.getPubDate()+ " title:" +rss.getTitle()+ " description: " + rss.getDescription());
        db.insert(rss);
        Log.d(TAG, "doInBackground: from updateDB in Scheduler.java");
    }


}
