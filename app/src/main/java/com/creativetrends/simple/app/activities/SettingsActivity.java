package com.creativetrends.simple.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.settingfragments.Settings;
import com.creativetrends.simple.app.utils.FacebookStatusBugs;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;

public class SettingsActivity extends AppCompatActivity {
    public static FragmentManager fm;
    SharedPreferences preferences;
    Toolbar toolbar;
    boolean MenuLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(NetworkConnection.isConnected(this)) {
            new FacebookStatusBugs().execute();
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        }


        try {
            fm = getSupportFragmentManager();
        } catch (Exception i) {
            i.printStackTrace();
        }

        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new Settings()).commit();
    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("changed", "false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (StaticUtils.isLollipop()) {
            getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
        }
        setColors();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            changes();
        } else
            getFragmentManager().popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void changes() {
        if (PreferencesUtility.getString("changed", "").equals("false")) {
            PreferencesUtility.putString("needs_lock", "false");
            finish();
        } else {
            Intent intent = new Intent(getPackageManager().getLaunchIntentForPackage(getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void setColors() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black));
        } else if (!MenuLight) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        }
        toolbar.setBackgroundColor(setToolbarColor(this));

        setOverflowIconColor(ContextCompat.getColor(this, R.color.m_color));

        if(PreferencesUtility.getBoolean("color_status", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));

            }
        }else{
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if(StaticUtils.isMarshmallow()) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightStatusBar(this);
                }else{
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(setToolbarColor(this));
            }
        }
        if (PreferencesUtility.getBoolean("color_nav", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            }
        } else {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if(StaticUtils.isOreo()){
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightNavigationBar(this);
                }else{
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(setToolbarColor(this));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().setNavigationBarDividerColor(ThemeUtils.getTheme(this));
                }
            }
        }

    }


    private void setOverflowIconColor(int color) {
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            Drawable newIcon = overflowIcon.mutate();
            newIcon.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            toolbar.setOverflowIcon(newIcon);
        }
    }

    private int setToolbarColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.darcula);
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.black);
                default:
                    return ContextCompat.getColor(context, R.color.white);
            }

        }
    }


}