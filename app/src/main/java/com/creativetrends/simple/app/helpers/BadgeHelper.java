package com.creativetrends.simple.app.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BadgeHelper {
    private static final int REQUEST_STORAGE = 1;


    public static void videoView(WebView view) {
        view.loadUrl("javascript:(function prepareVideo() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;console.log(i);var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'Downloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');}}})()");
        view.loadUrl("javascript:( window.onload=prepareVideo;)()");
    }

    public static void videoViewPlay(WebView view) {
        view.loadUrl("javascript:(function autoPlayVideo() { var el='div[data-store*=videoID]';var tolerance=200;el=document.querySelectorAll(el);for(i=0;i<el.length;i+=1){if(el!=null){var elRect=el[i].getBoundingClientRect();var isVidVisible=elRect.bottom-tolerance>0&&elRect.right-tolerance>0&&elRect.left+tolerance<window.innerWidth&&elRect.top+tolerance<window.innerHeight;if(isVidVisible){el[i].querySelector('div[data-sigil*=playInlineVideo]').click();el[i].querySelector('video_prefs').muted=true;}}}})()");
        view.loadUrl("javascript:(window.onload=autoPlayVideo;)()");
    }


    public static String getCookie() {
        try {
            final CookieManager cookieManager = CookieManager.getInstance();
            final String cookies = cookieManager.getCookie("https://m.facebook.com/");
            if (cookies != null) {
                String[] temp = cookies.split(";");
                for (String ar1 : temp) {
                    if (ar1.contains("c_user")) {
                        final String[] temp1 = ar1.split("=");
                        return temp1[1];
                    }
                }
            }
        } catch (Exception i) {
            i.printStackTrace();
        }
        return null;
    }

    public static boolean hasStoragePermission(Activity activity) {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ContextCompat.checkSelfPermission(activity, storagePermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestStoragePermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasStoragePermission(activity)) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_STORAGE);
        }
    }

}




