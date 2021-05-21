package com.creativetrends.simple.app.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.creativetrends.simple.app.adapters.UserItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;

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

            if (mName == null) {
                mName = document.title();
            }


            if (mPage == null) {
                mPage = document.location();
            }



            String p = document.getElementsByClass("br").select("div > a > img").attr("src");
            if (p == null) {
                mPicture = document.getElementsByClass("_5s61").select("div >  img").attr("src");
            } else if (p.isEmpty()) {
                mPicture = "https://graph.facebook.com/" + BadgeHelper.getCookie() + "/picture?type=large&access_token=784465452426381|7fa1ac1552c1d1fffec293c854cfa616";
            } else {
                mPicture = p;
            }




        } catch (IllegalArgumentException | NullPointerException ignored) {
        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        if (mPicture != null) {
            PreferencesUtility.putString("user_picture", mPicture);
        }

        if (mName != null) {
            PreferencesUtility.putString("user_name", mName);
        }

        new Handler().postDelayed(this::addUser, 2000);
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
                Log.d("Ignore!", "User already there.");
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
                Log.d("Add!", "its ok to add user.");
            }else{
                Log.d("Ignore!", "User already there.");
            }
        } catch (Exception ignored) {
        }

    }
}