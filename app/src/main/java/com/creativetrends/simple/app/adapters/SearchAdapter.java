package com.creativetrends.simple.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creativetrends.simple.app.lite.R;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolderBookmark> {

    private final LayoutInflater layoutInflater;
    private ArrayList<SearchItems> listSearch;
    private final SearchAdapter.onSearchSelected onSearchSelected;

    public SearchAdapter(Context context, ArrayList<SearchItems> listBookmarks, SearchAdapter.onSearchSelected onSearchSelected) {
        this.listSearch = listBookmarks;
        this.onSearchSelected = onSearchSelected;
        layoutInflater = LayoutInflater.from(context);
    }


    public void setFilter(List<SearchItems> newList) {
        listSearch = new ArrayList<>();
        listSearch.addAll(newList);
        notifyDataSetChanged();
    }


    @NonNull
    public SearchAdapter.ViewHolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchAdapter.ViewHolderBookmark(layoutInflater.inflate(R.layout.search_items, parent, false));
    }

    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolderBookmark holder, int position) {
        holder.bind(listSearch.get(position));
    }

    public int getItemCount() {
        return listSearch.size();
    }

    public interface onSearchSelected {
        void loadSearch(String str);
    }

    class ViewHolderBookmark extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SearchItems bookmark;
        private final RelativeLayout searchHolder;
        private final TextView title;

        ViewHolderBookmark(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            searchHolder = itemView.findViewById(R.id.search_holder);
        }

        void bind(SearchItems bookmark) {
            this.bookmark = bookmark;
            title.setText(bookmark.getName());
            searchHolder.setOnClickListener(this);
        }

        public void onClick(View v) {
            if (v.getId() == R.id.search_holder) {
                if(bookmark.getName().contains("#")){
                    onSearchSelected.loadSearch(bookmark.getName().replace("#", "%23"));
                }else{
                    onSearchSelected.loadSearch(bookmark.getName());
                }
            }
        }
    }
}