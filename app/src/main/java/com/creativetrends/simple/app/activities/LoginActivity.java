package com.creativetrends.simple.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.creativetrends.simple.app.adapters.UserItems;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PrefManager;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Calendar;


public class LoginActivity extends AppCompatActivity {
    CardView login;
    private PrefManager prefManager;
    TextView privacy, terms;
    Calendar calendar;
    int year;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        if(PreferencesUtility.getBoolean("is_new_clear", true)){
            ArrayList<UserItems> users = PreferencesUtility.getUsers();
            users.clear();
            PreferencesUtility.saveUsers(users);
            StaticUtils.clearCookies();
            StaticUtils.clearStrings();
            prefManager.setFirstTimeLaunch(true);
            PreferencesUtility.putBoolean("is_new_clear", false);
        }
        setTranslucentStatus();
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        setContentView(R.layout.activity_login_screen);
        login = findViewById(R.id.custom_facebook_button);
        login.setOnClickListener(v -> onStartLogin());
        terms = findViewById(R.id.simple_terms);
        privacy = findViewById(R.id.simple_policy);

        terms.setOnClickListener(v -> {
            if(!isDestroyed()) {
                AlertDialog.Builder terms = new AlertDialog.Builder(LoginActivity.this);
                terms.setTitle(getResources().getString(R.string.terms_settings));
                terms.setMessage(getResources().getString(R.string.eula_string, year));
                terms.setPositiveButton(R.string.ok, (arg0, arg1) -> {
                });
                terms.show();
            }
        });

        privacy.setOnClickListener(v -> {
            if(!isDestroyed()) {
                AlertDialog.Builder policy = new AlertDialog.Builder(LoginActivity.this);
                policy.setTitle("Privacy Policy");
                policy.setMessage(Html.fromHtml(getString(R.string.policy_about)));
                policy.setPositiveButton(R.string.ok, (arg0, arg1) -> {
                });
                policy.show();
            }

        });
        TextView copyright = findViewById(R.id.copyright_text);
        copyright.setText(getResources().getString(R.string.copy_right, year));
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!prefManager.isFirstTimeLaunch()) {
            goToSplash();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!prefManager.isFirstTimeLaunch()) {
            goToSplash();
            finish();
        }

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
    protected void onStop() {
        super.onStop();
    }

    private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    private void onStartLogin() {
        Intent loginIntent = new Intent(this, WebViewLoginActivity.class);
        loginIntent.setData(Uri.parse("https://m.facebook.com/login.php"));
        startActivity(loginIntent);
    }


    private void goToSplash() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(LoginActivity.this, SplashActivity.class));
        finish();
    }


}
