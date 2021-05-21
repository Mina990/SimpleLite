package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.adapters.PinsAdapter;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class PinsActivity extends OtherBaseActivity implements PinsAdapter.onBookmarkSelected, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    RecyclerView recyclerUsers;
    RelativeLayout fakeCard;
    SearchView pin_search;
    boolean MenuLight;
    LinearLayout emptyText, pin_card;
    View shadow;
    SwipeRefreshLayout swipeRefreshLayout;
    private PinsAdapter adapterPins;
    private ArrayList<PinItems> pinItems;
    private ItemTouchHelper mItemTouchHelper;
    private Toolbar toolbar;
    public AppBarLayout appBarLayout;
    AHBottomNavigation ahBottomNavigation;
    AHBottomNavigationAdapter ahBottomNavigationAdapter;
    ImageView close, voice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pins);
        appBarLayout = findViewById(R.id.pin_appbar);
        toolbar = findViewById(R.id.pin_toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setEnabled(false);
        StaticUtils.setSwipeColor(swipeRefreshLayout, this);
        shadow = findViewById(R.id.bot_line);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(null);
        }

        fakeCard = findViewById(R.id.card_fake);
        fakeCard.setBackgroundColor(ThemeUtils.getTheme(this));
        pin_card = findViewById(R.id.pin_card);
        pin_card.getBackground().setColorFilter(new PorterDuffColorFilter(ThemeUtils.getThemeCard(this), PorterDuff.Mode.SRC_IN));

        ahBottomNavigation = findViewById(R.id.bottom_navigation_pins);
        ahBottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_pins);
        ahBottomNavigationAdapter.setupWithBottomNavigation(ahBottomNavigation);
        ahBottomNavigation.setTitleTypeface(Typeface.DEFAULT_BOLD);

        if (ThemeUtils.isNightTime()) {
            ahBottomNavigation.setDefaultBackgroundColor(ThemeUtils.getColorPrimary(this));
        } else if (PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme")) {
            ahBottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.white));
        } else {
            ahBottomNavigation.setDefaultBackgroundColor(ThemeUtils.getMenu(this));
        }
        ThemeUtils.setTabsPins(this, ahBottomNavigation);
        ahBottomNavigation.setBehaviorTranslationEnabled(false);
        ahBottomNavigation.setForceTint(true);
        ahBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        ahBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            try{
                appBarLayout.setExpanded(true, true);
                switch (position) {
                    case 0:
                        resetDialog();
                        break;
                    case 1:
                        deleteDialog();
                        break;

                    case 2:
                        customPin();
                        break;

                }

            } catch (NullPointerException ignored) {
            } catch (Exception i) {
                i.printStackTrace();
            }
            return true;
        });


        close = findViewById(R.id.pin_close);



        emptyText = findViewById(R.id.empty_view);
        pinItems = PreferencesUtility.getBookmarks();
        pin_search = findViewById(R.id.pin_search);
        pin_search.setOnQueryTextListener(this);

        close.setOnClickListener(view -> {
            if (!pin_search.getQuery().toString().isEmpty()) {
                pin_search.setQuery("", false);
                new Handler().postDelayed(this::finish, 200);
            } else {
                finish();
            }
        });


        pin_search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        recyclerUsers = findViewById(R.id.recycler_users);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapterPins = new PinsAdapter(this, pinItems, this, this);
        recyclerUsers.setAdapter(adapterPins);

        if (adapterPins.getItemCount() > 0) {
            emptyText.setVisibility(View.GONE);
        }
        adapterPins.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                emptyText.setVisibility(adapterPins.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        PreferencesUtility.putBoolean("did_restore", true);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapterPins.getFilter().filter(newText);
        ImageView searchViewIcon = pin_search.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (searchViewIcon != null)
            searchViewIcon.setVisibility(View.GONE);
        return false;
    }

    @Override
    public void onRefresh() {

    }


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(PreferencesUtility.getBoolean("first_time_pins", true) && pinItems.isEmpty()){
            firstPins();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onResume() {
        setColors();
        super.onResume();
        pinItems = PreferencesUtility.getBookmarks();
    }

    @Override
    public void onBackPressed() {
        if (!pin_search.getQuery().toString().isEmpty()) {
            pin_search.setQuery("", false);
            new Handler().postDelayed(super::onBackPressed, 200);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            PreferencesUtility.saveBookmarks(adapterPins.getListBookmarks());
            PreferencesUtility.saveFavorites(this, adapterPins.getListFavorites());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void loadBookmark(String title, final String url) {
        new Handler().postDelayed(() -> loadPage(url), 250);
    }

    public void loadPage(String url) {
        getPins();
        Intent link = new Intent(this, NewPageActivity.class);
        link.putExtra("url", url);
        startActivity(link);
        finish();
    }

    public void setColors() {
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

    protected void resetDialog() {
        if(!isDestroyed()) {
            MaterialAlertDialogBuilder reset_all = new MaterialAlertDialogBuilder(this);
            reset_all.setTitle(R.string.reset_all_pins);
            reset_all.setMessage(getResources().getString(R.string.reset_all_message));
            reset_all.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                adapterPins.clear();
                new Handler().postDelayed(() -> {
                    Uri day = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_on_this_day));
                    PinItems onthisday = new PinItems();
                    onthisday.setTitle(getString(R.string.onthisday));
                    onthisday.setUrl("https://m.facebook.com/onthisday");
                    onthisday.setImage(day.toString());
                    adapterPins.addItem(onthisday);


                    Uri photo = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pics));
                    PinItems photos = new PinItems();
                    photos.setTitle(getString(R.string.photos));
                    photos.setUrl("https://m.facebook.com/profile.php?v=photos&soft=composer");
                    photos.setImage(photo.toString());
                    adapterPins.addItem(photos);


                    Uri page = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_page));
                    PinItems pages = new PinItems();
                    pages.setTitle(getString(R.string.pages));
                    pages.setUrl("https://m.facebook.com/pages/launchpoint/");
                    pages.setImage(page.toString());
                    adapterPins.addItem(pages);


                    Uri group = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_group));
                    PinItems groups = new PinItems();
                    groups.setTitle(getString(R.string.groups));
                    groups.setUrl("https://m.facebook.com/groups/?category=membership");
                    groups.setImage(group.toString());
                    adapterPins.addItem(groups);


                    Uri event = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_cal));
                    PinItems events = new PinItems();
                    events.setTitle(getString(R.string.events));
                    events.setUrl("https://m.facebook.com/events");
                    events.setImage(event.toString());
                    adapterPins.addItem(events);
                    runOnUiThread(() -> StaticUtils.showSnackBar(this, "All Pins reset"));
                    PreferencesUtility.saveBookmarks(adapterPins.getListBookmarks());


                }, 500);
            });
            reset_all.setNegativeButton(getString(R.string.cancel), null);
            reset_all.show();
        }
    }

    private void firstPins(){
        Uri day = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_on_this_day));
        PinItems onthisday = new PinItems();
        onthisday.setTitle(getString(R.string.onthisday));
        onthisday.setUrl("https://m.facebook.com/onthisday");
        onthisday.setImage(day.toString());
        adapterPins.addItem(onthisday);


        Uri photo = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pics));
        PinItems photos = new PinItems();
        photos.setTitle(getString(R.string.photos));
        photos.setUrl("https://m.facebook.com/profile.php?v=photos&soft=composer");
        photos.setImage(photo.toString());
        adapterPins.addItem(photos);


        Uri page = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_page));
        PinItems pages = new PinItems();
        pages.setTitle(getString(R.string.pages));
        pages.setUrl("https://m.facebook.com/pages/launchpoint/");
        pages.setImage(page.toString());
        adapterPins.addItem(pages);


        Uri group = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_group));
        PinItems groups = new PinItems();
        groups.setTitle(getString(R.string.groups));
        groups.setUrl("https://m.facebook.com/groups/?category=membership");
        groups.setImage(group.toString());
        adapterPins.addItem(groups);


        Uri event = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_cal));
        PinItems events = new PinItems();
        events.setTitle(getString(R.string.events));
        events.setUrl("https://m.facebook.com/events");
        events.setImage(event.toString());
        adapterPins.addItem(events);
        PreferencesUtility.saveBookmarks(adapterPins.getListBookmarks());
        PreferencesUtility.putBoolean("first_time_pins", false);

    }



    private void customPin() {
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View alertLayout = inflater.inflate(R.layout.custom_pin_layout, null);
        EditText pin_name = alertLayout.findViewById(R.id.pin_edit);
        EditText pin_url = alertLayout.findViewById(R.id.url_edit);
        new MaterialAlertDialogBuilder(this)
        .setTitle(getResources().getString(R.string.create_smart))
        .setMessage(getResources().getString(R.string.create_smart_message))
        .setView(alertLayout)
        .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
            if (!pin_url.getText().toString().isEmpty()) {
                Uri pinUri;
                if (pin_url.getText().toString().contains("messages")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pin_mess));
                } else if (pin_url.getText().toString().contains("/groups/")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_group));
                } else if (pin_url.getText().toString().contains("/photos/a.") || pin_url.getText().toString().contains("photos/pcb.") || pin_url.getText().toString().contains("/photo.php?") || pin_url.getText().toString().contains("/photos/") && !pin_url.getText().toString().contains("?photoset")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_pics));
                } else if (pin_url.getText().toString().contains("/marketplace")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_market));
                } else if (pin_url.getText().toString().contains("/events/")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_cal));
                } else if (pin_url.getText().toString().contains("/home.php?sk=fl_")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_news_set));
                } else if (pin_url.getText().toString().contains("/instantgames/play/")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_games));
                } else if (pin_url.getText().toString().startsWith("m.facebook.com")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_facebook_pins));
                } else if (pin_url.getText().toString().contains("pages")) {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_page));
                } else {
                    pinUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                            "://" + getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                            + '/' + getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + getResources().getResourceEntryName(R.drawable.ic_links));
                }
                ArrayList<PinItems> newList = PreferencesUtility.getBookmarks();
                PinItems bookmark = new PinItems();
                if (pin_name.getText().toString().isEmpty()) {
                    bookmark.setTitle(pin_name.getHint().toString());
                } else {
                    bookmark.setTitle(pin_name.getText().toString());
                }
                if (!pin_url.getText().toString().matches("^(?i)(https?|ftp)://.*$")) {
                    bookmark.setUrl("https://" + pin_url.getText().toString());
                } else {
                    bookmark.setUrl(pin_url.getText().toString());
                }
                bookmark.setImage(pinUri.toString());
                newList.add(bookmark);
                PreferencesUtility.saveBookmarks(newList);
                StaticUtils.showSnackBar(this, String.format(getString(R.string.added_to_pins), pin_name.getText().toString()));
                getPins();
            } else {
                StaticUtils.showSnackBar(this, getString(R.string.error) + " " + System.currentTimeMillis());
            }
        })
        .setNegativeButton(getResources().getString(R.string.cancel), null)
        .create().show();
    }



    public void deleteDialog() {
        try {
            if(!isDestroyed()) {
                if (adapterPins.getItemCount() == 0) {
                    MaterialAlertDialogBuilder removeFavorite = new MaterialAlertDialogBuilder(this);
                    removeFavorite.setMessage("One cannot delete what one does not have.");
                    removeFavorite.setNegativeButton(R.string.ok, null);
                    removeFavorite.show();
                } else {
                    MaterialAlertDialogBuilder removeFavorite = new MaterialAlertDialogBuilder(this);
                    removeFavorite.setTitle("Remove All Pins?");
                    if (adapterPins.getItemCount() > 1) {
                        removeFavorite.setMessage(String.format(getString(R.string.are_you_sure_remove), valueOf(adapterPins.getItemCount())));
                    } else {
                        removeFavorite.setMessage(getResources().getString(R.string.are_you_sure_remove_single));
                    }
                    removeFavorite.setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                        adapterPins.clear();
                        StaticUtils.showSnackBar(this, "All Pins reset");
                    });
                    removeFavorite.setNegativeButton(R.string.cancel, null);
                    removeFavorite.show();
                }
            }
        }catch (Exception p){
            p.printStackTrace();
        }
    }

    private void getPins() {
        adapterPins.restore();
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
}
