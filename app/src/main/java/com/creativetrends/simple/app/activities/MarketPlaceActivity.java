package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.os.PersistableBundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.Cleaner;
import com.creativetrends.simple.app.utils.PreferencesUtility;
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


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 8/24/2018.
 */
public class MarketPlaceActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int FILECHOOSER_RESULTCODE = 1;
    public boolean searchInitialized = false;
    AppBarLayout appBar;
    NestedWebView webView;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    int timesInjected = 0;
    String appDirectoryName;
    WebSettings webSettings;
    CoordinatorLayout color;
    AppCompatTextView load;
    @SuppressLint("StaticFieldLeak")
    FrameLayout searchCard;
    SearchView searchView;
    SearchManager searchManager;
    boolean MenuLight;
    private ValueCallback<Uri[]> mFilePathCallback;
    private int progress = -2;
    String page;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        ThemeUtils.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpage);
        appDirectoryName = getString(R.string.app_name_pro).replace(" ", "");
        appBar = findViewById(R.id.appbar);
        load = findViewById(R.id.loading_fragment);
        toolbar = findViewById(R.id.toolbar);
        initToolBar();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        page = getIntent().getStringExtra("url");
        swipeRefresh = findViewById(R.id.swipeRefresh);
        StaticUtils.setSwipeColor(swipeRefresh, this);
        searchCard = findViewById(R.id.search_layout);
        swipeRefresh.setOnRefreshListener(this);
        color = findViewById(R.id.background_color);
        color.setBackgroundColor(ThemeUtils.getTheme(this));
        webView = findViewById(R.id.webViewPage);
        webView.setBackgroundColor(ThemeUtils.getTheme(this));
        webSettings = webView.getSettings();
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
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
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setTextZoom(Integer.parseInt(PreferencesUtility.getInstance(this).getFont()));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (PreferencesUtility.getBoolean("peek_View", false)) {
            webView.setOnLongClickListener(v -> LinkHandler.handleLongClicks(MarketPlaceActivity.this, webView));
        }

        webView.loadUrl(page);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                try {
                    timesInjected = 0;
                    webView.setVisibility(View.INVISIBLE);
                    load.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(true);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("", "");
                }

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
                    BadgeHelper.videoView(view);
                    if (webView != null) {
                        view.evaluateJavascript(readHtml(), null);
                    }
                    if (timesInjected < 5 || timesInjected == 20) {
                        ThemeUtils.pageStarted(getApplicationContext(), view);
                        ThemeUtils.facebookTheme(getApplicationContext(), view);
                    }


                    if (url.contains("sharer")) {
                        ThemeUtils.pageFinished(view, url);
                    }

                    if (url.contains("/photo/view_full_size/")) {
                        imageLoader(url);
                    }
                    if (view.getUrl() != null && view.getUrl().contains("home.php") && !view.getUrl().contains("home.php?sk=fl")) {
                        swipeRefresh.setRefreshing(false);
                        view.stopLoading();
                        finish();
                    }

                    if (timesInjected <= 20) {
                        timesInjected++;
                    }

                    if (timesInjected == 10) {
                        swipeRefresh.setRefreshing(false);
                    }

                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("", "");
                }
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    view.setVisibility(View.VISIBLE);
                    load.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    ThemeUtils.pageFinished(view, url);
                    ThemeUtils.injectPhotos(view);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("", "");
                }
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                url = Cleaner.cleanAndDecodeUrl(url);
                if (url.contains("view_full_size")) {
                    imageLoader(url);
                    return true;
                } else if (url.startsWith("https://www.facebook.com/")) {
                    view.loadUrl(url.replace("www", "m"));
                    return false;
                } else if (url.contains("/?_rdr")) {
                    view.loadUrl(url.replace("/?_rdr", ""));
                    return false;
                } else if (url.contains("&_rdr")) {
                    view.loadUrl(url.replace("&_rdr", ""));
                    return false;
                } else if (url.contains(".jpg") || url.contains(".png") && !url.contains(".mp4") && !url.contains("/video_redirect/")) {
                    loadPhoto(url, url);
                    return true;
                } else if (url.contains("m.me/")) {
                    Intent me = new Intent(MarketPlaceActivity.this, MessageActivity.class);
                    me.putExtra("url", url.replace("m.me/", "m.facebook.com/messages/thread/"));
                    startActivity(me);
                    //LinkHandler.setMessagesFragment(MarketPlaceActivity.this, url.replace("m.me/", "m.facebook.com/messages/thread/"));
                    Log.e("NewPageActivity", "starting message activity");
                    return true;
                } else if (url.contains("/read/?tid=") || url.contains("/thread/") || url.contains("/messages/thread/")) {
                    //LinkHandler.setMessagesFragment(MarketPlaceActivity.this, url);
                    Intent me = new Intent(MarketPlaceActivity.this, MessageActivity.class);
                    me.putExtra("url", url);
                    startActivity(me);
                    Log.e("NewPageActivity", "starting message activity");
                    return true;
                } else if(url.contains("/marketplace/item/")){
                    Intent me = new Intent(MarketPlaceActivity.this, NewPageActivity.class);
                    me.putExtra("url", url);
                    startActivity(me);
                    return true;
                } else if(url.contains("/photo.php?") || url.contains("/photos/") && !url.contains("?photoset")){
                    Intent me = new Intent(MarketPlaceActivity.this, PhotoPage.class);
                    me.putExtra("url", url);
                    startActivity(me);
                    return true;
                } else {
                    return handleUrl(LinkHandler.cleanUpUrl(url));
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(MarketPlaceActivity.this)
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
                        new MaterialAlertDialogBuilder(MarketPlaceActivity.this)
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
                        new MaterialAlertDialogBuilder(MarketPlaceActivity.this)
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
            // for >= Lollipop, all in one
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (!Helpers.hasStoragePermission(MarketPlaceActivity.this)) {
                    Helpers.requestStoragePermission(MarketPlaceActivity.this);
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
                            view.setVisibility(View.VISIBLE);
                            load.setVisibility(View.GONE);
                        }, 800);
                    }
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (view != null) {
                    progress = view.copyBackForwardList().getCurrentIndex();
                }
                super.onProgressChanged(view, newProgress);
            }


        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_page, menu);
        menu.findItem(R.id.page_search).setVisible(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.page_search:
                //openSearch();
                return true;

            case R.id.page_share:
                if (webView.getUrl() != null) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_action)));
                }
                return true;

            case R.id.page_copy:
                addCopy();
                return true;

            case R.id.page_open:
                if (webView.getUrl() != null) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(webView.getUrl()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
                return true;

            case R.id.page_pin:
                addPin();
                return true;

            case R.id.onepage__pins:
                Intent pins = new Intent(this, PinsActivity.class);
                startActivity(pins);
                return true;

            case R.id.page_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("needs_lock", "false");
        setColors();
    }

    @Override
    protected void onResume() {
        if (webView != null) {
            webView.onResume();
        }
        super.onResume();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void onBackPressed() {
        if (searchCard.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else if (webView != null && webView.canGoBack()) {
            webView.goBack();
            swipeRefresh.setRefreshing(true);
            swipeRefresh.setEnabled(true);
            new Handler().postDelayed(() -> swipeRefresh.setRefreshing(false), 500);
        } else {
            super.onBackPressed();
            PreferencesUtility.putString("needs_lock", "false");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    public void loadPhoto(String imageLink, String commentsLink) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra("url", imageLink);
        photoIntent.putExtra("page", commentsLink);
        startActivity(photoIntent);
    }

    private void imageLoader(String url) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra("url", url);
        photoIntent.putExtra("title", webView.getTitle());
        startActivity(photoIntent);
        webView.stopLoading();
    }

    @Override
    public void onRefresh() {
        webView.reload();
    }

    @Override
    public void onClick(View v) {

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


    public void addPin() {
        ArrayList<PinItems> newList = PreferencesUtility.getBookmarks();
        PinItems bookmark = new PinItems();
        bookmark.setTitle(webView.getTitle());
        bookmark.setUrl(webView.getUrl());
        Uri eventUri;
        if (webView.getUrl() != null && webView.getUrl().contains("/messages/")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pin_mess));
        } else if (webView.getUrl() != null && webView.getUrl().contains("/groups/")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_group));
        } else if (webView.getUrl() != null && webView.getUrl().contains("/photos/a.") || webView.getUrl().contains("photos/pcb.") || (webView.getUrl().contains("/photo.php?") || webView.getUrl().contains("/photos/")) && !webView.getUrl().contains("?photoset")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pics));
        } else if (webView.getUrl() != null && webView.getUrl().contains("/marketplace")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_market));
        } else if (webView.getUrl() != null && webView.getUrl().contains("/events/")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_cal));
        } else if (webView.getTitle() != null && webView.getTitle().contains("- Home")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_page));
        } else if (webView.getUrl() != null && webView.getUrl().contains("/home.php?sk=fl_")) {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_news_set));
        } else {
            eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                    + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pin_page));
        }
        bookmark.setImage(eventUri.toString());
        newList.add(bookmark);
        PreferencesUtility.saveBookmarks(newList);
        StaticUtils.showSnackBar(this, getString(R.string.added_to_pins,  webView.getTitle()));
    }


    public void addCopy() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = null;
        try {
            clip = ClipData.newUri(getContentResolver(), "URI", Uri.parse(URLDecoder.decode(webView.getUrl(), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (clipboard != null) {
            if (clip != null) {
                clipboard.setPrimaryClip(clip);
            }
        }
        StaticUtils.showSnackBar(this, getString(R.string.content_copy_link_done));
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

    private void initializeSearch() {

        //searchView = findViewById(R.id.search_view);
        //searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                closeSearch();
                webView.stopLoading();
                if(query.contains("#")){
                    new Handler().postDelayed(() -> loadSearch(query.replace("#", "%23")), 300);
                }else{
                    new Handler().postDelayed(() -> loadSearch(query), 300);
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        findViewById(R.id.search_back).setOnClickListener(v -> closeSearch());
        searchInitialized = true;
    }


    public void openSearch() {
        if (!searchInitialized) {
            initializeSearch();
        }
        searchCard.setClickable(false);
        searchCard.setFocusable(false);
        searchCard.setFocusableInTouchMode(false);
        searchView.setIconified(false);
        searchCard.setVisibility(View.VISIBLE);

    }


    public void loadSearch(String str) {
        if (searchCard.getVisibility() == View.VISIBLE) {
            closeSearch();
        }
        Intent link = new Intent(this, NewPageActivity.class);
        if (((AppCompatCheckBox) findViewById(R.id.filter_people_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/people/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/pages/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_events_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/events/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/groups/?q=" + str);
        } else {
            link.putExtra("url", "https://m.facebook.com/search/top/?q=" + str);
        }
        startActivity(link);
    }

    public void closeSearch() {
        searchCard.setVisibility(View.GONE);
        searchView.setQuery("", false);
        searchView.setOnCloseListener(() -> false);

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
    private boolean handleUrl(String url) {
        if (url.endsWith("/null")) {
            url = url.replace(url, "");
        }
        if (url.startsWith("https://lm.facebook.com/l.php?u=")) {
            url = url.replace("https://lm.facebook.com/l.php?u=", "");
        }
        if (url.startsWith("https://m.facebook.com/flx/warn/?u=")) {
            url = url.replace("https://m.facebook.com/flx/warn/?u=", "");
        }
        if (progress == 0 || progress >= 0) {
            try {
                if (Uri.parse(url).getHost() != null) {
                    if (url.startsWith("https://video") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav") || url.contains("/video_redirect/")) {
                        if (url.contains("/video_redirect/?src=")) {
                            url = url.substring(url.indexOf("/video_redirect/?src=")).replace("/video_redirect/?src=", "");
                            try {
                                url = URLDecoder.decode(url, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            Intent i = new Intent(this, VideoActivity.class);
                            i.putExtra("VideoUrl", url);
                            startActivity(i);
                            PreferencesUtility.putString("needs_lock", "false");
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
                            return true;
                        } else if (url.contains("view_overview") || url.contains("view_photo")) {
                            startActivity(new Intent(this, Sharer.class).setData(Uri.parse(url)));
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
                            return true;
                        } else if (url.contains("/photos/pcb.") || url.contains("/photos/ms.c")) {
                            if (webView != null) {
                                webView.loadUrl(url);
                            }
                            return true;
                        } else if(url.contains("/marketplace/item/")){
                            Intent me = new Intent(MarketPlaceActivity.this, NewPageActivity.class);
                            me.putExtra("url", url);
                            startActivity(me);
                            return true;
                        } else if (url.contains("/read/?tid=") || url.contains("/thread/") || url.contains("/messages/thread/")) {
                            startActivity(new Intent(MarketPlaceActivity.this, MessageActivity.class).putExtra("url", url));
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
                            Log.e("NewPageAcivity", "starting message activity");
                            return true;
                        } else if (url.contains("/photos?lst")) {
                            if (webView != null) {
                                webView.loadUrl(url);
                            }
                            return true;
                        } else if (url.startsWith("simple:")) {
                            return true;
                        } else {
                            if (url.contains("/photo.php?") || (url.contains("/photos/a.") && !(url.contains("photoset") && url.contains("/photos/viewer/")))) {
                                if (webView != null) {
                                    webView.loadUrl(url);
                                }
                                return true;
                            } else if (url.contains("/photos/viewer/")) {
                                if (webView != null) {
                                    webView.loadUrl(url);
                                }
                                return false;
                            } else {
                                startActivity(new Intent(this, NewPageActivity.class).putExtra("url", url));
                                return true;
                            }
                        }
                    } else {
                        LinkHandler.handleExternalLinks(this, url);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return true;
            }

            return true;
        }
        if (webView != null) {
            webView.loadUrl(url);
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


}