/*
 * *
 *  * Created by Jorell Rutledge on 5/13/19 9:34 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 5/13/19 9:34 AM
 *
 */

package com.creativetrends.simple.app.utils;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FacebookStatusBugs extends AsyncTask<Void, Void, String> {
    private String latestVersion, issues, latest;
    public static boolean didCheck;
    public FacebookStatusBugs() {
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        didCheck = false;
    }

    @Override
    protected String doInBackground(Void[] params) {
        try {
            Document doc = Jsoup.connect("https://downdetector.com/status/facebook/").get();
            latestVersion = doc.select("div.h2.entry-title*").text();
            latest = doc.select("a.text-danger.d-block").text();
            Elements problems = doc.select("#indicators-card div.mx-auto");
            StringBuilder sb = new StringBuilder();
            for (Element problem : problems){
                sb.append("\u2022 ").append(problem.text()).append("<br/>");
                issues = sb.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        didCheck = true;
        PreferencesUtility.putString("face_stat", latestVersion);
        PreferencesUtility.putString("current_issues", issues);
        PreferencesUtility.putString("last_issue", latest);
    }


}