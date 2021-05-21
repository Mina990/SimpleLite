package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.interfaces.DownloadInterface;
import com.creativetrends.simple.app.interfaces.HtmlInterface;
import com.creativetrends.simple.app.interfaces.OnHtmlCallback;
import com.creativetrends.simple.app.interfaces.OnPhotoPageCheck;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.Cleaner;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.NestedWebView;
import com.creativetrends.simple.app.webview.SimpleChromeClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Creative Trends Apps on 11/28/2016.
 */

public class PopupView extends AppCompatActivity implements OnHtmlCallback, OnPhotoPageCheck {
    private static final int FILECHOOSER_RESULTCODE = 1;
    static boolean isConnectedMobile;
    public SwipeRefreshLayout swipeRefreshLayout;
    SharedPreferences preferences;
    String appDirectoryName;
    int themePusher = 0;
    DownloadManager mgr = null;
    boolean simplewhite;
    RelativeLayout buttons;
    FloatingActionButton fullFAB, downFAB;
    String url;
    private ValueCallback<Uri[]> mFilePathCallback;
    private NestedWebView webView;
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
    boolean MenuLight;
    CardView back_color;
    @SuppressWarnings({"deprecation", "typo"})
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(this, getString(R.string.notification_widget_channel));
        super.onCreate(savedInstanceState);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        isConnectedMobile = NetworkConnection.isConnectedMobile(SimpleApplication.getContextOfApplication());
        simplewhite = PreferencesUtility.getInstance(this).getTheme().equals("simplewhite");
        setContentView(R.layout.activity_peekview);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        appDirectoryName = getString(R.string.app_name_pro);
        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        buttons = findViewById(R.id.imageExtrasFABHolder);
        fullFAB = findViewById(R.id.fullImageFAB);
        downFAB = findViewById(R.id.downloadFAB);
        fullFAB.setOnClickListener(v -> onFullImage());
        downFAB.setOnClickListener(v -> {
            if (!Helpers.hasStoragePermission(PopupView.this)) {
                Helpers.requestStoragePermission(PopupView.this);
            } else {
                getDownload();
            }

        });


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        back_color = findViewById(R.id.back_color);
        back_color.setCardBackgroundColor(ThemeUtils.getTheme(this));
        webView = findViewById(R.id.peek_webview);
        webView.setBackgroundColor(ThemeUtils.getTheme(this));


        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        swipeRefreshLayout = findViewById(R.id.swipe_float);
        StaticUtils.setSwipeColor(swipeRefreshLayout, this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            if (!NetworkConnection.isConnected(getApplicationContext()))
                swipeRefreshLayout.setRefreshing(false);
            else {
                new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2500);
            }
        });


        url = getIntent().getStringExtra("url");
        webView = findViewById(R.id.peek_webview);
        assert webView != null;
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        if (preferences.getBoolean("allow_location", false)) {
            webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());
        } else {
            webView.getSettings().setGeolocationEnabled(false);
        }
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64 rv:85.0) Gecko/20100101 Firefox/85.0");
        }else {
            webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
        }
        webView.addJavascriptInterface(new HtmlInterface(this), "HTML");
        webView.addJavascriptInterface(new DownloadInterface(this), "Html");
        webView.addJavascriptInterface(PopupView.this, "Downloader");
        webView.setLongClickable(true);
        try {
            webView.getSettings().setTextZoom(Integer.parseInt(PreferencesUtility.getInstance(this).getFont()));
        } catch (Exception ignored) {

        }

        webView.setWebViewClient(new WebViewClient() {
            @SuppressLint("NewApi")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                try {
                    url = Cleaner.cleanAndDecodeUrl(url);
                    if (url.contains(".jpg") || url.contains(".png") && !url.contains(".mp4") && !url.contains("/video_redirect/")) {
                        loadPhoto(url, null);
                        return true;
                    }

                    if (url.startsWith("https://video") || url.contains(".mp4") || url.endsWith(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav")) {
                        if (url.contains("https://m.facebook.com/video_redirect/?src=")) {
                            url = url.replace("https://m.facebook.com/video_redirect/?src=", "");
                            Intent i = new Intent(PopupView.this, VideoActivity.class);
                            i.putExtra("VideoUrl", url);
                            i.putExtra("VideoName", view.getTitle());
                            startActivity(i);
                            PreferencesUtility.putString("needs_lock", "false");
                            return true;
                        }

                    }
                    if (url.contains("market://") || url.contains("mailto:")
                            || url.contains("play.google") || url.contains("youtube")
                            || url.contains("tel:")
                            || url.contains("vid:")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    if (url.startsWith("https://m.facebook.com") || url.contains("http://m.facebook.com")
                            || url.startsWith("akamaihd.net")
                            || url.startsWith("ad.doubleclick.net")
                            || url.startsWith("sync.liverail.com")
                            || url.startsWith("cdn.fbsbx.com")
                            || url.startsWith("lookaside.fbsbx.com")
                            || url.startsWith("https://mobile.facebook.com")
                            || url.startsWith("http://h.facebook.com")
                            || url.startsWith("https://free.facebook.com")
                            || url.startsWith("https://0.facebook.com")) {
                        return false;
                    }

                    if (url.startsWith("https://www.facebook.com/") || url.startsWith("http://www.facebook.com/")) {
                        url = url.replace("www.facebook.com", "m.facebook.com");
                        webView.loadUrl(url);
                        return true;
                    }

                    if (preferences.getBoolean("allow_inside", false)) {
                        Intent intent = new Intent(PopupView.this, BrowserPopup.class);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        PreferencesUtility.putString("needs_lock", "false");
                        return true;
                    }

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                    } catch (NullPointerException ignored) {
                    } catch (ActivityNotFoundException e) {
                        Log.e("shouldOverrideUrlLoad", "" + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                } catch (NullPointerException ignored) {
                } catch (ActivityNotFoundException e) {
                    Log.e("shouldOverrideUrlLoad", "" + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    themePusher = 0;
                    swipeRefreshLayout.setRefreshing(true);
                    ThemeUtils.pageFinished(view, url);
                } catch (NullPointerException i) {
                    i.printStackTrace();
                } catch (Exception ignored) {

                }

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                try {
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

                    if (url.contains("sharer")) {
                        ThemeUtils.pageFinished(view, url);
                    }

                    BadgeHelper.videoView(view);
                    if (view.getUrl() != null) {
                        OnPhotoPage((view.getUrl().contains("facebook.com/photo.php?") || view.getUrl().contains("/photos/") || view.getUrl().contains("&photo=")) && !view.getUrl().contains("?photoset"));
                    }
                    if (url.contains("photo/view_full_size/")) {
                        loadPhotoImage(url, view.getTitle());
                    }

                    if (themePusher <= 10) {
                        themePusher++;
                    }
                    if (view.getUrl() != null && view.getUrl().contains("home.php") && !view.getUrl().contains("sk=")) {
                        view.stopLoading();
                        finish();
                    }


                    if (themePusher == 10) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    if(url.contains("&changedcover=") || url.contains("?success=")) {
                        PreferencesUtility.putString("changed_picture", "true");
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
                    swipeRefreshLayout.setRefreshing(false);
                    ThemeUtils.pageFinished(view, url);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("", "");
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                if (view != null) {
                    ThemeUtils.pageStarted(getApplicationContext(), view);
                    ThemeUtils.facebookTheme(SimpleApplication.getContextOfApplication(), view);
                    ThemeUtils.hideClose(view);
                }
                super.onPageCommitVisible(view, url);
            }
        });
        webView.loadUrl(url);

        webView.setWebChromeClient(new SimpleChromeClient(this) {
            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(PopupView.this)
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
                        new MaterialAlertDialogBuilder(PopupView.this)
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
                        new MaterialAlertDialogBuilder(PopupView.this)
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

            // for >= Lollipop, all in one
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (!Helpers.hasStoragePermission(PopupView.this)) {
                    Helpers.requestStoragePermission(PopupView.this);
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
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);

            }

            @Override
            public void onReceivedTitle(final WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    ThemeUtils.pageStarted(getApplicationContext(), view);
                    ThemeUtils.facebookTheme(SimpleApplication.getContextOfApplication(), view);
                    ThemeUtils.hideClose(view);
                }
                super.onProgressChanged(view, newProgress);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quick_menu, menu);

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;


            case R.id.quick_share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "");
                i.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                startActivity(Intent.createChooser(i, getString(R.string.share_action)));
                return true;


            default:
                return super.onOptionsItemSelected(item);


        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            swipeRefreshLayout.setRefreshing(true);
            swipeRefreshLayout.setEnabled(true);
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 500);
        } else {
            super.onBackPressed();
            PreferencesUtility.putString("needs_lock", "false");
        }
    }

    @Override
    protected void onResume() {
        if (webView != null) {
            webView.onResume();
            registerForContextMenu(webView);
        }
        PreferencesUtility.putString("needs_lock", "false");
        super.onResume();
        setColors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            unregisterForContextMenu(webView);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(onComplete);
        super.onDestroy();
        PreferencesUtility.putString("needs_lock", "false");
    }




    public void onFullImage() {
        webView.loadUrl("javascript: var img = document.querySelector(\"a[href*='.jpg']\");if (img != null){window.HTML.handleHtml(img.getAttribute(\"href\"));} else {img = document.querySelector(\"i.img[data-sigil*='photo-image']\");if (img != null) {window.HTML.handleHtml(img.getAttribute(\"style\"));}}");
    }

    public void getDownload() {
        webView.loadUrl("javascript: var pic = document.querySelector(\"a[href*='.jpg']\");if (pic != null){window.Html.getPhotoUrl(pic.getAttribute(\"href\"));} else { pic = document.querySelector(\"i.img[data-sigil*='photo-image']\");if (pic != null) {window.Html.getPhotoUrl(pic.getAttribute(\"style\"));}}");

    }


    @Override
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

    public void loadPhoto(String imageLink, String commentsLink) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra("url", imageLink);
        photoIntent.putExtra("page", commentsLink);
        startActivity(photoIntent);
    }

    public void loadPhotoImage(String imageLink, String title) {
        Intent photoIntent = new Intent(this, PhotoActivity.class);
        photoIntent.putExtra("url", imageLink);
        photoIntent.putExtra("title", title);
        photoIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(photoIntent);
        webView.stopLoading();
    }

    @JavascriptInterface
    public void processVideo(String vidData, String vidID) {
        Intent i = new Intent(this, VideoActivity.class);
        i.putExtra("VideoUrl", vidData);
        i.putExtra("VideoName", vidID);
        startActivity(i);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
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
            Toast.makeText(PopupView.this, i.toString(), Toast.LENGTH_SHORT).show();

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
            if(!isDestroyed()) {
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
                            build.setColor(ContextCompat.getColor(PopupView.this, R.color.jorell_blue));
                            Notification notification = build.build();
                            mNotifyManager.notify(notificationId++, notification);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                    StaticUtils.showSnackBar(PopupView.this, getResources().getString(R.string.image_downloaded));
                } else {
                    build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    build.setContentTitle(getString(R.string.file_downloaded));
                    Notification notification = build.build();
                    mNotifyManager.notify(notificationId++, notification);
                    StaticUtils.showSnackBar(PopupView.this, getResources().getString(R.string.file_downloaded));
                }
                MediaScannerConnection.scanFile(this, new String[]{simple + File.separator + filename}, null, (newpath, newuri) -> Log.i("Saved and scanned to", newpath));
            }
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



    private void setColors() {
        if (PreferencesUtility.getBoolean("color_status", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));

            }
        } else {
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
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimaryDark()));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimaryDark()));
        } else if (!MenuLight) {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
        } else {
            fullFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
            downFAB.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
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
                case "amoledtheme":
                    return ContextCompat.getColor(context, R.color.black);
                default:
                    return ContextCompat.getColor(context, R.color.white);
            }

        }
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