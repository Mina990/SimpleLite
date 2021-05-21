package com.creativetrends.simple.app.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.activities.PhotoActivity;

/**
 * Created by Creative Trends Apps.
 */
public class ImageInterface {
    Activity mActivity;

    public ImageInterface(Activity activity) {
        mActivity = activity;
    }

    private void loadPhoto(String image) {
        Intent intent = new Intent(mActivity, PhotoActivity.class);
        intent.putExtra("url", image);
        intent.putExtra("page", "simple");
        mActivity.startActivity(intent);
    }

    private void loadPhotoLink(String image, String page) {
        Intent intent = new Intent(mActivity, PhotoActivity.class);
        intent.putExtra("url", image);
        intent.putExtra("page", page);
        mActivity.startActivity(intent);
    }

    private void loadPage(String page) {
        Intent intent = new Intent(mActivity, NewPageActivity.class);
        intent.putExtra("url", page);
        mActivity.startActivity(intent);
    }

    @JavascriptInterface
    public void getImage(String image) {
        loadPhoto(image);
    }

    @JavascriptInterface
    public void getImageLink(String image, String page) {
        loadPhotoLink(image, page);
    }

    @JavascriptInterface
    public void getPage(String page) {
        loadPage(page);
    }
}
