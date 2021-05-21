package com.creativetrends.simple.app.adapters;

import android.net.Uri;

/**
 * Created by Tushar on 9/10/2017.
 */

public class UserFiles {
    private String name;
    private Uri uri;
    private String path;
    private String filename;

    public UserFiles() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
