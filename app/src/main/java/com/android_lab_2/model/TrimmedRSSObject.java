package com.android_lab_2.model;

public class TrimmedRSSObject {

    public  String  title;
    public  String  pupDate;
    public  String  link;
    public  String  description;
   // public  Object enclosure;


    public TrimmedRSSObject(String title, String pupDate, String link, String description) {
        this.title = title;
        this.pupDate = pupDate;
        this.link = link;
        this.description = description;
    }


    public TrimmedRSSObject(RSSObject rssObject) {
        this.title =
        this.pupDate = pupDate;
        this.link = link;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPupDate() {
        return pupDate;
    }

    public void setPupDate(String pupDate) {
        this.pupDate = pupDate;
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
