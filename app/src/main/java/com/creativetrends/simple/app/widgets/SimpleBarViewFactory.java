/*
 * *
 *  * Created by Jorell Rutledge on 5/23/19 10:46 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 5/23/19 10:46 AM
 *
 */

package com.creativetrends.simple.app.widgets;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class SimpleBarViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;

    SimpleBarViewFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


}
