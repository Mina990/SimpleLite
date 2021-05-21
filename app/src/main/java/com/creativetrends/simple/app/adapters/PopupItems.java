package com.creativetrends.simple.app.adapters;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 5/11/2018.
 */
public class PopupItems {
    private String title;
    int imageRes;

    PopupItems(String title, int imageRes) {
        this.title = title;
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    int getImageRes() {
        return imageRes;
    }


}