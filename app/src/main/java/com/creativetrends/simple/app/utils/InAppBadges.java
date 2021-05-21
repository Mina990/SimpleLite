/*
 * *
 *  * Created by Jorell Rutledge on 7/28/19 11:04 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 7/27/19 9:30 PM
 *
 */

package com.creativetrends.simple.app.utils;

import android.os.AsyncTask;
import android.webkit.CookieManager;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.MainActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 8/26/2018.
 */
public class InAppBadges extends AsyncTask<Void, Void, String> {
    public static String noCount, messCount, requestCount, feedCount, groupCount;
    Document document;

    @Override
    protected String doInBackground(Void[] params) {
        try {
            if(EUCheck.isEU(SimpleApplication.getContextOfApplication())){
                document = Jsoup.connect("https://m.facebook.com/").cookie(("https://m.facebook.com/"), CookieManager.getInstance().getCookie(("https://m.facebook.com"))).get();
                Element notifications = document.select("div#notifications_jewel").select("span._59tg").first();
                Element requests = document.select("div#requests_jewel").select("span._59tg").first();
                Element feed = document.select("div#feed_jewel").select("span._59tg").first();
                if (feed != null && !feed.text().equals("0")) {
                    feedCount = feed.text().replaceAll("[^\\d]", "");
                } else {
                    feedCount = "";
                }

                if (notifications != null && !notifications.text().equals("0")) {
                    noCount = notifications.text().replaceAll("[^\\d]", "");
                } else {
                    noCount = "";
                }

                if (requests != null && !requests.text().equals("0")) {
                    requestCount = requests.text().replaceAll("[^\\d]", "");
                } else {
                    requestCount = "";
                }

                //if (messages != null && !messages.text().equals("0")) {
                //    messCount = messages.text().replaceAll("[^\\d]", "");
                // } else {
                //     messCount = "";
                // }

            }else {
                document = Jsoup.connect("https://mbasic.facebook.com/").cookie(("https://m.facebook.com/"), CookieManager.getInstance().getCookie(("https://m.facebook.com"))).method(Connection.Method.GET).followRedirects(true).get();
                Element notifications = document.select("a[href*='/notifications.php?']").select("strong").first();
                Element messages = document.select("a[href*='/messages']").select("strong").first();
                Element requests = document.select("a[href*='/friends/center/']").select("strong").first();
                Element feed = document.select("a[href*='/home.php?']").select("strong").first();
                Element groups = document.select("a[href*='/groups/?']").select("strong").first();

                if (feed != null) {
                    feedCount = feed.text().replaceAll("[^\\d]", "");
                } else {
                    feedCount = "";
                }

                if (PreferencesUtility.getBoolean("show_group_count", false)) {
                    if (groups != null) {
                        String total = groups.text().replaceAll("[^\\d]", "");
                        int more = Integer.parseInt(total);
                        try {
                            if (!total.equals("") && more > 20) {
                                groupCount = "20+";
                            } else if (!total.equals("") && more < 20) {
                                groupCount = total;
                            }
                        } catch (NumberFormatException ignored) {
                            groupCount = total;
                        }
                    } else {
                        groupCount = "";
                    }
                }

                if (notifications != null) {
                    noCount = notifications.text().replaceAll("[^\\d]", "");
                } else {
                    noCount = "";
                }

                if (messages != null) {
                    messCount = messages.text().replaceAll("[^\\d]", "");
                } else {
                    messCount = "";
                }

                if (requests != null) {
                    requestCount = requests.text().replaceAll("[^\\d]", "");
                } else {
                    requestCount = "";
                }
            }

        } catch (IllegalArgumentException | NullPointerException | StringIndexOutOfBoundsException ignored) {
        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        try {
            ((MainActivity) MainActivity.getMainActivity()).refreshBadges();
        } catch (NullPointerException ignored) {
        } catch (IndexOutOfBoundsException i) {
            i.printStackTrace();
        }
    }
}