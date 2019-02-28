package com.android_lab_2;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import com.android_lab_2.DataBase.DBHelper;
import com.android_lab_2.model.ParseFeed;
import com.android_lab_2.model.RSSObject;
import com.android_lab_2.model.TrimmedRSSObject;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
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
    private boolean isRunningManually = false;
    private List<String> getURLs = new ArrayList<>();


    public boolean manualJob(JobParameters params, URL url) {
        isRunningManually = true;

        // ensure only 1 instance of the database exists
        if (ListFragment.db == null) {
            db = new DBHelper(this, ListFragment.DATABASE_NAME);
        } else {
            db = ListFragment.db;
        }

        try {
            getNewRRS(params);
        } catch (Exception e) {
            Log.d(TAG, "manualJob: job rejected: ");
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: ");

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
        getNewRRS(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: ");
        jobCancelled = true;
        // retry later if canceled
        return true;
    }

    private void getNewRRS(final JobParameters params) {

        new Thread(new Runnable() {
            @Override
            public void run() {


                List<TrimmedRSSObject> parseFeedList;

                String RSSLink = "https://www.nrk.no/nyheter/siste.rss";// "https://stackoverflow.com/questions/11072576/set-selected-item-of-spinner-programmatically";    //"https://www.vg.no/rss/feed/";            //
               // getURLs.add("https://www.nrk.no/nyheter/siste.rss");
               // getURLs.add("https://www.vg.no/rss/feed/");


                for (String currentURL : getURLs) {
                    if (jobCancelled) {
                        Log.d(TAG, "run: jobCancelled == true");
                        return;
                    }

                    if (currentURL == null) {
                        continue;
                    }
                    if (currentURL.equals("") || currentURL.equals(" ")) {
                        continue;
                    }

                    try {
                        if(!currentURL.startsWith("http://") && !currentURL.startsWith("https://"))
                            currentURL = "http://" + currentURL;

                        URL url = new URL(currentURL);
                        InputStream inputStream = url.openConnection().getInputStream();
                        parseFeedList = parseFeed(inputStream, currentURL);

                        for (TrimmedRSSObject temp : parseFeedList){
                            if (jobCancelled) {
                                Log.d(TAG, "run: jobCancelled == true");
                                return;
                            }
                            upDateDB(temp);
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Error", e);
                    } catch (XmlPullParserException e) {
                        Log.e(TAG, "Error", e);
                    }
                }


            }
        }).start();

    }

    public static List<TrimmedRSSObject> parseFeed(InputStream inputStream, String currentURL) throws XmlPullParserException,
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

                if (name.equalsIgnoreCase("title")) {
                    title = result;

                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }

                if (title != null && link != null && description != null && pubDate !=null) {
                    if(isItem) {
                        TrimmedRSSObject item = new TrimmedRSSObject(title, pubDate, link, description, currentURL);
                        Log.d(TAG, "parseFeed: pubDate: "+ pubDate + "title:" +title + " description: " + description);
                        items.add(item);
                    }
                    else {
                       /* mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;*/
                    }

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





    public void upDateDB(TrimmedRSSObject rss) {
        Log.d(TAG, "upDateDB: date: " + rss.getPubDate()+ " title:" +rss.getTitle()+ " description: " + rss.getDescription());

        db.insert(rss);
        //ListFragment.db.insert(rss);
        Log.d(TAG, "doInBackground: from updateDB in Scheduler.java");

    }
}
