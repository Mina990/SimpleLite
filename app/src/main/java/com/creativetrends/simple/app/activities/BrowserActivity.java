package com.creativetrends.simple.app.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.AnimatedProgressBar;
import com.creativetrends.simple.app.ui.BrowserSheet;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.SimpleDownloader;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.NestedWebView;
import com.creativetrends.simple.app.webview.SimpleChromeClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.os.Build.VERSION_CODES.M;


//Created by Jorell on 3/15/2016.


public class BrowserActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE = 1;
    private static final int ID_CONTEXT_MENU_SAVE_IMAGE = 2562617;
    private static final int ID_CONTEXT_MENU_SHARE_IMAGE = 2562618;
    private static final int ID_CONTEXT_MENU_COPY_IMAGE = 2562619;
    public static Bitmap favoriteIcon;
    FrameLayout customViewContainer;
    TextView toolbarTitle;
    Uri url;
    private String mPendingImageUrlToSave;
    private Toolbar toolbar;
    private NestedWebView webView;
    AppBarLayout appBar;
    AnimatedProgressBar progressBar;
    boolean isDark = PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime();
    BufferedReader bufferedReader;
    boolean MenuLight;
    AppCompatImageButton b, f, s, m, c, p;
    LinearLayout bottom;

    @SuppressLint("StaticFieldLeak")
    public static Activity mainActivity;
    public static Activity getBrowser() {
        return mainActivity;
    }


    public static void scrollToTop(WebView webView) {
        ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
        anim.setDuration(250);
        anim.start();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        super.onCreate(savedInstanceState);
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        setContentView(R.layout.activity_browser);


        bottom = findViewById(R.id.lin_browser);


        bottom.setBackgroundColor(ThemeUtils.getColorPrimary(this));




        //bottom
        b = findViewById(R.id.b_back);

        b.setOnClickListener(view -> {
            if(webView != null && webView.canGoBack()){
                webView.goBack();
            }else{
                finish();
            }
        });

        f = findViewById(R.id.b_forward);

        f.setOnClickListener(view -> {
            if(webView != null && webView.canGoForward()){
                webView.goForward();
            }
        });
        s = findViewById(R.id.b_share);
        s.setOnClickListener(view -> {
            if (webView != null && webView.getUrl() != null) {
                try {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_action)));
                } catch (NullPointerException ignored) {
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        m = findViewById(R.id.b_menu);
        m.setOnClickListener(view -> {
            if (!isDestroyed()) {
                BrowserSheet b =  BrowserSheet.newInstance();
                b.show(getSupportFragmentManager(), "");
            }
        });

        //close and pin
        c = findViewById(R.id.close_browser);
        p = findViewById(R.id.pin_browser);


        c.setOnClickListener(view -> finish());

        p.setOnClickListener(view -> {
            try {
                if (webView != null && webView.getUrl() != null) {
                    Uri link = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_links));
                    ArrayList<PinItems> newList = PreferencesUtility.getBookmarks();
                    PinItems bookmark = new PinItems();
                    bookmark.setTitle(webView.getTitle());
                    bookmark.setUrl(webView.getUrl());
                    bookmark.setImage(link.toString());
                    newList.add(bookmark);
                    PreferencesUtility.saveBookmarks(newList);
                    Toast.makeText(BrowserActivity.this, String.format(getString(R.string.added_to_pins), webView.getTitle()), Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException ignored) {
            } catch (Exception e) {
                e.printStackTrace();

            }
        });



        if (StaticUtils.isLollipop() && PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        } else {
            getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
        }
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (PreferencesUtility.getBoolean("color_nav", false) && StaticUtils.isLollipop()) {
            getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
        } else {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        }
        customViewContainer = findViewById(R.id.fullscreen_custom_content);
        toolbar = findViewById(R.id.toolbar);
        appBar = findViewById(R.id.appbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        new Handler().postDelayed(this::showMenuText, 3000);
        new Handler().postDelayed(this::hideMenuText, 3500);
        setSupportActionBar(toolbar);
        initToolBar();
        toolbar.setBackgroundColor(setToolbarColor(this));


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setTitle("");
        }

        url = getIntent().getData();
        progressBar = findViewById(R.id.browser_progress);
        webView = findViewById(R.id.simple_webview);


        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
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
        webView.getSettings().setGeolocationEnabled(false);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, false);



        toolbarTitle.setOnClickListener(v -> {
            if (webView != null && webView.getScrollY() > 10) {
                scrollToTop(webView);
            } else if (webView != null && webView.getScrollY() == 0) {
                webView.reload();
            }
        });


        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> bottom.setTranslationY(Math.round(-verticalOffset)));

        webView.loadUrl(url.toString());

        final Set<String> adServersSet = new HashSet<>();

        try {
            if(!url.toString().contains("9gag") || !url.toString().contains("cnn") || !url.toString().startsWith("ad")) {
                bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("adblock.txt")));
            }else{
                bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("adblocknone.txt")));
            }

            String adServer;

            while ((adServer = bufferedReader.readLine()) != null) {
                adServersSet.add(adServer);
            }

            bufferedReader.close();
        } catch (IOException ioException) {
            // `IOException.
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("market:")
                        || url.contains("https://m.youtube.com")
                        || url.contains("yout.be")
                        || url.contains("https://play.google.com")
                        || url.contains("mailto:")
                        || url.contains("intent:")
                        || url.contains("https://mail.google.com")
                        || url.contains("geo:")
                        || url.contains("google.com/maps")
                        || url.contains("google.streetview:")
                        || url.contains("mobile.twitter")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    }catch (Exception p){
                        p.printStackTrace();
                        view.loadUrl(url);
                        return false;
                    }
                } else if ((url.contains("http://") || url.contains("https://"))) {
                    return false;
                }
                return false;
            }


            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                webView.copyBackForwardList();
                if(webView != null && webView.canGoForward()){
                    f.setAlpha(.9f);
                }else{
                    f.setAlpha(0.4f);
                }
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Uri requestUri = Uri.parse(url);
                String requestHost = requestUri.getHost();
                boolean requestHostIsAdServer = false;
                if (requestHost != null) {
                    while (requestHost.contains(".") && !requestHostIsAdServer) {
                        if (adServersSet.contains(requestHost)) {
                            requestHostIsAdServer = true;
                        }
                        requestHost = requestHost.substring(requestHost.indexOf(".") + 1);
                    }
                }

                if (requestHostIsAdServer) {
                    return new WebResourceResponse("text/plain", "utf8", new ByteArrayInputStream("".getBytes()));
                } else {
                    return null;
                }
            }

        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (Build.VERSION.SDK_INT >= M) {
                if (ActivityCompat.checkSelfPermission(BrowserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BrowserActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                } else {
                    new SimpleDownloader(this, this).execute(url);
                }

            }
        });


        webView.setWebChromeClient(new SimpleChromeClient(this) {

            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(BrowserActivity.this)
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
                        new MaterialAlertDialogBuilder(BrowserActivity.this)
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
                        new MaterialAlertDialogBuilder(BrowserActivity.this)
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
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                try {
                    if (icon != null) {
                        setColor(Palette.from(icon).generate().getVibrantColor(Palette.from(icon).generate().getMutedColor(ThemeUtils.getColorPrimary(BrowserActivity.this))));
                    } else {
                        setColor(ContextCompat.getColor(getApplicationContext(), ThemeUtils.getColorPrimary(BrowserActivity.this)));

                    }
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        setColors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            // Inflate the menu; this adds items to the action bar if it is present.
            for (int i = 1; i < menu.size(); i++) {
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(Color.parseColor("#959595"), PorterDuff.Mode.SRC_IN);
                }
            }
            for (int i = 0; i < menu.size(); i++) {
                MenuItem items = menu.getItem(i);
                SpannableString spanStringg = new SpannableString(menu.getItem(i).getTitle().toString());
                int ended = spanStringg.length();
                spanStringg.setSpan(new AbsoluteSizeSpan(15, true), 0, ended, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                items.setTitle(spanStringg);
            }


        } catch (Exception i) {
            i.printStackTrace();
        }
        return true;
    }



    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    @Override
    public void onBackPressed() {
        try {
            if (webView.canGoBack()) {
                webView.stopLoading();
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        } catch (Exception ignored) {

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            registerForContextMenu(webView);
        }
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
        super.onDestroy();
        PreferencesUtility.putString("needs_lock", "false");
    }

    private void requestStoragePermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasStoragePermission()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE);
        } else {

            if (mPendingImageUrlToSave != null)
                new SimpleDownloader(this, this).execute(mPendingImageUrlToSave);
        }
    }


    private boolean hasStoragePermission() {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ContextCompat.checkSelfPermission(this, storagePermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPendingImageUrlToSave != null)
                    new SimpleDownloader(this, this).execute(mPendingImageUrlToSave);
            } else {
                StaticUtils.showSnackBar(this, getResources().getString(R.string.permission_denied));

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult result = webView.getHitTestResult();
        if (result != null) {
            int type = result.getType();

            if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                showLongPressedImageMenu(menu, result.getExtra());
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_CONTEXT_MENU_SAVE_IMAGE:
                requestStoragePermission();
                break;
            case ID_CONTEXT_MENU_SHARE_IMAGE:
                try {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, mPendingImageUrlToSave);
                    startActivity(Intent.createChooser(share, getString(R.string.context_share_image)));
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ID_CONTEXT_MENU_COPY_IMAGE:
                try {
                    ClipboardManager clipboard = (ClipboardManager) BrowserActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newUri(this.getContentResolver(), "URI", Uri.parse(mPendingImageUrlToSave));
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    StaticUtils.showSnackBar(this, getString(R.string.content_copy_link_done));
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return super.onContextItemSelected(item);
    }


    private void showLongPressedImageMenu(ContextMenu menu, String imageUrl) {
        mPendingImageUrlToSave = imageUrl;
        menu.setHeaderTitle(webView.getTitle());
        menu.add(0, ID_CONTEXT_MENU_SAVE_IMAGE, 0, getString(R.string.save_img));
        menu.add(0, ID_CONTEXT_MENU_SHARE_IMAGE, 1, getString(R.string.context_share_image));
        menu.add(0, ID_CONTEXT_MENU_COPY_IMAGE, 2, getString(R.string.context_copy_image_link));
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }



    public void showMenuText() {
        Animation fade = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                toolbarTitle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        toolbarTitle.startAnimation(fade);
    }


    public void hideMenuText() {
        try {
            Animation fade = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            toolbarTitle.startAnimation(fade);
            toolbarTitle.setText(StaticUtils.getUrlDomainName(webView.getUrl()));
            if (webView.getUrl() != null && webView.getUrl().startsWith("https")) {
                toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_secure_white, 0, 0, 0);
            } else {
                toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        } catch (Exception ignored) {

        }
    }

    private void setColor(int color) {
        color = isDark ? ContextCompat.getColor(this, R.color.dark) : color;
        {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), StaticUtils.darkColor(color));
            colorAnimation.setDuration(800);
            colorAnimation.addUpdateListener(animator -> {
                getWindow().setStatusBarColor((int) animator.getAnimatedValue());
                if (PreferencesUtility.getBoolean("color_nav", false)) {
                    getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
                } else {
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark));

                }

            });
            colorAnimation.start();
        }
        int colorFrom = ContextCompat.getColor(this, !isDark ? R.color.dark : R.color.jorell_blue);
        Drawable backgroundFrom = toolbar.getBackground();
        if (backgroundFrom instanceof ColorDrawable)
            colorFrom = ((ColorDrawable) backgroundFrom).getColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(800);
        colorAnimation.addUpdateListener(animator -> {
            toolbar.setBackgroundColor((int) animator.getAnimatedValue());
            progressBar.setBackgroundColor((int) animator.getAnimatedValue());
            bottom.setBackgroundColor((int) animator.getAnimatedValue());

            if (PreferencesUtility.getBoolean("color_nav", false)) {
                getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
                bottom.setBackgroundColor((int) animator.getAnimatedValue());

            }

        });


        colorAnimation.start();

    }
    private int setToolbarColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                case "darktheme":
                    return StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark());
                default:
                    return ThemeUtils.getColorPrimaryDark();
            }

        }
    }


    private void setColors() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            bottom.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
            toolbar.setBackgroundColor(ThemeUtils.getColorPrimaryDark());
            bottom.setBackgroundColor(ThemeUtils.getColorPrimaryDark());
        } else if (!MenuLight) {
            getWindow().setStatusBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            toolbar.setBackgroundColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            bottom.setBackgroundColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));

        }

        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (PreferencesUtility.getBoolean("color_nav", false) && StaticUtils.isLollipop()) {
            getWindow().setNavigationBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            }

        }

    }


    public void isRefresh(){
        try {
            webView.stopLoading();
            webView.reload();
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void isCopy(){
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Page URL", StaticUtils.sanitizeUrl(webView.getUrl()));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            StaticUtils.showSnackBar(this, getString(R.string.content_copy_link_done));
        } catch (NullPointerException ignored) {
        } catch (Exception i) {
            i.printStackTrace();
            StaticUtils.showSnackBar(this, i.toString());

        }
    }

    public void isOpen(){
        if (webView != null && webView.getUrl() != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webView.getUrl()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            finish();
        }
    }


    public void isPins(){
        Intent pin = new Intent(this, PinsActivity.class);
        startActivity(pin);
    }

}