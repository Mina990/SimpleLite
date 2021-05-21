package com.creativetrends.simple.app.interfaces;

import android.webkit.JavascriptInterface;

public class HtmlInterface {
    private final OnHtmlCallback onHtmlCallback;

    public HtmlInterface(OnHtmlCallback onHtmlCallback) {
        this.onHtmlCallback = onHtmlCallback;
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void handleHtml(String html) {
        onHtmlCallback.OnHtmlReceived(html);
    }
}
