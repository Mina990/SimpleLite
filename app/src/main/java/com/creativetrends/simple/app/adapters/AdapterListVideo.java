package com.creativetrends.simple.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.creativetrends.simple.app.lite.R;

import java.util.List;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 5/11/2018.
 */
public class AdapterListVideo extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private final List<ListItemsVideo> mItemList;

    public AdapterListVideo(Context context, List<ListItemsVideo> itemList) {
        mLayoutInflater = LayoutInflater.from(context);
        mItemList = itemList;

    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public ListItemsVideo getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.menu_popup_video, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(getItem(position).getTitle());

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        RelativeLayout mCardView;

        ViewHolder(View view) {
            tvTitle = view.findViewById(R.id.text);
            mCardView = view.findViewById(R.id.menu_card_pop);
        }
    }
}