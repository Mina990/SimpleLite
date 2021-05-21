package com.creativetrends.simple.app.interfaces;

public interface OnHtmlCallback {
    void OnHtmlReceived(String str);

    @SuppressWarnings("unused")
    void OnPhotoHtml(String str);
}
