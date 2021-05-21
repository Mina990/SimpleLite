package com.creativetrends.simple.app.settingfragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.lite.BuildConfig;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NetworkConnection;
import com.creativetrends.simple.app.utils.FacebookStatusBugs;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.text.WordUtils;


@SuppressWarnings("ALL")
public class Settings extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public boolean mListStyled;
    Context context;
    SharedPreferences.OnSharedPreferenceChangeListener myPrefListner;
    SharedPreferences preferences;

    Preference versionnumber, n, versionCode, buildType, buildDate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.settings);

        myPrefListner = (prefs, key) -> preferences.edit().putString("changed", "true").apply();

        versionnumber = findPreference("version_number");

        versionCode = findPreference("version_code");

        buildType = findPreference("version_type");

        buildDate = findPreference("version_date");

        versionnumber.setSummary(BuildConfig.VERSION_NAME);
        versionCode.setSummary(String.valueOf(BuildConfig.VERSION_CODE));
        buildType.setSummary(WordUtils.capitalize(BuildConfig.BUILD_TYPE));
        buildDate.setSummary(BuildConfig.BUILD_DATE);


        Preference browse = findPreference("browse_key");
        Preference customize = findPreference("customize_key");
        Preference facebook = findPreference("facebook_key");
        Preference notifications = findPreference("notifications_key");
        Preference privacy = findPreference("privacy_key");
        Preference tabs = findPreference("tabs_key");
        Preference priva = findPreference("privacy");
        Preference terms = findPreference("terms");
        Preference vids = findPreference("vid");
        Preference status = findPreference("check_status");
        browse.setOnPreferenceClickListener(this);
        customize.setOnPreferenceClickListener(this);
        facebook.setOnPreferenceClickListener(this);
        notifications.setOnPreferenceClickListener(this);
        privacy.setOnPreferenceClickListener(this);
        tabs.setOnPreferenceClickListener(this);
        priva.setOnPreferenceClickListener(this);
        terms.setOnPreferenceClickListener(this);
        vids.setOnPreferenceClickListener(this);
        status.setOnPreferenceClickListener(this);


    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {

            case "browse_key":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Browsing()).commit();
                break;

            case "customize_key":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Customize()).commit();
                break;

            case "facebook_key":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Facebook()).commit();
                break;

            case "notifications_key":
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    getFragmentManager().beginTransaction()
                            .addToBackStack(null).replace(R.id.settings_frame, new NotificationsOreo()).commit();
                }else{
                    getFragmentManager().beginTransaction()
                            .addToBackStack(null).replace(R.id.settings_frame, new Notifications()).commit();
                }
                break;

            case "privacy_key":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Privacy()).commit();
                break;

            case "tabs_key":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Tabs()).commit();
                break;

            case "vid":
                getFragmentManager().beginTransaction()
                        .addToBackStack(null).replace(R.id.settings_frame, new Video()).commit();
                break;



            case "privacy":
                if(!getActivity().isDestroyed()) {
                    StaticUtils.showPolicy(getActivity());
                }
                break;

            case "terms":
                if(!getActivity().isDestroyed()) {
                    StaticUtils.showTerms(getActivity());
                }
                break;

            case "check_status":
                if(NetworkConnection.isConnected(context) && FacebookStatusBugs.didCheck) {
                    showUpdateDialog(getActivity());
                }
                break;


        }

        return false;


    }


    @Override
    public void onStart() {
        super.onStart();
        View rootView = getView();
        if (rootView != null) {
            ListView list = rootView.findViewById(android.R.id.list);
            list.setPadding(0, 0, 0, 0);
            list.setDivider(null);
            list.setVerticalScrollBarEnabled(false);
            mListStyled = true;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Settings");
        preferences.registerOnSharedPreferenceChangeListener(myPrefListner);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(myPrefListner);
    }



    /*public void showDonateDialog() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.donate_1:
                        if(dialog.isShowing()) {
                            dialog.cancel();
                        }
                        if(bp.isPurchased(getResources().getString(R.string.c))){
                            Toast.makeText(context, "You've already made this donation. Thank you!", Toast.LENGTH_SHORT).show();
                        }else{
                            bp.purchase(getActivity(), getResources().getString(R.string.c));
                        }
                        break;
                    case R.id.donate_5:
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        if (bp.isPurchased(getResources().getString(R.string.l))) {
                            Toast.makeText(context, "You've already made this donation. Thank you!", Toast.LENGTH_SHORT).show();
                        } else {
                            bp.purchase(getActivity(), getResources().getString(R.string.l));
                        }
                        break;

                    case R.id.donate_15:
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        if (bp.isPurchased(getResources().getString(R.string.d))) {
                            Toast.makeText(context, "You've already made this donation. Thank you!", Toast.LENGTH_SHORT).show();
                        } else {
                            bp.purchase(getActivity(), getResources().getString(R.string.d));
                        }
                        break;

                    case R.id.donate_paypal:
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        donatePaypal();
                        break;

                    case R.id.donate_cash_app:
                        if (dialog.isShowing()) {
                            dialog.cancel();
                        }
                        donateCash();
                        break;
                }
            }
        };
        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.donate_dialog, null);
        view.findViewById(R.id.donate_1).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_5).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_15).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_paypal).setOnClickListener(clickListener);
        view.findViewById(R.id.donate_cash_app).setOnClickListener(clickListener);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(view);
        alert.setCancelable(true);
        try {
            dialog = alert.create();
            dialog.show();
        }catch(NullPointerException ignored){
        }catch (Exception i){
            i.printStackTrace();
        }

    }
    public void donatePaypal() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/creativetrends"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch(ActivityNotFoundException i){
            i.printStackTrace();
            Toast.makeText(context, "Couldn't open link :(.", Toast.LENGTH_SHORT).show();
        }
    }

    public void donateCash() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cash.me/app/TJVRPRS"));
            context.startActivity(browserIntent);
        }catch(ActivityNotFoundException i){
            i.printStackTrace();
            Toast.makeText(context, "Couldn't open link :(.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }*/

    @SuppressLint("SetTextI18n")
    private void showUpdateDialog(Activity activity) {
        if (!activity.isDestroyed()) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("InflateParams")
            View alertLayout = inflater.inflate(R.layout.status_dialog, null);
            MaterialAlertDialogBuilder update = new MaterialAlertDialogBuilder(getActivity());
            update.setTitle(getString(R.string.facebook_status));
            update.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                if(NetworkConnection.isConnected(context)) {
                    new FacebookStatusBugs().execute();
                }
            });
            update.setCancelable(false);
            update.setView(alertLayout);
            AlertDialog OptionDialog = update.create();
            TextView rec;
            TextView from;
            TextView issues;
            TextView latest;

            issues = alertLayout.findViewById(R.id.update_issues);
            issues.setText(Html.fromHtml(PreferencesUtility.getString("current_issues", "")));

            latest = alertLayout.findViewById(R.id.status_last);
            if(!PreferencesUtility.getString("last_issue", "").isEmpty()) {
                latest.setText(PreferencesUtility.getString("last_issue", ""));
            }else{
                latest.setVisibility(View.GONE);
            }


            rec = alertLayout.findViewById(R.id.update_rec);

            if (PreferencesUtility.getString("face_stat", "").equalsIgnoreCase("User reports indicate problems at Facebook")) {
                rec.setTextColor(ContextCompat.getColor(SimpleApplication.getContextOfApplication(), R.color.md_red_500));
                rec.setText(getResources().getString(R.string.problems));
                System.out.println("Current status: " + "Facebook is fucked!!!");
            } else if (PreferencesUtility.getString("face_stat", "").equalsIgnoreCase("User reports indicate possible problems at Facebook")) {
                rec.setTextColor(ContextCompat.getColor(SimpleApplication.getContextOfApplication(), R.color.md_amber_500));
                rec.setText(getResources().getString(R.string.possible));
                System.out.println("Current status: " + "Facebook has a possible issue!?");
            } else {
                rec.setTextColor(ContextCompat.getColor(SimpleApplication.getContextOfApplication(), R.color.md_cyan_500));
                rec.setText(getResources().getString(R.string.no_issues));
                System.out.println("Current status: " + "Facebook has no issues :)");
            }


            from = alertLayout.findViewById(R.id.status_click_go);
            from.setText(Html.fromHtml(getString(R.string.current_status_mention, "downdetector.com")));
            from.setOnClickListener(v -> {
                Uri uri = Uri.parse("https://downdetector.com/status/facebook/");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                try {
                    activity.startActivity(goToMarket);
                    if(!activity.isDestroyed()){
                        OptionDialog.dismiss();
                    }
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            });

            if (OptionDialog != null) {
                OptionDialog.show();
            }
        }
    }

}