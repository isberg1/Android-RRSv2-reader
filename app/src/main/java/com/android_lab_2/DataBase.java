package com.android_lab_2;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

// the apps database object. uses a ROOM SQLite database
@Database(entities = {TrimmedRSSObject.class}, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract itemDao daoAccess();
}