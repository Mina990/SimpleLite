package com.creativetrends.simple.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Creative Trends Apps on 05/05/16.
 */

public class PrefManager {
    // Shared preferences file name
    private static final String PREF_NAME = "simple-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "FirstTimeLaunch";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public PrefManager(Context context) {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

}
