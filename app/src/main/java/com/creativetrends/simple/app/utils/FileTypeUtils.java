/*
 * *
 *  * Created by Jorell Rutledge on 6/6/19 6:48 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 6/6/19 10:59 AM
 *
 */

package com.creativetrends.simple.app.utils;

import android.webkit.MimeTypeMap;

import com.creativetrends.simple.app.lite.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Creative Trends Apps on 25.10.15.
 */

public class FileTypeUtils {
    private static final Map<String, FileType> fileTypeExtensions = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }

        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
        }

        return FileType.DOCUMENT;
    }

    private static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }

    public enum FileType {
        DIRECTORY(R.drawable.ic_folder),
        DOCUMENT(R.drawable.ic_file),
        CERTIFICATE(R.drawable.ic_certif, "cer", "der", "pfx", "p12", "arm", "pem"),
        DRAWING(R.drawable.ic_drawing, "ai", "cdr", "dfx", "eps", "svg", "stl", "wmf", "emf", "art", "xar"),
        EXCEL(R.drawable.ic_excel, "xls", "xlk", "xlsb", "xlsm", "xlsx", "xlr", "xltm", "xlw", "numbers", "ods", "ots"),
        IMAGE(R.drawable.ic_image, "bmp", "gif", "ico", "jpeg", "jpg", "pcx", "png", "psd", "tga", "tiff", "tif", "xcf"),
        MUSIC(R.drawable.ic_music, "aiff", "aif", "wav", "flac", "m4a", "wma", "amr", "mp2", "mp3", "wma", "aac", "mid", "m3u", "ogg"),
        VIDEO(R.drawable.ic_video_file, "avi", "mov", "wmv", "mkv", "3gp", "f4v", "flv", "mp4", "mpeg", "webm"),
        PDF(R.drawable.ic_pdf, "pdf"),
        POWER_POINT(R.drawable.ic_powerpoint, "pptx", "keynote", "ppt", "pps", "pot", "odp", "otp"),
        WEB(R.drawable.ic_web, "html", "htm", "mth"),
        WORD(R.drawable.ic_word, "doc", "docm", "docx", "dot", "mcw", "rtf", "pages", "odt", "ott"),
        APK(R.drawable.ic_apk, "apk", "aab"),
        ARCHIVE(R.drawable.ic_zip, "cab", "7z", "alz", "arj", "bzip2", "bz2", "dmg", "gzip", "gz", "jar", "lz", "lzip", "lzma", "zip", "rar", "tar", "tgz"),
        ;

        private final int icon;
        private final String[] extensions;

        FileType(int icon, String... extensions) {
            this.icon = icon;
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public int getIcon() {
            return icon;
        }


    }
}
