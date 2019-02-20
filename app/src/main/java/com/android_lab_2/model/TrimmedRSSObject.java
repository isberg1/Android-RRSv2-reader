package com.android_lab_2.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "trimmedRSSObject")
public class TrimmedRSSObject  {

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
        this.pubDate = pubDate;
        this.link = link;
        this.description = description;
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
}
