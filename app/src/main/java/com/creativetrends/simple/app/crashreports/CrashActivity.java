package com.creativetrends.simple.app.crashreports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.ThemeUtils;


public final class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ThemeUtils.getTheme(this);
            setContentView(R.layout.activity_error);
            Button restartButton = findViewById(R.id.customactivityoncrash_error_activity_restart_button);

            final Class<? extends Activity> restartActivityClass = CrashInfo.getRestartActivityClassFromIntent(getIntent());
            final CrashInfo.EventListener eventListener = CrashInfo.getEventListenerFromIntent(getIntent());

            if (restartActivityClass != null) {
                restartButton.setText(R.string.error_activity_restart_app);
                restartButton.setOnClickListener(v -> {
                    Intent intent = new Intent(CrashActivity.this, restartActivityClass);
                    CrashInfo.restartApplicationWithIntent(CrashActivity.this, intent, eventListener);
                });
            } else {
                restartButton.setOnClickListener(v -> CrashInfo.closeApplication(CrashActivity.this, eventListener));
            }

            Button moreInfoButton = findViewById(R.id.customactivityoncrash_error_activity_more_info_button);

            if (CrashInfo.isShowErrorDetailsFromIntent(getIntent())) {
                moreInfoButton.setOnClickListener(v -> {
                    AlertDialog dialog = new AlertDialog.Builder(CrashActivity.this)
                            .setTitle(R.string.error_activity_error_details_title)
                            .setMessage(CrashInfo.getAllErrorDetailsFromIntent(getIntent()))
                            .setPositiveButton(R.string.error_activity_error_details_send, (dialog1, which) -> copyErrorToClipboard())
                            .setNegativeButton(R.string.error_activity_error_details_close, null)
                            .show();
                    TextView textView = dialog.findViewById(android.R.id.message);
                    if (textView != null) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size));
                    }
                });
            } else {
                moreInfoButton.setVisibility(View.GONE);
            }

            int defaultErrorActivityDrawableId = CrashInfo.getDefaultErrorActivityDrawableIdFromIntent(getIntent());
            ImageView errorImageView = findViewById(R.id.customactivityoncrash_error_activity_image);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId, getTheme()));
            } else {
                //noinspection deprecation
                errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId));
            }
        } catch (Exception ignored) {

        }
    }

    private void copyErrorToClipboard() {
        String errorInformation = CrashInfo.getAllErrorDetailsFromIntent(getIntent());
        Intent bugIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "simpleappbugs@creativetrendsapps.com", null));
        bugIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name_pro) + " Crash");
        bugIntent.putExtra(Intent.EXTRA_TEXT, "Awe, snap!" + " " + getString(R.string.app_name_pro) + " " + "Crashed!" + "\n\n" + errorInformation);
        startActivity(Intent.createChooser(bugIntent, null));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

}