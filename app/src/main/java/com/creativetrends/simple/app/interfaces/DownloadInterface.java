package com.creativetrends.simple.app.interfaces;


import android.webkit.JavascriptInterface;

public class DownloadInterface {
    private final OnPhotoPageCheck onPhotoPageCheck;

    public DownloadInterface(OnPhotoPageCheck onPhotoPageCheck) {
        this.onPhotoPageCheck = onPhotoPageCheck;
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void getPhotoUrl(String html) {
        onPhotoPageCheck.OnPhotoUrl(html);
    }


}

