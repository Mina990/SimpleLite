package com.creativetrends.simple.app.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.CookieManager;

import com.creativetrends.simple.app.adapters.UserItems;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 8/26/2018.
 */
public class UserInfo extends AsyncTask<Void, Void, String> {
    private String mName;
    private String mPicture;
    private String mPage;
    Document document;

    public UserInfo() {
        super();

    }

    @Override
    protected String doInBackground(Void[] params) {
        try {
            document = Jsoup.connect("https://mbasic.facebook.com/me").cookie(("https://m.facebook.com/"), CookieManager.getInstance().getCookie(("https://m.facebook.com/"))).timeout(600000).get();


            if(mName == null) {
                mName = document.title();
            }

            if(mPage == null) {
                mPage = document.location();
            }

            if (mPicture == null) {
                mPicture = document.select("div#m-timeline-cover-section").select("img").eq(1).attr("src");
            }


        } catch (IllegalArgumentException | NullPointerException ignored) {
        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        try {

            if (mPicture != null) {
                PreferencesUtility.putString("user_picture", mPicture);
            }

            if (mName != null) {
                PreferencesUtility.putString("user_name", mName);
            }

            new Handler().postDelayed(this::addUser, 2000);


        } catch (IllegalArgumentException | NullPointerException ignored) {
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void addUser() {
        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        String cookie = instance.getCookie("https://touch.facebook.com");
        if (TextUtils.isEmpty(cookie)) {
            cookie = instance.getCookie("https://www.facebook.com");
        }
        if (TextUtils.isEmpty(cookie)) {
            cookie = instance.getCookie("https://0.facebook.com");
        }
        if (TextUtils.isEmpty(cookie)) {
            cookie = instance.getCookie("https://m.facebook.com");
        }
        String string;
        try {
            int indexOf = cookie.indexOf("c_user=") + "c_user=".length();
            string = cookie.substring(indexOf, cookie.indexOf(";", indexOf));
            if (PreferencesUtility.getString("user_name", "").contains("Facebook") || PreferencesUtility.getString("user_name", "").isEmpty()) {
                //noinspection UnnecessaryReturnStatement
                return;
            } else if (!PreferencesUtility.isUser(mPage)) {
                ArrayList<UserItems> newList = PreferencesUtility.getUsers();
                UserItems user = new UserItems();
                user.setName(mName);
                user.setCookie(cookie);
                user.setImage(mPicture);
                user.setUserID(string);
                user.setCoverPic("");
                user.setUserPage(mPage);
                newList.add(user);
                PreferencesUtility.saveUsers(newList);
            }
        } catch (Exception ignored) {
        }

    }
}
