package com.android_lab_2;

import android.util.Log;

import com.android_lab_2.model.TrimmedRSSObject;

import java.util.regex.Pattern;



public class UtilityClass {
    private static final String TAG = "UtilityClass";
    // tries to match a regex pattern with a searchTerm, and return the boolean of the result
    public static boolean searchMatch(TrimmedRSSObject object, String searchTerm) {


        Log.d(TAG, "searchMatch: object: " + object.toString() + " searchTerm" + searchTerm);
        Log.d(TAG, "searchMatch: bool: " + Pattern.matches(searchTerm, object.toString()));

        Pattern p = Pattern.compile(searchTerm);
        // if title matches
        if (p.matcher(object.getTitle()).matches()){
            return true;
        }
        // if pubDate matches
        if (p.matcher(object.getPubDate()).matches()){
            return true;
        }
        // if description matches
        return p.matcher(object.getDescription()).matches();
    }


}
