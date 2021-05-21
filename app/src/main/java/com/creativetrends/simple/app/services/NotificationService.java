package com.creativetrends.simple.app.services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.activities.MessageActivity;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.helpers.Helpers;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.EUCheck;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Pattern;

public class NotificationService extends Worker {
    private static final String TAG;

    static {
        TAG = NotificationService.class.getSimpleName();
    }

    String user;
    private SharedPreferences preferences;

    public static void clearMessages(Context context) {
        NotificationManagerCompat mNotificationManager = (NotificationManagerCompat.from(context));
        mNotificationManager.cancel(1);
    }

    public static void clearNotifications(Context context) {
        NotificationManagerCompat mNotificationManager = (NotificationManagerCompat.from(context));
        mNotificationManager.cancel(2);
    }


    public NotificationService (@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            String cookie = cookieManager.getCookie("https://m.facebook.com");
            if (TextUtils.isEmpty(cookie)) {
                System.out.println(TAG + "sync finished");
                return Worker.Result.failure();
            }
            if (PreferencesUtility.getBoolean("notifications_activated", false))
                try {
                    CheckNotificationsTask(cookie);
                } catch (Exception ignored) {
                }
            if (PreferencesUtility.getBoolean("messages_activated", false))
                try {
                    CheckMessagesTask(cookie);
                } catch (Exception e) {
                    e.printStackTrace();
                    CheckMessagesTaskWorkAround(cookie);
                }
        } catch (Exception e) {
            e.printStackTrace();
            return Worker.Result.failure();
        }
        return Worker.Result.success();
    }



    @Override
    public void onStopped() {
        super.onStopped();
    }


    public void CheckNotificationsTask(String cookie) throws Exception {
        Document doc = Jsoup.connect("http://m.facebook.com/notifications")
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .cookie("https://m.facebook.com", cookie).get();
        Element pd = doc.select("div.aclb > div.touchable-notification").first();
        Element at = pd.select("abbr[data-sigil]").first();
        JSONObject dataObject = new JSONObject(at.attr("data-store"));
        long notificationTime = dataObject.getLong("time") * 1000;
        long lastnotificationTime = PreferencesUtility.getLastNotificationTime();
        if (notificationTime > lastnotificationTime) {
            Element aLink = pd.select("a[href]").first();
            String link = "https://m.facebook.com" + aLink.attr("href");
            String time = at.text();
            Element iT = pd.select("div.ib > i").first();
            String ir = Pattern.quote("url(\"") + "(.*?)" + Pattern.quote("\")");
            String aI = StaticUtils.getTextBetween(iT.attr("style"), ir);
            if (Helpers.isEmpty(aI)) {
                String string8 = Pattern.quote("url('") + "(.*?)" + Pattern.quote("')");
                aI = StaticUtils.getTextBetween(iT.attr("style"), string8);
            }
            Elements sTs = pd.select("div.ib > div.c");
            String text = Html.fromHtml(sTs.html().replace(time, "").replaceAll("<[^>]*>", "")).toString();
            PreferencesUtility.setLastNotificationTime(notificationTime);
            notifier(getApplicationContext().getString(R.string.app_name_pro), text, link, false, Helpers.decodeImg(aI), notificationTime);
        }
    }

    public void CheckMessagesTask(String cookie) throws IOException {
        Document doc = Jsoup.connect("https://m.facebook.com/mobile/messages/jewel/content/?spinner_id=u_0_b")
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .cookie("https://m.facebook.com", cookie)
                .get();
        String body = doc.getElementsByTag("body").first().text();
        body = body.substring(9);

        String htmlRe = Pattern.quote("\"html\":\"") + "(.*?)" + Pattern.quote("},{");
        String html = StaticUtils.getTextBetween(body, htmlRe);
        html = html.substring(0, html.lastIndexOf(">\"")) + ">";

        html = StringEscapeUtils.unescapeJson(html);
        doc = Jsoup.parse(html);
        Element liElement = doc.select("ol._7k7.inner > li.item").first();
        if (liElement.toString().contains("aclb")) {
            Element aElement = liElement.select("a.touchable.primary[href]").first();
            String sMlink = aElement.attr("href");
            Element img = liElement.select("i.img._33zg.profpic").first();
            String image = img.attr("style");
            String sIRegex = Pattern.quote("url(\"") + "(.*?)" + Pattern.quote("\")");
            String sImg = StaticUtils.getTextBetween(image, sIRegex);
            if (Helpers.isEmpty(sImg)) {
                sImg = StaticUtils.getTextBetween(image, Pattern.quote("url('") + "(.*?)" + Pattern.quote("')"));
            }
            Element stimeElement = aElement.select("div.content > div.lr > div.time > abbr").first();
            String sTimeRegex = Pattern.quote("time\":") + "(.*?)" + Pattern.quote(",\"");
            if (TextUtils.isEmpty(sTimeRegex)) {
                sTimeRegex = StaticUtils.getTextBetween(stimeElement.toString(), Pattern.quote("time':") + "(.*?)" + Pattern.quote(",'"));
            }
            String sTimeString = StaticUtils.getTextBetween(stimeElement.toString(), sTimeRegex);


            long smime = Long.parseLong(sTimeString) * 1000;
            long lastMessageTime = PreferencesUtility.getLastMessageTime();


            Element sNameElement = aElement.select("div.content > div.lr > div.title").first();
            Element sMessElement = aElement.select("div.content > div.oneLine.preview").first();

            if (smime > lastMessageTime) {
                    String title = sNameElement.text().replace("(", "").replace(")", "").replace("()", "").replaceAll("[0-9]", "");
                    String message;
                    if (TextUtils.isEmpty(sMessElement.text())) {
                        try {
                            message = getApplicationContext().getResources().getString(R.string.sent_a_message, title.substring(0, title.indexOf(" ")));
                        } catch (StringIndexOutOfBoundsException i) {
                            message = getApplicationContext().getResources().getString(R.string.sent_a_message, sNameElement.text().replace("(", "").replace(")", "").replace("()", "").replaceAll("[0-9]", ""));
                        }
                    } else if (sMessElement.text().contains(":") && sMessElement.text().contains("\uDB80\uDC00") && !sMessElement.text().contains("Thug Life")) {
                        message = sMessElement.text().replace("\uDB80\uDC00", getApplicationContext().getResources().getString(R.string.thumb));
                    } else if(sMessElement.text().equals("\uDB80\uDC00")){
                        try {
                            message = getApplicationContext().getResources().getString(R.string.sent_a_message, title.substring(0, title.indexOf(" ")));
                        } catch (StringIndexOutOfBoundsException i) {
                            message = getApplicationContext().getResources().getString(R.string.sent_a_message, sNameElement.text().replace("(", "").replace(")", "").replace("()", "").replaceAll("[0-9]", ""));
                        }
                    } else {
                        message = sMessElement.text();
                    }
                    PreferencesUtility.setLastMessageTime(smime);
                    notifier(title, message, "https://m.facebook.com" + sMlink, true, Helpers.decodeImg(sImg), smime);

                }
            }
    }

    private void CheckMessagesTaskWorkAround(String cookie) throws IOException {
        Document doc = Jsoup.connect("http://m.facebook.com/messages")
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36")
                .cookie("https://m.facebook.com", cookie).get();
        try {
            for (Element tag : doc.getElementsByTag("script")) {
                for (DataNode node : tag.dataNodes()) {
                    if (node.getWholeData().contains("_2ykg")) {
                        String contentRegex = Pattern.quote("\"content\":") + "(.*?)" + Pattern.quote(",\"pageletConfig\"");
                        String contentObjString = StaticUtils.getTextBetween(node.toString(), contentRegex);
                        JSONObject content = new JSONObject(contentObjString);
                        doc = Jsoup.parse(content.getString("__html"));
                        break;
                    }
                }
            }
            Element divParent = doc.select("div._2ykg").first().select("div").first().select("div.aclb").first();

            Element abbrTag = divParent.select("abbr[data-sigil]").first();
            JSONObject dataObject = new JSONObject(abbrTag.attr("data-store"));
            long messageTime = dataObject.getLong("time") * 1000;

            Element imgTag = divParent.child(0).select("div._5xu4").first().select("i").first();
            String imagRegex = Pattern.quote("url(\"") + "(.*?)" + Pattern.quote("\")");
            String avatarImg = StaticUtils.getTextBetween(imgTag.attr("style"), imagRegex);
            if (Helpers.isEmpty(avatarImg)) {
                avatarImg = StaticUtils.getTextBetween(imagRegex, Pattern.quote("url('") + "(.*?)" + Pattern.quote("')"));
            }

            Element headerTag = divParent.select("div._5xu4 > header").first();
            String title = headerTag.select("h3").first().text();
            String message = headerTag.select("h3").get(1).text();
            if (TextUtils.isEmpty(message)) {
                message = getApplicationContext().getResources().getString(R.string.sent_a_message, title);
            }
            Element aTag = divParent.select("div > div._5xu4 > a._5b6s").first();
            String link = aTag.attr("href");
            if (PreferencesUtility.getString("last_message_text", "").equals(message + " " + user) && !user.equals(BadgeHelper.getCookie())) {

                return;
            }

            if (user.equals(BadgeHelper.getCookie())) {
                return;
            }

            PreferencesUtility.putString("last_message_text", message + " " + user);
            notifier(title, message, "https://m.facebook.com" + link, true, Helpers.decodeImg(avatarImg), messageTime);

            Log.d(message, user);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    private void notifier(String notificationTitle, String notificationText, final String url, boolean isMessage, final String image_url, long time) {
        String ringtoneKey, vibrate_, led_, channel, name, des;
        Bitmap avatar;
        if (image_url != null) {
            avatar = Helpers.getImage(image_url);
        } else {
            avatar = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        }

        if (isMessage) {
            ringtoneKey = "ringtone_msg";
            channel = "com.creativetrends.simple.app.lite.messages";
            name = "Simple Lite Messages";
            des = "Notification settings for Simple Lite message notifications.";
        } else {
            ringtoneKey = "ringtone";
            channel = "com.creativetrends.simple.app.lite.notifications";
            name = "Simple Lite Notifications";
            des = "Notification settings for Simple Lite Facebook notifications.";
        }
        vibrate_ = "vibrate";
        led_ = "led_light";
        Uri ringtoneUri = Uri.parse(preferences.getString(ringtoneKey, "content://settings/system/notification_sound"));
        if (ringtoneUri != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //noinspection ResultOfMethodCallIgnored
                    ringtone.getAudioAttributes();
                }
            }
        }
        final NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert mNotificationManager != null;
            NotificationChannel notificationChannel = new NotificationChannel(channel, name, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(des);
            notificationChannel.setShowBadge(true);
            notificationChannel.enableVibration(preferences.getBoolean(vibrate_, false));
            notificationChannel.enableLights(preferences.getBoolean(led_, false));
            if (preferences.getBoolean(vibrate_, false)) {
                notificationChannel.setVibrationPattern(new long[]{500, 500});

            }
            if (preferences.getBoolean(led_, false))
                notificationChannel.setLightColor(Color.BLUE);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), channel)
                .setContentText(notificationText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setWhen(time)
                .setShowWhen(true)
                .setLargeIcon(avatar)
                .setSound(ringtoneUri);


        if (preferences.getBoolean(vibrate_, false)) {
            mBuilder.setVibrate(new long[]{500, 500});
        } else {
            mBuilder.setVibrate(new long[]{0L});
        }

        if (notificationTitle != null) {
            mBuilder.setContentTitle(notificationTitle);
        } else {
            mBuilder.setContentTitle(getApplicationContext().getString(R.string.app_name_pro));
        }

        if (preferences.getBoolean(led_, false)) {
            mBuilder.setLights(Color.BLUE, 500, 2000);
        }

        mBuilder.setAutoCancel(true);

        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        if (isMessage) {
            if(EUCheck.isEU(getApplicationContext())) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                intent.putExtra("url","https://messenger.com/t/"+StaticUtils.getUserId(url));
                PreferencesUtility.putString("needs_lock", "true");
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setSmallIcon(R.drawable.ic_mess);
                if (mNotificationManager != null) {
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }else{
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setData(Uri.parse(url));
                PreferencesUtility.putString("needs_lock", "true");
                PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.setSmallIcon(R.drawable.ic_mess);
                if (mNotificationManager != null) {
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setData(Uri.parse(url));
            PreferencesUtility.putString("needs_lock", "true");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setSmallIcon(R.drawable.ic_simple_s);
            if (mNotificationManager != null) {
                mNotificationManager.notify(2, mBuilder.build());
            }


        }
    }

}