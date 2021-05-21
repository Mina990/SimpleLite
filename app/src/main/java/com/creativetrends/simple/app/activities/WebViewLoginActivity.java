package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PrefManager;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;

/**
 * Created by Creative Trends Apps on 10/16/2016.
 */

public class WebViewLoginActivity extends AppCompatActivity {
    SwipeRefreshLayout swipe_dialog;
    CardView back_color;
    private WebView webView;
    private PrefManager prefManager;
    private ProgressDialog share;
    int themePusher = 0;
    boolean MenuLight;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        showWindow();
        prefManager = new PrefManager(this);
        setTranslucentStatus();
        super.onCreate(savedInstanceState);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        setContentView(R.layout.dialog_login);
        back_color = findViewById(R.id.back_color);
        back_color.setCardBackgroundColor(ThemeUtils.getTheme(this));
        back_color.setVisibility(View.GONE);
        webView = findViewById(R.id.dialog_webview);
        webView.setVisibility(View.GONE);


        swipe_dialog = findViewById(R.id.dialog_swipe);
        StaticUtils.setSwipeColor(swipe_dialog, this);

        Uri url = getIntent().getData();

        webView.getSettings().setJavaScriptEnabled(true);
        //noinspection deprecation
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        if (url != null) {
            webView.loadUrl(url.toString());

        }
        share = new ProgressDialog(WebViewLoginActivity.this);
        setColors();
        share.setMessage("Loading...");
        if(share != null) {
            share.show();
        }


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                swipe_dialog.setRefreshing(false);
                themePusher = 0;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (themePusher < 5) {
                    ThemeUtils.facebookTheme(getApplicationContext(), view);
                }
                if (themePusher == 10) {
                    ThemeUtils.facebookTheme(getApplicationContext(), view);
                }
                if (view.getProgress() > 50 && themePusher < 3 && view.getUrl() != null) {
                    ThemeUtils.facebookTheme(getApplicationContext(), view);
                    themePusher += -10;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipe_dialog.setRefreshing(false);
                if (!isDestroyed()) {
                    if (share != null && share.isShowing()) {
                        share.dismiss();
                        webView.setVisibility(View.VISIBLE);
                        back_color.setVisibility(View.VISIBLE);
                    }
                }
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("%2Fdevice-based%2") || url.contains("/home.php") || url.contains("device-save") || url.contains("device-based") || url.contains("save-device") || url.contains("?login_") || url.contains("/?_rdr") || url.contains("/?refsrc=")) {
                    finish();
                    prefManager.setFirstTimeLaunch(false);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        swipe_dialog.setColorSchemeColors(ThemeUtils.getColorPrimaryDark());
        swipe_dialog.setOnRefreshListener(() -> {
            if (webView != null) {
                webView.reload();
            }
        });


    }

    @Override
    public void onBackPressed() {
        prefManager.setFirstTimeLaunch(true);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
        super.onResume();

    }

    @Override
    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.destroy();
            webView.removeAllViews();
        }
        super.onDetachedFromWindow();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void setTranslucentStatus() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent));
    }

    public void showWindow() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        getWindow().setAttributes(params);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        getWindow().setLayout((int) (width * .9), (int) (height * .9));


    }

    private void setColors(){
        Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_IN);
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            drawable.setColorFilter(ThemeUtils.getColorPrimary(this), PorterDuff.Mode.SRC_IN);
        } else if (!MenuLight) {
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_IN);
        } else {
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_IN);
        }
        share.setIndeterminateDrawable(drawable);

    }

}