package com.creativetrends.simple.app.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.creativetrends.simple.app.adapters.AdapterListVideo;
import com.creativetrends.simple.app.adapters.ListItemsVideo;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.Sharer;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.video.FloatingVideoService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class VideoActivity extends AppCompatActivity {
    PlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;
    ProgressBar progressBar;
    public static String VIDEO_URL;
    String videoID;
    Toolbar toolbar;
    DefaultTimeBar timeBar;
    LinearLayout background;
    View saveButton;
    View shareButton;
    View copyButton;
    View openButton;
    MediaSource mediaSource;
    TextView pos, dur;
    String userAgent;
    long lastVideoPosition = 0;
    DataSource.Factory dataSourceFactory;
    DownloadManager downloadManager;
    private String filename;
    private File simple;
    String real;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder build;
    static long time = new Date().getTime();
    static String tmpStr = String.valueOf(time);
    static String last4Str = tmpStr.substring(tmpStr.length() -1);
    static int notificationId = Integer.parseInt(last4Str);
    TextView speed;
    ImageButton mute;
    ListPopupWindow popupWindow;
    public static long start;
    private static final int REQUEST_CODE_HOVER_PERMISSION = 1000;
    private boolean mPermissionsRequested = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        build = new NotificationCompat.Builder(this, getString(R.string.notification_widget_channel));
        setContentView(R.layout.activity_video);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_new);
            getSupportActionBar().setTitle(null);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        saveButton = findViewById(R.id.save_image);
        shareButton = findViewById(R.id.share_image);
        copyButton = findViewById(R.id.copy_video);
        openButton = findViewById(R.id.open_video);
        background = findViewById(R.id.player_back);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));

        exoPlayerView = findViewById(R.id.video_player);
        pos = findViewById(R.id.exo_position);
        dur = findViewById(R.id.exo_duration);
        progressBar = findViewById(R.id.video_progress);

        progressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.new_facebook)));

        View pip = findViewById(R.id.pip_button);
        pip.setOnClickListener(v -> addNewBubble());


        VIDEO_URL = getIntent().getStringExtra("VideoUrl");
        videoID = getIntent().getStringExtra("VideoName");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        timeBar = findViewById(R.id.exo_progress);
        mute = findViewById(R.id.mute_button);
        if (VIDEO_URL.contains(getResources().getString(R.string.live_feed))) {
            copyButton.setVisibility(View.GONE);
            openButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);

        }




        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        userAgent = System.getProperty("http.agent");
        dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);





        popupWindow = new ListPopupWindow(this);

        speed = findViewById(R.id.speed_start);
        speed.setOnClickListener(view -> {
            if(!isDestroyed() && popupWindow.isShowing()){
                popupWindow.dismiss();
            }else {
                showSpeed(speed);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VIDEO_URL == null) {
                    return;
                }
                if (!NetworkConnection.isConnected(VideoActivity.this)) {
                    return;
                }
                if (VIDEO_URL != null) {
                    ClipboardManager clipboard = (ClipboardManager) VideoActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getString(R.string.context_share_video), VIDEO_URL);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    StaticUtils.showSnackBar(VideoActivity.this, getString(R.string.content_copy_link_done));
                } else {
                    StaticUtils.showSnackBar(VideoActivity.this, getString(R.string.error) +" "+System.currentTimeMillis());

                }
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VIDEO_URL == null) {
                    return;
                }
                if (!NetworkConnection.isConnected(VideoActivity.this)) {
                    return;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(VIDEO_URL));
                    startActivity(intent);
                } catch (SecurityException | ActivityNotFoundException p) {
                    p.printStackTrace();
                }

            }
        });


        if(PreferencesUtility.getBoolean("play_mute", false)){
            exoPlayer.setVolume(0f);
            mute.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.ic_mute_button));
        }else{
            exoPlayer.setVolume(1f);
            mute.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.ic_unmute));
        }

        mute.setOnClickListener(view -> {
            if(exoPlayer.getVolume() == 0f){
                exoPlayer.setVolume(1f);
                mute.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.ic_unmute));
            }else{
                exoPlayer.setVolume(0f);
                mute.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, R.drawable.ic_mute_button));
            }
        });

        if (VIDEO_URL.contains(getResources().getString(R.string.live_feed))) {
            mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL), null, null);
        }else{
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL));
        }
        exoPlayerView.setPlayer(exoPlayer);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        if(!PreferencesUtility.getBoolean("close_video", false) && PreferencesUtility.getBoolean("replay_video", false)){
            exoPlayer.setRepeatMode(exoPlayer.REPEAT_MODE_ONE);
        }

        if(PreferencesUtility.getBoolean("always_show", false)){
            exoPlayerView.setControllerShowTimeoutMs(0);
            exoPlayerView.setControllerHideOnTouch(false);
        }else {
            exoPlayerView.setControllerVisibilityListener(i -> {
                if (i == 0) {
                    toolbar.setVisibility(View.VISIBLE);
                    showSystemUI();
                } else {
                    toolbar.setVisibility(View.INVISIBLE);
                    hideSystemUI();
                    if(!isDestroyed() && popupWindow != null && popupWindow.isShowing()){
                        popupWindow.dismiss();
                    }
                }
            });
        }
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    lastVideoPosition = exoPlayer.getContentPosition();
                }

                if (PreferencesUtility.getBoolean("close_video", false) && playbackState == Player.STATE_ENDED) {
                    onBackPressed();
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (error.getCause() instanceof FileDataSource.FileDataSourceException) {
                    try {
                        playVideo(videoID);
                        timeBar.setVisibility(View.GONE);
                        pos.setVisibility(View.GONE);
                        dur.setVisibility(View.GONE);
                        saveButton.setVisibility(View.GONE);
                        shareButton.setVisibility(View.GONE);
                    } catch (Exception p) {
                        p.printStackTrace();
                    }
                }else {
                    if (!isDestroyed()) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(VideoActivity.this);
                        adb.setTitle(getString(R.string.app_name_pro));
                        if (error.getCause() instanceof ParserException) {
                            adb.setMessage(getString(R.string.live_feed_ended));
                        } else if (error.getCause() instanceof ExoPlaybackException) {
                            adb.setMessage(getString(R.string.error_with_url));
                        } else if (error.getCause() instanceof DataSourceException) {
                            adb.setMessage(getString(R.string.error_with_url));
                        } else if (error.getCause() instanceof HttpDataSource.HttpDataSourceException) {
                            playVideo(videoID);
                        } else if (error.getCause() instanceof BehindLiveWindowException) {
                            if(exoPlayer != null && mediaSource != null) {
                                exoPlayer.prepare(mediaSource);
                                exoPlayer.setPlayWhenReady(true);
                            }
                        } else {
                            adb.setMessage(error.toString());
                        }
                        adb.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
                        AlertDialog ad = adb.create();
                        ad.show();
                    }
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        saveButton.setOnClickListener(v -> {
            try {
                if (VIDEO_URL == null) {
                    StaticUtils.showSnackBar(this, "Issue with this URL");
                    return;
                }
                if (!NetworkConnection.isConnected(VideoActivity.this)) {
                    StaticUtils.showSnackBar(this, "No network connection");
                } else if (NetworkConnection.isConnected(VideoActivity.this)) {
                    requestStoragePermission();
                }
            } catch (Exception i) {
                i.printStackTrace();
                StaticUtils.showSnackBar(this, i.toString());

            }

        });

        shareButton.setOnClickListener(v -> {
            try {
                if (VIDEO_URL == null) {
                    StaticUtils.showSnackBar(this, "Issue with this URL");
                    return;
                }
                if (!NetworkConnection.isConnected(VideoActivity.this)) {
                    StaticUtils.showSnackBar(this, "No network connection");
                } else if (NetworkConnection.isConnected(VideoActivity.this)) {
                    try {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, VIDEO_URL);
                        startActivity(Intent.createChooser(share, getResources().getString(R.string.context_share_video)));
                    } catch (ActivityNotFoundException ignored) {
                    } catch (Exception p) {
                        p.printStackTrace();
                        StaticUtils.showSnackBar(this, p.toString());
                    }
                }
            } catch (Exception i) {
                i.printStackTrace();
                StaticUtils.showSnackBar(this, i.toString());

            }
        });

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            adjustFullScreen(getResources().getConfiguration());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
    protected void onResume() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
        super.onResume();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    public void onPause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
        super.onPause();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferencesUtility.putString("needs_lock", "false");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.release();
        }
        super.onBackPressed();
        PreferencesUtility.putString("needs_lock", "false");

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                        VIDEO_URL = null;
                        return;
                    }
                   // new SimpleDownloader(this, this).execute(VIDEO_URL);
                    startDownload(VIDEO_URL);
                } else {
                    StaticUtils.showSnackBar(this, getString(R.string.permission_denied));
                }
            } catch (IllegalStateException ex) {
                StaticUtils.showSnackBar(this, getString(R.string.permission_denied));
            } catch (Exception exc) {
                StaticUtils.showSnackBar(this, exc.toString());
            }
        }
    }



    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (Objects.equals(i.getStringExtra("from"), "service")) {
            VIDEO_URL = i.getStringExtra("VideoUrl");
            String userAgent = System.getProperty("http.agent");
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
            if (VIDEO_URL.contains("/live-dash/")) {
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL));
            } else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL));
            }
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.seekTo(0, FloatingVideoService.currentTime);
            exoPlayer.setPlayWhenReady(true);
        }else {
            VIDEO_URL = i.getStringExtra("VideoUrl");
            String userAgent = System.getProperty("http.agent");
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
            if (VIDEO_URL.contains("/live-dash/")) {
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL));
            } else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(VIDEO_URL));
            }
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);
        }
    }

    private void startDownload(String url) {
        if (!Sharer.resolve(this)) {
            VIDEO_URL = null;
            return;
        }
        String appDirectoryName = "Simple";
        try {
            filename = getFileName(url);
            real = url;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +File.separator + appDirectoryName;
            simple = new File(path);
            if(!simple.exists()){
                //noinspection ResultOfMethodCallIgnored
                simple.mkdir();
            }
            File newFile = new File(simple + File.separator, filename);
            if (newFile.isFile()) {
                StaticUtils.showSnackBar(VideoActivity.this, "Video already downloaded.");
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
            Toast.makeText(VideoActivity.this, i.toString(), Toast.LENGTH_SHORT).show();

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

    private void adjustFullScreen(Configuration config) {
        final View decorView = getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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



    void playVideo(String vid){
        try {
            lastVideoPosition = exoPlayer.getCurrentPosition();
            if (VIDEO_URL.contains(getResources().getString(R.string.live_feed))) {
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(Uri.parse(vid));
            } else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(vid));
            }
            exoPlayerView.setPlayer(exoPlayer);
            exoPlayer.prepare(mediaSource);
            exoPlayer.seekTo(lastVideoPosition);
            exoPlayer.setPlayWhenReady(true);
        }catch (Exception p){
            p.printStackTrace();
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showNotification();
        }
    };


    private void showNotification(){
        try {
            if(!isDestroyed()) {
                String vidExtension = ".mp4";
                if (filename.contains(".3gp"))
                    vidExtension = ".3gp";

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
                if (filename != null && filename.contains(vidExtension)) {
                    Glide.with(this).asBitmap().load(files.toString()).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            build.setSmallIcon(R.drawable.ic_video_download);
                            build.setLargeIcon(Circle(resource));
                            build.setContentTitle(getString(R.string.video_downloaded) + " \u2022 " + getReadableFileSize(newFile.length()));
                            build.setContentText(filename);
                            build.setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(resource)
                                    .bigLargeIcon(null));
                            build.setColor(ContextCompat.getColor(VideoActivity.this, R.color.jorell_blue));
                            Notification notification = build.build();
                            mNotifyManager.notify(notificationId++, notification);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                    StaticUtils.showSnackBar(VideoActivity.this, getResources().getString(R.string.video_downloaded));
                } else {
                    build.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    build.setContentTitle(getString(R.string.file_downloaded));
                    Notification notification = build.build();
                    mNotifyManager.notify(notificationId++, notification);
                    StaticUtils.showSnackBar(VideoActivity.this, getResources().getString(R.string.file_downloaded));
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
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }


    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    private void showSpeed(View v) {
        List<ListItemsVideo> itemList = new ArrayList<>();
        itemList.add(new ListItemsVideo("0.25x"));
        itemList.add(new ListItemsVideo("0.50x"));
        itemList.add(new ListItemsVideo("0.75x"));
        itemList.add(new ListItemsVideo("1.00x"));
        itemList.add(new ListItemsVideo("1.25x"));
        itemList.add(new ListItemsVideo("1.50x"));
        itemList.add(new ListItemsVideo("1.75x"));
        itemList.add(new ListItemsVideo("2.00x"));
        ListAdapter adapter = new AdapterListVideo(this, itemList);
        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(v);
        popupWindow.setWidth(StaticUtils.measureContentWidth(adapter));
        popupWindow.setOnDismissListener(popupWindow::dismiss);
        popupWindow.setOnItemClickListener((adapterView, view, i, l) -> {
            popupWindow.dismiss();
            speed.setText(itemList.get(i).getTitle());
            try {
                itemList.get(i).setTitle(speed.getText().toString() + "\u2713");
            }catch (Exception p){
                p.printStackTrace();
            }
            switch (i) {
                case 0:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(0.25f));
                    break;

                case 1:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(0.50f));
                    break;

                case 2:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(0.75f));
                    break;

                case 3:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(1.00f));
                    break;

                case 4:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(1.25f));
                    break;

                case 5:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(1.50f));
                    break;

                case 6:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(1.75f));
                    break;

                case 7:
                    exoPlayer.setPlaybackParameters(new PlaybackParameters(2.00f));
                    break;

                default:
                    popupWindow.dismiss();
                    break;
            }

        });
        popupWindow.show();
    }


    private void setColor(int color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getStatusBarColor(), StaticUtils.adjustAlpha(color, 0.3f));
        colorAnimation.setDuration(0);
        colorAnimation.addUpdateListener(animator -> {
            background.getBackground().setColorFilter(new PorterDuffColorFilter(((int) animator.getAnimatedValue()), PorterDuff.Mode.SRC_IN));
            getWindow().setNavigationBarColor((int) animator.getAnimatedValue());
        });
        colorAnimation.start();
    }



    private void addNewBubble() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_HOVER_PERMISSION);
        }else {
            start = exoPlayer.getCurrentPosition();
            Intent chat = new Intent(getApplicationContext(), FloatingVideoService.class);
            if (StaticUtils.isOreo()) {
                startForegroundService(chat);
            } else {
                startService(chat);
            }
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            addNewBubble();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


   /* private void setTranslucentStatus() {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }*/


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
