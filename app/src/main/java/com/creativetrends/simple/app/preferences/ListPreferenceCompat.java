package com.creativetrends.simple.app.preferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import static android.graphics.Color.parseColor;
import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ListPreferenceCompat extends ListPreference {

    static final int FALLBACK_COLOR = parseColor("#00bcd4");
    private Context mContext;
    private AlertDialog mDialog;
    private int mColor = 0;


    public ListPreferenceCompat(Context context) {
        super(context);
        mContext = context;
    }

    public ListPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean MenuLight = PreferencesUtility.getInstance(context).getFreeTheme().equals("materialtheme");
        int fallback = 0;
        if (MenuLight && !com.creativetrends.simple.app.utils.ThemeUtils.isNightTime()) {
            mColor = preferences.getInt("custom_color", 0);
        } else if (com.creativetrends.simple.app.utils.ThemeUtils.isNightTime()) {
            mColor = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent}).getColor(0, fallback);
        }
    }

    public ListPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean MenuLight = PreferencesUtility.getInstance(context).getFreeTheme().equals("materialtheme");
        int fallback = 0;
        if (MenuLight && !com.creativetrends.simple.app.utils.ThemeUtils.isNightTime()) {
            mColor = preferences.getInt("custom_color", 0);
        } else if (com.creativetrends.simple.app.utils.ThemeUtils.isNightTime()) {
            mColor = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent}).getColor(0, fallback);
        }
    }

    @SuppressLint("PrivateResource")
    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        if (mDialog != null) {
            ArrayList<HashMap<String, CharSequence>> listItems = new ArrayList<>();
            for (CharSequence entry : entries) {
                HashMap<String, CharSequence> map = new HashMap<>();
                map.put("item", entry);
                listItems.add(map);
            }
            mDialog.getListView().setAdapter(new SimpleAdapter(
                    mContext,
                    listItems,
                    R.layout.select_dialog_singlechoice_material,
                    new String[]{"item"},
                    new int[]{android.R.id.text1}
            ));
        }
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException("MaterialList requires an entries array and an entryValues array.");
        }

        int preselect = findIndexOfValue(getValue());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext)
                .setTitle(getDialogTitle())
                .setMessage(getDialogMessage())
                .setIcon(getDialogIcon())
                .setNegativeButton(getNegativeButtonText(), null)
                .setSingleChoiceItems(getEntries(), preselect, (dialogInterface, i) -> {
                    if (i >= 0 && getEntryValues() != null) {
                        String value = getEntryValues()[i].toString();
                        if (callChangeListener(value) && isPersistent()) {
                            setValue(value);
                        }
                    }
                    dialogInterface.dismiss();
                });

        final View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(getDialogMessage());
        }

        PreferenceManager pm = getPreferenceManager();
        try {
            @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"}) Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDialog = builder.create();
        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }
        try {
            mDialog = builder.create();
            mDialog.show();
        } catch (NullPointerException ignored) {
        } catch (Exception i) {
            i.printStackTrace();
        }
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        try {
            CharSequence title = getTitle();
            TextView titleView = view.findViewById(android.R.id.title);
            titleView.setText(title);
            titleView.setVisibility(!isEmpty(title) ? VISIBLE : GONE);
            titleView.setTextSize(15);
            titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

            CharSequence summary = getSummary();
            TextView summaryView = view.findViewById(android.R.id.summary);
            summaryView.setText(summary);
            summaryView.setVisibility(!isEmpty(summary) ? VISIBLE : GONE);
            summaryView.setTextSize(14);

        } catch (Exception i) {
            i.printStackTrace();
        }
    }

}