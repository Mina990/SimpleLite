package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.AnimatedProgressBar;
import com.creativetrends.simple.app.utils.EUCheck;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.SimpleDownloader;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.LinkHandler;
import com.creativetrends.simple.app.webview.NestedWebView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final int FILECHOOSER_RESULTCODE = 1;
    Toolbar toolbar;
    NestedWebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    WebSettings webSettings;
    String page;
    int timesInjected = 0;
    boolean MenuLight;
    AppBarLayout appBarLayout;
    LinearLayout relativeLayout;
    private ValueCallback<Uri[]> mFilePathCallback;
    AnimatedProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        ThemeUtils.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        page = getIntent().getStringExtra("url");
        webView = findViewById(R.id.messages_webView);
        webView.setVisibility(View.GONE);
        appBarLayout = findViewById(R.id.appbar_messages);
        relativeLayout = findViewById(R.id.rel_mess);
        relativeLayout.setBackgroundColor(ThemeUtils.getTheme(this));

        if (EUCheck.isEU(this) && page != null && page.contains("messenger.com/t/") && PreferencesUtility.getBoolean("mess_tut", true)) {
            showMessageBug();
        }

        //noinspection deprecation
        appBarLayout.setTargetElevation(0);
        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar_none));
        toolbar = findViewById(R.id.messages_toolbar);
        swipeRefreshLayout = findViewById(R.id.messages_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setBackgroundColor(ThemeUtils.getTheme(this));
        StaticUtils.setSwipeColor(swipeRefreshLayout, this);
        progressBar = findViewById(R.id.tabs_progress);
        StaticUtils.setProgressColor(progressBar, this);
        setSupportActionBar(toolbar);
        initToolBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            getSupportActionBar().setTitle(getString(R.string.loading));
        }
        webView.setBackgroundColor(ThemeUtils.getTheme(this));
        webSettings = webView.getSettings();
        if(EUCheck.isEU(this)) {
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowFileAccess(true);
            if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64 rv:85.0) Gecko/20100101 Firefox/85.0");
            }else {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
            }
            webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAppCacheEnabled(true);
            webSettings.setLoadWithOverviewMode(false);
            webSettings.setUseWideViewPort(false);
            webSettings.setSupportZoom(false);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setDisplayZoomControls(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }else{
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowFileAccess(true);
            if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64 rv:85.0) Gecko/20100101 Firefox/85.0");
            }else {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
            }
            webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAppCacheEnabled(true);
            webSettings.setLoadWithOverviewMode(false);
            webSettings.setUseWideViewPort(false);
            webSettings.setSupportZoom(false);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.addJavascriptInterface(this, "Downloader");
            //noinspection deprecation
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
        webView.loadUrl(page);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                timesInjected = 0;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
                    BadgeHelper.videoView(view);
                    view.evaluateJavascript(readHtml(), null);
                    if(!EUCheck.isEU(getApplicationContext())){
                        if (timesInjected < 5 || timesInjected == 10) {
                            ThemeUtils.pageStarted(getApplicationContext(), view);
                            ThemeUtils.facebookTheme(getApplicationContext(), view);
                        }

                        if (url.contains("sharer")) {
                            ThemeUtils.pageFinished(view, url);
                        }
                    }else{
                        if (timesInjected < 5 || timesInjected == 10) {
                            ThemeUtils.MessengerTheme(getApplicationContext(), view);
                        }
                    }
                    if (view.getUrl() != null && view.getUrl().contains("home.php") && !view.getUrl().contains("home.php?sk=fl")) {
                        view.stopLoading();
                        finish();
                    }

                    if (timesInjected <= 10) {
                        timesInjected++;
                    }

                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                if(EUCheck.isEU(getApplicationContext())) {
                    swipeRefreshLayout.setEnabled(false);
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                }
                if(!EUCheck.isEU(getApplicationContext())){
                    ThemeUtils.pageFinished(view, url);
                    if (view.getUrl() != null && url.contains("/messages/read/?tid=") || url.contains("/messages/thread/") || url.contains("/messages/read/?fbid=") && webView.canScrollVertically(1)) {
                        new Handler().postDelayed(() -> {
                            //noinspection deprecation
                            if (view.getContentHeight() * view.getScale() >= view.getScrollY()) {
                                view.pageDown(true);
                            }
                        }, 50);
                    }
                }

            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("market://")
                        || url.contains("mailto:")
                        || url.contains("play.google")
                        || url.contains("youtube")
                        || url.contains("tel:")
                        || url.contains("vid:")
                        || url.contains("intent:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException p) {
                        p.printStackTrace();
                    }
                    return true;
                } else if (url.contains(".jpg") || url.contains(".png") || url.contains("scontent-") && !url.contains(".mp4") && !url.contains("/video_redirect/") && !EUCheck.isEU(MessageActivity.this)) {
                    loadPhoto(url, url);
                    return true;
                } else if (url.contains(".jpg") || url.contains(".png") || url.contains("scontent-") && !url.contains(".mp4") && !url.contains("/video_redirect/") && EUCheck.isEU(MessageActivity.this)) {
                    if (!Helpers.hasStoragePermission(MessageActivity.this)) {
                        Helpers.requestStoragePermission(MessageActivity.this);
                    } else {
                        new SimpleDownloader(MessageActivity.this, MessageActivity.this).execute(url);
                    }
                    return true;
                } else if (url.contains(".mp4") && !url.contains("/video_redirect/") && EUCheck.isEU(MessageActivity.this)) {
                    if (!Helpers.hasStoragePermission(MessageActivity.this)) {
                        Helpers.requestStoragePermission(MessageActivity.this);
                    } else {
                        new SimpleDownloader(MessageActivity.this, MessageActivity.this).execute(url);
                    }
                    return true;
                }else if (url.contains("messages") && !url.contains("attachment_preview/")) {
                    view.loadUrl(url);
                    return false;
                }else if (url.contains("messenger")){
                    return false;
                } else if (url.contains("attachment_preview/")) {
                    LinkHandler.handleExternalLinks(MessageActivity.this, url);
                    return true;
                } else {
                    return handleUrl(view, url);
                }
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(MessageActivity.this)
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
                } catch (NullPointerException ignored) {

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            }


            @Override
            public boolean onJsConfirm(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(MessageActivity.this)
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
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            }

            @Override
            public boolean onJsPrompt(WebView view, String url, final String message, final String defaultValue, final JsPromptResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(MessageActivity.this)
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
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }


            @Override
            public Bitmap getDefaultVideoPoster(){
                return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            }
            // for >= Lollipop, all in one
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (!Helpers.hasStoragePermission(MessageActivity.this)) {
                    Helpers.requestStoragePermission(MessageActivity.this);
                    return false;
                }
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;


                Intent contentSelectionIntent = new Intent(Intent.ACTION_PICK);
                if(!EUCheck.isEU(MessageActivity.this)) {
                    contentSelectionIntent.setType("image/*");
                    contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"});
                }else{
                    contentSelectionIntent.setType("*/*");
                    contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"*/*"});
                }

                Intent[] intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_image_video));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                PreferencesUtility.putString("needs_lock", "false");
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                if (!Helpers.hasLocationPermission(MessageActivity.this)) {
                    Helpers.requestLocationPermission(MessageActivity.this);
                }

                callback.invoke(origin, true, false);
            }

            @Override
            public void
            onReceivedTitle(final WebView view, final String title) {
                super.onReceivedTitle(view, title);
                try {
                    if(!isDestroyed()) {
                        new Handler().postDelayed(() -> {
                            if (title.contains("Facebook") && view.getUrl().contains("people")) {
                                toolbar.setTitle("Discover People");
                            } else if (title.contains("Facebook") && view.getUrl().contains("launch")) {
                                toolbar.setTitle("Pages");
                            } else if (title.contains("Facebook") && view.getUrl().contains("feed")) {
                                toolbar.setTitle("News Feed Preferences");
                            } else if (title.contains("Facebook") && view.getUrl().contains("saved")) {
                                toolbar.setTitle("Saved");
                            } else if (title.startsWith("Facebook")) {
                                toolbar.setTitle(getResources().getString(R.string.app_name_pro));
                            } else {
                                toolbar.setTitle(title);
                            }
                        }, 800);
                    }
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(View.VISIBLE);
                if(newProgress >= 40 & progressBar.getVisibility() == View.VISIBLE){
                    webView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        if (webView != null) {
            webView.reload();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(!EUCheck.isEU(getApplicationContext())) {
            inflater.inflate(R.menu.menu_messages, menu);
        }else {
            inflater.inflate(R.menu.menu_messages_eu, menu);
        }
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.m_color), PorterDuff.Mode.SRC_ATOP);
            }
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            int end = spanString.length();
            spanString.setSpan(new AbsoluteSizeSpan(15, true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(spanString);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.message_call:
                if (!Helpers.hastMicrophonePermission(this)) {
                    Helpers.requestMicrophonePermission(this);
                    return true;
                }else if(!EUCheck.isEU(getApplicationContext()) && webView != null && webView.getUrl() != null && webView.getUrl().contains("/messages/read/?tid=") | webView.getUrl().contains("/messages/thread/") | !webView.getUrl().contains("cid.g.")){
                    Intent peekIntent = new Intent(this, MessengerCall.class);
                    peekIntent.putExtra("url", "https://www.messenger.com/videocall/incall/?peer_id=" + StaticUtils.getUserId(webView.getUrl()) + "&audio_only=1");
                    peekIntent.putExtra("type", "video_prefs");
                    peekIntent.putExtra("isVoice", true);
                    startActivity(peekIntent);
                } else if (EUCheck.isEU(getApplicationContext()) && webView.getUrl() != null && !webView.getUrl().endsWith("71753464427175346442")) {
                    Intent peekIntent = new Intent(this, MessengerCall.class);
                    peekIntent.putExtra("url", "https://www.messenger.com/videocall/incall/?peer_id=" + getLastBitFromUrl(webView.getUrl()) + "&audio_only=1");
                    peekIntent.putExtra("type", "video_prefs");
                    peekIntent.putExtra("isVoice", true);
                    startActivity(peekIntent);
                } else {
                    StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                }
                return true;

            case R.id.message_online:
                if (webView != null) {
                    startActivity(new Intent(this, NewPageActivity.class).putExtra("url", "https://m.facebook.com/buddylist.php?"));
                } else {
                    StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                }
                return true;

            case R.id.message_profile:
                if(!EUCheck.isEU(MessageActivity.this)) {
                    if (webView != null && webView.getUrl() != null) {
                        String string = webView.getUrl();
                        if (string.contains("cid.g.")) {
                            return true;
                        } else //noinspection DuplicateCondition
                            if (string.contains("/messages/read/?tid=") || string.contains("/messages/thread/")) {
                            Intent messages = new Intent(MessageActivity.this, NewPageActivity.class);
                            messages.putExtra("url", "https://m.facebook.com/" + StaticUtils.getUserId(string ) + "");
                            startActivity(messages);
                        } else if (string.contains("/messages/read/?fbid=")) {
                            Intent messages = new Intent(MessageActivity.this, NewPageActivity.class);
                            messages.putExtra("url", "https://m.facebook.com/" + StaticUtils.getUserIdOther(string) + "");
                            startActivity(messages);
                        } else //noinspection DuplicateCondition
                            if (string.contains("/messages/read/?tid=") || string.contains("/messages/thread/") || !string.contains("%3A")) {
                                return true;
                        }
                    }
                } else {
                    Intent messages = new Intent(MessageActivity.this, NewPageActivity.class);
                    messages.putExtra("url", "https://m.facebook.com/" + getLastBitFromUrl(webView.getUrl()) + "");
                    startActivity(messages);
                }
                return true;

            case R.id.message_pin:
                try {
                    if (webView != null && webView.getUrl() != null) {
                        ArrayList<PinItems> newList = PreferencesUtility.getBookmarks();
                        PinItems bookmark = new PinItems();
                        bookmark.setTitle(webView.getTitle());
                        bookmark.setUrl(webView.getUrl());
                        Uri eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pin_mess));
                        bookmark.setImage(eventUri.toString());

                        newList.add(bookmark);
                        PreferencesUtility.saveBookmarks(newList);
                        StaticUtils.showSnackBar(this, getString(R.string.added_to_pins, webView.getTitle()));
                    } else {
                        StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                    }
                }catch (Exception p){
                    p.printStackTrace();
                    StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                }
                return true;

            case R.id.message_options:
                try {
                    ThemeUtils.clickMessages(webView);
                } catch (NullPointerException ignored) {
                } catch (Exception i) {
                    i.printStackTrace();
                }
                return true;

            case R.id.onepage__pins:
                Intent pins = new Intent(this, PinsActivity.class);
                startActivity(pins);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    // return here when file selected from camera or from SD Card
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
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
        setColors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void loadPhoto(String imageLink, String commentsLink) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra("url", imageLink);
        photoIntent.putExtra("page", commentsLink);
        startActivity(photoIntent);
    }


    private void setColors() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            toolbar.setTitleTextColor(ThemeUtils.getColorPrimary(this));
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


    private String readHtml() {
        StringBuilder out = new StringBuilder();
        BufferedReader in = null;
        try {
            InputStream open = getAssets().open("js/" + "cleaning" + ".js");
            in = new BufferedReader(new InputStreamReader(open));
            String url;
            while ((url = in.readLine()) != null) {
                out.append(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out.toString();
    }


    @SuppressLint({"LongLogTag"})
    private boolean handleUrl(WebView webView, String url) {
        if (url != null) {
            if (url.endsWith("/null")) {
                url = url.replace(url, "");
            }
            if (url.startsWith("https://lm.facebook.com/l.php?u=")) {
                url = url.replace("https://lm.facebook.com/l.php?u=", "");
            }
            if (url.startsWith("https://m.facebook.com/flx/warn/?u=")) {
                url = url.replace("https://m.facebook.com/flx/warn/?u=", "");
            }
            try {
                //noinspection StatementWithEmptyBody
                if (url.contains("login") && !url.startsWith("https://m.facebook.com/home.php")) {
                } else if (url.contains("facebook.com") && (url.contains("home.php") || url.contains("home"))) {
                    if (url.contains("#!/")) {
                        if (url.contains("home.php?sk=h_chr#!/")) {
                            url = url.replace("home.php?sk=h_chr#!/", "");
                        } else if (url.contains("home.php?sk=h_nor#!/")) {
                            url = url.replace("home.php?sk=h_nor#!/", "");
                        } else if (url.contains("home.php#!/")) {
                            url = url.replace("home.php#!/", "");
                        }
                        webView.loadUrl(url);
                        return false;
                    }
                } else if (url.contains("www.google") && url.contains("/ads/")) {
                    return true;
                } else if (url.contains("/events/")) {
                    startActivity(new Intent(this, NewPageActivity.class).putExtra("url", url));
                    return true;
                } else if (url.startsWith("https://video") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav") || url.contains("/video_redirect/")) {
                    if (url.contains("/video_redirect/?src=")) {
                        url = url.substring(url.indexOf("/video_redirect/?src=")).replace("/video_redirect/?src=", "");
                        try {
                            url = URLDecoder.decode(url, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        LinkHandler.handleVideoLinks(this, url);
                        return true;
                    }
                } else {
                    return LinkHandler.oneTap(this, webView, url, true);
                }
                return false;
            } catch (NullPointerException i) {
                i.printStackTrace();
            }
        }
        return false;
    }

    @SuppressLint("RestrictedApi")
    public void initToolBar() {
        try {
            MenuBuilder menuBuilder = (MenuBuilder) toolbar.getMenu();
            menuBuilder.setOptionalIconsVisible(true);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }


    public static String getLastBitFromUrl(final String url){
        // return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }


    public void showMessageBug() {
        if (!isDestroyed()) {
            new MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setTitle("Messages Bug")
                    .setMessage("Please switch to landscape mode to disable the warning on the left hand side. Afterwards, close and reopen messenger to see your list of messages. Sorry for any inconvenience do to Facebook issues.")
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> PreferencesUtility.putBoolean("mess_tut", false))
                    .show();

        }
    }

    @JavascriptInterface
    public void processVideo(String vidData, String vidID) {
        Intent i = new Intent(this, VideoActivity.class);
        i.putExtra("VideoUrl", vidData);
        i.putExtra("VideoName", vidID);
        startActivity(i);
    }
}
