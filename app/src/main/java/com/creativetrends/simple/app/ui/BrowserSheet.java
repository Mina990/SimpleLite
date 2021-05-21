package com.creativetrends.simple.app.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.BrowserActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

public class BrowserSheet extends BottomSheetDialogFragment {
    Context context;
    boolean mLight;

    public static BrowserSheet newInstance() {
        return new BrowserSheet();
    }

    @Override
    public int getTheme() {
        if (PreferencesUtility.getBoolean("auto_night", false) && ThemeUtils.isNightTime()) {
            return R.style.BottomSheetDialog_Rounded;
        } else if (mLight && !ThemeUtils.isNightTime()) {
            return R.style.BottomSheetDialog_Rounded_Light;
        } else if (!mLight) {
            return R.style.BottomSheetDialog_Rounded;
        }
        return super.getTheme();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SimpleApplication.getContextOfApplication();
        mLight = PreferencesUtility.getInstance(context).getFreeTheme().equals("materialtheme");
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        try{
            Objects.requireNonNull(dialog.getWindow()).setNavigationBarColor(ThemeUtils.getSheetNav(context));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Objects.requireNonNull(dialog.getWindow()).setNavigationBarDividerColor(ThemeUtils.getTheme(context));
            }
        }catch (Exception p){
            p.printStackTrace();
        }
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getContext()).inflate(R.layout.custom_bottom_browser, null);
        TextView refresh, copy, open, smart;



        refresh = v.findViewById(R.id.browser_refresh);
        refresh.setOnClickListener(view -> {
            ((BrowserActivity) BrowserActivity.getBrowser()).isRefresh();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });


        copy = v.findViewById(R.id.browser_copy);
        copy.setOnClickListener(view -> {
            ((BrowserActivity) BrowserActivity.getBrowser()).isCopy();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });


        open = v.findViewById(R.id.browser_open);
        open.setOnClickListener(view -> {
            ((BrowserActivity) BrowserActivity.getBrowser()).isOpen();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });


        smart = v.findViewById(R.id.browser_smart);
        smart.setOnClickListener(view -> {
            ((BrowserActivity) BrowserActivity.getBrowser()).isPins();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });




        dialog.setContentView(v);


        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) v.getParent()).getLayoutParams();
        //noinspection rawtypes

        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior instanceof BottomSheetBehavior) {
            //noinspection rawtypes
            ((BottomSheetBehavior) behavior).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_DRAGGING:
                        case BottomSheetBehavior.STATE_SETTLING:
                        case BottomSheetBehavior.STATE_EXPANDED:
                        case BottomSheetBehavior.STATE_COLLAPSED: {
                            break;
                        }
                        case BottomSheetBehavior.STATE_HIDDEN: {
                            dismiss();
                            break;
                        }
                        case BottomSheetBehavior.STATE_HALF_EXPANDED:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }

    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

    }
}