/*
 * *
 *  * Created by Jorell Rutledge on 3/11/19 2:04 PM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 10/26/18 9:42 AM
 *
 */
package com.creativetrends.simple.app.adapters;


import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.creativetrends.simple.app.mainfragments.FragmentFeed;
import com.creativetrends.simple.app.mainfragments.FragmentGroups;
import com.creativetrends.simple.app.mainfragments.FragmentMore;
import com.creativetrends.simple.app.mainfragments.FragmentNotifications;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private FragmentFeed feed;
    private FragmentGroups groups;
    private FragmentNotifications notifications;
    private FragmentMore more;

    @SuppressWarnings("deprecation")
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                feed = new FragmentFeed();
                return feed;
            case 1:
                groups = new FragmentGroups();
                return groups;
            case 2:
                notifications = new FragmentNotifications();
                return notifications;
            case 3:
                more = new FragmentMore();
                return more;
            default:
                return null;
        }
    }

    public void resetTab(int position) {
        switch (position) {
            case 0:
                if (feed != null) {
                    feed.scrollToTop();
                }
                break;
            case 1:
                if (groups != null) {
                    groups.scrollToTop();
                }
                break;
            case 2:
                if (notifications != null) {
                    notifications.scrollToTop();
                }
                break;
            case 3:
                if (more != null) {
                    more.scrollToTop();
                }
                break;
        }
    }

    public void loadRecent(int position) {
        switch (position) {
            case 0:
                if (feed != null) {
                    feed.loadRecent();
                }
                break;
            case 1:
            case 3:
            case 2:

                break;
        }
    }

    public void loadTop(int position) {
        switch (position) {
            case 0:
                if (feed != null) {
                    feed.loadTop();
                }
                break;
            case 1:
            case 2:
            case 3:

                break;
        }
    }


    public void loadMark(int position) {
        switch (position) {
            case 0:
            case 1:
            case 3:
            case 2:
                if (notifications != null) {
                    notifications.loadNotif();
                }
                break;
        }
    }

    public void loadSet(int position) {
        switch (position) {
            case 0:
            case 1:
            case 3:
            case 2:
                if (notifications != null) {
                    notifications.loadPage();
                }
                break;
        }
    }

    public void loadFeed(int position) {
        switch (position) {
            case 0:
                if (feed != null) {
                    feed.getLoad();
                }
                break;
            case 1:
            case 3:
            case 2:
                break;
        }
    }

    public void loadDis(int position) {
        switch (position) {
            case 0:
            case 1:
                if (groups != null) {
                    groups.loadDiscover();
                }
                break;
            case 3:
            case 2:

                break;
        }
    }

    public void loadCre(int position) {
        switch (position) {
            case 0:
            case 1:
                if (groups != null) {
                    groups.loadCreate();
                }
                break;
            case 2:
            case 3:

                break;
        }
    }


    public void onPageChange(int position) {
        switch (position) {
            case 0:
                if (feed != null && feed.mWebView != null) {
                    feed.mWebView.onResume();
                    feed.mWebView.resumeTimers();
                }
                break;
            case 1:
                if (groups != null && groups.mWebView != null) {
                    groups.mWebView.onResume();
                    groups.mWebView.resumeTimers();
                }
                break;
            case 2:
                if (notifications != null && notifications.mWebView != null) {
                    notifications.mWebView.onResume();
                    notifications.mWebView.resumeTimers();
                }
                break;
            case 3:
                break;
        }
    }

}