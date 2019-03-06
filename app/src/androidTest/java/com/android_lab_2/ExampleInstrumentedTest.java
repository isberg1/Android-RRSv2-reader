package com.android_lab_2;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android_lab_2.model.TrimmedRSSObject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.android_lab_2", appContext.getPackageName());
    }



    // tests the rss url parser with a invalid rss url
   @Test
    public void parsingInvalidRssUrl_test()  {

       Scheduler scheduler = new Scheduler();

       String inValidRssFeed = "https://www.vg.no";

       List<TrimmedRSSObject> trimmedRSSObjectList = new ArrayList<>();

       try {
           URL url = new URL(inValidRssFeed);
           InputStream inputStream = url.openConnection().getInputStream();
            // this should generate a exception
           trimmedRSSObjectList = scheduler.parseFeed(inputStream, inValidRssFeed);
            // if this statement is reached, something is wrong
           fail("invalid url");

       } catch (Exception e) {
           e.printStackTrace();
           // no rss objects should be in list
           assertEquals(0, trimmedRSSObjectList.size());
       }

   }

    // tests the rss url parser with a valid rss url
    @Test
    public void parsingValidRssUrl_test()  {

        Scheduler scheduler = new Scheduler();

        String inValidRssFeed = "https://www.vg.no/rss/feed/";

        List<TrimmedRSSObject> trimmedRSSObjectList;

        try {
            URL url = new URL(inValidRssFeed);
            InputStream inputStream = url.openConnection().getInputStream();
            // this should generate a exception
            trimmedRSSObjectList = scheduler.parseFeed(inputStream, inValidRssFeed);
            // no rss objects should be in list

            assertNotEquals(0, trimmedRSSObjectList.size());

        } catch (Exception e) {
            e.printStackTrace();

            // if this statement is reached, something is wrong
            fail("invalid url");
        }

    }

    // make a TrimmedRSSObject with a valid pubDate value
    @Test
    public void TrimmedRSSObject_correctDate() {

        TrimmedRSSObject trimmedRSSObject =
                new TrimmedRSSObject("","Tue, 05 Mar 2019 17:52:51 +0100","","","");

        assertEquals("2019-03-05 17:52:51",trimmedRSSObject.getPubDate());
    }


    // make a TrimmedRSSObject with a invalid pubDate value
    @Test
    public void TrimmedRSSObject_inCorrectDate() {

        TrimmedRSSObject trimmedRSSObject = null;

        try {
            trimmedRSSObject =
                    new TrimmedRSSObject("","Tue,  17:52:51 +0100","","","");
            // if this statement is reached, something is wrong
            fail("invalid date");
        } catch (Exception e) {
            assertEquals(null, trimmedRSSObject);
        }
    }


    // test the  regex filter functionality in ListFragment
    @Test
    public void searchMatch_test() {
        String validDate = "Tue, 05 Mar 2019 17:52:51 +0100";
        boolean result;

        TrimmedRSSObject trimmedRSSObject =
               new TrimmedRSSObject("123",validDate,"", "some numbers and some text","");

        // run UtilityClass.searchMatch with a variety of regex expressions

        result = UtilityClass.searchMatch(trimmedRSSObject, ".*num.*");
        assertTrue(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, "numbers");
        assertFalse(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, "111");
        assertFalse(result);


        result = UtilityClass.searchMatch(trimmedRSSObject, "[1-9]*");
        assertTrue(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, "[1-9][1-9][1-9]");
        assertTrue(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, "[^A-Z]*");
        assertTrue(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, ".*2018.*");
        assertFalse(result);

        result = UtilityClass.searchMatch(trimmedRSSObject, ".*2019.*");
        assertTrue(result);


    }



}
