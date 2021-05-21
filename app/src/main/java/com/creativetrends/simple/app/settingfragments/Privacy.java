package com.creativetrends.simple.app.settingfragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.simplelock.SimpleLock;

/**
 * Created by Creative Trends Apps.
 */

@SuppressWarnings("ALL")
public class Privacy extends PreferenceFragment {
    final int REQUEST_LOCATION = 1;
    public boolean mListStyled;
    Context context;
    SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.privacy);

        Preference locker = findPreference("simple_locker");
        locker.setSummary(getActivity().getResources().getString(R.string.lock_text_new));

        myPrefListner = (prefs, key) -> {
            preferences.edit().putString("changed", "true").apply();
            Log.i("Settings", "Applying changes needed");
            switch (key) {

                case "simple_locker":
                    if (prefs.getBoolean("simple_locker", false)) {
                        AlertDialog.Builder terms = new AlertDialog.Builder(getActivity());
                        terms.setTitle("Simple Lock");
                        terms.setMessage(getResources().getString(R.string.saved_pin_message));
                        terms.setPositiveButton(R.string.ok, (arg0, arg1) -> {
                            Intent lock = new Intent(getActivity(), SimpleLock.class);
                            lock.putExtra("from", "settings");
                            startActivity(lock);
                        });
                        terms.show();
                    } else {
                        Intent lock = new Intent(getActivity(), SimpleLock.class);
                        lock.putExtra("from", "settings_disable");
                        startActivity(lock);
                    }
                    break;

                case "allow_location":
                    if (prefs.getBoolean("allow_location", false)) {
                        requestLocationPermission();
                    }

                    break;


                default:
                    break;

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
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Privacy");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }

    private void requestLocationPermission() {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int hasPermission = ContextCompat.checkSelfPermission(context, locationPermission);
        String[] permissions = new String[]{locationPermission};
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_LOCATION);
        } else {
            Log.i("", "");
        }

    }
}
