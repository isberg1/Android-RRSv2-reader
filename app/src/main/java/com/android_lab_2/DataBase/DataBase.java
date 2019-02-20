package com.android_lab_2.DataBase;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.android_lab_2.Interface.itemDao;
import com.android_lab_2.model.TrimmedRSSObject;

@Database(entities = {TrimmedRSSObject.class}, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract itemDao daoAccess();
}