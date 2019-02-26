package com.android_lab_2.DataBase;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.android_lab_2.model.TrimmedRSSObject;

import java.util.List;

public class DBHelper {
    private DataBase db;
    private String name;

    public DBHelper(Context context, String name) {
        this.name = name;
        this.db = Room.databaseBuilder(context, DataBase.class, name).build();
    }

    public DataBase getDb() {
        return db;
    }

    public void setDb(DataBase db) {
        this.db = db;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void insert(TrimmedRSSObject object) {
        db.daoAccess().insert(object);
    }

    public TrimmedRSSObject get1ByID(String link) {

        TrimmedRSSObject trimmedRSSObjectById = db.daoAccess().getTrimmedRSSObjectById(link);
        return trimmedRSSObjectById;
    }

    public List<TrimmedRSSObject> getAllEntries() {
        // todo get num from shared preferences
        int num = 10;
        List<TrimmedRSSObject> trimmedRSSObjects = db.daoAccess().getTrimmedRSSObjects(num);
        return trimmedRSSObjects;
    }

    public List<TrimmedRSSObject> getAllTrimmedRSSObjectsWhereSourceIs(String source) {
        List<TrimmedRSSObject> trimmedRSSObjects = db.daoAccess().getAllTrimmedRSSObjectsWhereSourceIs();
        return trimmedRSSObjects;
    }
    
    public void dropTable() {
        db.daoAccess().dropTable();
    }

    public void delete1ByObject(TrimmedRSSObject object) {
        db.daoAccess().delete(object);
    }
    


}
