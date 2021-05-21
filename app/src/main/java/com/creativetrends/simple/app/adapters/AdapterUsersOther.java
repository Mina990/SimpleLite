package com.creativetrends.simple.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.services.NotificationService;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;

import java.util.ArrayList;


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 6/1/2018.
 */
public class AdapterUsersOther extends RecyclerView.Adapter<AdapterUsersOther.ViewHolderBookmark> {
    private final Context context;
    private final ArrayList<UserItems> listUsers;
    private final String currentUser;


    public AdapterUsersOther(Context context, ArrayList<UserItems> listUsers, String currentUser) {
        this.context = context;
        this.listUsers = listUsers;
        this.currentUser = currentUser;
    }

    private void removeCookie(UserItems user) {
        listUsers.remove(user);
        user.setCookie(null);
        PreferencesUtility.saveUsers(listUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdapterUsersOther.ViewHolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_items_other, parent, false);
        return new ViewHolderBookmark(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsersOther.ViewHolderBookmark holder, int position) {
        holder.bind(listUsers.get(position));

    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }


    public class ViewHolderBookmark extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final RelativeLayout filterHolder;
        private final TextView name;
        private final ImageView userImage;
        private final ImageView delete;
        private UserItems user;

        ViewHolderBookmark(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            delete = itemView.findViewById(R.id.remove_user);
            userImage = itemView.findViewById(R.id.user_image);
            filterHolder = itemView.findViewById(R.id.user_holder);
        }

        void bind(UserItems user) {
            this.user = user;
            name.setText(user.getName());
            if (!user.getImage().isEmpty() && user.getImage() != null) {
                Glide.with(context).load(user.getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_fb_round)
                        .error(R.drawable.ic_fb_round)
                        .dontAnimate()
                        .dontTransform()
                        .into(userImage);
            }
            if (currentUser != null & user.getUserID().equals(currentUser)) {
                filterHolder.setVisibility(View.GONE);
                filterHolder.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                delete.setVisibility(View.GONE);
            } else {
                filterHolder.setVisibility(View.VISIBLE);
                filterHolder.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            filterHolder.setOnClickListener(this);
            delete.setOnClickListener(this);

        }


        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.remove_user:
                    int poz = getAdapterPosition();
                    UserItems userItems = listUsers.get(poz);
                    if (userItems.getUserID() != null && userItems.getUserID().equals(currentUser)) {
                        return;
                    }
                    removeCookie(userItems);
                    break;

                case R.id.user_holder:
                    try {
                        NotificationService.clearNotifications(context);
                        NotificationService.clearMessages(context);
                        StaticUtils.showSnackBar(MainActivity.getMainActivity(), context.getString(R.string.switching_to, user.getName().substring(0, user.getName().indexOf(" "))));
                    } catch (IndexOutOfBoundsException p) {
                        p.printStackTrace();
                        StaticUtils.showSnackBar(MainActivity.getMainActivity(), context.getString(R.string.switching_to_other));
                    }
                    StaticUtils.deleteCookies("https://.facebook.com");
                    StaticUtils.deleteCookies("https://.messenger.com");
                    StaticUtils.clearStrings();
                    PreferencesUtility.setCookie(user.getCookie());
                    final CookieManager instance = CookieManager.getInstance();
                    //noinspection deprecation
                    instance.removeSessionCookie();
                    new Handler().postDelayed(() -> {
                        try {
                            for (String cookie : user.getCookie().split(";")) {
                                instance.setCookie(".facebook.com", cookie);
                                instance.setCookie(".messenger.com", cookie);
                            }
                            instance.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        new Handler().postDelayed(() -> {
                            try {
                                ((MainActivity) MainActivity.getMainActivity()).reloadAll();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, 300);
                    }, 200);
                    break;
            }
        }


    }

}