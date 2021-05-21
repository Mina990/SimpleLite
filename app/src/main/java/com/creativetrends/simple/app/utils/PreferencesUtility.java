package com.creativetrends.simple.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.adapters.SearchItems;
import com.creativetrends.simple.app.adapters.UserFiles;
import com.creativetrends.simple.app.adapters.UserItems;
import com.creativetrends.simple.app.adapters.UserItemsOther;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public final class PreferencesUtility {
    private static final String LAST_MESSENGER_TIME = "simple.last_message_time";
    private static final String LAST_NOTIFICATION_TIME = "simple.last_notification_time";
    private static final String THEME_PREFERENCE = "theme_preference";
    private static final String FONT_SIZE = "font_size";
    private static final String FACEBOOK_THEMES = "theme_preference_fb";
    private static final String NEWS_FEED = "news_feed";
    private static final String GET_BROWSER = "key_pref_browser";
    private static final String SIMPLE_PINS_STAR = "simple_pins_starred";
    public static ArrayList<String> list = new ArrayList<>();
    public static String[] str = new String[0];
    private static SharedPreferences mPreferences;
    private static PreferencesUtility sInstance;
    public static ArrayList<String> likes = new ArrayList<>();

    public PreferencesUtility(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getBoolean(key, defValue);
    }

    public static void putBoolean(String key, boolean defValue) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).edit();
        editor.putBoolean(key, defValue);
        editor.apply();
    }

    public static String getString(String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getString(key, defValue);
    }

    public static void putString(String key, String value) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static String getAppVersionName(Context context) {
        String res = "0.0.0.0.0.0";
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public static String getAppVersionNameUpdate(Context context) {
        String res = "00.0.0";
        try {
            res = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }


    public static void saveSearch(ArrayList<SearchItems> pinsList) {
        JSONArray array = new JSONArray();
        Iterator<SearchItems> it = pinsList.iterator();
        if (it.hasNext()) {
            do {
                SearchItems bookmark = it.next();
                JSONObject ob = new JSONObject();
                try {
                    ob.put("name", bookmark.getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(ob);
            } while (it.hasNext());
        }
        putString("simple_search", array.toString());
    }

    public static ArrayList<SearchItems> getSearch() {
        String bookmarks = getString("simple_search", "[]");
        ArrayList<SearchItems> pinsList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(bookmarks);
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                SearchItems bookmark = new SearchItems();
                bookmark.setName(ob.getString("name"));
                pinsList.add(bookmark);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pinsList;
    }

    public static ArrayList<PinItems> getBookmarks() {
        String bookmarks = getString("simple_pins", "[]");
        ArrayList<PinItems> pinsList = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(bookmarks);
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                PinItems bookmark = new PinItems();
                bookmark.setTitle(ob.getString("title"));
                bookmark.setUrl(ob.getString("url"));
                bookmark.setImage(ob.getString("image"));
                pinsList.add(bookmark);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pinsList;
    }

    public static void saveBookmarks(ArrayList<PinItems> pinsList) {
        JSONArray array = new JSONArray();
        Iterator<PinItems> it = pinsList.iterator();
        if (it.hasNext()) {
            do {
                PinItems bookmark = it.next();
                JSONObject ob = new JSONObject();
                try {
                    ob.put("title", bookmark.getTitle());
                    ob.put("url", bookmark.getUrl());
                    ob.put("image", bookmark.getImage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(ob);
            } while (it.hasNext());
        }
        putString("simple_pins", array.toString());
    }


    public static ArrayList<UserItems> getUsers() {
        String users = getString("simple_users", "[]");
        ArrayList<UserItems> userList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(users);
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                UserItems user = new UserItems();
                user.setName(ob.getString("name"));
                user.setImage(ob.getString("image"));
                user.setCookie(ob.getString("cookie"));
                user.setUserID(ob.getString("userID"));
                user.setCoverPic(ob.getString("coverPic"));
                user.setUserPage(ob.getString("userPage"));
                userList.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userList;
    }

    public static void saveUsers(ArrayList<UserItems> userList) {
        JSONArray array = new JSONArray();
        for (UserItems bookmark : userList) {
            JSONObject ob = new JSONObject();
            try {
                ob.put("name", bookmark.getName());
                ob.put("image", bookmark.getImage());
                ob.put("cookie", bookmark.getCookie());
                ob.put("userID", bookmark.getUserID());
                ob.put("coverPic", bookmark.getCoverPic());
                ob.put("userPage", bookmark.getUserPage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(ob);
        }
        putString("simple_users", array.toString());
    }


    public static ArrayList<UserItemsOther> getUsersOther() {
        String users = getString("simple_users", "[]");
        ArrayList<UserItemsOther> userList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(users);
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = array.getJSONObject(i);
                UserItemsOther user = new UserItemsOther();
                user.setName(ob.getString("name"));
                user.setImage(ob.getString("image"));
                user.setCookie(ob.getString("cookie"));
                user.setUserID(ob.getString("userID"));
                user.setCoverPic(ob.getString("coverPic"));
                userList.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userList;
    }

    public static void saveUsersOther(ArrayList<UserItemsOther> userList) {
        JSONArray array = new JSONArray();
        Iterator<UserItemsOther> it = userList.iterator();
        if (it.hasNext()) {
            do {
                UserItemsOther userItems = it.next();
                JSONObject ob = new JSONObject();
                try {
                    ob.put("name", userItems.getName());
                    ob.put("image", userItems.getImage());
                    ob.put("cookie", userItems.getCookie());
                    ob.put("userID", userItems.getUserID());
                    ob.put("coverPic", userItems.getCoverPic());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(ob);
            } while (it.hasNext());
        }
        putString("simple_users", array.toString());
    }

    public static String getCookie() {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getString("cookie_key", null);
    }

    public static void setCookie(String str) {
        PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).edit().putString("cookie_key", str).apply();
    }


    public static boolean isUser(String bookmark) {
        if (bookmark == null || bookmark.isEmpty()) {
            return false;
        }
        String removePro = removeCover(bookmark);
        for (UserItems profile : getUsers()) {
            if (removeCover(profile.getUserPage()).equals(removePro)) {
                return true;
            }
        }
        return false;
    }

    private static String removeCover(String url) {
        return url.replaceFirst("^(http(?>s)://\\.|http(?>s)://)", "");
    }


    public static int getInt(String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getInt(key, defValue);
    }


    public static long getLastNotificationTime() {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getLong(LAST_NOTIFICATION_TIME, 0);
    }

    public static void setLastNotificationTime(long time) {
        PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).edit().putLong(LAST_NOTIFICATION_TIME, time).apply();
    }

    public static long getLastMessageTime() {
        return PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).getLong(LAST_MESSENGER_TIME, 0);
    }

    public static void setLastMessageTime(long time) {
        PreferenceManager.getDefaultSharedPreferences(SimpleApplication.getContextOfApplication()).edit().putLong(LAST_MESSENGER_TIME, time).apply();
    }

    public String getTheme() {
        return mPreferences.getString(THEME_PREFERENCE, "");
    }

    public String getFont() {
        return mPreferences.getString(FONT_SIZE, "");
    }

    public String getFeed() {
        return mPreferences.getString(NEWS_FEED, "");
    }

    public String getFreeTheme() {
        return mPreferences.getString(FACEBOOK_THEMES, "");
    }

    public String getBrowser() {
        return mPreferences.getString(GET_BROWSER, "");
    }

    public static ArrayList<String> getFavorites(Context context) {
        String bookmarks = getString(SIMPLE_PINS_STAR, "[]");
        ArrayList<String> listFavorites = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(bookmarks);
            for (int i = 0; i < array.length(); i++) {
                String fav = (String) array.get(i);
                listFavorites.add(fav);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listFavorites;
    }

    public static void saveFavorites(Context context, ArrayList<String> listFavorites) {
        JSONArray array = new JSONArray();
        for (String string : listFavorites) {
            array.put(string);
        }
        putString(SIMPLE_PINS_STAR, array.toString());
    }


    public static ArrayList<UserFiles> getUserDownloads() {
        ArrayList<UserFiles> filesList = new ArrayList<>();
        UserFiles f;
        String targetPath;
        if (PreferencesUtility.getBoolean("custom_pictures", false)) {
            targetPath = PreferencesUtility.getString("custom_directory", "");
        }else{
            targetPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "Simple";
        }
        File targetDirector = new File(targetPath);
        File[] files = targetDirector.listFiles();
        try {
            if (files != null) {
                Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    f = new UserFiles();
                    f.setName("Unknown file: " + (i + 1));
                    f.setFilename(file.getName());
                    f.setUri(Uri.fromFile(file));
                    f.setPath(files[i].getAbsolutePath());
                    filesList.add(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filesList;
    }
}