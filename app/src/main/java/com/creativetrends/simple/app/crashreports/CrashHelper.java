package com.creativetrends.simple.app.crashreports;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;

import java.util.Locale;


public class CrashHelper {
    private static final String GET_RESULTS = "Unverified";
    private static final String GET_RESULTS_OTHER = "Verified";

    public static String getInfo() {

        Context context = SimpleApplication.getContextOfApplication();
        String mobile = "Mobile data";
        String wifi = "WiFi";
        StringBuilder sb = new StringBuilder();

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            String pname = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            String installPM = pm.getInstallerPackageName(pname);
            sb.append("\nApp Package Name: ").append(context.getResources().getString(R.string.app_name_pro));
            sb.append("\nApp Version: ").append(pInfo.versionName);
            sb.append("\nPackage Manager: ").append(installPM);
            if (installPM == null) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            } else if (installPM.equals("com.android.vending")) {
                sb.append("\nBug Status: ").append(GET_RESULTS_OTHER);
            } else if (installPM.equals("installer")) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            } else if (installPM.equals("com.google.android.feedback")) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            } else if (installPM.contains("creativetrends")) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            } else if (installPM.contains("key")) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            } else if (installPM.contains("aptoide")) {
                sb.append("\nBug Status: ").append(GET_RESULTS);
            }
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e("Misc: getDeviceInfo", ex.getMessage());
        }
        sb.append("\nAndroid Version: ").append(android.os.Build.VERSION.RELEASE);
        sb.append("\nDevice: ").append(android.os.Build.DEVICE);
        sb.append("\nModel: ").append(android.os.Build.MODEL);
        sb.append("\nManufacturer: ").append(android.os.Build.MANUFACTURER);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                sb.append("\nConnection: ").append(wifi);
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                sb.append("\nConnection: ").append(mobile);
            }
        }
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication());
        //sb.append("\nSync Interval: ").append(Integer.parseInt(preferences.getString("interval_pref", ""))).append(" seconds");
        sb.append("\nLocale: ").append(Locale.getDefault().toString());
        return sb.toString();
    }

}