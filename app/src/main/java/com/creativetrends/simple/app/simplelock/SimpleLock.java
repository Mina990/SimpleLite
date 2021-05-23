package com.creativetrends.simple.app.simplelock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import java.security.Key;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Creative Trends Apps.
 */

public class SimpleLock extends AppCompatActivity {
    SharedPreferences preferences;

    AppCompatTextView name, message;
    PinLockView mPinLockView;
    AppCompatImageView lockIcon;
    ScrollView splash;
    String UNLOCK_STRING = DXDecryptorlaLlEVmT.decode("juLzXVmX8fgu7Lw=");
    boolean simplewhite;
    String input = "";
    Boolean resetPassword = false;
    Boolean disablePassword = false;
    String passwordConfirmed = null;
    int attempts = 4;
    int seconds = 30;
    CountDownTimer cTimer = null;
    View lock_click;
    String from;
    boolean MenuLight;

    private final PinLockListener mPinLockListener = new PinLockListener() {

        @Override
        public void onComplete(String pin) {
            input = pin;
            if (disablePassword) {
                if (pin.equals(preferences.getString(UNLOCK_STRING, ""))) {
                    preferences.edit().putString(UNLOCK_STRING, "").apply();
                    finish();
                    preferences.edit().putString("needs_lock", "false").apply();
                } else {
                    if (attempts == 0) {
                        lock_click.setVisibility(View.VISIBLE);
                        mPinLockView.post(() -> mPinLockView.resetPinLockView());
                        startDisable();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.attempts_left, attempts--), Toast.LENGTH_SHORT).show();
                        mPinLockView.post(() -> mPinLockView.resetPinLockView());
                    }
                }
            } else if (resetPassword) {
                if (passwordConfirmed != null) {
                    if (input.equals(passwordConfirmed)) {
                        preferences.edit().putString(UNLOCK_STRING, passwordConfirmed).apply();
                        AlertDialog.Builder close = new AlertDialog.Builder(SimpleLock.this);
                        close.setCancelable(false);
                        close.setTitle(getResources().getString(R.string.your_pin) + " " + pin);
                        close.setMessage(getResources().getString(R.string.your_pin_message));
                        close.setPositiveButton(R.string.ok, (arg0, arg1) -> finish());
                        close.setNeutralButton(null, null);
                        close.show();

                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.none_matching), Toast.LENGTH_SHORT).show();
                        message.setText(getResources().getString(R.string.pin_code_step_create));
                        mPinLockView.post(() -> mPinLockView.resetPinLockView());
                    }
                    passwordConfirmed = null;
                } else {
                    mPinLockView.resetPinLockView();
                    message.setText(getResources().getString(R.string.pin_code_step_confirm));
                    passwordConfirmed = input;
                }
            } else {
                if (pin.equals(preferences.getString(UNLOCK_STRING, ""))) {
                    finish();
                    preferences.edit().putString("needs_lock", "false").apply();
                } else {
                    if (attempts == 0) {
                        lock_click.setVisibility(View.VISIBLE);
                        mPinLockView.post(() -> mPinLockView.resetPinLockView());
                        startTimer();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.attempts_left, attempts--), Toast.LENGTH_SHORT).show();
                        mPinLockView.post(() -> mPinLockView.resetPinLockView());
                    }
                }
            }

        }


        @Override
        public void onEmpty() {

        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUp();
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        setContentView(R.layout.activity_lock);
        lock_click = findViewById(R.id.lock_view);
        lock_click.setSoundEffectsEnabled(false);
        lock_click.setOnClickListener(v -> Log.i("User", "Clicking"));
        splash = findViewById(R.id.scroll);
        splash.scrollTo(0, 0);
        if (PreferencesUtility.getBoolean("color_splash", false)) {
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
        } else {
            splash.setBackgroundColor(setToolbarColor(this));
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if (StaticUtils.isMarshmallow()) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightStatusBar(this);
                } else {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(setToolbarColor(this));
            }
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if (StaticUtils.isOreo()) {
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightNavigationBar(this);
                } else {
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(setToolbarColor(this));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().setNavigationBarDividerColor(ThemeUtils.getTheme(this));
                }
            }
        }

        name = findViewById(R.id.lock_name_new);
        message = findViewById(R.id.pin_code_step_textview);
        lockIcon = findViewById(R.id.pin_code_lock_imageview);
        mPinLockView = findViewById(R.id.pin_lock_view);
        mPinLockView.setPinLength(4);
        IndicatorDots mIndicatorDots = findViewById(R.id.indicator_dots);

        assert mPinLockView != null;
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
        mPinLockView.setPinLength(4);
        from = getIntent().getStringExtra(DXDecryptorlaLlEVmT.decode("h/3yWQ==")/*"from"*/);
        if (DXDecryptorlaLlEVmT.decode("kurpQFKM5dQ=")/*"settings"*/.equals(from)) {
            resetPassword = true;
            lockIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_settings));
            message.setText(getResources().getString(R.string.pin_code_step_create));
        } else if (DXDecryptorlaLlEVmT.decode("kurpQFKM5dQB4bsbREdgLw==")/*"settings_disable"*/.equals(from)) {
            disablePassword = true;
            lockIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_settings));
            message.setText(getResources().getString(R.string.pin_code_step_disable));
        } else if (StaticUtils.isMarshmallow() && Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
            lockIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fingerprint));
            message.setText(getResources().getString(R.string.pin_code_step_enter));
        } else {
            lockIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_settings));
            message.setText(getResources().getString(R.string.pin_code_step_enter));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getUserPic();
        splash.scrollTo(0, 0);
        if (StaticUtils.isMarshmallow() && Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
            if (disablePassword | resetPassword) {
                cancel();
            } else {
                start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (StaticUtils.isMarshmallow() && Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
            Reprint.cancelAuthentication();
            cancel();
        }
    }


    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        cancelTimer();
    }

    @SuppressLint("SetTextI18n")
    void getUserPic() {
        try {
            Calendar c = Calendar.getInstance();
            int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
            if (timeOfDay >= 6 && timeOfDay < 12) {
                name.setText(R.string.welcome_day_new);
            } else if (timeOfDay >= 12 && timeOfDay < 18) {
                name.setText(R.string.welcome_afternoon_new);
            } else if (timeOfDay >= 18) {
                name.setText(R.string.welcome_night_new);
            } else {
                name.setText(R.string.welcome_general_new);
            }
            final ImageView profilePic = findViewById(R.id.lock_image);
            Glide.with(this)
                    .load(PreferencesUtility.getString("user_picture", ""))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.ic_facebook)
                    .placeholder(R.drawable.ic_facebook)
                    .dontAnimate()
                    .dontTransform()
                    .into(profilePic);

        } catch (Exception ignored) {

        }
    }


    public void setUp() {
        ThemeUtils.setSettingsTheme(this);
        if (preferences.getBoolean(DXDecryptorlaLlEVmT.decode("gPrpW2SM68A28Q==")/*"auto_night"*/, false) && ThemeUtils.isNightTime()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
                getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
            }
        }
    }

    private void startTraditional() {
        Reprint.authenticate(new AuthenticationListener() {
            @Override
            public void onSuccess(int moduleTag) {
                message.setText(getResources().getString(R.string.pin_code_step_enter));
                lockIcon.setImageResource(R.drawable.ic_fingerprint_success);
                message.postDelayed(() -> {
                    preferences.edit().putString(DXDecryptorlaLlEVmT.decode("j+r4UEi97sg97g==")/*"needs_lock"*/, DXDecryptorlaLlEVmT.decode("h+7xR14=")/*"false"*/).apply();
                    finish();
                }, 500);
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                if (fatal) {
                    Log.i("", "");
                }
                message.setText(getResources().getString(R.string.pin_code_step_enter));
                lockIcon.setImageResource(R.drawable.ic_fingerprint_error);
                message.postDelayed(() -> {
                    message.setText(getResources().getString(R.string.pin_code_step_enter));
                    lockIcon.setImageResource(R.drawable.ic_fingerprint);
                }, 1500);
            }
        });
    }

    private void start() {
        if (!resetPassword || !disablePassword) {
            startTraditional();
        } else {
            cancel();
        }
    }

    private void cancel() {
        Reprint.cancelAuthentication();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }


    void startTimer() {
        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                message.setText(getResources().getString(R.string.please_seconds, seconds--));
            }

            public void onFinish() {
                seconds = 30;
                attempts = 4;
                lock_click.setVisibility(View.GONE);
                message.setText(getResources().getString(R.string.pin_code_step_enter));
            }
        };
        cTimer.start();
    }

    void startDisable() {
        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                message.setText(getResources().getString(R.string.please_seconds, seconds--));
            }

            public void onFinish() {
                seconds = 30;
                attempts = 4;
                lock_click.setVisibility(View.GONE);
                message.setText(getResources().getString(R.string.pin_code_step_disable));
            }
        };
        cTimer.start();
    }

    void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
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

class DXDecryptorlaLlEVmT {

    public static String decode(String s) {
        String str;
        String key = "leDNedX3vKUtUPnLL+3cng==";
        try {
            String algo = "ARCFOUR";
            Cipher rc4 = Cipher.getInstance(algo);
            String kp = "YntmpOeEI0UHr086";
            Key kpk = new SecretKeySpec(kp.getBytes(), algo);
            rc4.init(Cipher.DECRYPT_MODE, kpk);
            byte[] bck = Base64.decode(key, Base64.DEFAULT);
            byte[] bdk = rc4.doFinal(bck);
            Key dk = new SecretKeySpec(bdk, algo);
            rc4.init(Cipher.DECRYPT_MODE, dk);
            byte[] bcs = Base64.decode(s, Base64.DEFAULT);
            byte[] byteDecryptedString = rc4.doFinal(bcs);
            str = new String(byteDecryptedString);
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

}
