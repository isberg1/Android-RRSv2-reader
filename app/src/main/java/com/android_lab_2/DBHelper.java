package com.android_lab_2;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.List;

// a wrapper class for the actual database object. provides public methods for
// interacting with database
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

    // insets an rss object to the database
    public void insert(TrimmedRSSObject object) {
        db.daoAccess().insert(object);
    }

    public TrimmedRSSObject get1ByID(String link) {

        TrimmedRSSObject trimmedRSSObjectById = db.daoAccess().getTrimmedRSSObjectById(link);
        return trimmedRSSObjectById;
    }

    // the main DB query, returns a list based on specified length(num) and URL(origin)
    public List<TrimmedRSSObject> getAllEntries(int num, String origin) {
        // todo get num from shared preferences
        //int num = 10;
        List<TrimmedRSSObject> trimmedRSSObjects = db.daoAccess().getTrimmedRSSObjects(num, origin);
        return trimmedRSSObjects;
    }

    // secondary DB query, returns a list of all rss objects in the database,
    // list is used for finding regex pattern searches
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
