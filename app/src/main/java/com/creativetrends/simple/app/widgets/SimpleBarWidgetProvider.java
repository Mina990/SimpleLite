/*
 * *
 *  * Created by Jorell Rutledge on 5/23/19 10:44 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 5/23/19 10:44 AM
 *
 */

package com.creativetrends.simple.app.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.creativetrends.simple.app.activities.PopupView;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.ThemeUtils;

public class SimpleBarWidgetProvider extends AppWidgetProvider {

    final String UPDATE_WIDGET = "updateWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            for (int appWidgetId : appWidgetIds) {
                Intent updateWidget = new Intent(context, SimpleBarService.class);
                updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                updateWidget.setData(Uri.parse(updateWidget.toUri(Intent.URI_INTENT_SCHEME)));
                RemoteViews views = new RemoteViews(context.getPackageName(), ThemeUtils.getSimplebarTheme(context));
                AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context,  R.id.simple_bar_profile, views, appWidgetIds);

                if (BadgeHelper.getCookie() != null) {
                    views.setTextViewText(R.id.simple_bar_profile_name, context.getResources().getString(R.string.app_name_pro));
                    Glide
                            .with(context.getApplicationContext()) // safer!
                            .asBitmap()
                            .load(PreferencesUtility.getString("user_picture", ""))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_fb_round)
                            .error(R.drawable.ic_fb_round)
                            .apply(new RequestOptions().circleCrop())
                            .into(appWidgetTarget);
                } else {
                    views.setTextViewText(R.id.simple_bar_profile_name, context.getResources().getString(R.string.app_name_pro));
                    Glide
                            .with(context.getApplicationContext() ) // safer!
                            .asBitmap()
                            .load(R.drawable.ic_fb_round)
                            .into(appWidgetTarget);
                }

                Intent simple = new Intent(context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()));
                simple.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                simple.putExtra("from_mess", false);
                simple.putExtra("from_no", false);
                PendingIntent simpleIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, simple, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.simple_bar_launch, simpleIntent);


                Intent profile = new Intent(context, PopupView.class);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                profile.putExtra("url", "https://m.facebook.com/profile.php");
                profile.setAction(Intent.ACTION_VIEW);
                profile.putExtra("from_mess", false);
                profile.putExtra("from_no", false);
                PendingIntent profileIntent = PendingIntent.getActivity(context.getApplicationContext(), 1, profile, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.simple_bar_profile, profileIntent);

                Intent news = new Intent(context, PopupView.class);
                news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                if (PreferencesUtility.getBoolean("top_news", false)) {
                    news.putExtra("url", "https://m.facebook.com/home.php?sk=h_nor");
                } else {
                    news.putExtra("url", "https://m.facebook.com/home.php?sk=h_chr");
                }
                news.setAction(Intent.ACTION_VIEW);
                news.putExtra("from_mess", false);
                news.putExtra("from_no", false);
                PendingIntent newsIntent = PendingIntent.getActivity(context.getApplicationContext(), 2, news, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.simple_bar_news_feed, newsIntent);


                Intent messages = new Intent(context, PopupView.class);
                messages.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                messages.putExtra("url", "https://m.facebook.com/messages");
                messages.setAction(Intent.ACTION_VIEW);
                messages.putExtra("from_mess", true);
                messages.putExtra("from_no", false);
                PendingIntent messagesIntent = PendingIntent.getActivity(context.getApplicationContext(), 3, messages, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.simple_bar_messages, messagesIntent);

                Intent notifications = new Intent(context, PopupView.class);
                notifications.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                notifications.putExtra("url", "https://m.facebook.com/notifications.php");
                notifications.setAction(Intent.ACTION_VIEW);
                notifications.putExtra("from_mess", false);
                notifications.putExtra("from_no", true);
                PendingIntent notificationsIntent = PendingIntent.getActivity(context.getApplicationContext(), 4, notifications, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.simple_bar_notifications, notificationsIntent);


                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.simple_bar_profile);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (UPDATE_WIDGET.equals(intent.getAction())) {
            updateWidget(context);
        } else {
            super.onReceive(context, intent);
        }

    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass())));
    }


}