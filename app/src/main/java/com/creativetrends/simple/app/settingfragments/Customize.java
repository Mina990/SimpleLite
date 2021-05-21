package com.creativetrends.simple.app.settingfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.ListView;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.SettingsActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.preferences.CustomSwitchPreference;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.thebluealliance.spectrum.SpectrumDialog;

/**
 * Created by Creative Trends Apps.
 */

@SuppressWarnings("ALL")
public class Customize extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public boolean mListStyled;
    Context context;
    CustomSwitchPreference colored_nav;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.customize);
        colored_nav = (CustomSwitchPreference) findPreference("color_nav");


        if (!hasSoftKeys() && StaticUtils.isLollipop()) {
            colored_nav.setEnabled(false);
            colored_nav.setSelectable(false);
            colored_nav.setSummary(getResources().getString(R.string.not_supported));
        } else if (hasSoftKeys() && StaticUtils.isLollipop()) {
            colored_nav.setEnabled(true);
            colored_nav.setSelectable(true);
            colored_nav.setSummary(getResources().getString(R.string.enable_color));
        }


        myPrefListner = (prefs, key) -> preferences.edit().putString("changed", "true").apply();
        Preference color = findPreference("custom_color");
        color.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if ("custom_color".equals(key)) {
            showPicker();
        }
        return false;
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
        getActivity().setTitle("Customize");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }


    public boolean hasSoftKeys() {
        boolean hasSoftwareKeys;
        Display d = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;
        hasSoftwareKeys = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        return hasSoftwareKeys;
    }

    private void showPicker() {
        try {
            new SpectrumDialog.Builder(getActivity())
                    .setTitle("Preset Colors")
                    .setColors(R.array.demo_colors)
                    .setSelectedColor(preferences.getInt("custom_color", 0))
                    .setDismissOnColorSelected(false)
                    .setPositiveButtonText(getResources().getString(R.string.ok))
                    .setNegativeButtonText(getResources().getString(R.string.cancel))
                    .setOutlineWidth(0)
                    .setOnColorSelectedListener((positiveResult, color) -> {
                        if (positiveResult) {
                            preferences.edit().putInt("custom_color", color).apply();

                        }
                    }).build().show(SettingsActivity.fm, "");

        } catch (Exception i) {
            i.printStackTrace();
        }
    }

}