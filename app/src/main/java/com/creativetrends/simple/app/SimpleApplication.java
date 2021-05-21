package com.creativetrends.simple.app;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.creativetrends.simple.app.crashreports.CrashInfo;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.github.ajalt.reprint.core.Reprint;


public class SimpleApplication extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    SharedPreferences preferences;

    public static Context getContextOfApplication() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        if (PreferencesUtility.getBoolean("enable_crash", false)) {
            CrashInfo.install(this);
        }
        Reprint.initialize(this, new Reprint.Logger() {
            @Override
            public void log(String message) {
            }

            @Override
            public void logException(Throwable throwable, String message) {
            }
        });
        checkAppReplacingState();
        createNotificationChannel();
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        PreferenceManager.setDefaultValues(this, R.xml.browsing, true);
        PreferenceManager.setDefaultValues(this, R.xml.customize, true);
        PreferenceManager.setDefaultValues(this, R.xml.facebook, true);
        PreferenceManager.setDefaultValues(this, R.xml.notifications, true);
        PreferenceManager.setDefaultValues(this, R.xml.privacy, true);
        PreferenceManager.setDefaultValues(this, R.xml.tabs, true);
        PreferenceManager.setDefaultValues(this, R.xml.video_prefs, true);
        if (preferences.getBoolean("first_theme", true)) {
            preferences.edit().putInt("custom_color", ThemeUtils.getDefaultPrimary(mContext)).apply();
            preferences.edit().putBoolean("first_theme", false).apply();
        }
    }


    private void checkAppReplacingState() {
        if (getResources() == null) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //reg
            CharSequence name = getString(R.string.notification_reg);
            String description = getString(R.string.channel_description);
            //messages
            CharSequence mess_name = getString(R.string.notification_mess);
            String mess_description = getString(R.string.mess_description);
            //downloads
            CharSequence widget_name = getString(R.string.notifications_widget);
            String widget_description = getString(R.string.notifications_widget_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            int wimp = NotificationManager.IMPORTANCE_LOW;
            //regular
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_reg_channel), name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.enableLights(true);

            //message
            NotificationChannel channel2 = new NotificationChannel(getString(R.string.notification_mess_channel), mess_name, importance);
            channel2.setDescription(mess_description);
            channel2.setShowBadge(true);
            channel2.enableLights(true);
            //downloads
            NotificationChannel channel5 = new NotificationChannel(getString(R.string.notification_widget_channel), widget_name, wimp);
            channel5.setDescription(widget_description);
            channel5.setShowBadge(true);
            channel5.enableVibration(false);
            channel5.enableLights(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;

            if (notificationManager.getNotificationChannel(getString(R.string.notification_reg_channel)) == null || notificationManager.getNotificationChannel(getString(R.string.notification_mess_channel)) == null || notificationManager.getNotificationChannel(getString(R.string.notification_widget_channel)) == null) {
                notificationManager.createNotificationChannel(channel);
                notificationManager.createNotificationChannel(channel2);
                notificationManager.createNotificationChannel(channel5);
            }

        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}