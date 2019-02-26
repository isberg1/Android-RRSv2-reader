package com.android_lab_2;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Scheduler extends JobService {
    private static final String TAG = "Scheduler";
    private boolean jobCancelled = false;
    DBHelper db;

   /* @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }*/

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: ");

        if (ListFragment.db == null) {
            db = new DBHelper(this, ListFragment.DATABASE_NAME);
        } else {
            db = ListFragment.db;
        }
        getNewRRS(params);

        //jobFinished(params,false);
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
                List<String> getURLs = new ArrayList<>();
                String RSSLink = "https://www.nrk.no/nyheter/siste.rss";
                getURLs.add(RSSLink);
                //   getURLs.add(vgRSS);

                if (jobCancelled) {
                    Log.d(TAG, "run: jobCancelled == true");
                    return;
                }

                try {
                    if(!RSSLink.startsWith("http://") && !RSSLink.startsWith("https://"))
                        RSSLink = "http://" + RSSLink;

                    URL url = new URL(RSSLink);
                    InputStream inputStream = url.openConnection().getInputStream();
                    parseFeedList = parseFeed(inputStream);

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
        }).start();

    }

    public List<TrimmedRSSObject> parseFeed(InputStream inputStream) throws XmlPullParserException,
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

                if (title != null && link != null && description != null) {
                    if(isItem) {
                        TrimmedRSSObject item = new TrimmedRSSObject(title, pubDate, link, description);
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
