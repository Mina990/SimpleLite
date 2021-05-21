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
import com.creativetrends.simple.app.utils.PreferencesUtility;

@SuppressWarnings("deprecation")
public class Video extends PreferenceFragment {
    Context context;
    boolean mListStyled;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    private SharedPreferences preferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.video_prefs);

        myPrefListner = (prefs, key) -> PreferencesUtility.putString("changed", "true");
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
        getActivity().setTitle(R.string.videos);
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);

    }


    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }



}
