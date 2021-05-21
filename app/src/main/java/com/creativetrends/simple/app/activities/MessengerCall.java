package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MessengerCall extends AppCompatActivity {
    WebView webView;
    WebSettings webSettings;
    ProgressBar progressBar;
    RelativeLayout background;
    AppCompatTextView call_load;
    String type;
    private int timesInjected;
    boolean MenuLight;

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        type = getIntent().getStringExtra("type");
        webView = findViewById(R.id.call_webView);
        background = findViewById(R.id.call_background);
        call_load = findViewById(R.id.call_loading);
        progressBar = findViewById(R.id.call_progress);
        if (getIntent().getBooleanExtra("isVoice", true)) {
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            webView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            call_load.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else {
            background.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            webView.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            call_load.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        webSettings = webView.getSettings();
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
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //noinspection deprecation
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        try {
            webView.loadUrl(getIntent().getStringExtra("url"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                call_load.setVisibility(View.VISIBLE);
                webView.setVisibility(View.INVISIBLE);
                timesInjected = 0;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (!isDestroyed()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MessengerCall.this);
                    String message = "SSL Certificate error.";
                    switch (error.getPrimaryError()) {
                        case SslError.SSL_UNTRUSTED:
                            message = "The certificate authority is not trusted.";
                            break;
                        case SslError.SSL_EXPIRED:
                            message = "The certificate has expired.";
                            break;
                        case SslError.SSL_IDMISMATCH:
                            message = "The certificate Hostname mismatch.";
                            break;
                        case SslError.SSL_NOTYETVALID:
                            message = "The certificate is not yet valid.";
                            break;
                    }
                    message += " Do you want to continue anyway?";

                    builder.setTitle("SSL Certificate Error");
                    builder.setMessage(message);
                    builder.setPositiveButton("continue", (dialog, which) -> handler.proceed());
                    builder.setNegativeButton("cancel", (dialog, which) -> handler.cancel());
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                webView.loadUrl(request.getUrl().toString());
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return false;
            }

            @Override
            public void onLoadResource(final WebView view, final String url) {
                try {
                    if (timesInjected < 5 || timesInjected == 10) {
                        ThemeUtils.injectTextMessenger(view);
                    }

                    if (timesInjected == 10) {
                        ThemeUtils.injectTextMessenger(view);
                    }
                    if (timesInjected <= 10) {
                        timesInjected++;
                    }
                } catch (NullPointerException i) {
                    i.printStackTrace();
                }
                super.onLoadResource(view, url);

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    if (url != null && url.contains("messenger.com/")) {
                        view.loadUrl("javascript:document.querySelector('[data-testid*=\"sso_login_button\"]').click();");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                new Handler().postDelayed(() -> {
                    if (webView != null) {
                        webView.setVisibility(View.VISIBLE);
                        call_load.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                }, 3500);
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                window.destroy();
                window.removeAllViews();
                finish();
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }


            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!isDestroyed()) {
                        new MaterialAlertDialogBuilder(MessengerCall.this)
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
                        new MaterialAlertDialogBuilder(MessengerCall.this)
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
                        new MaterialAlertDialogBuilder(MessengerCall.this)
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


        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
            webView = null;

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
