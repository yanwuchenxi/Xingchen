package com.xingchen.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.bean.Site;
import com.xingchen.android.tv.databinding.AdapterSiteBinding;

import java.util.ArrayList;
import java.util.List;

public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.ViewHolder> {

    private final OnClickListener listener;
    private final List<Site> mAll = new ArrayList<>();
    private final List<Site> mItems;
    private boolean search;
    private boolean change;

    public SiteAdapter(OnClickListener listener) {
        this.listener = listener;
        this.mItems = new ArrayList<>();
        this.addAll();
    }

    public interface OnClickListener {

        void onTextClick(Site item);

        void onSearchClick(int position, Site item);

        void onChangeClick(int position, Site item);

        boolean onSearchLongClick(Site item);

        boolean onChangeLongClick(Site item);
    }

    public SiteAdapter search(boolean search) {
        this.search = search;
        return this;
    }

    public SiteAdapter change(boolean change) {
        this.change = change;
        return this;
    }

    private void addAll() {
        mAll.clear();
        mItems.clear();
        for (Site site : VodConfig.get().getSites()) if (!site.isHide()) mAll.add(site);
        mItems.addAll(mAll);
    }

    public void filter(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase();
        mItems.clear();
        if (q.isEmpty()) {
            mItems.addAll(mAll);
        } else {
            for (Site site : mAll) {
                String name = site.getName() == null ? "" : site.getName().toLowerCase();
                if (name.contains(q)) mItems.add(site);
            }
        }
        notifyDataSetChanged();
    }

    public List<Site> getItems() {
        return mItems;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterSiteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Site item = mItems.get(position);
        boolean on = !search || change;
        // 无痕关闭搜索时降低不透明度作提示
        String name = item.getName() == null ? "" : item.getName();
        if (!item.isSearchable()) name = name + " · 无痕";
        holder.binding.text.setText(name);
        holder.binding.text.setEnabled(on);
        holder.binding.text.setFocusable(on);
        holder.binding.text.setSelected(on && item.isSelected());
        holder.binding.text.setAlpha(item.isSearchable() ? 1f : 0.72f);
        holder.binding.search.setVisibility(View.GONE);
        holder.binding.change.setVisibility(View.GONE);
        holder.binding.text.setOnClickListener(v -> listener.onTextClick(item));
        // 长按开启/关闭无痕（切换 searchable）
        holder.binding.text.setOnLongClickListener(v -> {
            item.setSearchable(!item.isSearchable()).save();
            notifyItemChanged(position);
            return true;
        });
    }

    private int getSearchIcon(Site item) {
        return item.isSearchable() ? R.drawable.ic_site_search : R.drawable.ic_site_block;
    }

    private int getChangeIcon(Site item) {
        return item.isChangeable() ? R.drawable.ic_site_change : R.drawable.ic_site_block;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterSiteBinding binding;

        ViewHolder(@NonNull AdapterSiteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
