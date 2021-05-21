package com.creativetrends.simple.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.creativetrends.simple.app.activities.MainActivity;
import com.creativetrends.simple.app.activities.NewPageActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;

import java.util.ArrayList;


/**
 * Created by Creative Trends Apps (Jorell Rutledge) 6/1/2018.
 */
public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.ViewHolderBookmark> {
    private final Context context;
    private ArrayList<UserItems> listUsers;
    private final String currentUser;


    public AdapterUsers(Context context, ArrayList<UserItems> listUsers, String currentUser) {
        this.context = context;
        this.listUsers = listUsers;
        this.currentUser = currentUser;
    }


    public ArrayList<UserItems> getListUsers() {
        return listUsers;

    }

    public void restore() {
        listUsers = PreferencesUtility.getUsers();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdapterUsers.ViewHolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_items, parent, false);
        return new ViewHolderBookmark(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUsers.ViewHolderBookmark holder, int position) {
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

        ViewHolderBookmark(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            delete = itemView.findViewById(R.id.remove_user);
            userImage = itemView.findViewById(R.id.user_image);
            filterHolder = itemView.findViewById(R.id.user_holder);
        }

        void bind(UserItems user) {
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
                delete.setVisibility(View.GONE);
                filterHolder.setVisibility(View.VISIBLE);
                filterHolder.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                filterHolder.setVisibility(View.GONE);
                filterHolder.setLayoutParams(new RecyclerView.LayoutParams(0,0));
            }
            filterHolder.setOnClickListener(this);
            delete.setOnClickListener(this);


        }


        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.remove_user:
                    break;

                case R.id.user_holder:
                    Intent profile = new Intent(MainActivity.getMainActivity(), NewPageActivity.class);
                    profile.putExtra("url", "https://m.facebook.com/profile.php");
                    MainActivity.getMainActivity().startActivity(profile);
                    break;
            }
        }


    }

}