package com.creativetrends.simple.app.settingfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.preferences.CustomSwitchPreference;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 8/18/2018.
 */

@SuppressWarnings("ALL")
public class Tabs extends PreferenceFragment {
    public boolean mListStyled;
    Context context;
    CustomSwitchPreference top, title, lock;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.tabs);
        top = (CustomSwitchPreference) findPreference("top_tabs");
        title = (CustomSwitchPreference) findPreference("tab_labels");
        lock = (CustomSwitchPreference) findPreference("lock_tabs");

        if (preferences.getBoolean("top_tabs", false)) {
            title.setChecked(false);
            lock.setChecked(false);
            title.setSelectable(false);
            lock.setSelectable(false);
        }
        myPrefListner = (prefs, key) -> {
            preferences.edit().putString("changed", "true").apply();
            if ("top_tabs".equals(key)) {
                if (prefs.getBoolean("top_tabs", false)) {
                    title.setChecked(false);
                    lock.setChecked(false);
                    title.setSelectable(false);
                    lock.setSelectable(false);
                    preferences.edit().putBoolean("show_panels", false).apply();
                }else{
                    title.setSelectable(true);
                    lock.setSelectable(true);
                    preferences.edit().putBoolean("show_panels", true).apply();

                }
            }
        };
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Tabs");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }

}
