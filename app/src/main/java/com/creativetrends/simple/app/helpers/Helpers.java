package com.creativetrends.simple.app.helpers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.utils.PreferencesUtility;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * Created by Creative Trends Apps.
 */

public class Helpers {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_STORAGE = 1;
    private static final int REQUEST_LOCATION = 2;
    private static final int REQUEST_RECORD = 300;
    private static final int REQUEST_FLOATING = 4;



    // request storage permission
    public static void requestFloatingPermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW};
        if (!hasFloatingPermission(activity)) {
            Log.e(TAG, "No floating permission at the moment. Requesting...");
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_FLOATING);
        }
    }

    // check is storage permission granted
    public static boolean hasFloatingPermission(Activity activity) {
        String storagePermission = Manifest.permission.SYSTEM_ALERT_WINDOW;
        int hasPermission = ContextCompat.checkSelfPermission(activity, storagePermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }




    // request storage permission
    public static void requestStoragePermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasStoragePermission(activity)) {
            Log.e(TAG, "No storage permission at the moment. Requesting...");
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_STORAGE);
        }
    }

    // check is storage permission granted
    public static boolean hasStoragePermission(Activity activity) {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ContextCompat.checkSelfPermission(activity, storagePermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestLocationPermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasLocationPermission(activity)) {
            Log.e(TAG, "No location permission at the moment. Requesting...");
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_LOCATION);
        } else {
            Log.e(TAG, "We already have location permission. Yay!");
        }
    }

    // check is location permission granted
    public static boolean hasLocationPermission(Activity activity) {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int hasPermission = ContextCompat.checkSelfPermission(activity, locationPermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }


    public static void requestMicrophonePermission(Activity activity) {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        if (!hastMicrophonePermission(activity)) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_RECORD);
        } else {
            Log.e(TAG, "We already have microphone permission. Yay!");
        }
    }
    public static boolean hastMicrophonePermission(Activity activity) {
        String locationPermission = Manifest.permission.RECORD_AUDIO;
        int hasPermission = ContextCompat.checkSelfPermission(activity, locationPermission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }





    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    public static void setUpWebViewSettings(Context context, WebSettings webSettings) {
        try {
            CookieManager.getInstance().setAcceptCookie(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
            webSettings.setAllowFileAccess(true);
            webSettings.setSaveFormData(false);
            if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64 rv:85.0) Gecko/20100101 Firefox/85.0");
            }else {
                webSettings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
            }
            webSettings.setAppCachePath(context.getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAppCacheEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            webSettings.setTextZoom(Integer.parseInt(PreferencesUtility.getInstance(context).getFont()));
            webSettings.setDatabaseEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        } catch (Exception i) {
            i.printStackTrace();
        }

    }


    public static boolean isEmpty(String string) {
        return string == null || string.length() < 1;
    }


    // method for clearing cache
    public static void deleteCache(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(Objects.requireNonNull(cache.getParent()));
        if (appDir.exists()) {
            String[] children = appDir.list();
            if (children != null) {
                for (String s : children) {
                    if (!s.equals("lib") && !s.equals("shared_prefs") && !s.equals("databases")) {
                        deleteDir(new File(appDir, s));
                        Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                    }
                }
            }
        }
    }

    // helper method for clearing cache
    private static boolean deleteDir(File dir) {
        if (dir == null)
            return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success)
                        return false;
                }
            }
        }
        return dir.delete();
    }


    public static String decodeImg(String img_url) {
        return img_url.replace("\\3a ", ":").replace("efg\\3d ", "oh=").replace("\\3d ", "=").replace("\\26 ", "&").replace("\\", "").replace("&amp;", "&");
    }

    private static Bitmap getImage(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            //Log.e(LOG_TAG, convertStreamToString(connection.getInputStream()));
            if (responseCode == 200) {
                return BitmapFactory.decodeStream(connection.getInputStream());
            } else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Bitmap getImage(String urlString) {
        try {
            URL url = new URL(urlString);
            return getImage(url);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return null;
    }
}
