package com.creativetrends.simple.app.adapters;

/**
 * Created by Creative Trends Apps on 10/19/2016.
 */

public class PinItems {
    private String title;
    private String url;
    private String image;
    private Boolean hascount;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getHascount() {
        return hascount;
    }

    public void setHascount(Boolean hascount) {
        this.hascount = hascount;
    }
}