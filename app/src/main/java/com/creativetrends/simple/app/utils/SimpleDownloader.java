/*
 * *
 *  * Created by Jorell Rutledge on 8/6/19 9:53 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 8/6/19 9:53 PM
 *
 */

package com.creativetrends.simple.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static com.creativetrends.simple.app.utils.FileUtils.getReadableFileSize;
import static com.creativetrends.simple.app.utils.StaticUtils.getFileSize;


public class SimpleDownloader extends AsyncTask<String, Float, String> {
    private final int id = 101;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    @SuppressLint("StaticFieldLeak")
    private final Activity activity;
    private final NotificationManager mNotifyManager;
    private final NotificationCompat.Builder build;
    int fileLength;
    float total = 0;
    long totalOther = 0;
    private String filename;
    private File simple;
    String real;

    @SuppressWarnings("deprecation")
    public SimpleDownloader(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(context, context.getString(R.string.notification_widget_channel));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        build.setProgress(0, 0, true);
        build.setAutoCancel(true);
        build.setContentTitle(context.getString(R.string.fragment_main_downloading));
        build.setSmallIcon(android.R.drawable.stat_sys_download);
        build.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.stat_sys_download)).build();
        build.setWhen(System.currentTimeMillis());
        build.setColor(ContextCompat.getColor(context, R.color.jorell_blue));

    }

    @Override
    protected String doInBackground(String... string) {

        try {
            String path = PreferencesUtility.getString("custom_directory", "");
            if (!path.contains("emulated")) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "Simple";
            }

            InputStream input;
            OutputStream output;
            URL url = new URL(string[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            if (PreferencesUtility.getBoolean("custom_pictures", false)) {
                simple = new File(path);
            } else {
                simple = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Simple");
            }
            if (!simple.exists()) {
                //noinspection ResultOfMethodCallIgnored
                simple.mkdirs();
            }

            real = string[0];
            String imgExtension = ".jpg";
            if (string[0].contains(".gif"))
                imgExtension = ".gif";
            else if (string[0].contains(".png"))
                imgExtension = ".png";
            String vidExtension = ".mp4";
            if (string[0].contains(".3gp"))
                vidExtension = ".3gp";

            if (PreferencesUtility.getBoolean("rename", false) && string[0].contains(imgExtension)) {
                filename = PreferencesUtility.getString("image_name", "") + imgExtension;
            } else if (!PreferencesUtility.getBoolean("rename", false) && string[0].contains(imgExtension)) {
                filename = StaticUtils.getFileName(string[0]);
            } else if (PreferencesUtility.getBoolean("rename", false) && string[0].contains(vidExtension)) {
                filename = PreferencesUtility.getString("video_name", "") + vidExtension;
            } else if (!PreferencesUtility.getBoolean("rename", false) && string[0].contains(vidExtension)) {
                filename = StaticUtils.getFileName(string[0]);
            } else {
                filename = URLUtil.guessFileName(string[0], null, Helpers.getMimeType(Uri.parse(string[0]).toString()));
            }

            File newFile = new File(simple + File.separator, filename);
            if (newFile.exists()) {
                if(filename.contains(imgExtension)) {
                    StaticUtils.showSnackBar(activity, "Image already downloaded.");
                }else if(filename.contains(vidExtension)){
                    StaticUtils.showSnackBar(activity, "Video already downloaded.");
                }else{
                    StaticUtils.showSnackBar(activity, "File already downloaded.");
                }
                cancel(true);
            } else {
                mNotifyManager.notify(id, build.build());
                input = connection.getInputStream();
                output = new FileOutputStream(simple + File.separator + filename);
                byte[] data = new byte[0x2000];
                int count, latestPercentDone;
                int percentDone = -1;
                while ((count = input.read(data, 0, 0x2000)) != -1) {
                    total = total + count;
                    totalOther +=count;
                    latestPercentDone = (int) Math.round(total / fileLength * 100.0);
                    if (percentDone != latestPercentDone) {
                        percentDone = latestPercentDone;
                        publishProgress(total * 100f / fileLength);
                    }
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            }

        } catch (Exception e) {
            Log.e("Error ", e.toString());
            return null;
        }

        return filename;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            if (s == null) {
                mNotifyManager.cancel(id);
                build.setContentText(context.getResources().getString(android.R.string.httpErrorBadUrl))
                        .setContentTitle(context.getResources().getString(R.string.downloader, context.getResources().getString(R.string.app_name_pro)))
                        .setShowWhen(true)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setTimeoutAfter(0)
                        .setProgress(0, 0, false)
                        .setColor(ContextCompat.getColor(context, R.color.jorell_blue))
                        .setSmallIcon(R.drawable.ic_download_fail);
                Notification notification = build.build();
                mNotifyManager.notify(id, notification);
            } else {
                try {
                    String imgExtension = ".jpg";
                    if (filename.contains(".gif"))
                        imgExtension = ".gif";
                    else if (filename.contains(".png"))
                        imgExtension = ".png";
                    else if (filename.contains(".3gp"))
                        imgExtension = ".3gp";
                    String vidExtension = ".mp4";
                    if (filename.contains(".3gp"))
                        vidExtension = ".3gp";

                    mNotifyManager.cancel(id);
                    File newFile = new File(simple + File.separator, filename);
                    Uri files = FileProvider.getUriForFile(context, context.getResources().getString(R.string.auth), newFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(files, getMimeType(files));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(context.getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    build.setContentIntent(resultPendingIntent)
                            .setShowWhen(true)
                            .setWhen(System.currentTimeMillis())
                            .setContentText(context.getString(R.string.tap_to_view))
                            .setAutoCancel(true)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_s))
                            .setColor(ThemeUtils.getColorPrimary(context))
                            .setProgress(0, 0, false);
                    if (filename != null && filename.contains(imgExtension)) {
                        Glide.with(context).asBitmap().load(real).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                build.setSmallIcon(R.drawable.ic_image_download);
                                build.setLargeIcon(Circle(resource));
                                build.setContentTitle(context.getString(R.string.image_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                                build.setContentText(filename);
                                build.setStyle(new NotificationCompat.BigPictureStyle()
                                        .bigPicture(resource)
                                        .bigLargeIcon(null));
                                build.setColor(ContextCompat.getColor(context, R.color.jorell_blue));
                                Notification notification = build.build();
                                mNotifyManager.notify(id, notification);
                                StaticUtils.showSnackBar(activity, context.getResources().getString(R.string.image_downloaded));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    } else if (filename != null && filename.contains(vidExtension)) {
                        Glide.with(context).asBitmap().load(files.toString()).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                build.setSmallIcon(R.drawable.ic_video_download);
                                build.setLargeIcon(Circle(resource));
                                build.setContentTitle(context.getString(R.string.video_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                                build.setContentText(filename);
                                build.setStyle(new NotificationCompat.BigPictureStyle()
                                        .bigPicture(resource)
                                        .bigLargeIcon(null));
                                build.setColor(ContextCompat.getColor(context, R.color.jorell_blue));
                                Notification notification = build.build();
                                mNotifyManager.notify(id, notification);
                                StaticUtils.showSnackBar(activity, context.getResources().getString(R.string.video_downloaded));
                            }
                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    } else {
                        build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                        build.setContentTitle(context.getString(R.string.file_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                        build.setContentText(filename);
                        build.setColor(ContextCompat.getColor(context, R.color.jorell_blue));
                        Notification notification = build.build();
                        mNotifyManager.notify(id, notification);
                        StaticUtils.showSnackBar(activity, context.getResources().getString(R.string.file_downloaded));
                    }
                    MediaScannerConnection.scanFile(context, new String[]{simple + File.separator + filename}, null, (newpath, newuri) -> Log.i("Saved and scanned to", newpath));
                } catch (Exception p) {
                    p.printStackTrace();
                    String imgExtension = ".jpg";
                    if (filename.contains(".gif"))
                        imgExtension = ".gif";
                    else if (filename.contains(".png"))
                        imgExtension = ".png";
                    else if (filename.contains(".3gp"))
                        imgExtension = ".3gp";
                    String vidExtension = ".mp4";
                    if (filename.contains(".3gp"))
                        vidExtension = ".3gp";
                    Bitmap avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_s);
                    if (filename != null && filename.contains(imgExtension)) {
                        build.setSmallIcon(R.drawable.ic_image_download);
                        build.setLargeIcon(avatar);
                        build.setContentTitle(context.getString(R.string.image_downloaded));
                        build.setContentText(context.getString(R.string.tap_to_view));
                    } else if (filename != null && filename.contains(vidExtension)) {
                        build.setSmallIcon(R.drawable.ic_video_download);
                        build.setLargeIcon(avatar);
                        build.setContentTitle(context.getString(R.string.video_downloaded));
                        build.setContentText(context.getString(R.string.tap_to_view));
                    } else {
                        build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                        build.setLargeIcon(avatar);
                        build.setContentTitle(context.getString(R.string.file_downloaded));
                        build.setContentText(context.getString(R.string.tap_to_view));
                    }
                    build.setColor(ContextCompat.getColor(context, R.color.jorell_blue));
                    Notification notification = build.build();
                    mNotifyManager.notify(id, notification);
                    if (filename != null && filename.contains(imgExtension)) {
                        Toast.makeText(context, context.getResources().getString(R.string.image_downloaded), Toast.LENGTH_SHORT).show();
                    } else if (filename != null && filename.contains(vidExtension)) {
                        Toast.makeText(context, context.getResources().getString(R.string.video_downloaded), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.file_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception p){
            p.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(@NonNull final Float... values) {
        final int dl_progress = (int) ((double) total / (double) fileLength * 100f);
        if(build != null) {
            build.setProgress(100, values[0].intValue(), false);
            build.setContentText(getFileSize(totalOther) + "/" + getFileSize(fileLength) + " " + dl_progress + "%");
            mNotifyManager.notify(id, build.build());
        }
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }




    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    @SuppressWarnings({"SuspiciousNameCombination", "IntegerDivisionInFloatingPointContext"})
    public static Bitmap Circle(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);


        return output;
    }
}
