package com.creativetrends.simple.app.settingfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;

/**
 * Created by Creative Trends Apps.
 */

@SuppressWarnings("ALL")
public class Browsing extends PreferenceFragment {
    public boolean mListStyled;
    Context context;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.browsing);


        myPrefListner = (prefs, key) -> preferences.edit().putString("changed", "true").apply();

    }

    @Override
    public void onStart() {
        super.onStart();
        View rootView = getView();
        if (rootView != null) {
            ListView list = rootView.findViewById(android.R.id.list);
            list.setPadding(0, 0, 0, 0);
            list.setDivider(null);
            list.setVerticalScrollBarEnabled(false);
            mListStyled = true;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Browsing");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }


}