package com.creativetrends.simple.app.mainfragments;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.AnimatedProgressBar;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.LinkHandler;
import com.creativetrends.simple.app.webview.NestedWebView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class FragmentGroups extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int FILECHOOSER_RESULTCODE = 1;
    public NestedWebView mWebView;
    private SwipeRefreshLayout mSwipe;
    private int injectTime = 0;
    private AppCompatTextView loading;
    private Context context;
    private ValueCallback<Uri[]> mFilePathCallback;
    private int scrollPosition = 0;
    private boolean _hasLoadedOnce= false;
    AnimatedProgressBar progressBar;

    public FragmentGroups() {


    }

    private static void scrollToTop(WebView webView) {
        ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
        anim.setDuration(500);
        anim.start();
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups, container, false);

        FrameLayout background = v.findViewById(R.id.root);
        background.setBackgroundColor(ThemeUtils.getTheme(getActivity()));
        CardView card = v.findViewById(R.id.card_notifications);
        card.setCardBackgroundColor(ThemeUtils.getMenu(context));
        mSwipe = v.findViewById(R.id.frag_swipe);
        progressBar = v.findViewById(R.id.tabs_progress);
        StaticUtils.setSwipeColor(mSwipe, context);
        StaticUtils.setProgressColor(progressBar, context);
        loading = v.findViewById(R.id.loading_fragment);
        ImageView set = v.findViewById(R.id.no_settings);
        ImageView mark = v.findViewById(R.id.mark_check);
        mark.setOnClickListener(v12 -> loadPage("https://m.facebook.com/groups_browse/"));
        set.setOnClickListener(v12 -> loadPage("https://m.facebook.com/groups_browse/create/"));
        boolean MenuLight = PreferencesUtility.getInstance(context).getFreeTheme().equals("materialtheme");
        if (MenuLight && !ThemeUtils.isNightTime()) {
            mark.setColorFilter(ThemeUtils.getColorPrimaryDark());
            set.setColorFilter(ThemeUtils.getColorPrimaryDark());
        } else {
            mark.setColorFilter(ContextCompat.getColor(context, R.color.m_color));
            set.setColorFilter(ContextCompat.getColor(context, R.color.m_color));
        }
        if (PreferencesUtility.getBoolean("show_panels", false)) {
            card.setVisibility(View.VISIBLE);
        }

        mSwipe = v.findViewById(R.id.frag_swipe);
        loading = v.findViewById(R.id.loading_fragment);
        StaticUtils.setSwipeColor(mSwipe, context);
        mSwipe.setOnRefreshListener(this);
        mWebView = v.findViewById(R.id.frag_webview);
        mWebView.setVisibility(View.GONE);
        mWebView.setBackgroundColor(ThemeUtils.getTheme(getActivity()));
        WebSettings webSettings = mWebView.getSettings();
        Helpers.setUpWebViewSettings(getActivity(), webSettings);
        if (PreferencesUtility.getBoolean("peek_View", false)) {
            mWebView.setOnLongClickListener(v1 -> LinkHandler.handleLongClicks(getActivity(), mWebView));
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    injectTime = 0;
                    mSwipe.setRefreshing(false);
                    mWebView.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
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
                    if (mWebView != null) {
                        view.evaluateJavascript(readHtml(), null);
                    }

                    if (injectTime < 5 || injectTime == 10) {
                        ThemeUtils.pageStarted(context, view);
                        ThemeUtils.facebookTheme(context, view);
                        ThemeUtils.injectPadding(mWebView);
                        ThemeUtils.injectTextGroups(view);
                    }

                    if (injectTime <= 10) {
                        injectTime++;
                    }

                    if (injectTime == 10) {
                        mSwipe.setRefreshing(false);
                    }

                } catch (Exception i) {
                    i.printStackTrace();
                }
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    mWebView.setVisibility(View.VISIBLE);
                    mSwipe.setRefreshing(false);
                    loading.setVisibility(View.GONE);
                    ThemeUtils.pageFinished(view, url);
                    ThemeUtils.injectPadding(mWebView);
                } catch (Exception i) {
                    i.printStackTrace();
                }
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("/groups/") && url.contains("?ref=group_browse")) {
                    LinkHandler.oneTap(getActivity(), view, url, true);
                    if (view != null) {
                        view.reload();
                    }
                    return true;
                }else {
                    return handleUrl(view, url);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (!Helpers.hasStoragePermission(getActivity())) {
                    Helpers.requestStoragePermission(getActivity());
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

                PreferencesUtility.putString("needs_lock", "false");
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }


            @Override
            public boolean onJsAlert(WebView view, final String url, final String message, final JsResult result) {
                try {
                    if (!Objects.requireNonNull(getActivity()).isDestroyed()) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(R.string.app_name_pro);
                        builder1.setMessage(message);
                        builder1.setCancelable(true);
                        builder1.setPositiveButton(R.string.ok, (dialog, id) -> {
                            result.confirm();
                            dialog.dismiss();
                        });
                        builder1.setNegativeButton(R.string.cancel, (dialog, id) -> {
                            result.cancel();
                            dialog.dismiss();
                        });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
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
                    if (!Objects.requireNonNull(getActivity()).isDestroyed()) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(R.string.app_name_pro);
                        builder1.setMessage(message);
                        builder1.setCancelable(true);
                        builder1.setPositiveButton(R.string.ok, (dialog, id) -> {
                            result.confirm();
                            dialog.dismiss();
                        });
                        builder1.setNegativeButton(R.string.cancel, (dialog, id) -> {
                            result.cancel();
                            dialog.dismiss();
                        });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
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
                    if (!Objects.requireNonNull(getActivity()).isDestroyed()) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                        builder1.setTitle(message);
                        builder1.setCancelable(true);
                        builder1.setPositiveButton(R.string.ok, (dialog, id) -> {
                            result.confirm();
                            dialog.dismiss();
                        });
                        builder1.setNegativeButton(R.string.cancel, (dialog, id) -> {
                            result.cancel();
                            dialog.dismiss();
                        });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {

                Helpers.requestLocationPermission(getActivity());
                if (!Helpers.hasLocationPermission(getActivity()))
                    return;

                callback.invoke(origin, true, false);
            }


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
                    if (newProgress < 90) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }
                }else {
                    if (newProgress < 50) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }
                }
            }
        });
        mWebView.setOnScrollChangedCallback((l, t) -> scrollPosition = t);
        if(PreferencesUtility.getBoolean("load_all", false)) {
            loadStart();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }



    private void loadStart() {
        if (mWebView != null) {
            mWebView.loadUrl("https://m.facebook.com/groups_browse/your_groups/");
        }
    }


    private String readHtml() {
        String content;
        try {
            InputStream is = context.getAssets().open("js/cleaning.js");
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            content = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return content;
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
                        } else if (url.contains("&_rdc=2&_rdr")) {
                            url = url.replace("&_rdc=2&_rdr", "");
                        }
                        if (webView != null)
                            webView.loadUrl(url);
                        return false;
                    }
                } else if (url.contains("www.google") && url.contains("/ads/")) {
                    return true;
                } else if (url.contains("/groups/?_rdr")) {
                    if (webView != null)
                        webView.loadUrl(url);
                    return false;
                } else {
                    LinkHandler.oneTap(getActivity(), webView, url, true);
                    if(mWebView != null){
                        mWebView.onResume();
                        mWebView.resumeTimers();
                        mWebView.reload();
                    }
                }
                return false;
            } catch (NullPointerException i) {
                i.printStackTrace();
            }

            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        scrollPosition = 0;
        mSwipe.setRefreshing(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        if(mWebView != null) {
            mWebView.loadUrl("https://m.facebook.com/groups_browse/your_groups/");
        }
    }

    public void scrollToTop(){
        if (scrollPosition > 10) {
            scrollToTop(mWebView);
        }else{
            onRefresh();
        }
    }

    public void loadDiscover() {
        loadPage("https://m.facebook.com/groups_browse/");
    }

    public void loadCreate() {
        loadPage("https://m.facebook.com/groups_browse/create/");
    }

    public void loadPage(String url) {
        Intent photoIntent = new Intent(getActivity(), NewPageActivity.class);
        photoIntent.putExtra("url", url);
        startActivity(photoIntent);
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            String dataString = data.getDataString();
            if (dataString != null) {
                results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);
        if (this.isVisible()) {
            if (isFragmentVisible_ && !_hasLoadedOnce) {
                if(!PreferencesUtility.getBoolean("load_all", false)) {
                    loadStart();
                }
                _hasLoadedOnce = true;
            }
        }
    }



}