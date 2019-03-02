package com.android_lab_2.Interface;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.android_lab_2.model.TrimmedRSSObject;

import java.util.List;

@Dao
public interface itemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TrimmedRSSObject... objects);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(TrimmedRSSObject... objects);

    @Delete
    void delete(TrimmedRSSObject objects);

    @Query("SELECT * FROM trimmedrssobject")
    List<TrimmedRSSObject> getAllTrimmedRSSObjectsWhereSourceIs();

    @Query("SELECT * FROM trimmedrssobject WHERE origin = :url ORDER BY sortValue DESC Limit :num")
    List<TrimmedRSSObject> getTrimmedRSSObjects(int num, String url);

    @Query(value = "SELECT * FROM trimmedrssobject WHERE link = :id")
    TrimmedRSSObject getTrimmedRSSObjectById(String id);

    @Query("DELETE FROM trimmedrssobject")
    void dropTable();
}
