package com.creativetrends.simple.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.AnimatedProgressBar;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticUtils {

    public static int darkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.0f;
        return Color.HSVToColor(hsv);

    }

    public static int darkColorTheme(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.4f;
        return Color.HSVToColor(hsv);

    }


    public static int darkColorOther(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.6f;
        return Color.HSVToColor(hsv);

    }

    public static int adjustAlpha(int color, @SuppressWarnings("SameParameterValue") float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }


    public static Uri getUrl(int res) {
        return Uri.parse("android.resource://com.creativetrends.simple.app.lite/" + res);
    }


    private static Map<String, String> getQueryParams(String str) {
        try {
            Map<String, String> hashMap = new HashMap<>();
            String[] split = str.split("\\?");
            if (split.length > 1) {
                for (String str2 : split[1].split("&")) {
                    String[] split2 = str2.split("=");
                    String decode = URLDecoder.decode(split2[0], "UTF-8");
                    Object obj = "";
                    if (split2.length > 1) {
                        obj = URLDecoder.decode(split2[1], "UTF-8");
                    }
                    hashMap.put(decode, obj.toString());
                }
            }
            return hashMap;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static String getUserIdOther(String string) {
        try {
            if (!string.equals("https://m.facebook.com/messages")) {
                String id_url = string;
                id_url = id_url.substring(id_url.indexOf("?fbid=") + 6);
                if (string.contains("&entrypoint")) {
                    id_url = id_url.substring(0, id_url.indexOf("&entry"));
                } else {
                    id_url = id_url.substring(0, id_url.indexOf("&_rdr"));
                }
                string = id_url;
            }
        } catch (Exception i) {
            i.printStackTrace();
        }
        return string;
    }

    public static long getUserId(String userID) {
        if (userID == null) {
            return 0;
        }
        try {
            String userID2;
            if (userID.contains("m.facebook.com/messages/read/")) {
                userID2 = getQueryParams(userID).get("tid");
                if (userID2 == null) {
                    return 0;
                }
                if (userID2.startsWith("cid.c.")) {
                    String userID3 = "%3A";
                    if (userID2.contains(":")) {
                        userID3 = ":";
                    }
                    String substring = userID2.substring(6, userID2.indexOf(userID3));
                    userID2 = userID2.substring(userID2.indexOf(userID3) + 1);
                    if (BadgeHelper.getCookie() != null)
                        if (substring.trim().equals(BadgeHelper.getCookie())) {
                            return Long.parseLong(userID2);
                        }
                    return Long.parseLong(substring);
                } else if (!userID2.startsWith("cid.g.")) {
                    return Long.parseLong(userID2);
                } else {
                    return Long.parseLong(userID2.substring(6));
                }
            } else if (userID.contains("m.facebook.com/messages/thread/")) {
                userID2 = userID.split("\\?")[0];
                if (userID2.endsWith("/")) {
                    userID2 = userID2.substring(0, userID2.length() - 1);
                }
                return Long.parseLong(userID2.substring(userID2.indexOf("messages/thread/") + 16));
            }
        } catch (IndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getTextBetween(String str, String str2) {
        Matcher matcher = Pattern.compile(str2).matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }



    public static void setSwipeColor(SwipeRefreshLayout swipeRefreshLayout, Context mcontext) {
        boolean MenuLight = PreferencesUtility.getInstance(mcontext).getFreeTheme().equals("materialtheme");
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mcontext, R.color.m_color));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getMenu(mcontext));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            swipeRefreshLayout.setColorSchemeColors(ThemeUtils.getColorPrimaryDark());
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(mcontext, R.color.white));
        } else if (!MenuLight) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mcontext, R.color.m_color));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getMenu(mcontext));
        } else {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mcontext, R.color.m_color));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getMenu(mcontext));
        }
    }

    public static void setProgressColor(AnimatedProgressBar progressBar, Context mcontext) {
        boolean MenuLight = PreferencesUtility.getInstance(mcontext).getFreeTheme().equals("materialtheme");
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            progressBar.setBackgroundColor(ThemeUtils.getTheme(mcontext));
            progressBar.setProgressColor(ContextCompat.getColor(mcontext, R.color.m_color));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            progressBar.setBackgroundColor(ThemeUtils.getTheme(mcontext));
            progressBar.setProgressColor(ThemeUtils.getColorPrimaryDark());
        } else if (!MenuLight) {
            progressBar.setBackgroundColor(ThemeUtils.getTheme(mcontext));
            progressBar.setProgressColor(ContextCompat.getColor(mcontext, R.color.m_color));
        } else {
            progressBar.setBackgroundColor(ThemeUtils.getTheme(mcontext));
            progressBar.setProgressColor(ContextCompat.getColor(mcontext, R.color.m_color));
        }
    }


    public static void setProgressColorNative(ProgressBar progressBar, Context mcontext) {
        boolean MenuLight = PreferencesUtility.getInstance(mcontext).getFreeTheme().equals("materialtheme");
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getTheme(mcontext)));
            progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(mcontext, R.color.m_color)));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getTheme(mcontext)));
            progressBar.setProgressTintList(ColorStateList.valueOf(ThemeUtils.getColorPrimary(mcontext)));
        } else if (!MenuLight) {
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getTheme(mcontext)));
            progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(mcontext, R.color.m_color)));
        } else {
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getTheme(mcontext)));
            progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(mcontext, R.color.m_color)));
        }
    }





    public static String processAlbumUrltoPhotoUrl(String url) {
        String storyId;
        String profileId = null;
        if (url.contains("photo=")) {
            storyId = url.substring(url.indexOf("photo="));
            storyId = storyId.substring(0, storyId.indexOf("&")).replace("photo=", "");
        } else if (url.contains("pcb.")) {
            storyId = url.substring(url.indexOf("pcb."));
            storyId = storyId.substring(0, storyId.indexOf("&")).replace("pcb.", "");
        } else {
            storyId = null;
        }
        if (url.contains("profileid=")) {
            profileId = url.substring(url.indexOf("profileid="));
            profileId = profileId.substring(0, profileId.indexOf("&")).replace("profileid=", "");
        }
        if (storyId == null || profileId == null) {
            return url;
        }
        return "https://m.facebook.com/story.php?story_fbid=" + storyId + "&id=" + profileId;
    }


    public static String processImageUrlFromStyleString(String style) {
        String imageUrl;
        if (style.startsWith("https://")) {
            return style;
        }
        try {
            if (style.contains(")")) {
                imageUrl = style.substring(style.indexOf("https://"), style.indexOf(")"));
            } else {
                imageUrl = style.substring(style.indexOf("https://"));
            }
            return imageUrl.replace("\"", "");
        } catch (Exception e) {
            try {
                if (style.contains(")")) {
                    imageUrl = style.substring(style.indexOf("https"), style.indexOf(")"));
                } else {
                    imageUrl = style.substring(style.indexOf("https"));
                }
                imageUrl = imageUrl.replace("\"", "").replace("'", "").replace("&quot;", "").trim();
                while (imageUrl.contains(" ")) {
                    String charsToConvert = imageUrl.substring(imageUrl.indexOf(" ") - 3, imageUrl.indexOf(" ") + 1);
                    imageUrl = imageUrl.replace(charsToConvert, URLDecoder.decode(charsToConvert.replace("\\", "%").replace(" ", ""), "UTF-8"));
                }
                return imageUrl;
            } catch (Exception ignored) {

                return null;
            }
        }

    }

    static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    //bottom navigation for google
    public static void showBadge(Context context, BottomNavigationView bottomNavigationView, @IdRes int itemId, String value) {
        removeBadge(bottomNavigationView, itemId);
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        View badge = LayoutInflater.from(context).inflate(R.layout.notification_badge, bottomNavigationView, false);


        TextView text = badge.findViewById(R.id.badge_text_view);
        text.setText(value);
        itemView.addView(badge);

        if (!PreferencesUtility.getBoolean("tab_labels", false)) {
            setMargin(text, context.getResources().getDimensionPixelSize(R.dimen.bottom_navigation_notification_icon));
        }
    }

    public static void removeBadge(BottomNavigationView bottomNavigationView, @IdRes int itemId) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }


    private static void setMargin(View view, int top) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                view.getLayoutParams();
        params.setMargins(0, top, 10, 10);
        view.setLayoutParams(params);
    }

    public static void clearCookies() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

    public static void clearStrings() {
        //basic
        PreferencesUtility.putString("user_cover", "");
        PreferencesUtility.putString("user_picture", "");
        PreferencesUtility.putString("user_name", "");
        //badges
        PreferencesUtility.putString("group_counts", "");
        PreferencesUtility.putString("chat_count", "");
        PreferencesUtility.putString("page_count", "");
        PreferencesUtility.putString("request_count", "");
        //page
        PreferencesUtility.putString("my_page_pic", "");
        PreferencesUtility.putString("my_page_text_view", "");
        PreferencesUtility.putString("my_page_linker", "");
    }


    public static void showTerms(Activity activity) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getResources().getString(R.string.terms_settings))
                .setMessage(activity.getResources().getString(R.string.eula_string, year))
                .setPositiveButton(R.string.ok, (arg0, arg1) -> {
                })
                .show();
    }

    public static void showPolicy(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.privacy_policy)
                .setMessage(Html.fromHtml(activity.getString(R.string.policy_about)))
                .setPositiveButton(R.string.ok, (arg0, arg1) -> {
                })
                .show();
    }

    public static boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }

        return containsDigit;
    }



    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean moveFile(File source, String destPath){
        if(source.exists()){
            File dest = new File(destPath);
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(dest)){
                if(!dest.exists()){
                    dest.createNewFile();
                }
                writeToOutputStream(fis, fos);
                source.delete();
                return true;
            } catch (IOException ioE){
                ioE.toString();
            }
        }
        return false;
    }

    private static void writeToOutputStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        if (is != null) {
            while ((length = is.read(buffer)) > 0x0) {
                os.write(buffer, 0x0, length);
            }
        }
        os.flush();
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


    public static void showSnackBar(Activity mActivity, String message){
        Snackbar snackbar = Snackbar.make(mActivity.getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setTranslationY(-(convertDpToPixel(48, mActivity)));
        snackbar.show();
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }



    public static int measureContentWidth(ListAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = listAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = listAdapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(SimpleApplication.getContextOfApplication());
            }

            itemView = listAdapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }


    public static void deleteCookies(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        try {
            String domainName = getUrlDomainName(url);
            String cookiesString = cookieManager.getCookie(url);
            String[] cookies = cookiesString.split("; ");
            for (String cookie : cookies) {
                if (cookie == null || TextUtils.isEmpty(cookie))
                    continue;
                int equalCharIndex = cookie.indexOf('=');
                if (equalCharIndex == -1)
                    continue;
                String cookieString = cookie.substring(0, equalCharIndex) + '=' + "; Domain=" + domainName;
                cookieManager.setCookie(url, cookieString);
                Log.e("Cookie?", cookiesString);
            }
        } catch (Exception e) {
            Log.e("TAG", "Message", e);
        }
    }

    public static String getUrlDomainName(String url) {
        String domainName = url;
        int index = domainName.indexOf("://");
        if (index != -1) {
            domainName = domainName.substring(index + 3);
        }
        index = domainName.indexOf('/');
        if (index != -1) {
            domainName = domainName.substring(0, index);
        }
        return domainName;
    }


    public static String sanitizeUrl(String url) {
        // Remove `?utm_`.
        if (url.contains("?utm_")) {
            url = url.substring(0, url.indexOf("?utm_"));
        }

        // Remove `&utm_`.
        if (url.contains("&utm_")) {
            url = url.substring(0, url.indexOf("&utm_"));
        }

        // Remove `?fbclid=`.
        if (url.contains("?fbclid=")) {
            url = url.substring(0, url.indexOf("?fbclid="));
        }

        // Remove `&fbclid=`.
        if (url.contains("&fbclid=")) {
            url = url.substring(0, url.indexOf("&fbclid="));
        }

        // Remove `?fbadid=`.
        if (url.contains("?fbadid=")) {
            url = url.substring(0, url.indexOf("?fbadid="));
        }

        // Remove `&fbadid=`.
        if (url.contains("&fbadid=")) {
            url = url.substring(0, url.indexOf("&fbadid="));
        }

        // Remove `?amp=1`.
        if (url.contains("?amp=1")) {
            url = url.substring(0, url.indexOf("?amp=1"));
        }
        Log.e("Sanitized", url);
        // Return the sanitized URL.
        return url;

    }

}