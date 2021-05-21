package com.creativetrends.simple.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// Created by Jorell on 6/18/2016.
public class Sharer extends AppCompatActivity {
    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final int REQUEST_LOCATION = 2;
    public Context context;
    public SwipeRefreshLayout swipeRefreshLayout;
    int timesInjected = 0;
    Uri galleryImageUri;
    ProgressDialog share;
    String sharer_string;
    private ValueCallback<Uri[]> mFilePathCallback;
    private WebView webView;
    CardView cardView;
    boolean MenuLight;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showWindow();
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        setContentView(R.layout.activity_switch);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        swipeRefreshLayout = findViewById(R.id.switch_swipe);
        StaticUtils.setSwipeColor(swipeRefreshLayout, this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            if (!NetworkConnection.isConnected(SimpleApplication.getContextOfApplication()))
                swipeRefreshLayout.setRefreshing(false);
            else {
                new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2500);
            }
        });

        sharer_string = getIntent().getStringExtra("url");

        webView = findViewById(R.id.switchWebView);
        cardView = findViewById(R.id.back_color);
        cardView.setCardBackgroundColor(ThemeUtils.getTheme(this));
        webView.setVisibility(View.GONE);
        cardView.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //noinspection deprecation
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAppCacheEnabled(true);
        webView.callOnClick();
        webView.loadUrl(getIntent().getStringExtra("url"));

        share = new ProgressDialog(Sharer.this);
        setColors();
        share.setMessage("Loading...");
        share.setOnCancelListener(dialog -> {
            if (webView != null) {
                webView.loadUrl("about:blank");
                webView.stopLoading();
                webView.clearHistory();
                webView.clearCache(true);
                webView.destroy();
                webView.removeAllViews();
                webView = null;
                galleryImageUri = null;
            }
            finish();
        });
        share.show();



        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                swipeRefreshLayout.setRefreshing(true);
                timesInjected = 0;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
                    if (view.getProgress() > 50 && timesInjected < 3) {
                        if (view.getUrl() == null) {
                            return;
                        }
                        if ((view.getUrl().startsWith("https://www.facebook.com/") ||
                                view.getUrl().startsWith("https://web.facebook.com/")) &&
                                !view.getUrl().contains("sharer.php")) {


                            if (timesInjected == 10) {
                                if (!isDestroyed()) {
                                    share.dismiss();
                                    webView.setVisibility(View.VISIBLE);
                                    cardView.setVisibility(View.VISIBLE);
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                    WindowManager.LayoutParams params = getWindow().getAttributes();
                                    params.alpha = 1.0f;
                                    params.dimAmount = 0.5f;
                                    getWindow().setAttributes(params);
                                    if (galleryImageUri != null) {
                                        view.loadUrl("javascript:(function(){document.getElementsByClassName('_3-99')[0].click()})()");
                                    }
                                }
                            }
                        }

                    }
                    if (url.contains("_mupload_/composer/?target=")) {
                        view.loadUrl("javascript:try{document.querySelector('#feed_jewel > a').click();}catch(e){window.location.href='" + "https://m.facebook.com" + "home.php';}");
                    }
                    if (url.contains("home.php") && !url.contains("soft=composer")) {

                        finish();

                    }

                    if (timesInjected<= 10) {
                        timesInjected++;
                    }
                }catch (Exception ignored){

                }
            }



            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    swipeRefreshLayout.setRefreshing(false);
                    if (galleryImageUri != null) {
                        view.loadUrl("javascript:(function(){document.getElementsByClassName('_3-99')[0].click()})()");
                    }
                    if (url.contains("?pageload")) {
                        if (url.contains("photo")) {
                            view.loadUrl("javascript:document.querySelector('[name*=\"view_photo\"]').click();");
                            view.loadUrl("javascript:(function(){document.getElementsByClassName('_3-99')[0].click()})()");
                        } else if (url.contains("checkin")) {
                            if (!Helpers.hasLocationPermission(Sharer.this)) {
                                Helpers.requestLocationPermission(Sharer.this);
                            } else {
                                PreferencesUtility.putBoolean("allow_location", true);
                                view.getSettings().setGeolocationEnabled(true);
                                view.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());
                            }
                            view.loadUrl("javascript:document.querySelector('button#u_2r_13.50o7.touchable._21db').click();");
                            view.loadUrl("javascript:(function(){document.getElementsByClassName('_3-99')[2].click()})()");
                            view.loadUrl("javascript:document.querySelector('[name*=\"view_location\"]').click();");
                        } else if (url.contains("composer")) {
                            view.loadUrl("javascript:document.querySelector('._4g34._6ber._5i2i._52we').click();");
                            view.loadUrl("javascript:document.querySelector('[name*=\"view_overview\"]').click();");
                        }
                    } else {
                        view.loadUrl("javascript:document.querySelector('._4g34._6ber._5i2i._52we').click();");
                        view.loadUrl("javascript:document.querySelector('[name*=\"view_overview\"]').click();");
                    }
                    if (!url.contains("home") || url.contains("composer") || url.contains("sharer.php")) {
                        new Handler().postDelayed(() -> {
                            try {
                                if (!isDestroyed()) {
                                    if (share.isShowing()) {
                                        share.dismiss();
                                        webView.setVisibility(View.VISIBLE);
                                        cardView.setVisibility(View.VISIBLE);
                                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                        WindowManager.LayoutParams params = getWindow().getAttributes();
                                        params.alpha = 1.0f;
                                        params.dimAmount = 0.5f;
                                        getWindow().setAttributes(params);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, 600);
                    }
                    //noinspection StatementWithEmptyBody
                    if (url.contains("sharer.php")) {

                    } else if (url.contains("view_photo")) {
                        view.loadUrl("javascript:document.querySelector('button._50o7.touchable._21db').click();");
                        view.loadUrl("javascript:document.querySelector('[name*=\"view_photo\"]').click();");
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("dialog/close_window/")) {
                    finish();
                }

                if (url.contains("bookmarks")) {
                    finish();
                }

                if (url.contains("home.php") && !url.contains("soft=composer")) {
                    finish();
                }
                return false;
            }

        });


        webView.setWebChromeClient(new WebChromeClient() {


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    ThemeUtils.pageStarted(getApplicationContext(), view);
                    ThemeUtils.facebookTheme(SimpleApplication.getContextOfApplication(), view);
                    ThemeUtils.hideClose(view);
                }
            }

            @Override
            public void
            onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title != null && title.contains("https://www.facebook.com/dialog/return")) {
                    finish();
                }

            }

            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                if (!isDestroyed()) {
                    new MaterialAlertDialogBuilder(Sharer.this)
                            .setTitle(R.string.app_name_pro)
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok, (dialog, id) -> {
                                result.confirm();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                result.cancel();
                                dialog.dismiss();
                            })
                            .show();
                }
                return true;

            }


            @Override
            public boolean onJsConfirm(WebView view, final String url, final String message, final JsResult result) {
                if (!isDestroyed()) {
                    new MaterialAlertDialogBuilder(Sharer.this)
                            .setTitle(R.string.app_name_pro)
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok, (dialog, id) -> {
                                result.confirm();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                result.cancel();
                                dialog.dismiss();
                            })
                            .show();
                }
                return true;

            }

            @Override
            public boolean onJsPrompt(WebView view, String url, final String message, final String defaultValue, final JsPromptResult result) {
                if (!isDestroyed()) {
                    new MaterialAlertDialogBuilder(Sharer.this)
                            .setTitle(R.string.app_name_pro)
                            .setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok, (dialog, id) -> {
                                result.confirm();
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, id) -> {
                                result.cancel();
                                dialog.dismiss();
                            })
                            .show();
                }
                return true;
            }


            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }


            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                window.destroy();
                window.removeAllViews();
                finish();
            }


            // for Lollipop, all in one
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,  WebChromeClient.FileChooserParams fileChooserParams) {
                if (!hasStoragePermission()) {
                    requestStoragePermission();
                    return false;
                }

                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;


                Intent contentSelectionIntent = new Intent(Intent.ACTION_PICK);
                contentSelectionIntent.setType("image/* video/*");
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});

                Intent[] intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_image_video));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                PreferencesUtility.putString("needs_lock", "false");
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                return true;
            }


        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            String dataString = data.getDataString();
            if (dataString != null) {
                results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }



    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("needs_lock", "false");
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!isDestroyed()) {
            if(share.isShowing()) {
                share.dismiss();
                share.cancel();
            }else{
                finish();
            }
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
    protected void onResume() {
        super.onResume();
        webView.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(galleryImageUri!= null) {
            galleryImageUri = null;
        }
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

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private boolean hasStoragePermission() {
        String locationPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ContextCompat.checkSelfPermission(this, locationPermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                webView.reload();
            } else {
                StaticUtils.showSnackBar(this, getString(R.string.permission_denied));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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