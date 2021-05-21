package com.creativetrends.simple.app.settingfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.adapters.SearchItems;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.preferences.CustomSwitchPreference;
import com.creativetrends.simple.app.utils.PreferencesUtility;

import java.util.ArrayList;

/**
 * Created by Creative Trends Apps.
 */

@SuppressWarnings("ALL")
public class Facebook extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    Context context;
    SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    SharedPreferences preferences;
    boolean mListStyled;
    CustomSwitchPreference p;
    com.creativetrends.simple.app.preferences.Preference clear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.facebook);
        clear = (com.creativetrends.simple.app.preferences.Preference) findPreference("clear_search");
        clear.setOnPreferenceClickListener(this);
        p = (CustomSwitchPreference) findPreference("show_panels");
        if (PreferencesUtility.getBoolean("top_tabs", false)) {
            p.setSummary("Not available while using top tabs");
            p.setChecked(false);
            p.setEnabled(false);
            p.setSelectable(false);
            preferences.edit().putBoolean("show_panels", false).apply();
        }
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
        getActivity().setTitle("Facebook");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);

    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if ("clear_search".equals(key)) {
            ArrayList<SearchItems> listBookmarks = PreferencesUtility.getSearch();
            listBookmarks.clear();
            PreferencesUtility.saveSearch(listBookmarks);
            Toast.makeText(context, "Search history cleared", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
