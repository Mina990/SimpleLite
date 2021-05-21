package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.creativetrends.simple.app.adapters.SearchAdapter;
import com.creativetrends.simple.app.adapters.SearchItems;
import com.creativetrends.simple.app.adapters.ViewPagerAdapter;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.services.NotificationService;
import com.creativetrends.simple.app.simplelock.SimpleLock;
import com.creativetrends.simple.app.ui.CustomViewPager;
import com.creativetrends.simple.app.utils.EUCheck;
import com.creativetrends.simple.app.utils.InAppBadges;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.utils.UserInfo;
import com.google.android.material.appbar.AppBarLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends OtherBaseActivity implements  SearchAdapter.onSearchSelected {
    //we can call on our main activity when needed
    @SuppressLint("StaticFieldLeak")
    public static Activity mainActivity;



    //our toolbar and appbar (appbar must remain static for out feed tab)

    public static AppBarLayout appBarLayout;
    public Toolbar toolbar;

    //our bottom navigation bar using a library
    @SuppressLint("StaticFieldLeak")
    public static AHBottomNavigation bottomNavigationView;
    AHBottomNavigationAdapter bottomNavigationAdapter;



    //imageview for fb watch
    ImageView image;



    //"overflow menu"
    public CardView overflowMenu;
    private FrameLayout menuHolder;
    TextView textView1, textView2;
    boolean isTop;



    //search view and search history
    FrameLayout searchCard;
    RecyclerView search_recycler;
    ArrayList<SearchItems> arrayList = new ArrayList<>();
    CardView cardView;
    SearchView searchView;
    LinearLayout mFilters;
    RelativeLayout mMore;
    TextView mMoreTitle;
    TextView searchText;
    LinearLayout cards;
    public boolean searchInitialized = false;
    public SearchAdapter searchAdapter;



    //adapter and viewpager
    public static ViewPagerAdapter adapter;
    public static CustomViewPager viewPager;


    //notifications
    WorkManager mWorkManager;


    //other items we need
    public static boolean isPicture;
    public static Uri galleryImageUri = null;
    private static long back_pressed;


    //set up our main on click listener
    private final View.OnClickListener SimpleClicker = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.update_lin:
                   hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://play.google.com/apps"));
                            startActivity(i);
                        }catch (ActivityNotFoundException i){
                            i.printStackTrace();
                            StaticUtils.showSnackBar(MainActivity.this, i.toString());
                        }
                    }
                    break;
                case R.id.search_layout:
                case R.id.search_back:
                    closeSearch();
                    break;

                case R.id.filter_people_check:
                    ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                    break;

                case R.id.filter_pages_check:
                    ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                    break;

                case R.id.filter_events_check:
                    ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
                    break;

                case R.id.filter_groups_check:
                    ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
                    ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
                    break;


                case R.id.toolbar_search:
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");

                    } else {
                        openSearch();
                    }
                    break;

                case R.id.toolbar_drawer:
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    }else{
                       loadPage("https://m.facebook.com/watch");
                    }
                    break;

                case R.id.toolbar_messages:
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else {
                        if(EUCheck.isEU(getApplicationContext())) {
                            Intent mess = new Intent(MainActivity.this, MessageActivity.class);
                            mess.putExtra("url", "https://messenger.com/t/71753464427175346442");
                            startActivity(mess);
                        }else{
                            loadPage("https://m.facebook.com/messages");
                        }
                    }
                    break;


                case R.id.menu_holder:
                    hideOverFlow();
                    break;

                case R.id.toolbar_market:
                    showOverFlow();
                    break;

                case R.id.text1:
                    hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else if (bottomNavigationView.getCurrentItem() == 0) {
                        setRecent();
                        isTop = false;
                        PreferencesUtility.putBoolean("top_news", false);
                        textView1.setTextColor(setFeedColor(MainActivity.this));
                        textView2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.m_color));
                    } else if (bottomNavigationView.getCurrentItem() == 1) {
                        setDiscover();
                        textView1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.m_color));
                    } else if (bottomNavigationView.getCurrentItem() == 2) {
                        setNotifications();
                        textView1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.m_color));
                    }
                    break;

                case R.id.text2:
                    hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else if (bottomNavigationView.getCurrentItem() == 0) {
                        setTop();
                        isTop = true;
                        PreferencesUtility.putBoolean("top_news", true);
                        textView1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.m_color));
                        textView2.setTextColor(setFeedColor(MainActivity.this));
                    } else if (bottomNavigationView.getCurrentItem() == 1) {
                        setCreate();
                    } else if (bottomNavigationView.getCurrentItem() == 2) {
                        setSettings();
                    }
                    break;

                case R.id.overflow_pokes:
                    hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else {
                        loadPage("https://m.facebook.com/pokes");
                    }
                    break;

                case R.id.overflow_online:
                    hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else {
                        loadPage("https://m.facebook.com/buddylist");
                    }
                    break;

                case R.id.overflow_market:
                    hideOverFlow();
                    if (!NetworkConnection.isConnected(MainActivity.this)) {
                        StaticUtils.showSnackBar(MainActivity.this, "No network connection");
                    } else {
                        Intent link = new Intent(MainActivity.this, MarketPlaceActivity.class);
                        link.putExtra("url", "https://m.facebook.com/marketplace");
                        startActivity(link);
                    }
                    break;

                case R.id.overflow_close:
                    hideOverFlow();
                    finishAndRemoveTask();
                    System.exit(0);
                    break;

                case R.id.overflow_settings:
                    hideOverFlow();
                    Intent settings_right = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settings_right);
                    break;


                default:
                    break;
            }

        }
    };

    TextView friend_badge;
    boolean MenuLight;
    private boolean ifLaunched = true;
    private Handler badgeUpdate;
    private Runnable badgeTask;



    public static Activity getMainActivity() {
        return mainActivity;
    }



    @SuppressLint({"SetJavaScriptEnabled", "CommitTransaction"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = this;
        //set our main theme
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        //checks to see if a user has chosen top stories
        isTop = PreferencesUtility.getBoolean("top_news", false);
        super.onCreate(savedInstanceState);
        if (PreferencesUtility.getBoolean("top_tabs", false)) {
            setContentView(R.layout.activity_main_top);
        } else {
            setContentView(R.layout.activity_main);
        }
        try {
            mWorkManager = WorkManager.getInstance(this);
        }catch (Exception p){
            p.printStackTrace();
        }

        //find our views and get them setup.
        viewPager = findViewById(R.id.viewpager);
        viewPager.setBackgroundColor(ThemeUtils.getTheme(this));

        friend_badge = findViewById(R.id.friend_badge);
        searchView = findViewById(R.id.simple_search_view);
        cardView = findViewById(R.id.search_card);
        image = findViewById(R.id.toolbar_drawer);
        cards = findViewById(R.id.lin_card);
        cards.getBackground().setColorFilter(new PorterDuffColorFilter(ThemeUtils.getThemeCard(this), PorterDuff.Mode.SRC_IN));


        overflowMenu = findViewById(R.id.main_menu);
        menuHolder = findViewById(R.id.menu_holder);
        menuHolder.setOnClickListener(SimpleClicker);
        menuHolder.setClickable(false);
        menuHolder.setFocusable(false);
        textView1 = findViewById(R.id.text1);
        textView2 = findViewById(R.id.text2);
        searchText = findViewById(R.id.toolbar_search);


        //setup our search recyclerview && search view :)
        arrayList = PreferencesUtility.getSearch();
        search_recycler = findViewById(R.id.search_recycler);
        search_recycler.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this, arrayList, this);
        search_recycler.setAdapter(searchAdapter);
        search_recycler.setVisibility(View.GONE);
        searchCard = findViewById(R.id.search_layout);

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);

        //setup our bottom navigation

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation);


        bottomNavigationAdapter.setupWithBottomNavigation(bottomNavigationView);
        bottomNavigationView.setTitleTypeface(Typeface.DEFAULT_BOLD);



        if (PreferencesUtility.getBoolean("show_panels", false)) {
            //noinspection deprecation
            appBarLayout.setTargetElevation(0);
            appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar_none));
        }
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            try {
                if (!PreferencesUtility.getBoolean("lock_tabs", false) && !PreferencesUtility.getBoolean("top_tabs", false)) {
                    bottomNavigationView.setTranslationY(Math.round(-verticalOffset));
                }
            } catch (Exception b) {
                b.printStackTrace();
            }
        });

        if (ThemeUtils.isNightTime()) {
            bottomNavigationView.setDefaultBackgroundColor(ThemeUtils.getColorPrimary(this));
        } else if (PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme")) {
            bottomNavigationView.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else {
            bottomNavigationView.setDefaultBackgroundColor(ThemeUtils.getMenu(this));
        }

        ThemeUtils.setTabs(this, bottomNavigationView);

        bottomNavigationView.setBehaviorTranslationEnabled(false);
        bottomNavigationView.setNotificationBackgroundColor(ContextCompat.getColor(this, R.color.md_red_500));
        bottomNavigationView.setForceTint(true);
        bottomNavigationView.setCurrentItem(0);

        //turn off elevation for top tabs
        if (PreferencesUtility.getBoolean("top_tabs", false)) {
            bottomNavigationView.setUseElevation(false);
        }

        bottomNavigationView.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);




        //selecting tabs

        bottomNavigationView.setOnTabSelectedListener((position, wasSelected) -> {
            viewPager.setCurrentItem(position, true);
            appBarLayout.setExpanded(true, true);
            switch (position) {
                case 0:
                    if (!PreferencesUtility.getBoolean("show_panels", false)) {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar));
                    } else {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar_none));
                    }
                    if (wasSelected) {
                        adapter.resetTab(0);
                    }
                    return true;
                case 1:
                    if (!PreferencesUtility.getBoolean("show_panels", false)) {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar));
                    } else {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar_none));
                    }
                    if (wasSelected) {
                        adapter.resetTab(1);
                    }
                    return true;


                case 2:
                    if (!PreferencesUtility.getBoolean("show_panels", false)) {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar));
                    } else {
                        appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar_none));
                    }
                    NotificationService.clearNotifications(getApplicationContext());
                    if (wasSelected) {
                        adapter.resetTab(2);
                    }
                    return true;

                case 3:
                    appBarLayout.setElevation(getResources().getDimension(R.dimen.elevation_appbar));
                    if (wasSelected) {
                        adapter.resetTab(3);
                    }
                    return true;
            }
            return true;
        });

        viewPager.setOffscreenPageLimit(4);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        if (!PreferencesUtility.getBoolean("swipe_tabs", false)) {
            viewPager.disableScroll(true);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.setCurrentItem(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(null);
            getSupportActionBar().setTitle(null);
        }


        //setup our clicks

        findViewById(R.id.toolbar_search).setOnClickListener(SimpleClicker);
        findViewById(R.id.toolbar_messages).setOnClickListener(SimpleClicker);
        findViewById(R.id.toolbar_market).setOnClickListener(SimpleClicker);
        findViewById(R.id.toolbar_drawer).setOnClickListener(SimpleClicker);
        findViewById(R.id.overflow_pokes).setOnClickListener(SimpleClicker);
        findViewById(R.id.overflow_online).setOnClickListener(SimpleClicker);
        findViewById(R.id.overflow_market).setOnClickListener(SimpleClicker);
        findViewById(R.id.overflow_close).setOnClickListener(SimpleClicker);
        findViewById(R.id.overflow_settings).setOnClickListener(SimpleClicker);
        findViewById(R.id.text1).setOnClickListener(SimpleClicker);
        findViewById(R.id.text2).setOnClickListener(SimpleClicker);
        findViewById(R.id.update_lin).setOnClickListener(SimpleClicker);


        //lets animate our app name then check the text to search fb

        new Handler().postDelayed(this::showMenuText, 4000);
        new Handler().postDelayed(this::hideMenuText, 4900);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (getIntent() != null) {
            handleIntents(getIntent());
        }
        ifLaunched = true;
        return true;
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //get the users name and picture to display in the more tab
        new UserInfo().execute();

        //all about notifications

        if (PreferencesUtility.getBoolean("enable_notifications", false)) {
            Constraints constraints;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .build();
            }else{
                constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresCharging(false)
                        .build();
            }

            PeriodicWorkRequest periodicSyncDataWork =
                    new PeriodicWorkRequest.Builder(NotificationService.class, 15, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            // setting a backoff on case the work needs to retry
                            .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                            .build();
            mWorkManager.enqueueUniquePeriodicWork("lite_work", ExistingPeriodicWorkPolicy.KEEP, periodicSyncDataWork);
        }else{
            mWorkManager.cancelAllWork();
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ifLaunched) {
            handleIntents(intent);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //set our colors
        setColors();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if we should go to the lock activity
        if (PreferencesUtility.getString("needs_lock", "").equals("true") && (PreferencesUtility.getBoolean("simple_locker", false))) {
            Intent intent = new Intent(MainActivity.this, SimpleLock.class);
            startActivity(intent);
        }
        //get our search list
        arrayList = PreferencesUtility.getSearch();


        //checks badges from the fb basic site
        if (BadgeHelper.getCookie() != null && NetworkConnection.isConnected(this)) {
            badgeUpdate = new Handler();
            badgeTask = () -> {
                new InAppBadges().execute();
                badgeUpdate.postDelayed(badgeTask, 10000);
            };
            badgeTask.run();
        }

        //closes our post bottom sheet
        if (getFragmentManager() != null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("post_sheet");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //resets needing our lock
        PreferencesUtility.putString("needs_lock", "true");
        //stops checking for badges
        if (badgeTask != null && badgeUpdate != null)
            badgeUpdate.removeCallbacks(badgeTask);
        //closes our post bottom sheet
        if (getFragmentManager() != null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("post_fragment");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //expand the action bar and try to go back to the first tab
        appBarLayout.setExpanded(true, true);
        if (bottomNavigationView.getCurrentItem() != 0) {
            exit();
        }else if(bottomNavigationView.getCurrentItem() == 0){
            exit();
        }
    }

    public void exit() {
        if (searchCard.getVisibility() == View.VISIBLE) {
            closeSearch();
        }else if(overflowMenu.getVisibility() == View.VISIBLE){
            hideOverFlow();
        }else if (bottomNavigationView.getCurrentItem() != 0) {
            bottomNavigationView.setCurrentItem(0);
        } else {
            if (PreferencesUtility.getBoolean("confirm_close", false)) {
                if (back_pressed + 2000 > System.currentTimeMillis())
                    super.onBackPressed();
                else
                    StaticUtils.showSnackBar(this, "Press back again to close Simple");
                back_pressed = System.currentTimeMillis();
            } else {
                super.onBackPressed();
                finishAndRemoveTask();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ifLaunched = false;
        //stop checking for badges
        if (badgeTask != null && badgeUpdate != null) {
            badgeUpdate.removeCallbacks(badgeTask);
        }

    }



    //lets load a page
    public void loadPage(String url) {
        Intent link = new Intent(this, NewPageActivity.class);
        link.putExtra("url", url);
        startActivity(link);
    }

    //lets load a popup window for sharing
    public void createPopUpWebView(String url) {
        Intent link = new Intent(this, Sharer.class);
        link.putExtra("url", url);
        startActivity(link);
    }


    //handle new incoming intents
    private void handleIntents(Intent intent) {
        setIntent(intent);
        String webViewUrl = getIntent().getDataString();
        String sharedUrl = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Uri picture = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (picture != null) {
            galleryImageUri = picture;
            isPicture = true;
            createPopUpWebView("https://m.facebook.com/home.php");
        } else {
            if (sharedUrl != null) {
                if (!Patterns.WEB_URL.matcher(sharedUrl.toLowerCase()).matches()) {
                    if (sharedUrl.contains("http")) {
                        sharedUrl = sharedUrl.substring(sharedUrl.indexOf("http"));
                        if (sharedUrl.contains(" ")) {
                            sharedUrl = sharedUrl.substring(0, sharedUrl.indexOf(" "));
                        }
                    }
                }
                if (!Patterns.WEB_URL.matcher(sharedUrl.toLowerCase()).matches()) {
                    StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                    return;
                }
                try {
                    createPopUpWebView("https://www.facebook.com/sharer.php?u=" + URLEncoder.encode(sharedUrl, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    StaticUtils.showSnackBar(this, getString(R.string.error) +" "+System.currentTimeMillis());
                }
            }



            //check if the url is for fb
            if (webViewUrl != null && URLUtil.isValidUrl(getIntent().getDataString())) {
                webViewUrl = webViewUrl.replace("https://www.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("http://www.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("https://web.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("http://web.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("https://mobile.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("http://mobile.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("https://mbasic.facebook.com", "https://m.facebook.com");
                webViewUrl = webViewUrl.replace("http://mbasic.facebook.com", "https://m.facebook.com");
                try {
                    Intent peekIntent = new Intent(MainActivity.this, NewPageActivity.class);
                    peekIntent.putExtra("url", webViewUrl);
                    startActivity(peekIntent);
                } catch (Exception i) {
                    i.printStackTrace();
                }
            }

        }

    }



    //color some stuff
    private void setColors() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            searchText.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            searchText.setTextColor(ContextCompat.getColor(this, R.color.m_color));
        } else if (!MenuLight) {
            searchText.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            searchText.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        toolbar.setBackgroundColor(setToolbarColor(this));
        appBarLayout.setBackgroundColor(setToolbarColor(this));


        if(PreferencesUtility.getBoolean("color_status", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));

            }
        }else{
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if(StaticUtils.isMarshmallow()) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightStatusBar(this);
                }else{
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setStatusBarColor(setToolbarColor(this));
            }
        }

        if (PreferencesUtility.getBoolean("color_nav", false)) {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ThemeUtils.getColorPrimaryDark());
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(StaticUtils.darkColorTheme(ThemeUtils.getColorPrimaryDark()));
            }
        } else {
            if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            } else if (MenuLight && !ThemeUtils.isNightTime()) {
                if(StaticUtils.isOreo()){
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white));
                    ThemeUtils.setLightNavigationBar(this);
                }else{
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_nav));
                }
            } else if (!MenuLight) {
                getWindow().setNavigationBarColor(setToolbarColor(this));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().setNavigationBarDividerColor(ThemeUtils.getTheme(this));
                }
            }
        }
    }





    //setting up and loading our search layout
    @Override
    public void loadSearch(String str) {
        if (searchCard.getVisibility() == View.VISIBLE) {
            closeSearch();
        }
        Intent link = new Intent(this, NewPageActivity.class);
        if (((AppCompatCheckBox) findViewById(R.id.filter_people_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/people/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/pages/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_events_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/events/?q=" + str);
        } else if (((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).isChecked()) {
            link.putExtra("url", "https://m.facebook.com/search/groups/?q=" + str);
        } else {
            link.putExtra("url", "https://m.facebook.com/search/top/?q=" + str);
        }
        startActivity(link);
    }

    private void initializeSearch() {
        mMore = findViewById(R.id.search_more);
        mFilters = findViewById(R.id.filter_layout);
        mMoreTitle = findViewById(R.id.search_more_title);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<SearchItems> searchItems = PreferencesUtility.getSearch();
                SearchItems name_search = new SearchItems();
                name_search.setName(query.toLowerCase());
                searchItems.add(name_search);
                PreferencesUtility.saveSearch(searchItems);
                closeSearch();
                //fixes hashtag searches
                if(query.contains("#")){
                    new Handler().postDelayed(() -> loadSearch(query.replace("#", "%23")), 300);
                }else{
                    new Handler().postDelayed(() -> loadSearch(query), 300);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    search_recycler.setVisibility(View.VISIBLE);
                    mFilters.setVisibility(View.VISIBLE);
                    mMore.setVisibility(View.VISIBLE);
                    mMoreTitle.setText(getResources().getString(R.string.see_more_results, newText));
                    newText = newText.toLowerCase();
                    ArrayList<SearchItems> newList = new ArrayList<>();
                    for (SearchItems searchNames : arrayList) {
                        String name = searchNames.getName().toLowerCase();
                        if (name.contains(newText))
                            newList.add(searchNames);
                    }
                    searchAdapter.setFilter(newList);
                } else {
                    search_recycler.setVisibility(View.GONE);
                    mFilters.setVisibility(View.GONE);
                    mMore.setVisibility(View.GONE);
                }
                return true;
            }
        });
        findViewById(R.id.search_back).setOnClickListener(SimpleClicker);
        findViewById(R.id.filter_people_check).setOnClickListener(SimpleClicker);
        findViewById(R.id.filter_pages_check).setOnClickListener(SimpleClicker);
        findViewById(R.id.filter_events_check).setOnClickListener(SimpleClicker);
        findViewById(R.id.filter_groups_check).setOnClickListener(SimpleClicker);
        searchInitialized = true;
    }

    public void openSearch() {
        if (!searchInitialized) {
            initializeSearch();
        }
        searchCard.setVisibility(View.VISIBLE);
        searchCard.setClickable(false);
        searchCard.setFocusable(false);
        searchCard.setFocusableInTouchMode(false);
        searchView.setIconified(false);
        cardView.setClickable(true);
        ((AppCompatCheckBox) findViewById(R.id.filter_people_check)).setChecked(false);
        ((AppCompatCheckBox) findViewById(R.id.filter_pages_check)).setChecked(false);
        ((AppCompatCheckBox) findViewById(R.id.filter_events_check)).setChecked(false);
        ((AppCompatCheckBox) findViewById(R.id.filter_groups_check)).setChecked(false);
        showMenu();
        findViewById(R.id.lin_card).setVisibility(View.GONE);
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    public void closeSearch() {
        cardView.setClickable(false);
        mMore.setVisibility(View.GONE);
        mFilters.setVisibility(View.GONE);
        searchCard.setVisibility(View.GONE);
        searchView.setOnCloseListener(() -> false);
        searchView.setQuery("", false);
        hideMenu();
        findViewById(R.id.lin_card).setVisibility(View.VISIBLE);
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    private void showMenu() {
        searchCard.setVisibility(View.VISIBLE);

    }

    private void hideMenu() {
        searchCard.setVisibility(View.GONE);
    }




    private int setToolbarColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.darcula);
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.black);
                default:
                    return ContextCompat.getColor(context, R.color.white);
            }

        }
    }


    //refresh our badges
    public void refreshBadges() {
        try {
            runOnUiThread(() -> {
                if (InAppBadges.feedCount != null) {
                    if (InAppBadges.feedCount.isEmpty()) {
                        bottomNavigationView.setNotification("", 0);
                    } else {
                        bottomNavigationView.setNotification(InAppBadges.feedCount, 0);
                    }
                }
            });


            runOnUiThread(() -> {
                if (InAppBadges.noCount != null) {
                    if (InAppBadges.noCount.isEmpty()) {
                        bottomNavigationView.setNotification("", 2);
                        bottomNavigationView.getItem(2).setDrawable(R.drawable.ic_notifications_other);
                        bottomNavigationView.postDelayed(() -> bottomNavigationView.refresh(),50);
                    } else {
                        bottomNavigationView.getItem(2).setDrawable(R.drawable.ic_notifications_notify);
                        bottomNavigationView.postDelayed(() -> bottomNavigationView.refresh(),50);
                        bottomNavigationView.setNotification(InAppBadges.noCount, 2);
                    }

                }
            });


            runOnUiThread(() -> {
                if (InAppBadges.messCount != null && friend_badge != null) {
                    if (InAppBadges.messCount.isEmpty()) {
                        friend_badge.setVisibility(View.INVISIBLE);
                    } else {
                        friend_badge.setVisibility(View.VISIBLE);
                        friend_badge.setText(InAppBadges.messCount);
                    }

                }
            });

            runOnUiThread(() -> {
                if (InAppBadges.requestCount != null) {
                    if (InAppBadges.requestCount.isEmpty()) {
                        bottomNavigationView.setNotification("", 3);
                    } else {
                        bottomNavigationView.setNotification(InAppBadges.requestCount, 3);
                    }

                }
            });

        } catch (Exception p) {
            p.printStackTrace();
        }
    }


    //show our "overflow" menu
    public void showOverFlow() {
        Animation grow = AnimationUtils.loadAnimation(this, R.anim.grow_menu);
        grow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                overflowMenu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });


        //customise our "overflow" menu a bit for when on a different tab and when there is no panels being shown
        switch (bottomNavigationView.getCurrentItem()) {
            case 0:
                if (!PreferencesUtility.getBoolean("show_panels", false)) {
                    textView1.setText(R.string.most_recent);
                    textView2.setText(R.string.top_stories);
                    textView1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_news, 0, 0);
                    textView2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_top_stories, 0, 0);
                    findViewById(R.id.lin_border).setVisibility(View.VISIBLE);
                    findViewById(R.id.lin_bottom).setVisibility(View.VISIBLE);
                    if (isTop) {
                        textView2.setTextColor(setFeedColor(this));
                        textView1.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                        Drawable[] drawables = textView2.getCompoundDrawables();
                        if (drawables[1] != null) {  // left drawable
                            drawables[1].setColorFilter(setFeedColor(this), PorterDuff.Mode.SRC_ATOP);
                        }
                    } else {
                        textView1.setTextColor(setFeedColor(this));
                        textView2.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                        Drawable[] drawables = textView1.getCompoundDrawables();
                        if (drawables[1] != null) {  // left drawable
                            drawables[1].setColorFilter(setFeedColor(this), PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                }else{
                    findViewById(R.id.lin_border).setVisibility(View.GONE);
                    findViewById(R.id.lin_bottom).setVisibility(View.GONE);
                }
                break;

            case 1:
                if (!PreferencesUtility.getBoolean("show_panels", false)) {
                    findViewById(R.id.lin_border).setVisibility(View.VISIBLE);
                    findViewById(R.id.lin_bottom).setVisibility(View.VISIBLE);
                    textView1.setText(R.string.discover_group);
                    textView2.setText(R.string.create_new_group);
                    textView1.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                    textView2.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                    textView1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_group_search, 0, 0);
                    textView2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_create_group, 0, 0);
                    Drawable[] drawables = textView1.getCompoundDrawables();
                    if (drawables[1] != null) {  // left drawable
                        drawables[1].setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_ATOP);
                    }
                    Drawable[] drawable = textView2.getCompoundDrawables();
                    if (drawable[1] != null) {  // left drawable
                        drawable[1].setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_ATOP);
                    }
                }else{
                    findViewById(R.id.lin_border).setVisibility(View.GONE);
                    findViewById(R.id.lin_bottom).setVisibility(View.GONE);
                }
                break;

            case 2:
                if (!PreferencesUtility.getBoolean("show_panels", false)) {
                    findViewById(R.id.lin_border).setVisibility(View.VISIBLE);
                    findViewById(R.id.lin_bottom).setVisibility(View.VISIBLE);
                    textView1.setText(R.string.mark_all_read);
                    textView2.setText(R.string.notify_settings);
                    textView1.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                    textView2.setTextColor(ContextCompat.getColor(this, R.color.m_color));
                    textView1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mark_all, 0, 0);
                    textView2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_settings, 0, 0);
                    Drawable[] drawables = textView1.getCompoundDrawables();
                    if (drawables[1] != null) {  // left drawable
                        drawables[1].setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_ATOP);
                    }
                    Drawable[] drawable = textView2.getCompoundDrawables();
                    if (drawable[1] != null) {  // left drawable
                        drawable[1].setColorFilter(ContextCompat.getColor(this, R.color.m_color), PorterDuff.Mode.SRC_ATOP);
                    }
                }else{
                    findViewById(R.id.lin_border).setVisibility(View.GONE);
                    findViewById(R.id.lin_bottom).setVisibility(View.GONE);
                }
                break;

            case 3:
            default:
                findViewById(R.id.lin_border).setVisibility(View.GONE);
                findViewById(R.id.lin_bottom).setVisibility(View.GONE);
                break;
        }
        overflowMenu.startAnimation(grow);
        menuHolder.setClickable(true);
        menuHolder.setFocusable(true);
        menuHolder.setSoundEffectsEnabled(false);
    }

    //hide our "overflow" menu
    public void hideOverFlow() {
        Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade_menu);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                overflowMenu.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        overflowMenu.startAnimation(fade);
        menuHolder.setClickable(false);
        menuHolder.setFocusable(false);
    }


    private int setFeedColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.white);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.white);
                default:
                    return ThemeUtils.getColorPrimary(context);
            }

        }
    }

    public void showMenuText() {
        Animation fade = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                searchText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchText.startAnimation(fade);
    }

    public void hideMenuText() {
        Animation fade = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                searchText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchText.startAnimation(fade);
        searchText.setText(R.string.search);
    }

    //tab things
    public void setRecent(){
        adapter.loadRecent(0);
    }

    public void setTop(){
        adapter.loadTop(0);
    }

    public void setNotifications(){
        adapter.loadMark(2);
    }

    public void setSettings(){
        adapter.loadSet(2);
    }

    public void setDiscover(){
        adapter.loadDis(1);
    }

    public void setCreate(){
        adapter.loadCre(1);

    }


    //used to show the post dialog
    public void showPost(){
        if (!NetworkConnection.isConnected(this)) {
            StaticUtils.showSnackBar(this, "No network connection");
        } else {
            getPostDialog();
        }
    }


    //used to reload simple when switching accounts
    public void reloadAll() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


}