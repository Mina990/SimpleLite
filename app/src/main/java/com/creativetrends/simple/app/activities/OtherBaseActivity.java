package com.creativetrends.simple.app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativetrends.simple.app.adapters.AdapterUsers;
import com.creativetrends.simple.app.adapters.AdapterUsersOther;
import com.creativetrends.simple.app.adapters.UserItems;
import com.creativetrends.simple.app.helpers.BadgeHelper;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.ui.BottomSheetPost;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class OtherBaseActivity extends AppCompatActivity {
    private AdapterUsers adapterUsers;
    AdapterUsersOther adapterUsersOther;
    public static AlertDialog AccountDialog;
    AlertDialog PostDialog;
    boolean mLight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLight = PreferencesUtility.getInstance(this).getFreeTheme().equals("materialtheme");
    }

    public void getAccountDialog(Context context) {
        if (!isDestroyed()) {
            ArrayList<UserItems> userItems = PreferencesUtility.getUsers();
            adapterUsers = new AdapterUsers(context, userItems, BadgeHelper.getCookie());
            adapterUsersOther = new AdapterUsersOther(context, userItems, BadgeHelper.getCookie());
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams")
            View alertLayout = inflater.inflate(R.layout.custom_bottomsheet, null);
            RecyclerView recyclerUsers = alertLayout.findViewById(R.id.recycler_users);
            recyclerUsers.setLayoutManager(new LinearLayoutManager(context));
            recyclerUsers.setAdapter(adapterUsers);
            adapterUsers.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    checkEmpty();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    checkEmpty();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    checkEmpty();
                }

                void checkEmpty() {
                    adapterUsers.restore();
                }
            });
            //other users
            RecyclerView recyclerView = alertLayout.findViewById(R.id.recycler_users_other);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapterUsersOther);



            MaterialAlertDialogBuilder update = new MaterialAlertDialogBuilder(this);
            update.setCancelable(true);
            update.setView(alertLayout);
            AccountDialog = update.create();
            Objects.requireNonNull(AccountDialog.getWindow()).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_card_accounts));
            AccountDialog.setOnDismissListener(dialog -> PreferencesUtility.saveUsers(adapterUsers.getListUsers()));
            AccountDialog.setOnCancelListener(dialog -> PreferencesUtility.saveUsers(adapterUsers.getListUsers()));
            update.setView(alertLayout);
            MaterialButton policy, terms, manage;
            TextView brand = alertLayout.findViewById(R.id.brand_text);
            if(mLight && !ThemeUtils.isNightTime()){
                brand.setTextColor(ThemeUtils.getColorPrimaryDark());
            }else if(mLight){
                brand.setTextColor(ThemeUtils.getColorPrimaryDark());
            }
            policy = alertLayout.findViewById(R.id.policy_button);
            terms = alertLayout.findViewById(R.id.terms_button);
            manage = alertLayout.findViewById(R.id.sheet_manage);
            policy.setOnClickListener(view -> StaticUtils.showPolicy(OtherBaseActivity.this));
            terms.setOnClickListener(view -> StaticUtils.showTerms(OtherBaseActivity.this));
            LinearLayout addUser = alertLayout.findViewById(R.id.user_add);
            if (adapterUsers.getItemCount() == 3) {
                addUser.setVisibility(View.GONE);
            }else {
                addUser.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
                    addUser.setOnClickListener(null);
                    StaticUtils.clearStrings();
                    StaticUtils.clearCookies();
                    startActivity(new Intent(this, SwitchActivity.class));
                    if (AccountDialog != null && AccountDialog.isShowing()) {
                        AccountDialog.dismiss();
                    }
                }, 100));
            }
            manage.setOnClickListener(v1 -> {
                manage.setOnClickListener(null);
                new Handler().postDelayed(() -> {
                    Intent link = new Intent(this, NewPageActivity.class);
                    link.putExtra("url", "https://m.facebook.com/settings");
                    startActivity(link);
                    if (AccountDialog != null && AccountDialog.isShowing()) {
                        AccountDialog.dismiss();
                    }
                }, 100);
            });
            ImageView creative = alertLayout.findViewById(R.id.brand);
            creative.setOnClickListener(v1 -> new Handler().postDelayed(() -> {
                if (AccountDialog != null && AccountDialog.isShowing()) {
                    AccountDialog.dismiss();
                }
            }, 100));
            LinearLayout face = alertLayout.findViewById(R.id.face_settings);
            face.setOnClickListener(v1 -> {
                face.setOnClickListener(null);
                new Handler().postDelayed(() -> {
                    Intent link = new Intent(this, NewPageActivity.class);
                    link.putExtra("url", "https://m.facebook.com/privacy/");
                    startActivity(link);
                    if (AccountDialog != null && AccountDialog.isShowing()) {
                        AccountDialog.dismiss();
                    }
                }, 100);
            });
            if (AccountDialog != null) {
                AccountDialog.show();
            }
        }

    }


    public void getPostDialog() {
        if (!isDestroyed()) {
            BottomSheetPost bottomSheetPost = BottomSheetPost.newInstance();
            bottomSheetPost.show(getSupportFragmentManager(), "post_fragment");
        }

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (AccountDialog != null && AccountDialog.isShowing()) {
            AccountDialog.dismiss();
        }
        if (PostDialog != null && PostDialog.isShowing()) {
            PostDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AccountDialog != null && AccountDialog.isShowing()) {
            AccountDialog.dismiss();
        }
        if (PostDialog != null && PostDialog.isShowing()) {
            PostDialog.dismiss();
        }
    }
}