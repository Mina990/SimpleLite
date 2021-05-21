package com.creativetrends.simple.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.R;
import com.google.android.material.card.MaterialCardView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThemeUtils {

    public static int getColorPrimary(Context context) {
        int color;
        if (PreferencesUtility.getBoolean("auto_night", false) && isNightTime()) {
            color = ContextCompat.getColor(context, R.color.black);
        } else {
            color = PreferencesUtility.getInt("custom_color", 0);
        }
        return color;
    }

    public static int getColorPrimaryDark() {
        int color = PreferencesUtility.getInt("custom_color", 0);
        color = StaticUtils.darkColor(color);
        return color;
    }

    public static int getDefaultPrimary(Context context) {
        int color;
        if (PreferencesUtility.getBoolean("auto_night", false) && isNightTime()) {
            color = ContextCompat.getColor(context, R.color.black);
        } else {
            color = ContextCompat.getColor(context, R.color.new_facebook);
        }
        return color;
    }


    public static int getTheme(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.dcP);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.dark);
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.dcP);
                default:
                    return ContextCompat.getColor(context, R.color.defaultcolor);
            }

        }

    }

    public static int getThemeCard(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.card_darker);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.card_dark);
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.card_darker);
                default:
                    return ContextCompat.getColor(context, R.color.card_light);
            }

        }

    }


    public static void getCardStroke(Context context, MaterialCardView materialCardView) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            materialCardView.setStrokeColor(Color.parseColor("#151515"));
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    materialCardView.setStrokeColor(Color.parseColor("#333333"));
                    break;
                case "darktheme":
                    materialCardView.setStrokeColor(Color.parseColor("#151515"));
                    break;
                default:
                    materialCardView.setStrokeColor(ContextCompat.getColor(context, R.color.m_color));
                    break;
            }

        }

    }

    public static int getThemePost(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.card_darker);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.card_dar);
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.card_darker);
                default:
                    return ContextCompat.getColor(context, R.color.defaultcolor);
            }

        }

    }


    public static void setTabs(Context context, AHBottomNavigation bottomNavigation) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            bottomNavigation.setInactiveColor(ContextCompat.getColor(context, R.color.m_color));
            bottomNavigation.setAccentColor(ContextCompat.getColor(context, R.color.white));
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                case "darktheme":
                    bottomNavigation.setInactiveColor(ContextCompat.getColor(context, R.color.m_color));
                    bottomNavigation.setAccentColor(ContextCompat.getColor(context, R.color.white));
                    break;
                default:
                    bottomNavigation.setInactiveColor(ContextCompat.getColor(context, R.color.m_color));
                    bottomNavigation.setAccentColor(ThemeUtils.getColorPrimary(context));
                    break;
            }

        }

    }

    public static void setTabsPins(Context context, AHBottomNavigation bottomNavigation) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            bottomNavigation.setInactiveColor(ContextCompat.getColor(context, R.color.white));
            bottomNavigation.setAccentColor(ContextCompat.getColor(context, R.color.white));
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                case "amoledtheme":
                case "darktheme":
                    bottomNavigation.setInactiveColor(ContextCompat.getColor(context, R.color.white));
                    bottomNavigation.setAccentColor(ContextCompat.getColor(context, R.color.white));
                    break;
                default:
                    bottomNavigation.setInactiveColor(ThemeUtils.getColorPrimaryDark());
                    bottomNavigation.setAccentColor(ThemeUtils.getColorPrimaryDark());
                    break;
            }

        }

    }

    public static int getSheetNav(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return ContextCompat.getColor(context, R.color.black);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return ContextCompat.getColor(context, R.color.darcula);
                case "amoledtheme":
                case "darktheme":
                    return ContextCompat.getColor(context, R.color.black);

                default:
                    if(StaticUtils.isOreo()) {
                        return ContextCompat.getColor(context, R.color.white);
                    }else {
                        return ContextCompat.getColor(context, R.color.light_nav);
                    }
            }

        }

    }


    public static int getMenu(Context context) {
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

    public static void setSettingsTheme(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            context.setTheme(R.style.AppThemeBlack);
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    context.setTheme(R.style.AppThemeDarkSettings);
                    return;
                case "darktheme":
                    context.setTheme(R.style.AppThemeBlackSettings);
                    return;
                default:
                    context.setTheme(R.style.FalconSettings);
            }
        }

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectNightCSS(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("materialamoled.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectAmoledCSS(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("materialblack.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectFolioCSS(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("materiallight.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectDraculaCSS(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("materialdark.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* messenger */

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectMessengerBlack(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("blackmessenger.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectMessenger(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("messenger.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void injectMessengerDark(Context context, WebView view) {
        try {
            InputStream inputStream = context.getAssets().open("darkmessenger.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static void injectJS(WebView webView) {
        webView.loadUrl("javascript: var links = document.getElementsByTagName('a');\nfor (var i = 0; i < links.length; i++) {\n\t if (links[i].hasAttribute(\"href\") && !links[i].getAttribute(\"href\").endsWith(\"#\") && !links[i].hasAttribute(\"target\")){\n\t\tlinks[i].setAttribute(\"target\",\"_blank\");\n\t\tif (links[i].getAttribute(\"href\").contains(\"photo\") && !links[i].getAttribute(\"href\").contains(\"&notif\") && !links[i].getAttribute(\"href\").contains(\"photo_attachments\") && !links[i].getAttribute(\"href\").contains(\"media_with_effects\") && !links[i].getAttribute(\"href\").contains(\"__tn__=-R\") && !links[i].getAttribute(\"href\").contains(\"%2AW-R\") && !links[i].getAttribute(\"href\").contains(\"EntVideoCreationStory\") && !links[i].getAttribute(\"href\").contains(\"&__tn__=%7E-R\") && !links[i].getAttribute(\"href\").contains(\"&__tn__=%7EH-R\") && !links[i].getAttribute(\"href\").contains(\"&__tn__=~-R\")  && !links[i].getAttribute(\"href\").contains(\"EntStatusCreationStory\") && !links[i].getAttribute(\"href\").contains(\"/photos/pcb.\")  && !links[i].getAttribute(\"href\").contains(\"/photos/p.\")){\n\t\t\tlinks[i].onclick = function(event){event.preventDefault();}\t\t }\n\t }\n}");
    }



    public static void pageStarted(Context context, WebView view) {
        try {
            StringBuilder extra = new StringBuilder("javascript:function addStyleString(str) { var node = document.createElement('style'); node.innerHTML = str; document.head.appendChild(node); } ");
            extra.append("addStyleString('._129-{ margin-top: -46px !important; }');");

            extra.append("addStyleString('#mbasic_inline_feed_composer{ display: none !important; }');");
            extra.append("addStyleString('#MComposer{display: none !important;}');");

            if (PreferencesUtility.getBoolean("hide_people", false)) {
                extra.append("addStyleString('article[data-ft*=\"ei\":\"\"]{ display: none !important; }');addStyleString('._55wo._5rgr._5gh8._5gh8._35au, ._2dr, ._d2r { display: none !important; }');");
            }

            if (PreferencesUtility.getBoolean("hide_new", false)) {
                extra.append("addStyleString('._32iy{display: none !important;}');");
            }


            if (PreferencesUtility.getBoolean("select", false)) {
                extra.append("addStyleString('a._5msj{display:none!important;}._5rgr{-webkit-user-select:initial!important;}');");
            }

            if (PreferencesUtility.getBoolean("hide_ads", false)) {
                extra.append("addStyleString('article._d2r {display: none;}');addStyleString('article[data-xt*=sponsor]{display: none !important;}'); addStyleString('article[data-store*=sponsor]{display: none !important;}');");
            }

            extra.append("addStyleString('article[data-story_category=\"4\"]{display: none !important;}');");

            if (PreferencesUtility.getBoolean("full_width", false)) {
                extra.append("addStyleString('article, #mbasic_inline_feed_composer, .timelinePublisher, ._10c_ ._2jl2, ._55wr._4g33._52wc._2-ck._51s, ._59e9._55wr._4g33._400s, .touch ._3iyw ._37fb, ._55wo._4g34, #friends_center_main, .touch._5b6o:not(.touchable),  ._2ykg .unreadMessage, #owned_pages_section, #liked_pages_section, ._gui, .item.tall.acw.abb, ._4g34._5o41, [data-sigil*=\"section-loader\"]{margin: 12px 12px !important;}');addStyleString('._3bg5 ._53_-, li, ._55wq ._4g33 ._5pxa ._5vbx, ._1_oa._4gvh._1w-s._1_od, ._1_oa._4gvh._1w-s, .item.buddylistItem.itemWithAction.acw.abt, div[data-sigil*=\"notification marea\"], [id*=\"threadlist_row_\"]{margin: 4px 8px !important;}');");
            }

            if (PreferencesUtility.getBoolean("stories", false)) {
                extra.append("addStyleString('._vi6 {display: none !important;}');");
                extra.append("addStyleString('#MStoriesTray {display: none !important;}');");
            }


            extra.append("addStyleString('.touch ._400s {display: none !important;}');");
            extra.append("addStyleString('._55wr._4g33._400s {display: none !important;}');");
            extra.append("addStyleString('._vqv{ display: none !important; }');");

            extra.append("addStyleString('._46e0 { display: none; }');addStyleString('._5xjd { display: none; }');addStyleString('#toggleHeader, .h.i#header, .i.j#header, #m_home_notice { display: none; }');addStyleString('header._4o57 { display:inline; }');");
            view.loadUrl(extra.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pageFinished(WebView view, String url) {
        try {
            StringBuilder makeCSS = new StringBuilder("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.body.appendChild(node); } ");

            if (null != url) {
                if (url.contains("sharer") || url.contains("/composer/") || url.contains("/saved")) {
                    makeCSS.append("addStyleString('._129-{ margin-top: -1px !important; }');");
                } else {
                    makeCSS.append("addStyleString('._129-{ margin-top: -46px !important; }');");
                }
            }
            makeCSS.append("addStyleString('[data-sigil*=m-promo-jewel-header]{ display: none !important; }');addStyleString('div#messages_flyout{ display: none !important; }');");

            makeCSS.append("addStyleString('._46e0 { display: none; }');addStyleString('._5xjd { display: none !important; }');addStyleString('#toggleHeader, .h.i#header, .i.j#header { display: none !important; }');");
            view.loadUrl(makeCSS.toString());
        } catch (Throwable i) {
            i.printStackTrace();
        }
    }


    public static void injectPhotos(WebView webView) {
        webView.loadUrl("javascript:var targetNode = document.querySelector('body');\nvar config = { attributes: true, childList: true, subtree: true };\nvar callback = function(mutationsList, observer) {\nvar classname_home = document.querySelectorAll(\"._7ab_\");\nfor (var i = 0; i < classname_home.length; i++) {\nif(classname_home[i].parentNode.hasAttribute(\"href\") || classname_home[i].parentNode.parentNode.hasAttribute(\"href\")) {\nclassname_home[i].parentNode.removeAttribute(\"href\") || classname_home[i].parentNode.parentNode.removeAttribute(\"href\");\nclassname_home[i].addEventListener('click', function(event) {\nvar targetElement = event.target || event.srcElement;\nvar hr = targetElement.querySelector('div i');\nvar style = hr.currentStyle || window.getComputedStyle(hr, false);\nvar bi = style.backgroundImage.slice(4, -1).replace(/\"/g, \"\");\nPhotos.getImage(bi);})}}\nvar classname = document.getElementsByClassName(\"_i81\");\nfor (var i = 0; i < classname.length; i++) {\nvar dd = classname[i].getAttribute(\"data-sheets\");\nif (!dd) {\nclassname[i].setAttribute(\"data-sheets\", \"simple\");\nclassname[i].addEventListener('click', function(event) {\nvar get = this.querySelector('img');\nvar base = get.src;\nPhotos.getImage(base);});}}};\nvar observer = new MutationObserver(callback);\nobserver.observe(targetNode, config);\n");

    }


    public static void hideClose(WebView view) {
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('._4wqq {display: none !important;}');addStyleString('._52z7 {display: none !important;}');");
    }



    public static void removeEditor(WebView view) {
        String CSS = "javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('#mbasic_inline_feed_composer{ display: none !important; }');" +
                "addStyleString('#MComposer > div > div,._6beq{display: none !important;}');";
        view.loadUrl(CSS);
    }


    public static void injectPadding(WebView view) {
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('#page  {padding-bottom:66px !important;}');");

    }

    public static void injectTextMessages(WebView view) {
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('._3-8h  {display: none !important;}');");

    }


    public static void injectTextMessenger(WebView view) {
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('._32uu ._4uwq.transparent {display: none !important;}');");
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('._3n07 {display: none !important;}');");
    }

    public static void injectTextGroups(WebView view) {
        view.loadUrl("javascript:function addStyleString(str) { var node = document.createElement('style');node.innerHTML = str; document.head.appendChild(node); } " + "addStyleString('._7k1c  {display: none !important;}');");
    }

    public static void clickMessages(WebView webView) {
        webView.loadUrl("javascript: setTimeout(function(){\n\t(function(element) {\nvar event;\n\t\t\t\tevent = document.createEvent('MouseEvents');\n\t\t\t\tevent.initMouseEvent('mousedown', true, true, window);\n\t\t\t\telement.dispatchEvent(event);\n})(document.querySelector('#root select'));\n\t\n}, 33)");
    }



    public static void facebookTheme(Context context, WebView view) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            injectNightCSS(context, view);
        } else {
            switch (PreferencesUtility.getInstance(SimpleApplication.getContextOfApplication()).getFreeTheme()) {
                case "draculatheme":
                    injectDraculaCSS(context, view);
                    break;
                case "darktheme":
                    injectAmoledCSS(context, view);
                    break;
                default:
                    injectFolioCSS(context, view);
                    break;
            }
        }
    }


    public static void MessengerTheme(Context context, WebView view) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            injectMessengerBlack(context, view);
        } else {
            switch (PreferencesUtility.getInstance(SimpleApplication.getContextOfApplication()).getFreeTheme()) {
                case "draculatheme":
                    injectMessengerDark(context, view);
                    break;
                case "darktheme":
                    injectMessengerBlack(context, view);
                    break;
                default:
                    injectMessenger(context, view);
                    break;
            }
        }
    }


    private static String startNightTime() {
        return PreferencesUtility.getString("startTime", "1900");
    }

    private static String endNightTime() {
        return PreferencesUtility.getString("endTime", "0700");
    }


    @SuppressLint("WrongConstant")
    public static boolean isNightTime() {
        if (!PreferencesUtility.getBoolean("auto_night", false)) {
            return false;
        }
        String nightStart = startNightTime();
        String nightEnd = endNightTime();
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.SECOND, 0);
        Date now = calNow.getTime();
        String timeNow = new SimpleDateFormat("HHmm", Locale.getDefault()).format(now);

        int startTime = Integer.parseInt(nightStart);
        int endTime = Integer.parseInt(nightEnd);
        int nowTime = Integer.parseInt(timeNow);

        if (startTime > endTime) {
            return nowTime > startTime || nowTime < endTime;
        } else if (startTime < endTime) {
            return nowTime >= startTime && nowTime <= endTime;
        } else {
            return false;
        }
    }

    public static void switchTheme(Context context, WebView webView, SwipeRefreshLayout swipeRefreshLayout) {
        StaticUtils.setSwipeColor(swipeRefreshLayout, context);
        removeThemes(webView);
        facebookTheme(context, webView);
    }

    public static void removeThemes(WebView webView) {
        webView.loadUrl("javascript: var styles = document.getElementsByClassName('style');while(styles[0]) { styles[0].parentNode.removeChild(styles[0]); }");
    }

    public static void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;   // add LIGHT_STATUS_BAR to flag
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    public static void clearLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // use XOR here for remove LIGHT_STATUS_BAR from flags
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    public static void setLightNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;   // add LIGHT_STATUS_BAR to flag
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.getWindow().setNavigationBarDividerColor(getTheme(SimpleApplication.getContextOfApplication()));
            }
        }
    }

    public static void clearLightNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; // use XOR here for remove LIGHT_STATUS_BAR from flags
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.getWindow().setNavigationBarDividerColor(getTheme(SimpleApplication.getContextOfApplication()));
            }
        }
    }


    public static int getSimplebarTheme(Context context) {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return R.layout.simple_bar_widget_black;
        } else {
            switch (PreferencesUtility.getInstance(context).getFreeTheme()) {
                case "draculatheme":
                    return R.layout.simple_bar_widget_dark;
                case "darktheme":
                case "amoledtheme":
                    return R.layout.simple_bar_widget_black;
                default:
                    return R.layout.simple_bar_widget_light;
            }

        }


    }

}
