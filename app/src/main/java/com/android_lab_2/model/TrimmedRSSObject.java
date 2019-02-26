package com.android_lab_2.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "trimmedRSSObject")
public class TrimmedRSSObject  {
    private static final String TAG = "TrimmedRSSObject";
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "link")
    public  String  link;
    @ColumnInfo(name = "title")
    public  String  title;
    @ColumnInfo(name = "pupDate")
    public  String  pubDate;
    @ColumnInfo(name = "description")
    public  String  description;
   // public  Object enclosure;




    public TrimmedRSSObject(String title, String pubDate, String link, String description) {
        this.title = title;
        this.pubDate = dateFormatConverter(pubDate);
        this.link = link;
        this.description = description;
    }

    public String dateFormatConverter(String oldTime) {

        if (validateJavaDate(oldTime)) {
            return oldTime;
        }



        Log.d(TAG, "dateFormatConverter: oldTime: " + oldTime);
        // RSS time format
        DateFormat originalFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        // SQLite compatible format
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = originalFormat.parse(oldTime);

        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG, "dateFormatConverter:  pubDate: " + oldTime);
            return "Unavailable";
        }
        String formattedDate = targetFormat.format(date);

        return formattedDate;
    }


    public TrimmedRSSObject(RSSObject rssObject) {
        this.title =
        this.pubDate = pubDate;
        this.link = link;
        this.description = description;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pupDate) {
        this.pubDate = pubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


// source: https://beginnersbook.com/2013/05/java-date-format-validation/
    public static boolean validateJavaDate(String strDate)
    {
        /* Check if date is 'null' */
        if (strDate.trim().equals(""))
        {
            return true;
        }
        /* Date is not 'null' */
        else
        {
            /*
             * Set preferred date format,
             * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfrmt.setLenient(false);
            /* Create Date object
             * parse the string into date
             */
            try
            {
                Date javaDate = sdfrmt.parse(strDate);
            }
            /* Date format is invalid */
            catch (ParseException e)
            {
                return false;
            }
            /* Return true if date format is valid */
            return true;
        }
    }


    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.title);
        stringBuilder.append(" ");
        stringBuilder.append(this.pubDate);
        stringBuilder.append(" ");
        stringBuilder.append(this.description);

        return stringBuilder.toString();
    }
}




