package com.creativetrends.simple.app.settingfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NotificationService;

import java.util.concurrent.TimeUnit;


/**
 * Created by Creative Trends Apps.
 */

@SuppressWarnings("ALL")
public class NotificationsOreo extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public boolean mListStyled;
    Context context;
    private SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    private SharedPreferences preferences;
    WorkManager mWorkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.notifications);
        mWorkManager = WorkManager.getInstance(context);

        myPrefListner = (prefs, key) -> {
            preferences.edit().putString("changed", "true").apply();
            Log.i("Settings", "Applying changes needed");
            if ("enable_notifications".equals(key)) {
                if (prefs.getBoolean("enable_notifications", false)) {
                    reschedule();
                }else{
                    mWorkManager.cancelAllWork();
                }
            }

        };

        Preference notifications = findPreference("notif_channel");
        notifications.setOnPreferenceClickListener(this);

        Preference messages = findPreference("mess_channel");
        messages.setOnPreferenceClickListener(this);

    }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            switch (key) {
                case "notif_channel":
                    notificationSettings();
                    break;

                case "mess_channel":
                    messageSettings();
                    break;

                default:
                    break;

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
        getActivity().setTitle("Notifications");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);

    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }

    private void reschedule() {
        Constraints constraints;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .build();
        }else{
            constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .setRequiresCharging(false)
                    .build();
        }

        PeriodicWorkRequest periodicSyncDataWork =
                new PeriodicWorkRequest.Builder(NotificationService.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        // setting a backoff on case the work needs to retry
                        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();
        mWorkManager.enqueueUniquePeriodicWork("lite_work", ExistingPeriodicWorkPolicy.KEEP, periodicSyncDataWork);
    }

    private void notificationSettings() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            intent = new Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                    .putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, getString(R.string.notification_reg_channel));
        }
        startActivity(intent);
    }


    private void messageSettings() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            intent = new Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                    .putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, getString(R.string.notification_mess_channel));
        }
        startActivity(intent);
    }


}