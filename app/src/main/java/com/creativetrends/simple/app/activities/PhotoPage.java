package com.creativetrends.simple.app.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.adapters.SearchAdapter;
import com.creativetrends.simple.app.adapters.SearchItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.interfaces.DownloadInterface;
import com.creativetrends.simple.app.interfaces.HtmlInterface;
import com.creativetrends.simple.app.interfaces.ImageInterface;
import com.creativetrends.simple.app.interfaces.OnHtmlCallback;
import com.creativetrends.simple.app.interfaces.OnPhotoPageCheck;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.EUCheck;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.LinkHandler;
import com.creativetrends.simple.app.webview.NestedWebView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 8/24/2018.
 */
public class PhotoPage extends AppCompatActivity implements OnHtmlCallback, OnPhotoPageCheck, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, SearchAdapter.onSearchSelected {
    private static final int FILECHOOSER_RESULTCODE = 1;
    public boolean searchInitialized = false;
    AppBarLayout appBar;
    NestedWebView webView;
    Toolbar toolbar;
    FloatingActionButton fullFAB, downFAB;
    RelativeLayout buttons;
    SwipeRefreshLayout swipeRefresh;
    int timesInjected = 0;
    String appDirectoryName;
    String page;
    WebSettings webSettings;
    CoordinatorLayout color;
    AppCompatTextView load;
    @SuppressLint("StaticFieldLeak")
    FrameLayout searchCard;
    CardView cardView;
    SearchView searchView;
    FragmentTransaction ft;
    LinearLayout mFilters;
    RelativeLayout mMore;
    TextView mMoreTitle;
    RecyclerView search_recycler;
    ArrayList<SearchItems> arrayList = new ArrayList<>();
    SearchAdapter adapter;
    boolean MenuLight;
    private int progress = -2;
    private ValueCallback<Uri[]> mFilePathCallback;
    private int scrollPosition = 0;
    //downloads
    DownloadManager downloadManager;
    private String filename;
    private File simple;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder build;
    static long time = new Date().getTime();
    static String tmpStr = String.valueOf(time);
    static String last4Str = tmpStr.substring(tmpStr.length() -1);
    static int notificationId = Integer.parseInt(last4Str);



    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(this, getString(R.string.notification_widget_channel));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpage);
        appDirectoryName = getString(R.string.app_name_pro).replace(" ", "");
        searchView = findViewById(R.id.simple_search_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initToolBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_new);
            getSupportActionBar().setTitle(getString(R.string.loading));
        }
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        appBar = findViewById(R.id.appbar);
        load = findViewById(R.id.loading_fragment);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        StaticUtils.setSwipeColor(swipeRefresh, this);
        swipeRefresh.setOnRefreshListener(this);
        color = findViewById(R.id.background_color);
        color.setBackgroundColor(ThemeUtils.getTheme(this));
        buttons = findViewById(R.id.imageExtrasFABHolder);
        fullFAB = findViewById(R.id.fullImageFAB);
        fullFAB.setOnClickListener(this);
        downFAB = findViewById(R.id.downloadFAB);
        downFAB.setOnClickListener(this);
        toolbar.setOnClickListener(v -> {
            if (webView != null && scrollPosition > 10) {
                scrollToTop(webView);
            }
        });
        try {
            arrayList = PreferencesUtility.getSearch();
            search_recycler = findViewById(R.id.search_recycler);
            search_recycler.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SearchAdapter(this, arrayList, this);
            search_recycler.setAdapter(adapter);
            search_recycler.setVisibility(View.GONE);
        } catch (Exception ignored) {

        }

        searchView = findViewById(R.id.simple_search_view);
        cardView = findViewById(R.id.search_card);
        searchCard = findViewById(R.id.search_layout);
        page = getIntent().getStringExtra("url");
        try {
            webView = findViewById(R.id.webViewPage);
            webView.setVisibility(View.GONE);
        } catch (Exception p) {
            p.printStackTrace();
            if (p.getMessage() != null && p.getMessage().toLowerCase().contains("webview") | p.getMessage().contains("webkit") | p.getMessage().contains("Chrome")) {
                Toast.makeText(this, p.toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Please install or enable the system webview.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
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
        webView.addJavascriptInterface(new HtmlInterface(this), "HTML");
        webView.addJavascriptInterface(new DownloadInterface(this), "Html");
        webView.addJavascriptInterface(new ImageInterface(this), "Photos");

        webView.addJavascriptInterface(this, "Downloader");
        webView.setLongClickable(true);
        if (PreferencesUtility.getBoolean("peek_View", false)) {
            webView.setOnLongClickListener(v -> LinkHandler.handleLongClicks(PhotoPage.this, webView));
        }
        webView.setOnScrollChangedCallback((l, t) -> scrollPosition = t);
        webView.loadUrl(page);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                try {
                    timesInjected = 0;
                    webView.setVisibility(View.GONE);
                    load.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(true);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
                    BadgeHelper.videoView(view);
                    if (timesInjected < 5 || timesInjected == 10) {
                        ThemeUtils.pageStarted(getApplicationContext(), view);
                        ThemeUtils.facebookTheme(getApplicationContext(), view);
                        ThemeUtils.injectTextMessages(view);
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


                    if (webView != null && webView.getUrl() != null) {
                        OnPhotoPage((webView.getUrl().contains("facebook.com/photo.php?") || webView.getUrl().contains("/photos/")) && !webView.getUrl().contains("?photoset"));
                    }

                    if (timesInjected <= 10) {
                        timesInjected++;
                    }

                    if (timesInjected == 10) {
                        swipeRefresh.setRefreshing(false);
                    }

                    if(url.contains("&changedcover=") || url.contains("?success=")) {
                        PreferencesUtility.putString("changed_picture", "true");
                    }
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
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
                    ThemeUtils.injectPhotos(webView);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(".jpg") || url.contains(".png") && !url.contains(".mp4") && !url.contains("/video_redirect/")) {
                    loadPhoto(url, null);
                    return true;
                } else if (url.startsWith("https://www.facebook.com/") || url.startsWith("http://www.facebook.com/")) {
                    url = url.replace("www.facebook.com", "m.facebook.com");
                    if (view != null) {
                        view.loadUrl(url);
                    }
                    return false;
                } else if (url.contains("m.me/")) {
                    if (!NetworkConnection.isConnected(PhotoPage.this)) {
                        StaticUtils.showSnackBar(PhotoPage.this, "No network connection");
                    } else {
                        if(!EUCheck.isEU(PhotoPage.this)) {
                            Intent mes = new Intent(PhotoPage.this, MessageActivity.class);
                            mes.putExtra("url", url.replace("m.me/", "m.facebook.com/messages/thread/"));
                            startActivity(mes);
                        }else{
                            Intent mes = new Intent(PhotoPage.this, MessageActivity.class);
                            mes.putExtra("url", "https://messenger.com/t/" + getLastBitFromUrl(url));
                            startActivity(mes);
                        }
                    }
                    return true;
                } else if (url.contains("/read/?tid=") || url.contains("/thread/") || url.contains("/messages/thread/")) {
                    if (!NetworkConnection.isConnected(PhotoPage.this)) {
                        StaticUtils.showSnackBar(PhotoPage.this, "No network connection");
                    } else {
                        if(!EUCheck.isEU(PhotoPage.this)) {
                            Intent mes = new Intent(PhotoPage.this, MessageActivity.class);
                            mes.putExtra("url", url);
                            startActivity(mes);
                        }else{
                            Intent mes = new Intent(PhotoPage.this, MessageActivity.class);
                            mes.putExtra("url", "https://messenger.com/t/"+StaticUtils.getUserId(url));
                            startActivity(mes);
                        }
                    }
                    return true;
                } else if (url.contains("?_rdr")) {
                    view.loadUrl(url);
                    return false;
                } else if(url.contains("/photo.php?") || url.contains("/photos/") && !url.contains("?photoset")){
                    view.loadUrl(url);
                    return false;
                } else if (url.contains("l.facebook.com") || (url.contains("lm.facebook.com") || url.contains("source=facebook.com&") || !url.contains("facebook.com"))) {
                    url = LinkHandler.cleanUpUrl(url);
                    return handleUrl(view, url);
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
                        new MaterialAlertDialogBuilder(PhotoPage.this)
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
                        new MaterialAlertDialogBuilder(PhotoPage.this)
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
                        new MaterialAlertDialogBuilder(PhotoPage.this)
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
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (!Helpers.hasStoragePermission(PhotoPage.this)) {
                    Helpers.requestStoragePermission(PhotoPage.this);
                    return false;
                }
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;


                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video_prefs/*"});

                Intent[] intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_image_video));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                if (!Helpers.hasLocationPermission(PhotoPage.this)) {
                    Helpers.requestLocationPermission(PhotoPage.this);
                }

                callback.invoke(origin, true, false);
            }

            @Override
            public void
            onReceivedTitle(final WebView view, final String title) {
                super.onReceivedTitle(view, title);
                try {
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
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (view != null) {
                    progress = view.copyBackForwardList().getCurrentIndex();
                }
                if(newProgress >= 48 & !swipeRefresh.isRefreshing()){
                   webView.setVisibility(View.VISIBLE);
                    load.setVisibility(View.GONE);
                }
            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_page, menu);
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

        } catch (Exception i) {
            i.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.page_search:
                openSearch();
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
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
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
        try {
            if (searchCard.getVisibility() == View.VISIBLE) {
                closeSearch();
                searchView.setQuery(null, false);
            } else if (webView != null && webView.canGoBack()) {
                webView.stopLoading();
                webView.goBack();
            } else {
                super.onBackPressed();
                PreferencesUtility.putString("needs_lock", "false");
            }

        } catch (NullPointerException ignored) {
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PreferencesUtility.putString("needs_lock", "false");

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(onComplete);
        super.onDestroy();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void OnHtmlReceived(String html) {
        if (html != null) {
            String commentsUrl = null;
            String imageUrl = null;
            if (html.contains("(urlEnd) ")) {
                commentsUrl = html.substring(0, html.indexOf("(urlEnd) "));
            }
            if (html.contains("url(")) {
                imageUrl = StaticUtils.processImageUrlFromStyleString(html.substring(html.indexOf("url(")));
            } else if (html.startsWith("https") && !html.contains(" ")) {
                imageUrl = html;
            }
            if (imageUrl != null) {
                loadPhoto(imageUrl, commentsUrl);
            }
        }
    }

    @Override
    public void OnPhotoHtml(String str) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fullImageFAB:
                getFull();
                break;
            case R.id.downloadFAB:
                getDownload();
                break;
            case R.id.search_more_title:
                Intent search = new Intent(PhotoPage.this, PhotoPage.class);
                search.putExtra("url", "https://m.facebook.com/search/top/?q=" + searchView.getQuery().toString());
                startActivity(search);
                new Handler().postDelayed(this::closeSearch, 800);
                break;
            case R.id.search_layout:
            case R.id.search_back:
                closeSearch();
                break;

            case R.id.filter_people_check:
                ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                break;

            case R.id.filter_pages_check:
                ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                break;

            case R.id.filter_events_check:
                ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                break;

            case R.id.filter_groups_check:
                ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                break;


            default:
                break;
        }

    }

    public void OnPhotoPage(boolean isPhotoPage) {
        if (isPhotoPage) {
            buttons.setVisibility(View.VISIBLE);
        } else {
            buttons.setVisibility(View.GONE);
        }
    }

    @Override
    public void OnPhotoUrl(String newUrl) {
        if (newUrl != null) {
            String imageUrl = null;
            if (newUrl.contains("url(")) {
                imageUrl = StaticUtils.processImageUrlFromStyleString(newUrl.substring(newUrl.indexOf("url(")));
            } else if (newUrl.startsWith("https") && !newUrl.contains(" ")) {
                imageUrl = newUrl;
            }
            startDownload(imageUrl);
        }
    }


    @JavascriptInterface
    public void processVideo(String vidData, String vidID) {
        Intent i = new Intent(this, VideoActivity.class);
        i.putExtra("VideoUrl", vidData);
        i.putExtra("VideoName", vidID);
        startActivity(i);
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
        startActivity(photoIntent);
        webView.stopLoading();
    }

    @Override
    public void onRefresh() {
        scrollPosition = 0;
        webView.reload();
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

        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            //fullFAB.setRippleColor(ContextCompat.getColor(this, R.color.black));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            //downFAB.setRippleColor(ContextCompat.getColor(this, R.color.black));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimaryDark()));
            //fullFAB.setRippleColor(ThemeUtils.getColorPrimaryDark());
            downFAB.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimaryDark()));
            //downFAB.setRippleColor(ThemeUtils.getColorPrimaryDark());
        } else if (!MenuLight) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            //fullFAB.setRippleColor(ThemeUtils.getColorPrimaryDark());
            downFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            //downFAB.setRippleColor(ThemeUtils.getColorPrimaryDark());
        } else {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            //fullFAB.setRippleColor(StaticUtils.darkColorTheme(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            //downFAB.setRippleColor(StaticUtils.darkColorTheme(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
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
        try {
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
        } catch (final NullPointerException ignored) {
        } catch (Exception i) {
            i.printStackTrace();
            StaticUtils.showSnackBar(this, i.toString());
        }
    }


    public void addCopy() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Page URL", webView.getUrl());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            StaticUtils.showSnackBar(this, getString(R.string.content_copy_link_done));
        } catch (Exception i) {
            i.printStackTrace();
            StaticUtils.showSnackBar(this, i.toString());
        }
    }

    private void getFull() {
        webView.loadUrl("javascript: var img = document.querySelector(\"a[href*='.jpg']\");if (img != null){window.HTML.handleHtml(img.getAttribute(\"href\"));} else {img = document.querySelector(\"i.img[data-sigil*='photo-image']\");if (img != null) {window.HTML.handleHtml(img.getAttribute(\"style\"));}}");
    }


    public void getDownload() {
        webView.loadUrl("javascript: var pic = document.querySelector(\"a[href*='.jpg']\");if (pic != null){window.Html.getPhotoUrl(pic.getAttribute(\"href\"));} else { pic = document.querySelector(\"i.img[data-sigil*='photo-image']\");if (pic != null) {window.Html.getPhotoUrl(pic.getAttribute(\"style\"));}}");

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
        mMore = findViewById(R.id.search_more);
        mFilters = findViewById(R.id.filter_layout);
        mMoreTitle = findViewById(R.id.search_more_title);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<SearchItems> searchItems = PreferencesUtility.getSearch();
                SearchItems name_search = new SearchItems();
                name_search.setName(query.toLowerCase());
                searchItems.add(name_search);
                PreferencesUtility.saveSearch(searchItems);
                closeSearch();
                if(query.contains("#")){
                    new Handler().postDelayed(() -> loadSearch(query.replace("#", "%23")), 300);
                }else{
                    new Handler().postDelayed(() -> loadSearch(query), 300);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    search_recycler.setVisibility(View.VISIBLE);
                    //mFilters.setVisibility(View.VISIBLE);
                    mMore.setVisibility(View.VISIBLE);
                    mMoreTitle.setText(getResources().getString(R.string.see_more_results, newText));
                    newText = newText.toLowerCase();
                    ArrayList<SearchItems> newList = new ArrayList<>();
                    for (SearchItems searchNames : arrayList) {
                        String name = searchNames.getName().toLowerCase();
                        if (name.contains(newText))
                            newList.add(searchNames);
                    }
                    adapter.setFilter(newList);
                } else {
                    search_recycler.setVisibility(View.GONE);
                    //mFilters.setVisibility(View.GONE);
                    mMore.setVisibility(View.GONE);
                }
                return true;
            }
        });
        findViewById(R.id.search_layout).setOnClickListener(this);
        findViewById(R.id.search_layout).setClickable(true);
        findViewById(R.id.search_back).setOnClickListener(this);
        findViewById(R.id.filter_people_check).setOnClickListener(this);
        findViewById(R.id.filter_pages_check).setOnClickListener(this);
        findViewById(R.id.filter_events_check).setOnClickListener(this);
        findViewById(R.id.filter_groups_check).setOnClickListener(this);
        findViewById(R.id.search_more_title).setOnClickListener(this);
        searchInitialized = true;
    }


    public void openSearch() {
        if (!searchInitialized) {
            initializeSearch();
        }
        searchCard.setVisibility(View.VISIBLE);
        searchCard.setClickable(false);
        searchCard.setFocusable(false);
        searchCard.setFocusableInTouchMode(false);
        searchView.setIconified(false);
        cardView.setClickable(true);
        ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(true);
        ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
        ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
        ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
        showMenu();
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }


    public void closeSearch() {
        cardView.setClickable(false);
        mMore.setVisibility(View.GONE);
        mFilters.setVisibility(View.GONE);
        searchCard.setVisibility(View.GONE);
        searchView.setOnCloseListener(() -> false);
        searchView.setQuery("", false);
        hideMenu();
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }


    private void showMenu() {
        searchCard.setVisibility(View.VISIBLE);
        LayoutAnimationController lac2 = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.anim.first_row_animation), 0.3f);
        cardView.setLayoutAnimation(lac2);
        searchCard.setLayoutAnimation(lac2);
        cardView.post(() ->
                searchCard.post(() -> {
                }));

    }

    private void hideMenu() {
        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_menu);
        fade.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                searchCard.setVisibility(View.GONE);
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        searchCard.startAnimation(fade);
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


    @SuppressWarnings("StatementWithEmptyBody")
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
                if (url.contains("login") && !url.startsWith("https://m.facebook.com/home.php")) {
                } else if (url.contains("facebook.com") && (url.contains("home.php") || url.contains("home"))) {
                    if (url.contains("#!/")) {
                        if (url.contains("home.php?sk=h_chr#!/")) {
                            url = url.replace("home.php?sk=h_chr#!/", "");
                        } else if (url.contains("home.php?sk=h_nor#!/")) {
                            url = url.replace("home.php?sk=h_nor#!/", "");
                        } else if (url.contains("home.php#!/")) {
                            url = url.replace("home.php#!/", "");
                        } else if (url.contains("&_rdc=2&_rdr")) {
                            url = url.replace("&_rdc=2&_rdr", "");
                        } else if (url.contains("_rdc=1&_rdr")) {
                            url = url.replace("_rdc=1&_rdr", "");
                        }
                        if (webView != null)
                            webView.loadUrl(url);
                        return false;
                    }
                } else if (url.contains("www.google") && url.contains("/ads/")) {
                    return true;
                } else if (url.contains("&anchor_reactions=") || url.contains("&focus_composer=")) {
                    Intent me = new Intent(PhotoPage.this, PhotoPage.class);
                    me.putExtra("url", url);
                    startActivity(me);
                    return true;
                } else if (url.contains("view_full_size")) {
                    imageLoader(url);
                    return true;
                } else if (url.contains("?_rdr")) {
                    if (webView != null)
                        webView.loadUrl(url);
                    return false;
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

    @Override
    public void loadSearch(String str) {
        if (searchCard.getVisibility() == View.VISIBLE) {
            closeSearch();
        }
        Intent link = new Intent(this, PhotoPage.class);
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

    public static void scrollToTop(WebView webView) {
        ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
        anim.setDuration(500);
        anim.start();
    }




    private void startDownload(String url) {
        String appDirectoryName = "Simple";
        try {
            filename = getFileName(url);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + appDirectoryName;
            simple = new File(path);
            if(!simple.exists()){
                //noinspection ResultOfMethodCallIgnored
                simple.mkdir();
            }
            File newFile = new File(simple + File.separator, filename);
            if (newFile.isFile()) {
                Toast.makeText(getApplicationContext(), filename + " already downloaded", Toast.LENGTH_SHORT).show();
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setDestinationUri(Uri.parse("file://" + path + File.separator + filename));
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.allowScanningByMediaScanner();
                downloadManager.enqueue(request);
            }
        } catch (Exception i) {
            i.printStackTrace();
            Toast.makeText(PhotoPage.this, i.toString(), Toast.LENGTH_SHORT).show();

        }
    }


    public static String getFileName(String url) {
        int index = url.indexOf("?");
        if (index > -1) {
            url = url.substring(0, index);
        }
        url = url.toLowerCase();

        index = url.lastIndexOf("/");
        if (index > -1) {
            return url.substring(index + 1);
        } else {
            return Long.toString(System.currentTimeMillis());
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showNotification();
        }
    };


    private void showNotification(){
        try {
            String imgExtension = ".jpg";
            if (filename.contains(".gif"))
                imgExtension = ".gif";
            else if (filename.contains(".png"))
                imgExtension = ".png";

            File newFile = new File(simple + File.separator, filename);
            Uri files = FileProvider.getUriForFile(this, getResources().getString(R.string.auth), newFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(files, getMimeType(files));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            build.setContentIntent(resultPendingIntent)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setContentText(getString(R.string.tap_to_view))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_s))
                    .setColor(ThemeUtils.getColorPrimary(this));
            if (filename != null && filename.contains(imgExtension)) {
                Glide.with(this).asBitmap().load(Uri.fromFile(newFile)).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        build.setSmallIcon(R.drawable.ic_image_download);
                        build.setLargeIcon(Circle(resource));
                        build.setContentTitle(getString(R.string.image_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                        build.setContentText(filename);
                        build.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(resource)
                                .bigLargeIcon(null));
                        build.setColor(ContextCompat.getColor(PhotoPage.this, R.color.jorell_blue));
                        Notification notification = build.build();
                        mNotifyManager.notify(notificationId++, notification);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
                StaticUtils.showSnackBar(PhotoPage.this, getResources().getString(R.string.image_downloaded));
            } else {
                build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                build.setContentTitle(getString(R.string.file_downloaded));
                Notification notification = build.build();
                mNotifyManager.notify(notificationId++, notification);
                StaticUtils.showSnackBar(PhotoPage.this, getResources().getString(R.string.file_downloaded));
            }
            MediaScannerConnection.scanFile(this, new String[]{simple + File.separator + filename}, null, (newpath, newuri) -> Log.i("Saved and scanned to", newpath));
        } catch (Exception p) {
            p.printStackTrace();

        }
    }

    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static String getLastBitFromUrl(final String url){
        // return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }


    @SuppressWarnings({"SuspiciousNameCombination", "IntegerDivisionInFloatingPointContext"})
    public static Bitmap Circle(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);


        return output;
    }
}
