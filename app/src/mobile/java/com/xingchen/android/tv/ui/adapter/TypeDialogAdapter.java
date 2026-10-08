package com.xingchen.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xingchen.android.tv.bean.Class;
import com.xingchen.android.tv.databinding.AdapterTypeDialogBinding;

import java.util.List;

public class TypeDialogAdapter extends RecyclerView.Adapter<TypeDialogAdapter.ViewHolder> {

    private final TypeAdapter.OnClickListener listener;
    private final List<Class> items;

    public TypeDialogAdapter(TypeAdapter.OnClickListener listener, List<Class> items) {
        this.listener = listener;
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterTypeDialogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Class item = items.get(position);
        holder.binding.text.setText(item.getTypeName());
        holder.binding.text.setSelected(item.isSelected());
        holder.binding.text.setOnClickListener(v -> listener.onItemClick(position, item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final AdapterTypeDialogBinding binding;

        ViewHolder(@NonNull AdapterTypeDialogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
