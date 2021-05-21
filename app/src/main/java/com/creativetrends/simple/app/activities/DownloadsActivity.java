package com.creativetrends.simple.app.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativetrends.simple.app.adapters.AdapterDownloads;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;

public class DownloadsActivity extends OtherBaseActivity {
    boolean MenuLight;
    RecyclerView recyclerView;
    AdapterDownloads recyclerViewAdapter;
    Toolbar mToolbar;
    LinearLayout emptyText;
    RelativeLayout background;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        emptyText = findViewById(R.id.empty_view);
        background = findViewById(R.id.download_coor);
        background.setBackgroundColor(ThemeUtils.getTheme(this));
        mToolbar = findViewById(R.id.toolbar);
        MenuLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
        emptyText = findViewById(R.id.empty_view);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Simple Downloads");
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerViewAdapter = new AdapterDownloads(this, this, PreferencesUtility.getUserDownloads());
        recyclerViewAdapter.setHasStableIds(false);

        recyclerView.setAdapter(recyclerViewAdapter);


        if (recyclerViewAdapter.getItemCount() > 0) {
            emptyText.setVisibility(View.GONE);
        }
        recyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
                emptyText.setVisibility(recyclerViewAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferencesUtility.putString("needs_lock", "false");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferencesUtility.putString("needs_lock", "false");
        setColors();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void setColors() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (MenuLight && !ThemeUtils.isNightTime()) {
            mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black));
        } else if (!MenuLight) {
            mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
           mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        }
        mToolbar.setBackgroundColor(setToolbarColor(this));

        setOverflowIconColor(ContextCompat.getColor(this, R.color.m_color));

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


    private void setOverflowIconColor(int color) {
        Drawable overflowIcon = mToolbar.getOverflowIcon();

        if (overflowIcon != null) {
            Drawable newIcon = overflowIcon.mutate();
            newIcon.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            mToolbar.setOverflowIcon(newIcon);
        }
    }

    private int setToolbarColor(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.darcula);
                case "darktheme":
                case "amoledtheme":
                    return ContextCompat.getColor(context, R.color.black);
                default:
                    return ContextCompat.getColor(context, R.color.white);
            }

        }
    }

}
