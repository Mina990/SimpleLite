package com.creativetrends.simple.app.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.ImageSharer;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.Sharer;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 4/8/2018.
 */
public class PhotoActivity extends AppCompatActivity {
    public Toolbar toolbar;
    boolean isDownloadPhoto = false;
    SharedPreferences preferences;
    EditText pt;
    RelativeLayout pic_back;
    View saveButton;
    View shareButton;
    View copyButton;
    View openButton;
    LinearLayout viewButtons;
    ProgressBar mProgressbar;
    boolean isLoaded = false;
    String genericImage;
    public static String pageUrl;
    String shareRandom;
    PhotoView fullImage;
    WebView webView;
    DownloadManager downloadManager;
    private String filename;
    private File simple;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder build;
    static long time = new Date().getTime();
    static String tmpStr = String.valueOf(time);
    static String last4Str = tmpStr.substring(tmpStr.length() -1);
    static int notificationId = Integer.parseInt(last4Str);

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(this, getString(R.string.notification_widget_channel));
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_photo);
        fullImage = findViewById(R.id.empty_image);
        pageUrl = getIntent().getStringExtra("page");
        saveButton = findViewById(R.id.save_image);
        copyButton = findViewById(R.id.copy_photo);
        openButton = findViewById(R.id.open_photo);
        shareButton = findViewById(R.id.share_image);

        viewButtons = findViewById(R.id.len);


        toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setTitle(null);
        }

        pt = new EditText(this);
        genericImage = getIntent().getStringExtra("url");
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                genericImage = url;
                loadImage();
            }
        });
        webView.loadUrl(genericImage);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        shareRandom = UUID.randomUUID() + ".png";
        mProgressbar = findViewById(R.id.progress_photo);
        mProgressbar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.m_color)));
        pic_back = findViewById(R.id.rel_pic);

        fullImage.setOnClickListener(view -> {
            if (toolbar.getVisibility() == View.VISIBLE || viewButtons.getVisibility() == View.VISIBLE ) {
                hideMenu();
                hideSystemUI();
            } else {
                showMenu();
                showSystemUI();
            }

        });





        saveButton.setOnClickListener(v -> {
            try {
                if (!NetworkConnection.isConnected(PhotoActivity.this)) {
                    return;
                }
                isDownloadPhoto = true;
                requestStoragePermission();
            } catch (Exception i) {
                i.printStackTrace();
            }

        });


        copyButton.setOnClickListener(view -> {
            try {
            if (genericImage == null) {
                return;
            }
            if (!NetworkConnection.isConnected(PhotoActivity.this)) {
                return;
            }
                if (genericImage != null) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Photo URL", genericImage);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(PhotoActivity.this, getResources().getString(R.string.content_copy_link_done), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception i) {
                i.printStackTrace();

            }
        });


        openButton.setOnClickListener(view -> {
            try {
            if (genericImage == null) {
                return;
            }
            if (!NetworkConnection.isConnected(PhotoActivity.this)) {
                return;
            }
                if (genericImage != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(genericImage));
                    startActivity(intent);
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        });


        shareButton.setOnClickListener(v -> {
            if (genericImage == null) {
                return;
            }
            if (!NetworkConnection.isConnected(PhotoActivity.this)) {
                return;
            }

            if (!BadgeHelper.hasStoragePermission(PhotoActivity.this)) {
                BadgeHelper.requestStoragePermission(PhotoActivity.this);
            } else if (isLoaded) {
                Toast.makeText(PhotoActivity.this, getResources().getString(R.string.context_share_image_progress), Toast.LENGTH_SHORT).show();
                ImageSharer shareImageDownloader = new ImageSharer(new ImageSharer.OnImageLoaderListener() {
                    @Override
                    public void onError(ImageSharer.ImageError error) {
                        Toast.makeText(PhotoActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChange(int percent) {

                    }

                    @Override
                    public void onComplete(Bitmap result) {
                        try {
                            String filename = "bitmap.png";
                            FileOutputStream stream = PhotoActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);
                            result.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(), result, getString(R.string.app_name_pro), null);

                            Uri uri = Uri.parse(path);
                            stream.close();
                            result.recycle();

                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("image/*");
                            share.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(share, getResources().getString(R.string.context_share_image)));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PhotoActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                shareImageDownloader.download(genericImage, false);
            }
        });

    }


    void loadImage() {
        if(!isDestroyed()) {
            Glide.with(this)
                    .asBitmap()
                    .load(genericImage)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            fullImage.setImageBitmap(resource);
                            if (!isLoaded) {
                                fullImage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                                mProgressbar.setVisibility(View.INVISIBLE);
                                viewButtons.setVisibility(View.VISIBLE);
                                setColor(StaticUtils.darkColorOther(Palette.from(resource).generate().getVibrantColor(Palette.from(resource).generate().getLightVibrantColor(Palette.from(resource).generate().getDarkVibrantColor(Palette.from(resource).generate().getDarkMutedColor(Palette.from(resource).generate().getLightMutedColor(Palette.from(resource).generate().getDarkMutedColor(Palette.from(resource).generate().getDominantColor(ThemeUtils.getColorPrimary(PhotoActivity.this))))))))));
                                isLoaded = true;
                            }

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

        }


    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferencesUtility.putString("needs_lock", "false");
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d("Photos", "onDetachedFromWindow: Detaching...");
        super.onDetachedFromWindow();
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.destroy();
            webView.removeAllViews();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(onComplete);
        super.onDestroy();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            try {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!Sharer.resolve(this)) {
                        genericImage = null;
                        return;
                    }
                    //new SimpleDownloader(this, this).execute(genericImage);
                    startDownload(genericImage);

                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalStateException ex) {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            } catch (Exception exc) {
                Toast.makeText(this, exc.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void startDownload(String url) {
        if (!Sharer.resolve(this)) {
            genericImage = null;
            return;
        }
        String appDirectoryName = "Simple";
        try {
            filename = getFileName(url);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +File.separator + appDirectoryName;
            simple = new File(path);
            if(!simple.exists()){
                //noinspection ResultOfMethodCallIgnored
                simple.mkdir();
            }
            File newFile = new File(simple + File.separator, filename);
            if (newFile.isFile()) {
                StaticUtils.showSnackBar(PhotoActivity.this, "Image already downloaded.");
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setDestinationUri(Uri.parse("file://" + path + File.separator + filename));
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                request.allowScanningByMediaScanner();
                downloadManager.enqueue(request);
            }
        } catch (Exception i) {
            i.printStackTrace();
            Toast.makeText(PhotoActivity.this, i.toString(), Toast.LENGTH_SHORT).show();

        }
    }

    public static String getFileName(String url) {
        int index = url.indexOf("?");
        if (index > -1) {
            url = url.substring(0, index);
        }
        url = url.toLowerCase();

        index = url.lastIndexOf("/");
        if (index > -1) {
            return url.substring(index + 1);
        } else {
            return Long.toString(System.currentTimeMillis());
        }
    }

    private void hideMenu() {
        toolbar.setVisibility(View.INVISIBLE);
        viewButtons.setVisibility(View.INVISIBLE);

    }

    private void showMenu() {
        toolbar.setVisibility(View.VISIBLE);
        viewButtons.setVisibility(View.VISIBLE);

    }


    private void adjustFullScreen(Configuration config) {
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideMenu();
            hideSystemUI();
        } else {
            showMenu();
            showSystemUI();
        }
    }


    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }



    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showNotification();
        }
    };


    private void showNotification(){
        try {
            if(!isDestroyed()) {
                String imgExtension = ".jpg";
                if (filename.contains(".gif"))
                    imgExtension = ".gif";
                else if (filename.contains(".png"))
                    imgExtension = ".png";

                File newFile = new File(simple + File.separator, filename);
                Uri files = FileProvider.getUriForFile(this, getResources().getString(R.string.auth), newFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(files, getMimeType(files));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                build.setContentIntent(resultPendingIntent)
                        .setShowWhen(true)
                        .setWhen(System.currentTimeMillis())
                        .setContentText(getString(R.string.tap_to_view))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_s))
                        .setColor(ThemeUtils.getColorPrimary(this));
                if (filename != null && filename.contains(imgExtension)) {
                    Glide.with(this).asBitmap().load(Uri.fromFile(newFile)).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            build.setSmallIcon(R.drawable.ic_image_download);
                            build.setLargeIcon(Circle(resource));
                            build.setContentTitle(getString(R.string.image_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                            build.setContentText(filename);
                            build.setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(resource)
                                    .bigLargeIcon(null));
                            build.setColor(ContextCompat.getColor(PhotoActivity.this, R.color.jorell_blue));
                            Notification notification = build.build();
                            mNotifyManager.notify(notificationId++, notification);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                    StaticUtils.showSnackBar(PhotoActivity.this, getResources().getString(R.string.image_downloaded));
                } else {
                    build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    build.setContentTitle(getString(R.string.file_downloaded));
                    Notification notification = build.build();
                    mNotifyManager.notify(notificationId++, notification);
                    StaticUtils.showSnackBar(PhotoActivity.this, getResources().getString(R.string.file_downloaded));
                }
                MediaScannerConnection.scanFile(this, new String[]{simple + File.separator + filename}, null, (newpath, newuri) -> Log.i("Saved and scanned to", newpath));
            }
        } catch (Exception p) {
            p.printStackTrace();

        }
    }

    private String getMimeType(Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }


    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private void setVideoColor(LinearLayout linearLayout) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            linearLayout.setBackgroundColor(StaticUtils.adjustAlpha(ContextCompat.getColor(this, R.color.black), 0.3f));
        } else {
            switch (PreferencesUtility.getInstance(this).getFreeTheme()) {
                case "draculatheme":
                case "darktheme":
                    linearLayout.getBackground().setColorFilter(new PorterDuffColorFilter(StaticUtils.adjustAlpha(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()), 0.3f), PorterDuff.Mode.SRC_IN));
                    break;
                case "amoledtheme":
                    linearLayout.getBackground().setColorFilter(new PorterDuffColorFilter(StaticUtils.adjustAlpha(ContextCompat.getColor(this, R.color.black), 0.3f), PorterDuff.Mode.SRC_IN));
                    break;
                default:
                    linearLayout.getBackground().setColorFilter(new PorterDuffColorFilter(StaticUtils.adjustAlpha(ThemeUtils.getColorPrimary(this), 0.3f), PorterDuff.Mode.SRC_IN));
                    break;
            }

        }
    }

    private void setColor(int color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), StaticUtils.adjustAlpha(color, 0.5f));
        colorAnimation.setDuration(100);
        colorAnimation.addUpdateListener(animator -> {
            viewButtons.getBackground().setColorFilter(new PorterDuffColorFilter(((int) animator.getAnimatedValue()), PorterDuff.Mode.SRC_IN));
            getWindow().setNavigationBarColor(StaticUtils.darkColorTheme((int) animator.getAnimatedValue()));
        });
        colorAnimation.start();
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