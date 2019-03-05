package com.android_lab_2;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.widget.LinearLayout;

import com.android_lab_2.model.TrimmedRSSObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }



    @Test
    public void TrimmedRSSObject_correctDate() {

        TrimmedRSSObject trimmedRSSObject =
                new TrimmedRSSObject("","Tue, 05 Mar 2019 17:52:51 +0100","","","");

        assertEquals("2019-03-05 17:52:51",trimmedRSSObject.getPubDate());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
   /*
   @Test
    public void parsing_test() throws IOException, XmlPullParserException {

       Scheduler scheduler = new Scheduler();

       String valideURL = "https://www.vg.no/rss/feed/forsiden";



       List<TrimmedRSSObject> trimmedRSSObjectList;

       try {
           thrown.expect(NullPointerException.class);
           URL url = new URL(valideURL);
           InputStream inputStream = url.openConnection().getInputStream();

           trimmedRSSObjectList = scheduler.parseFeed(inputStream, valideURL);


       } catch (Exception e) {
           e.printStackTrace();
       }


   }
*/
}