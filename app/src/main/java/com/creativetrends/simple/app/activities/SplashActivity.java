package com.creativetrends.simple.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;

/**
 * Created by Creative Trends Apps.
 */

public class SplashActivity extends AppCompatActivity {
    RelativeLayout splash;
    ImageView splashIcon;
    TextView splashText;
    boolean MenuLight;

    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash = findViewById(R.id.relsplash);
        splashIcon = findViewById(R.id.logo_simple);
        splashText = findViewById(R.id.app_name_simple);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");


        if(PreferencesUtility.getBoolean("color_splash", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
                splash.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
                getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
                splash.setBackgroundColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
                getWindow().setNavigationBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
                splash.setBackgroundColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));

            }
        }else{
            splash.setBackgroundColor(setToolbarColor(this));
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if(StaticUtils.isMarshmallow()) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightStatusBar(this);
                }else{
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
                splashIcon.setColorFilter(ThemeUtils.getColorPrimary(this), PorterDuff.Mode.MULTIPLY);

                if (!MenuLight) {
                    splashText.setTextColor(ContextCompat.getColor(this, R.color.white));
                } else if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                    splashText.setTextColor(ContextCompat.getColor(this, R.color.white));
                } else if (MenuLight && !ThemeUtils.isNightTime()) {
                    splashText.setTextColor(ContextCompat.getColor(this, R.color.dark));
                } else if (!MenuLight) {
                    splashText.setTextColor(ContextCompat.getColor(this, R.color.white));
                }


            } else if (!MenuLight) {
                getWindow().setStatusBarColor(setToolbarColor(this));
            }
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
        showBrand();
    }

    private void showBrand() {
        new Handler().postDelayed(() -> {
            PreferencesUtility.putString("needs_lock", "true");
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }, 200);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private int setToolbarColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.darcula);
                case "darktheme":
                case "amoledtheme":
                    return ContextCompat.getColor(context, R.color.black);
                default:
                    return ContextCompat.getColor(context, R.color.white);
            }

        }
    }
}