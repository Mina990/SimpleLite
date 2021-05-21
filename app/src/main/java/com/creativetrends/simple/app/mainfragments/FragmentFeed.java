package com.creativetrends.simple.app.mainfragments;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
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
import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.activities.VideoActivity;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.interfaces.ImageInterface;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.AnimatedProgressBar;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.webview.LinkHandler;
import com.creativetrends.simple.app.webview.NestedWebView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.creativetrends.simple.app.activities.MainActivity.bottomNavigationView;
import static com.creativetrends.simple.app.activities.MainActivity.getMainActivity;


public class FragmentFeed extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int FILECHOOSER_RESULTCODE = 1;
    public NestedWebView mWebView;
    private SwipeRefreshLayout mSwipe;
    private int injectTime;
    private int scrollPosition = 0;
    // save images
    private boolean isTop;
    private CardView card;
    private Context context;
    private ValueCallback<Uri[]> mFilePathCallback;
    public ExtendedFloatingActionButton status;
    View v;
    AnimatedProgressBar progressBar;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                goBack();
            }
        }
    };

    public FragmentFeed() {

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
        if(PreferencesUtility.getBoolean("top_tabs", false)) {
            v = inflater.inflate(R.layout.fragment_feed_top, container, false);
        }else{
            v = inflater.inflate(R.layout.fragment_feed, container, false);
        }
        FrameLayout background = v.findViewById(R.id.root);
        background.setBackgroundColor(ThemeUtils.getTheme(getActivity()));

        mSwipe = v.findViewById(R.id.frag_swipe);
        status = v.findViewById(R.id.statusFAB);
        if(!PreferencesUtility.getBoolean("expand_fab", false)) {
            status.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_add_filter));
            status.setExtended(false);
        }
        progressBar = v.findViewById(R.id.tabs_progress);
        StaticUtils.setSwipeColor(mSwipe, context);
        StaticUtils.setProgressColor(progressBar, context);
        card = v.findViewById(R.id.card_notifications);
        card.setCardBackgroundColor(ThemeUtils.getMenu(context));
        if(PreferencesUtility.getBoolean("show_panels", false)) {
            card.setVisibility(View.VISIBLE);
        }
        AppCompatTextView online = v.findViewById(R.id.no_text);
        if (!PreferencesUtility.getBoolean("top_news", false)) {
            online.setText(R.string.most_recent);
            isTop = false;
        } else {
            online.setText(R.string.top_stories);
            isTop = true;
        }
        ImageView mark = v.findViewById(R.id.mark_check);
        mark.setOnClickListener(v13 -> {
            online.setText(R.string.most_recent);
            mWebView.loadUrl("https://touch.facebook.com/home.php?sk=h_chr");
            isTop = false;
            PreferencesUtility.putBoolean("top_news", false);
        });
        ImageView set = v.findViewById(R.id.no_settings);
        set.setOnClickListener(v14 -> {
            online.setText(R.string.top_stories);
            mWebView.loadUrl("https://touch.facebook.com/home.php?sk=h_nor");
            isTop = true;
            PreferencesUtility.putBoolean("top_news", true);
        });


        ImageView active = v.findViewById(R.id.icon_active);
        boolean MenuLight = PreferencesUtility.getInstance(context).getFreeTheme().equals("materialtheme");

        if (MenuLight && !ThemeUtils.isNightTime()) {
            mark.setColorFilter(ThemeUtils.getColorPrimaryDark());
            set.setColorFilter(ThemeUtils.getColorPrimaryDark());
            active.setColorFilter(ThemeUtils.getColorPrimaryDark());
        } else {
            mark.setColorFilter(ContextCompat.getColor(context, R.color.m_color));
            set.setColorFilter(ContextCompat.getColor(context, R.color.m_color));
            active.setColorFilter(ContextCompat.getColor(context, R.color.m_color));
        }
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            status.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
            //recentFragment.status.setColorRipple(ContextCompat.getColor(this, R.color.black));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            status.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimaryDark()));
            //recentFragment.status.setColorRipple(ThemeUtils.getColorPrimaryDark());
        } else if (!MenuLight) {
            status.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
        } else {
            status.setBackgroundTintList(ColorStateList.valueOf(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark())));
        }

        mSwipe.setOnRefreshListener(this);
        mWebView = v.findViewById(R.id.frag_webview);
        mWebView.setBackgroundColor(ThemeUtils.getTheme(getActivity()));
        WebSettings webSettings = mWebView.getSettings();
        Helpers.setUpWebViewSettings(getActivity(), webSettings);
        mWebView.addJavascriptInterface(this, "Downloader");
        mWebView.addJavascriptInterface(new ImageInterface(getActivity()), "Photos");

        try {
            if (MainActivity.appBarLayout != null) {
                MainActivity.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        status.setTranslationY(-6 * verticalOffset);
                        if(PreferencesUtility.getBoolean("expand_fab", false)) {
                            if (verticalOffset == 0) {
                                status.setExtended(true);
                            } else {
                                status.setExtended(false);
                            }
                        }
                    }
                });
            }
        } catch (NullPointerException i) {
            i.printStackTrace();
        }

        status.setOnClickListener(v15 -> ((MainActivity) MainActivity.getMainActivity()).showPost());
        status.setOnLongClickListener(v15 -> {
            LinkHandler.handleExternalLinks(getActivity(), "https://messenger.com/t/0");
            return false;
        });

        mWebView.setOnKeyListener((v1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == MotionEvent.ACTION_UP
                    && mWebView.canGoBack() && mWebView != null && !mWebView.getUrl().contains("home.php?sk=h_nor") && !mWebView.getUrl().contains("home.php?sk=h_chr") | mWebView.getUrl().contains("sharer") | mWebView.getUrl().contains("soft=composer")) {
                handler.sendEmptyMessage(1);
                return true;
            }

            return false;
        });

        if (PreferencesUtility.getBoolean("peek_View", false)) {
            mWebView.setOnLongClickListener(v12 -> LinkHandler.handleLongClicks(getActivity(), mWebView));
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    injectTime = 0;
                    mSwipe.setRefreshing(false);
                    mWebView.setVisibility(View.GONE);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
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
                    }

                    if(mWebView != null) {
                        BadgeHelper.videoView(mWebView);
                    }


                    if (injectTime == 12) {
                        mSwipe.setRefreshing(false);
                    }

                    if (injectTime <= 10) {
                        injectTime++;
                    }

                } catch (Exception i) {
                    i.printStackTrace();
                }
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (view.getUrl() != null) {
                    OnStories(view.getUrl().contains("composer") | view.getUrl().contains("/?tray_session_id") | view.getUrl().contains("/stories/preview/") | view.getUrl().contains("sharer-dialog.php") | view.getUrl().contains("photo_attachments_list") | view.getUrl().contains("%2Fphotos%2Fviewer%2F%3Fphotoset_token") | view.getUrl().contains("facebook.com/photo.php?") | view.getUrl().contains("/photos/") | view.getUrl().contains("?photoset"));
                    ThemeUtils.removeEditor(mWebView);
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                if(view != null){
                    ThemeUtils.removeEditor(mWebView);
                }

            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    //mSwipe.setRefreshing(false);
                    mWebView.setVisibility(View.VISIBLE);
                    ThemeUtils.pageFinished(view, url);
                    ThemeUtils.injectPhotos(mWebView);
                    BadgeHelper.videoView(mWebView);
                    ThemeUtils.removeEditor(mWebView);
                    ThemeUtils.injectPadding(mWebView);
                } catch (Exception i) {
                    i.printStackTrace();
                }
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
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
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});

                Intent[] intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_image_video));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                PreferencesUtility.putString("needs_lock", "false");
                return true;

            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {

                Helpers.requestLocationPermission(getActivity());
                if (!Helpers.hasLocationPermission(getActivity()))
                    return;

                callback.invoke(origin, true, false);
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
                    }
                }else {
                    if (newProgress < 50) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onReceivedTitle(final WebView view, String title) {
                super.onReceivedTitle(view, title);
                try {
                    ThemeUtils.removeEditor(mWebView);
                    ThemeUtils.removeEditor(mWebView);
                } catch (NullPointerException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mWebView.setOnScrollChangedCallback(new NestedWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t) {
                scrollPosition = t;

            }
        });

        if (PreferencesUtility.getBoolean("top_news", false)) {
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_nor");
            isTop = true;
        } else {
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_chr");
            isTop = false;
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }

        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime() & context != null & mWebView != null & mSwipe != null){
            ThemeUtils.switchTheme(context, mWebView, mSwipe);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }




    private void goBack() {
        if (mWebView != null && mWebView.copyBackForwardList().getCurrentIndex() > 0) {
            mWebView.stopLoading();
            mWebView.goBack();
            mSwipe.setRefreshing(false);
            mSwipe.postDelayed(() -> mSwipe.setRefreshing(false), 500);
            if (mWebView != null) {
                ThemeUtils.removeEditor(mWebView);
                ThemeUtils.removeEditor(mWebView);
            }
        }
    }

    @JavascriptInterface
    public void processVideo(String vidData, String vidID) {
        Intent i = new Intent(getActivity(), VideoActivity.class);
        i.putExtra("VideoUrl", vidData);
        i.putExtra("VideoName", vidID);
        startActivity(i);
    }

    /*@JavascriptInterface
    public void processVideo(String vidData, String vidID) {
        Log.d("link", "https://m.facebook.com"+vidID);
        System.out.println("Video: " + vidData);
        Intent i = new Intent(getActivity(), VideoActivity.class);
        i.putExtra("VideoUrl", vidData);
        i.putExtra("VideoName", "https://m.facebook.com"+vidID);
        startActivity(i);
    }*/

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
                } else if (url.contains("sharer.php?m=message") && url.contains("&error=")) {
                    showMessageBug();
                    return false;
                } else if (url.startsWith("https://video") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav") || url.contains("/video_redirect/")) {
                    if (url.contains("/video_redirect/?src=")) {
                        url = url.substring(url.indexOf("/video_redirect/?src=")).replace("/video_redirect/?src=", "");
                        try {
                            url = URLDecoder.decode(url, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        LinkHandler.handleVideoLinks(getActivity(), url);
                        return true;
                    }
                } else {
                    return LinkHandler.oneTap(getActivity(), webView, url, true);
                }
                return false;
            } catch (NullPointerException i) {
                i.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onRefresh() {
        scrollPosition = 0;
        mSwipe.setRefreshing(false);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        if (isTop) {
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_nor");
        } else {
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_chr");
        }
    }

    public void scrollToTop(){
        if (scrollPosition > 10) {
            scrollToTop(mWebView);
        }else{
            onRefresh();
        }
    }

    public void loadRecent() {
        if (mWebView != null)
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_chr");
    }

    public void loadTop() {
        if (mWebView != null)
            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_nor");
    }

    public void getLoad() {
        if (mWebView != null) {
            mWebView.reload();
        }
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

    private void OnStories(boolean isStories) {
        try {
            if (isStories) {
                if (PreferencesUtility.getBoolean("show_panels", false)) {
                    card.setVisibility(View.GONE);
                }
                if (MainActivity.appBarLayout != null) {
                    MainActivity.appBarLayout.setExpanded(false, true);
                }
                bottomNavigationView.setVisibility(View.GONE);
                mSwipe.setEnabled(false);
                status.setVisibility(View.GONE);
            } else {
                if (PreferencesUtility.getBoolean("show_panels", false)) {
                    card.setVisibility(View.VISIBLE);
                }
                if (MainActivity.appBarLayout != null) {
                    MainActivity.appBarLayout.setExpanded(true, true);
                }
                bottomNavigationView.setVisibility(View.VISIBLE);
                mSwipe.setEnabled(true);
                status.setVisibility(View.VISIBLE);

            }
        }catch (Exception p){
            p.printStackTrace();
        }
    }


    public void showMessageBug() {
        if (!getMainActivity().isDestroyed()) {
            new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                    .setCancelable(false)
                    .setTitle("Messages Bug")
                    .setMessage("There is currently a bug on the Facebook mobile site which causes an error when sharing a post to messages. Creative Trends Apps LLC has no control over this and is waiting for a fix from Facebook. Thanks for your understanding.")
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                        if (isTop) {
                            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_nor");
                        } else {
                            mWebView.loadUrl("https://m.facebook.com/home.php?sk=h_chr");
                        }
                    })
                    .show();

        }
    }

}