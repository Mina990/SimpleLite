package com.creativetrends.simple.app.mainfragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.DownloadsActivity;
import com.creativetrends.simple.app.activities.MarketPlaceActivity;
import com.creativetrends.simple.app.activities.MessageActivity;
import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.activities.PinsActivity;
import com.creativetrends.simple.app.activities.SwitchActivity;
import com.creativetrends.simple.app.activities.WatchActivity;
import com.creativetrends.simple.app.adapters.AdapterFavs;
import com.creativetrends.simple.app.adapters.AdapterUsers;
import com.creativetrends.simple.app.adapters.AdapterUsersOther;
import com.creativetrends.simple.app.adapters.PinItems;
import com.creativetrends.simple.app.adapters.UserItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.InAppBadges;
import com.creativetrends.simple.app.utils.PrefManager;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.creativetrends.simple.app.utils.UserInfo;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 5/26/2018.
 */
public class FragmentMore extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, AdapterFavs.onBookmarkSelected {
    private NestedScrollView background;
    private TextView badge_requests_text;
    private MaterialCardView badge_requests;
    WorkManager mWorkManager;
    private PrefManager prefManager;
    private SwipeRefreshLayout menu_swipe;
    private Context context;
    private AdapterFavs adapterPins;
    private int scrollPosition = 0;
    private AdapterUsers adapterUsers;
    AdapterUsersOther adapterUsersOther;
    View clicker;
    ProgressBar progressBar;
    int SPLASH_TIME_OUT = 600;
    private boolean _hasLoadedOnce= false;
    public FragmentMore() {
        // Required empty public constructor
    }

    private static void scrollToTop(NestedScrollView nestedScrollView) {
        ObjectAnimator anim = ObjectAnimator.ofInt(nestedScrollView, "scrollY", nestedScrollView.getScrollY(), 0);
        anim.setDuration(500);
        anim.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = SimpleApplication.getContextOfApplication();
            prefManager = new PrefManager(context);
            mWorkManager = WorkManager.getInstance(context);

            ArrayList<PinItems> pinItems = PreferencesUtility.getBookmarks();
            adapterPins = new AdapterFavs(context, pinItems, this, getActivity());

            //logged in users//
            ArrayList<UserItems> userItems = PreferencesUtility.getUsers();
            adapterUsers = new AdapterUsers(context, userItems, BadgeHelper.getCookie());
            adapterUsersOther = new AdapterUsersOther(context, userItems, BadgeHelper.getCookie());

            //userItems = PreferencesUtility.getUsers();
            //adapterUsers = new AdapterUsers(context, userItems, null);
        } catch (Exception i) {
            i.printStackTrace();
        }

    }

    @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v1 = inflater.inflate(R.layout.fragment_more, container, false);


        RecyclerView recycler = v1.findViewById(R.id.recycler_users);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapterUsers);
        //logged out users//

        RecyclerView recyclerView = v1.findViewById(R.id.recycler_users_other);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapterUsersOther);



        background = v1.findViewById(R.id.root_scroll);
        background.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        background.setBackgroundColor(ThemeUtils.getTheme(getActivity()));
        background.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> scrollPosition = scrollY);
        menu_swipe = v1.findViewById(R.id.swipe_menu);
        menu_swipe.setOnRefreshListener(this);
        progressBar = v1.findViewById(R.id.tabs_progress);


        StaticUtils.setSwipeColor(menu_swipe, context);
        StaticUtils.setProgressColorNative(progressBar, context);

        try {
            RecyclerView recyclerUsers = v1.findViewById(R.id.recycler_favs);
            recyclerUsers.setVisibility(View.VISIBLE);
            recyclerUsers.setLayoutManager(new LinearLayoutManager(context));
            recyclerUsers.setAdapter(adapterPins);
            recyclerUsers.setNestedScrollingEnabled(false);
            recyclerUsers.setHasFixedSize(false);
        }catch (Exception p){
            p.printStackTrace();
        }


        badge_requests = v1.findViewById(R.id.requests_badge);
        badge_requests_text = v1.findViewById(R.id.requests_badge_text);

        clicker = v1.findViewById(R.id.click_pro);


        if (adapterUsers.getItemCount() == 3) {
            clicker.setVisibility(View.GONE);
        }else {
            clicker.setOnClickListener(view -> {
                StaticUtils.clearStrings();
                StaticUtils.clearCookies();
                startActivity(new Intent(getActivity(), SwitchActivity.class));
            });
        }


        //card views
        MaterialCardView pro = v1.findViewById(R.id.profile_card);
        MaterialCardView card0 = v1.findViewById(R.id.card_one);
        MaterialCardView card1 = v1.findViewById(R.id.card_two);
        MaterialCardView card2 = v1.findViewById(R.id.card_three);
        MaterialCardView card3 = v1.findViewById(R.id.card_four);
        MaterialCardView card4 = v1.findViewById(R.id.card_five);
        MaterialCardView card5 = v1.findViewById(R.id.card_seven);





        pro.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card0.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card1.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card2.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card3.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card4.setCardBackgroundColor(ThemeUtils.getMenu(context));
        card5.setCardBackgroundColor(ThemeUtils.getMenu(context));

        //card lookup
        RelativeLayout suggest_more = v1.findViewById(R.id.suggest_more);
        RelativeLayout pins = v1.findViewById(R.id.pins_more);
        RelativeLayout market = v1.findViewById(R.id.market_more);
        RelativeLayout photos = v1.findViewById(R.id.photos_more);
        RelativeLayout active = v1.findViewById(R.id.online_more);
        RelativeLayout saved = v1.findViewById(R.id.saved_more);
        RelativeLayout day = v1.findViewById(R.id.this_day_more);
        RelativeLayout events = v1.findViewById(R.id.events_more);
        RelativeLayout groups = v1.findViewById(R.id.groups_more);
        RelativeLayout group_create = v1.findViewById(R.id.create_group);
        RelativeLayout pages = v1.findViewById(R.id.pages_more);
        RelativeLayout create = v1.findViewById(R.id.create_more);
        RelativeLayout lang = v1.findViewById(R.id.lang_more);
        RelativeLayout privacy = v1.findViewById(R.id.privacy_more);
        RelativeLayout watch = v1.findViewById(R.id.watch_more);
        RelativeLayout live = v1.findViewById(R.id.live_more);

        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            live.setVisibility(View.GONE);
        }

        //OnClicks
        suggest_more.setOnClickListener(this);
        pins.setOnClickListener(this);
        photos.setOnClickListener(this);
        active.setOnClickListener(this);
        saved.setOnClickListener(this);
        day.setOnClickListener(this);
        events.setOnClickListener(this);
        market.setOnClickListener(this);
        groups.setOnClickListener(this);
        group_create.setOnClickListener(this);
        pages.setOnClickListener(this);
        create.setOnClickListener(this);
        lang.setOnClickListener(this);
        privacy.setOnClickListener(this);
        watch.setOnClickListener(this);
        v1.findViewById(R.id.al).setOnClickListener(this);
        v1.findViewById(R.id.as).setOnClickListener(this);
        v1.findViewById(R.id.lo).setOnClickListener(this);
        v1.findViewById(R.id.requests_more).setOnClickListener(this);
        v1.findViewById(R.id.face_more).setOnClickListener(this);
        v1.findViewById(R.id.live_more).setOnClickListener(this);
        v1.findViewById(R.id.down).setOnClickListener(this);
        if(PreferencesUtility.getBoolean("load_all", false)) {
            loadStart();
        }
        return v1;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        new Handler().postDelayed(this::loadGroup, 1500);
        super.onResume();
        try {
            adapterUsers.restore();
            adapterPins.restore();
        }catch (Exception p){
            p.printStackTrace();
        }
        if(PreferencesUtility.getString("changed_picture", "").equals("true")) {
            if(getActivity() != null)
                getActivity().runOnUiThread(this::refreshUser);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.suggest_more:
                loadPage("https://m.facebook.com/nt/?id=gaming");
                break;

            case R.id.watch_more:
                loadPage("https://m.facebook.com/watch/");
                break;

            case R.id.live_more:
                loadWatch();
                break;


            case R.id.market_more:
                loadMarketPlace();
                break;


            case R.id.photos_more:
                loadPage("https://m.facebook.com/profile.php?v=photos");
                break;


            case R.id.al:
                loadPage("https://m.facebook.com/" + BadgeHelper.getCookie() + "/allactivity");
                break;

            case R.id.as:
                loadPage("https://m.facebook.com/help");
                break;

            case R.id.pins_more:
                startActivity(new Intent(getActivity(), PinsActivity.class));
                break;

            case R.id.face_more:
                loadPage("https://m.facebook.com/settings");
                break;

            case R.id.lo:
                setLogout();
                break;


            case R.id.online_more:
                loadPage("https://m.facebook.com/buddylist");
                break;

            case R.id.saved_more:
                loadPage("https://m.facebook.com/saved/");
                break;

            /*case R.id.saved_more:
                loadPage("https://m.facebook.com/nt/?id=gaming");
                break;*/

            case R.id.this_day_more:
                loadPage("https://m.facebook.com/onthisday");
                break;

            case R.id.groups_more:
                loadPage("https://m.facebook.com/groups_browse/");
                break;

            case R.id.requests_more:
                loadPage("https://m.facebook.com/friends/center/requests");
                break;

            case R.id.create_group:
                loadPage("https://m.facebook.com/groups/create/");
                break;


            case R.id.events_more:
                loadPage("https://m.facebook.com/events/");
                break;

            case R.id.pages_more:
                loadPage("https://m.facebook.com/pages/launchpoint/");
                break;

            case R.id.create_more:
                loadPage("https://m.facebook.com/pages/create/");
                break;


            case R.id.lang_more:
                loadPage("https://m.facebook.com/language.php");
                break;


            case R.id.privacy_more:
                loadPage("https://m.facebook.com/privacy/");
                break;

            case R.id.down:
                if(!Helpers.hasStoragePermission(getActivity())){
                    Helpers.requestStoragePermission(getActivity());
                }else {
                    Intent settings_right = new Intent(getActivity(), DownloadsActivity.class);
                    startActivity(settings_right);
                }
                break;


        }
    }

    private void loadPage(String url) {
        Intent link = new Intent(getActivity(), NewPageActivity.class);
        link.putExtra("url", url);
        startActivity(link);
    }

    private void loadWatch() {
        Intent link = new Intent(getActivity(), WatchActivity.class);
        link.putExtra("url", "https://m.facebook.com/watch/live");
        startActivity(link);
    }

    private void loadMarketPlace() {
        Intent link = new Intent(getActivity(), MarketPlaceActivity.class);
        link.putExtra("url", "https://m.facebook.com/marketplace");
        startActivity(link);
    }

    private void setLogout() {
        try {
            if (!getActivity().isDestroyed()) {
                new MaterialAlertDialogBuilder(getActivity())
                        .setTitle("Are you sure?")
                        .setMessage("Tap below to close Simple and logout.")
                        .setPositiveButton(R.string.ok, (arg0, arg1) -> {
                            try {
                                Toast.makeText(getActivity(), R.string.logged_out, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                prefManager.setFirstTimeLaunch(true);
                                StaticUtils.clearStrings();
                                Helpers.deleteCache(context);
                                StaticUtils.deleteCookies("https://.facebook.com");
                                StaticUtils.deleteCookies("https://.messenger.com");
                                if (PreferencesUtility.getBoolean("enable_notifications", false)) {
                                    mWorkManager.cancelAllWork();
                                }

                            } catch (Exception ignored) {

                            }
                        })
                .setNeutralButton(R.string.cancel, null)
                .show();
            }
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    public void onRefresh() {
        setProgress();
        menu_swipe.setRefreshing(false);
        scrollPosition = 0;
        new Handler().postDelayed(() -> {
            loadGroup();
            refreshUser();
            try{
                adapterUsers.restore();
            }catch (Exception ignored){

            }
            menu_swipe.setRefreshing(false);
        }, 800);
    }

    public void scrollToTop(){
        if (scrollPosition > 10) {
            scrollToTop(background);
        }else{
            onRefresh();
        }
    }



    private void loadGroup() {
        try {
            new Handler().postDelayed(() -> {
                if (InAppBadges.requestCount != null && InAppBadges.requestCount.length() > 0) {
                    if (Character.isDigit(InAppBadges.requestCount.charAt(0))) {
                        int firstChar = Character.getNumericValue(InAppBadges.requestCount.charAt(0));
                        if (firstChar <= 0) {
                            badge_requests.setVisibility(View.GONE);
                        } else {
                            badge_requests.setVisibility(View.VISIBLE);
                            badge_requests_text.setText(InAppBadges.requestCount);
                        }

                    }
                }

            }, 1500);
        } catch (Exception p) {
            p.printStackTrace();
        }
    }



    private void refreshUser() {
        new UserInfo().execute();

    }

    private void loadStart() {
        menu_swipe.setRefreshing(false);
        try {
            new Handler().postDelayed(() -> {
                loadGroup();
                menu_swipe.setRefreshing(false);
                adapterUsers.restore();
            }, 3000);
            refreshUser();
        } catch (Exception p) {
            p.printStackTrace();
        }
    }

    @Override
    public void loadBookmark(String title, final String url) {
        PreferencesUtility.putString("needs_lock", "false");
        new Handler().postDelayed(() -> {
            Intent link;
            if (url.contains("facebook.com/messages")) {
                link = new Intent(getActivity(), MessageActivity.class);
            } else {
                link = new Intent(getActivity(), NewPageActivity.class);
            }
            link.putExtra("url", url);
            startActivity(link);
        }, 100);

    }

    private void setProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(SPLASH_TIME_OUT);
        CountDownTimer countDownTimer = new CountDownTimer(SPLASH_TIME_OUT, SPLASH_TIME_OUT / 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress((int) (SPLASH_TIME_OUT - millisUntilFinished));
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);

            }
        };
        countDownTimer.start();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);
        if (this.isVisible()) {
            if (isFragmentVisible_ && !_hasLoadedOnce) {
                if(!PreferencesUtility.getBoolean("load_all", false)) {
                    loadStart();
                    setProgress();
                    try{
                        adapterUsers.restore();
                    }catch (Exception ignored){

                    }
                }
                _hasLoadedOnce = true;
            }
        }
    }
}