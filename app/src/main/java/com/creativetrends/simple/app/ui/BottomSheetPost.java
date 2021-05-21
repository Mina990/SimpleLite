package com.creativetrends.simple.app.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creativetrends.simple.app.SimpleApplication;
import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class BottomSheetPost extends BottomSheetDialogFragment {
    Context context;
    boolean mLight;

    public static BottomSheetPost newInstance() {
        return new BottomSheetPost();
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
        View v = LayoutInflater.from(getContext()).inflate(R.layout.custom_bottom_post, null);

        MaterialButton policy, terms;
        policy = v.findViewById(R.id.policy_button);
        terms = v.findViewById(R.id.terms_button);
        policy.setOnClickListener(view -> StaticUtils.showPolicy(MainActivity.getMainActivity()));
        terms.setOnClickListener(view -> StaticUtils.showTerms(MainActivity.getMainActivity()));
        FloatingActionButton post = v.findViewById(R.id.post_fab);
        FloatingActionButton photo = v.findViewById(R.id.photo_fab);
        FloatingActionButton checkin = v.findViewById(R.id.check_in_fab);

        TextView name_sheet = v.findViewById(R.id.sheet_whats);

        ImageView post_image = v.findViewById(R.id.post_image);


        Glide.with(context).load(PreferencesUtility.getString("user_picture", "")).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_fb_round_pin).placeholder(R.drawable.ic_fb_round_pin).dontAnimate().dontTransform().into(post_image);


        name_sheet.getBackground().setColorFilter(new PorterDuffColorFilter(ThemeUtils.getTheme(context), PorterDuff.Mode.SRC_IN));

        name_sheet.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
            name_sheet.setOnClickListener(null);
            ((MainActivity) MainActivity.getMainActivity()).createPopUpWebView("https://m.facebook.com/?pageload=composer");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 100));
        post.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
            post.setOnClickListener(null);
            ((MainActivity) MainActivity.getMainActivity()).createPopUpWebView("https://m.facebook.com/?pageload=composer");
            //((MainActivity) MainActivity.getMainActivity()).createPopUpWebView("https://m.facebook.com/stories.php?");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 100));
        photo.setOnClickListener(v1 ->
                new Handler().postDelayed(() -> {
                    photo.setOnClickListener(null);
                    ((MainActivity) MainActivity.getMainActivity()).createPopUpWebView("https://m.facebook.com/?pageload=composer_photo");
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }, 100));
        checkin.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
            checkin.setOnClickListener(null);
            ((MainActivity) MainActivity.getMainActivity()).createPopUpWebView("https://m.facebook.com/?pageload=composer_checkin");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 100));
        post_image.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
            post_image.setOnClickListener(null);
            Intent link = new Intent(getActivity(), NewPageActivity.class);
            link.putExtra("url", "https://m.facebook.com/profile.php");
            startActivity(link);
            if(dialog.isShowing()){
                dialog.dismiss();
            }
        }, 100));
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

        dialog.setOnShowListener((DialogInterface.OnShowListener) dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = (FrameLayout) d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
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
