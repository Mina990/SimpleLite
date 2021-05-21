package com.creativetrends.simple.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.creativetrends.simple.app.activities.BrowserActivity;
import com.creativetrends.simple.app.activities.MarketPlaceActivity;
import com.creativetrends.simple.app.activities.MessageActivity;
import com.creativetrends.simple.app.lite.R;
import com.creativetrends.simple.app.utils.PreferencesUtility;
import com.creativetrends.simple.app.utils.StaticUtils;
import com.creativetrends.simple.app.utils.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Creative Trends Apps on 10/19/2016.
 */

public class PinsAdapter extends Adapter<PinsAdapter.ViewHolderBookmark> implements Filterable {
    @SuppressLint("StaticFieldLeak")
    private static PinsAdapter adapter;
    private final Context context;
    private ArrayList<PinItems> listBookmarks;
    private final ArrayList<PinItems> filteredBookmarks;
    private final onBookmarkSelected onBookmarkSelected;
    private ArrayList<String> listFavorites;
    private final Activity mActivity;
    private final Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PinItems> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filteredBookmarks);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (PinItems item : filteredBookmarks) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listBookmarks.clear();
            listBookmarks.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public PinsAdapter(Context context, ArrayList<PinItems> listBookmarks, onBookmarkSelected onBookmarkSelected, Activity activity) {
        this.context = context;
        this.onBookmarkSelected = onBookmarkSelected;
        this.listBookmarks = listBookmarks;
        listFavorites = (PreferencesUtility.getFavorites(context));
        filteredBookmarks = new ArrayList<>(listBookmarks);
        adapter = this;
        mActivity = activity;
    }

    public void addItem(PinItems bookmark) {
        listBookmarks.add(bookmark);
        notifyDataSetChanged();
    }

    public void clear() {
        int size = listBookmarks.size();
        listBookmarks.clear();
        notifyItemRangeRemoved(0, size);
        notifyDataSetChanged();
    }

    public void restore() {
        listBookmarks = PreferencesUtility.getBookmarks();
        listFavorites = PreferencesUtility.getFavorites(context);
        adapter.notifyDataSetChanged();
    }

    public ArrayList<PinItems> getListBookmarks() {
        return listBookmarks;

    }

    public ArrayList<String> getListFavorites() {
        return listFavorites;
    }

    @NonNull
    @Override
    public ViewHolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.pin_items, parent, false);
        return new ViewHolderBookmark(view);
    }

    public void onBindViewHolder(@NonNull ViewHolderBookmark holder, int position) {
        holder.bind(listBookmarks.get(position));
    }

    public int getItemCount() {
        return this.listBookmarks.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    public interface onBookmarkSelected {
        void loadBookmark(String str, String str2);
    }

    class ViewHolderBookmark extends ViewHolder implements View.OnClickListener {
        private final AppCompatImageView delete;
        private final AppCompatImageView pin_image;
        private final AppCompatImageView online;
        EditText et;
        private PinItems bookmark;
        private final RelativeLayout bookmarkHolder;
        private final AppCompatTextView title;
        private final AppCompatTextView url;
        LinearLayout cardView;

        ViewHolderBookmark(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.pin_title);
            url = itemView.findViewById(R.id.pin_url);
            delete = itemView.findViewById(R.id.remove_pin);
            pin_image = itemView.findViewById(R.id.pin_image);
            cardView = itemView.findViewById(R.id.card_holder);
            bookmarkHolder = itemView.findViewById(R.id.bookmark_holder);
            online = itemView.findViewById(R.id.star_pin);
        }

        void bind(PinItems bookmark) {
            this.bookmark = bookmark;
            title.setText(bookmark.getTitle());
            url.setText(bookmark.getUrl());
            Uri im = Uri.parse(bookmark.getImage());
            if (im != null && !im.toString().isEmpty()) {
                pin_image.setImageURI(Uri.parse(bookmark.getImage()));
            } else {
                pin_image.setImageURI(StaticUtils.getUrl(R.drawable.ic_pin_page));
            }
            delete.setOnClickListener(this);
            online.setOnClickListener(this);
            et = new EditText(context);
            if (!listFavorites.isEmpty() && listFavorites.contains(bookmark.getUrl())) {
                online.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fav));
            } else {
                online.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fav_none));
            }
            bookmarkHolder.setOnClickListener(this);
        }

        void deleteAlert() {
            if(!mActivity.isDestroyed()){
                new MaterialAlertDialogBuilder(mActivity)
                        .setTitle(R.string.remove_pin)
                        .setMessage(context.getString(R.string.are_you_sure, bookmark.getTitle()))
                        .setPositiveButton(context.getResources().getString(R.string.ok), (dialog, which) -> {
                        listBookmarks.remove(bookmark);
                        listFavorites.remove(bookmark.getUrl());
                        adapter.notifyDataSetChanged();
                        PreferencesUtility.saveBookmarks(listBookmarks);

                })
                .setNegativeButton(R.string.cancel, null)
                .show();
            }
        }

        @SuppressLint("RestrictedApi")
        private void editAlert() {
            try {
                LayoutInflater inflater = mActivity.getLayoutInflater();
                @SuppressLint("InflateParams")
                View alertLayout = inflater.inflate(R.layout.custom_pin_layout, null);
                EditText pin_name = alertLayout.findViewById(R.id.pin_edit);
                EditText pin_url = alertLayout.findViewById(R.id.url_edit);
                pin_url.setEnabled(false);
                pin_url.setClickable(false);
                pin_name.setHint(bookmark.getTitle());
                pin_url.setHint(bookmark.getUrl());
                pin_name.setText(bookmark.getTitle());
                pin_url.setText(bookmark.getUrl());
                new MaterialAlertDialogBuilder(mActivity)
                        .setTitle(context.getResources().getString(R.string.rename_title))
                        .setMessage(context.getResources().getString(R.string.rename_message))
                        .setView(alertLayout)
                        .setPositiveButton(context.getResources().getString(R.string.ok), (dialog, which) -> {
                            if (pin_name.getText().toString().isEmpty()) {
                                bookmark.setTitle(pin_name.getHint().toString());
                            } else {
                                bookmark.setTitle(pin_name.getText().toString());
                            }
                            if (!pin_url.getText().toString().matches("^(?i)(https?|ftp)://.*$")) {
                                bookmark.setUrl("https://" + pin_url.getText().toString());
                            } else if (pin_url.getText().toString().isEmpty()) {
                                bookmark.setUrl(pin_url.getHint().toString());
                            } else {
                                bookmark.setUrl(pin_url.getText().toString());
                            }
                            Uri im = Uri.parse(bookmark.getImage());
                            Uri eventUri;
                            if (!im.toString().isEmpty() && !im.toString().contains("scontent") && bookmark.getUrl().contains("messages")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_pin_mess));
                            } else  if (!im.toString().isEmpty() && im.toString().contains("scontent") && bookmark.getUrl().contains("messages")) {
                                eventUri = Uri.parse(im.toString());
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("/groups/")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_group));
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("/photos/a.") || bookmark.getUrl() != null && bookmark.getUrl().contains("photos/pcb.") || bookmark.getUrl() != null && (bookmark.getUrl().contains("/photo.php?") || bookmark.getUrl().contains("/photos/")) && !bookmark.getUrl().contains("?photoset")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_pics));
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("/marketplace")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_market));
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("/events/")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_cal));
                            } else if (bookmark.getTitle() != null && bookmark.getTitle().contains("- Home")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_page));
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("/home.php")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_news_set));
                            } else if (bookmark.getUrl() != null && !bookmark.getUrl().contains("facebook")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_links));
                            } else if (bookmark.getUrl() != null && bookmark.getUrl().contains("instantgames")) {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_games));
                            } else {
                                eventUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                                        "://" + context.getResources().getResourcePackageName(R.drawable.ic_launcher_s)
                                        + '/' + context.getResources().getResourceTypeName(R.drawable.ic_launcher_s) + '/' + context.getResources().getResourceEntryName(R.drawable.ic_pin_page));
                            }
                            bookmark.setImage(eventUri.toString());
                            adapter.notifyDataSetChanged();
                            PreferencesUtility.saveBookmarks(listBookmarks);
                        })
                        .setNegativeButton(context.getResources().getString(R.string.cancel), null)
                        .create().show();

            } catch (Exception ignored) {

            }
        }

        @SuppressLint("RestrictedApi")
        private void showFilterPopup() {
            final ListPopupWindow popupWindow = new ListPopupWindow(context);
            List<PopupItems> itemList = new ArrayList<>();
            itemList.add(new PopupItems("Edit", R.drawable.ic_edit_pin));
            itemList.add(new PopupItems("Copy", R.drawable.ic_copy));
            itemList.add(new PopupItems("Share", R.drawable.ic_share_now));
            itemList.add(new PopupItems("Delete", R.drawable.ic_delete_pin));

            ListAdapter adapter = new PopupAdapter(context, itemList);
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.round_card_drawable_menu));
            Objects.requireNonNull(popupWindow.getBackground()).setColorFilter(ThemeUtils.getMenu(context), PorterDuff.Mode.SRC_ATOP);
            popupWindow.setAnchorView(online);
            popupWindow.setWidth(context.getResources().getDimensionPixelSize(R.dimen.popup_width));
            popupWindow.setDropDownGravity(GravityCompat.END);
            popupWindow.setHorizontalOffset(context.getResources().getDimensionPixelSize(R.dimen.popup_offset));
            popupWindow.setAdapter(adapter);
            popupWindow.setOnDismissListener(popupWindow::dismiss);
            popupWindow.setOnItemClickListener((adapterView, view, i, l) -> {
                switch (i) {
                    case 0:
                        popupWindow.dismiss();
                        editAlert();
                        break;

                    case 1:
                        popupWindow.dismiss();
                        try {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(bookmark.getTitle(), bookmark.getUrl());
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(clip);
                            }
                            Toast.makeText(context, context.getResources().getString(R.string.content_copy_link_done), Toast.LENGTH_SHORT).show();
                        } catch (Exception c) {
                            c.printStackTrace();
                            Toast.makeText(context, c.toString(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2:
                        try {
                            popupWindow.dismiss();
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_TEXT, bookmark.getUrl());
                            context.startActivity(Intent.createChooser(share, "Share " + bookmark.getTitle()));
                        } catch (ActivityNotFoundException ignored) {
                        } catch (Exception p) {
                            p.printStackTrace();
                        }
                        break;
                    case 3:
                        popupWindow.dismiss();
                        deleteAlert();
                        break;
                    default:
                        break;
                }

            });
            popupWindow.show();

        }

        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bookmark_holder:
                    if (bookmark.getUrl().contains("marketplace")) {
                        Intent intent = new Intent(context, MarketPlaceActivity.class);
                        intent.putExtra("url", bookmark.getUrl());
                        context.startActivity(intent);
                        PreferencesUtility.putString("needs_lock", "false");
                    } else if (bookmark.getUrl().contains("messages") || bookmark.getUrl().contains("messenger")) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("url", bookmark.getUrl());
                        context.startActivity(intent);
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
                    break;
                case R.id.remove_pin:
                    showFilterPopup();
                    break;

                case R.id.star_pin:
                    if (!listFavorites.contains(bookmark.getUrl())) {
                        listFavorites.add(bookmark.getUrl());
                    } else {
                        listFavorites.remove(bookmark.getUrl());
                    }
                    adapter.notifyDataSetChanged();
                    PreferencesUtility.saveFavorites(context, listFavorites);
                    PreferencesUtility.saveBookmarks(listBookmarks);
                    break;
                default:
                    break;
            }
        }
    }
}