package com.creativetrends.simple.app.webview;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.BrowserActivity;
import com.creativetrends.simple.app.activities.BrowserPopup;
import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.activities.PhotoActivity;
import com.creativetrends.simple.app.activities.PopupView;
import com.creativetrends.simple.app.activities.VideoActivity;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.Cleaner;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class LinkHandler {


    private static boolean passesPreliminaryCheck(Activity activity, String url) {
        return !(!NetworkConnection.isConnected(activity) || url == null || url.isEmpty() || url.equals("about:blank") || url.contains("staticxx.facebook.com") || url.contains("sem_campaigns"));
    }

    private static boolean containsClickActionUrls(String url) {
        return url.startsWith("javascript") || url.endsWith("#") || url.contains("&p=") || url.contains("sharer") || url.contains("messages/?folder") || url.contains("/reaction/") || url.contains("pagination") || url.contains("action_redirect") || url.contains("/graphsearch/") || url.contains("like.php") || url.contains("/privacy/save") || url.contains("view_privacy") || url.contains("a/comment.php") || url.contains("a/like") || url.contains("like_");
    }


    private static void possiblyGoBackAfterClick(boolean shouldGoBack, final WebView webView) {
        if (!shouldGoBack) {
            return;
        }
        webView.stopLoading();
    }


    public static boolean oneTap(Activity activity, final WebView webView, String url, boolean shouldGoBack) {
        url = cleanUpUrl(url);
        if (!passesPreliminaryCheck(activity, url)) {
            return true;
        }
        if (containsClickActionUrls(url)) {
            return false;
        }
        possiblyGoBackAfterClick(shouldGoBack, webView);
        if (url.contains("tel:")) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            activity.startActivity(i);
            return true;
        } else if (url.contains("mailto:")) {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            activity.startActivity(i);
            return true;
        } else if (url.contains("geo:") || url.contains("google.com/maps") ) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(i);
            return true;
        } else if (url.contains("youtube.com") || url.contains("youtu.be")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(i);
            return true;
        } else if(url.contains("/marketplace/item/")){
            Intent me = new Intent(activity, NewPageActivity.class);
            me.putExtra("url", url);
            activity.startActivity(me);
            return true;
        } else if (url.startsWith("https://video") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav") || url.contains("/video_redirect/")) {
            if (url.contains("/video_redirect/?src=")) {
                url = url.substring(url.indexOf("/video_redirect/?src=")).replace("/video_redirect/?src=", "");
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handleVideoLinks(activity, url);
                return true;
            }
        } else {
            handleExternalLinks(activity, url);
        }
        return true;
    }

    public static boolean handleLongClicks(final Activity activity, WebView webView) {
        if (!NetworkConnection.isConnected(activity)) {
            return true;
        }
        WebView.HitTestResult result = webView.getHitTestResult();
        if (result == null) {
            return true;
        }
        if (result.getType() == 9 || result.getType() == 0) {
            return false;
        }
        String url = result.getExtra();
        if (url == null) {
            return true;
        }
        if (url.equals("about:blank") || url.contains("staticxx.facebook.com") || url.contains("sem_campaigns") || url.endsWith("#") || url.contains("/friends/pymk/")) {
            return true;
        }
        if (url.contains("photo.php?") || url.contains("/photos/a.") && !url.contains("photos/pcb.") && !url.contains("fs=1") && !url.contains("ref=m_notif")) {
            Intent photoIntent = new Intent(activity, PopupView.class);
            photoIntent.putExtra("url", url);
            photoIntent.putExtra("comments", false);
            activity.startActivity(photoIntent);
        } else if ((url.contains("photoset_token"))) {
            Intent intent = new Intent(activity, PopupView.class);
            intent.putExtra("url", StaticUtils.processAlbumUrltoPhotoUrl(url));
            intent.putExtra("comments", false);
            activity.startActivity(intent);
        } else if (url.contains("view_full_size")) {
            Intent photoIntent = new Intent(activity, PhotoActivity.class);
            photoIntent.putExtra("url", url);
            activity.startActivity(photoIntent);
            webView.stopLoading();
        } else if (url.contains(".jpg") || url.contains(".png") || url.contains("scontent")) {
            Intent photoIntent = new Intent(activity, PhotoActivity.class);
            photoIntent.putExtra("url", url);
            activity.startActivity(photoIntent);
        } else if (url.startsWith("https://video") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(".wav") || url.contains("/video_redirect/")) {
            if (url.contains("/video_redirect/?src=")) {
                url = url.substring(url.indexOf("/video_redirect/?src=")).replace("/video_redirect/?src=", "");
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                handleVideoLinks(activity, url);
                return true;
            }
        } else if (url.contains("l.facebook.com") || (url.contains("lm.facebook.com") || url.contains("source=facebook.com&") || !url.contains("facebook.com"))) {
            url = Cleaner.cleanAndDecodeUrl(url);
            Intent intent = new Intent(activity, BrowserPopup.class);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
        } else {
            url = Cleaner.cleanAndDecodeUrl(url);
            Intent intent = new Intent(activity, PopupView.class);
            intent.putExtra("url", url);
            intent.putExtra("comments", false);
            activity.startActivity(intent);
        }
        return true;
    }

    public static void handleExternalLinks(Activity activity, String url) {
        //external link
        if (url.contains("l.facebook.com") || (url.contains("lm.facebook.com") || url.contains("source=facebook.com&") || !url.contains("facebook.com"))) {
            String linkUrl = Cleaner.cleanAndDecodeUrl(url);
            switch (PreferencesUtility.getInstance(SimpleApplication.getContextOfApplication()).getBrowser()) {
                case "external_browser":
                    try {
                        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
                        activity.startActivity(browser);
                        PreferencesUtility.putString("needs_lock", "false");
                    } catch (ActivityNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "in_app_browser":
                    Intent peekIntent = new Intent(activity, BrowserActivity.class);
                    peekIntent.setData(Uri.parse(linkUrl));
                    peekIntent.putExtra("fullscreen", false);
                    activity.startActivity(peekIntent);
                    break;
            }
        } else {
            if (url.startsWith("https://cdn.fb") || url.startsWith("http://cdn.fb")) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    activity.startActivity(browser);
                    PreferencesUtility.putString("needs_lock", "false");
                } catch (ActivityNotFoundException ex) {
                    Log.e("peekActivity", "" + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                Intent intent = new Intent(activity, NewPageActivity.class);
                intent.putExtra("url", url);
                activity.startActivity(intent);
            }
        }
    }

    public static void handleVideoLinks(Activity activity, String url) {
        PreferencesUtility.putString("needs_lock", "false");
        Intent i = new Intent(activity, VideoActivity.class);
        i.putExtra("VideoUrl", url);
        i.putExtra("VideoName", "");
        activity.startActivity(i);
    }


    public static void handleMessageLinks(Activity activity, String url) {
        PreferencesUtility.putString("needs_lock", "false");
        Intent i = new Intent(activity, NewPageActivity.class);
        i.putExtra("VideoUrl", url);
        i.putExtra("VideoName", "");
        activity.startActivity(i);
    }

    @SuppressWarnings("deprecation")
    public static String cleanUpUrl(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            String replaceAll = url.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            try {
                url = replaceAll.replaceAll("\\+", "%2B");
                url = URLDecoder.decode(url, "utf-8");
            } catch (Exception exception) {
                exception.printStackTrace();
                return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeJava(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeJava(url));
        }
        return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeJava(url));
    }
}
