package com.xingchen.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.databinding.AdapterLiveEpgBinding;
import com.xingchen.android.tv.setting.LiveEpgSetting;
import com.xingchen.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class LiveEpgAdapter extends RecyclerView.Adapter<LiveEpgAdapter.ViewHolder> {

    private final OnClickListener listener;
    private final List<String> items = new ArrayList<>();

    public LiveEpgAdapter(OnClickListener listener) {
        this.listener = listener;
        reload();
    }

    public void reload() {
        items.clear();
        items.add(""); // 默认
        for (String url : LiveEpgSetting.getHistory()) {
            if (!url.isEmpty() && !items.contains(url)) items.add(url);
        }
        String current = LiveEpgSetting.getUrl();
        if (!current.isEmpty() && !items.contains(current)) items.add(1, current);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterLiveEpgBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        boolean editable = !item.isEmpty();
        holder.binding.text.setText(item.isEmpty() ? ResUtil.getString(R.string.live_epg_default) : item);
        holder.binding.getRoot().setSelected(item.equals(LiveEpgSetting.getUrl()));
        holder.binding.edit.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.binding.delete.setVisibility(editable ? View.VISIBLE : View.GONE);
        holder.binding.getRoot().setOnClickListener(v -> listener.onEpgClick(item));
        holder.binding.edit.setOnClickListener(v -> listener.onEpgEdit(item));
        holder.binding.delete.setOnClickListener(v -> listener.onEpgDelete(item));
    }

    public interface OnClickListener {
        void onEpgClick(String url);
        void onEpgEdit(String url);
        void onEpgDelete(String url);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final AdapterLiveEpgBinding binding;

        ViewHolder(@NonNull AdapterLiveEpgBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
