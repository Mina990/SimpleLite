package com.creativetrends.simple.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.creativetrends.simple.app.activities.BrowserActivity;
import com.creativetrends.simple.app.activities.MarketPlaceActivity;
import com.creativetrends.simple.app.activities.MessageActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;

import java.util.ArrayList;

/**
 * Created by Creative Trends Apps.
 */

public class AdapterFavs extends Adapter<AdapterFavs.ViewHolderBookmark> {

    @SuppressLint("StaticFieldLeak")
    private static AdapterFavs adapter;
    private final Context context;
    private ArrayList<PinItems> listBookmarks;
    private ArrayList<String> listFavorites;
    private final onBookmarkSelected onBookmarkSelected;
    private final Activity mActivity;

    public AdapterFavs(Context context, ArrayList<PinItems> listBookmarks, onBookmarkSelected onBookmarkSelected, Activity activity) {
        this.context = context;
        this.onBookmarkSelected = onBookmarkSelected;
        this.listBookmarks = listBookmarks;
        listFavorites = (PreferencesUtility.getFavorites(context));
        adapter = this;
        mActivity = activity;
    }

    public void restore() {
        listBookmarks = PreferencesUtility.getBookmarks();
        listFavorites = (PreferencesUtility.getFavorites(context));
        adapter.notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_items, parent, false);
        return new ViewHolderBookmark(view);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull final ViewHolderBookmark holder, int position) {
        holder.bind(listBookmarks.get(position));

    }


    public int getItemCount() {
        return this.listBookmarks.size();
    }

    public interface onBookmarkSelected {
        void loadBookmark(String str, String str2);

    }

    public class ViewHolderBookmark extends RecyclerView.ViewHolder implements View.OnClickListener {
        PinItems bookmark;
        AppCompatImageView pin_image;
        AppCompatTextView title;
        RelativeLayout bookmarkHolder;

        ViewHolderBookmark(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.pin_title);
            pin_image = itemView.findViewById(R.id.pin_image);
            bookmarkHolder = itemView.findViewById(R.id.bookmark_holder);
        }


        void bind(PinItems bookmark) {
            this.bookmark = bookmark;
            title.setText(bookmark.getTitle());
            try {
                Uri im = Uri.parse(bookmark.getImage());
                if (im != null && !im.toString().isEmpty()) {
                    pin_image.setImageURI(Uri.parse(bookmark.getImage()));
                } else {
                    pin_image.setImageURI(StaticUtils.getUrl(R.drawable.ic_pin_page));
                }
                if (!listFavorites.isEmpty() && listFavorites.contains(bookmark.getUrl())) {
                    bookmarkHolder.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.fav_offset);
                } else {
                    bookmarkHolder.getLayoutParams().height = 0;
                }
            } catch (Exception i) {
                i.printStackTrace();
            }
            bookmarkHolder.setOnClickListener(this);
        }


        public void onClick(View v) {
            if (v.getId() == R.id.bookmark_holder) {
                PreferencesUtility.putString("needs_lock", "false");
                try {
                    if (bookmark.getUrl().contains("marketplace")) {
                        Intent intent = new Intent(mActivity, MarketPlaceActivity.class);
                        intent.putExtra("url", bookmark.getUrl());
                        mActivity.startActivity(intent);
                    } else if (bookmark.getUrl().contains("messages") || bookmark.getUrl().contains("messenger")) {
                        Intent intent = new Intent(mActivity, MessageActivity.class);
                        intent.putExtra("url", bookmark.getUrl());
                        mActivity.startActivity(intent);
                        PreferencesUtility.putString("needs_lock", "false");
                    } else if (bookmark.getUrl().contains("facebook")) {
                        onBookmarkSelected.loadBookmark(bookmark.getTitle(), bookmark.getUrl());
                    } else if (PreferencesUtility.getInstance(context).getBrowser().equals("in_app_browser")) {
                        Intent intent = new Intent(mActivity, BrowserActivity.class);
                        intent.setData(Uri.parse(bookmark.getUrl()));
                        mActivity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(bookmark.getUrl()));
                        mActivity.startActivity(intent);
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

}