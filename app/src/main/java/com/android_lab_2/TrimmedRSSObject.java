package com.android_lab_2;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


// the apps RSS object
// also a table in the SQLite database
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
    @ColumnInfo(name = "sortValue")
    public long sortValue;
    @ColumnInfo(name = "imageUrl")
    public String imageUrl;

    public TrimmedRSSObject(String title, String pubDate, String link, String description, String origin, String imageUrl) {
        this.title = title.trim();
        this.pubDate = dateFormatConverter(pubDate);
        this.link = link.trim();
        this.description = description.trim();
        this.sortValue = getSortingValueFromDate(this.pubDate);
        this.origin = origin;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public long getSortValue() {
        return sortValue;
    }

    public void setSortValue(long sortValue) {
        this.sortValue = sortValue;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @ColumnInfo(name = "origin")
    public String origin;


// used to convert date to a number that the database can later sort entries based on
    private long getSortingValueFromDate(String date) {


        date = date.trim();
        String[] stage1 = date.split(" ");
        String[] stage2 = stage1[0].split("-");
        String[] stage3 = stage1[1].split(":");

        String sortNum = date.replaceAll("-","");
        sortNum = sortNum.replaceAll(" ", "");
        sortNum = sortNum.replaceAll(":", "");


        Log.d(TAG, "getSortingValueFromDate: " + sortNum);
        // set a default value
        Long num =Long.valueOf(0);
        try {
            num = Long.parseLong(sortNum);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Log.d(TAG, "getSortingValueFromDate: parisng sortNum failed");
        }

        return num;
    }
    // convert the rss field "pubDate" to a simpler date format
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


    // validates a date, not my one code:
// source: https://beginnersbook.com/2013/05/java-date-format-validation/
    public boolean validateJavaDate(String strDate) {
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
            String format = "yyyy-MM-dd HH:mm:ss";  // cant access it from strings.xml
            SimpleDateFormat sdfrmt = new SimpleDateFormat(format);
            sdfrmt.setLenient(false);
            /* Create Date object
             * parse the string into date
             */
            try
            {
                Date javaDate = sdfrmt.parse(strDate);
            }
            /* Date ffformat is invalid */
            catch (ParseException e)
            {
                return false;
            }
            /* Return true if date format is valid */
            return true;
        }
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



// used for debugging
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




