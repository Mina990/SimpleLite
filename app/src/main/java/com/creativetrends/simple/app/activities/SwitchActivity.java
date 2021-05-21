package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;


/**
 * Created by Creative Trends Apps on 9/14/2016.
 */

public class SwitchActivity extends AppCompatActivity {
    int themePusher = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog popupProgress;
    private WebView webView;
    CardView cardView;
    boolean MenuLight;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        showWindow();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        super.onCreate(savedInstanceState);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        setContentView(R.layout.activity_switch);

        webView = findViewById(R.id.switchWebView);
        cardView = findViewById(R.id.back_color);
        cardView.setCardBackgroundColor(ThemeUtils.getTheme(this));
        webView.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);

        swipeRefreshLayout = findViewById(R.id.switch_swipe);
        StaticUtils.setSwipeColor(swipeRefreshLayout, this);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //noinspection deprecation
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAppCacheEnabled(true);
        webView.callOnClick();

        webView.loadUrl("https://m.facebook.com/login");

        popupProgress = new ProgressDialog(SwitchActivity.this);
        setColors();
        popupProgress.setMessage("Loading...");
        popupProgress.show();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    themePusher = 0;
                    swipeRefreshLayout.setRefreshing(true);
                } catch (Exception e) {
                    Log.e("onPageStarted", "" + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
                    if (themePusher < 5 || themePusher == 10) {
                        ThemeUtils.pageStarted(getApplicationContext(), view);
                        ThemeUtils.facebookTheme(getApplicationContext(), view);
                    }

                    if (themePusher == 10) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if (themePusher <= 10) {
                        themePusher++;
                    }

                } catch (Exception ignored) {

                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    swipeRefreshLayout.setRefreshing(false);
                    if (!isDestroyed()) {
                        if (popupProgress.isShowing()) {
                            popupProgress.dismiss();
                            webView.setVisibility(View.VISIBLE);
                            cardView.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                            WindowManager.LayoutParams params = getWindow().getAttributes();
                            params.alpha = 1.0f;
                            params.dimAmount = 0.5f;
                            getWindow().setAttributes(params);
                        }
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("%2Fdevice-based%2") || url.contains("/home.php") || url.contains("device-save") || url.contains("device-based") || url.contains("save-device") || url.contains("?login_") || url.contains("/?_rdr") || url.contains("/?refsrc=")) {
                    finish();
                    ((MainActivity) MainActivity.getMainActivity()).reloadAll();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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


        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .9));
        } else {
            getWindow().setLayout((int) (width * .9), (int) (height * .9));
        }


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
        popupProgress.setIndeterminateDrawable(drawable);

    }

}