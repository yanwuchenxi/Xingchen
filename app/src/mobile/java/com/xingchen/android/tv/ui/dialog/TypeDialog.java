package com.xingchen.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.xingchen.android.tv.bean.Class;
import com.xingchen.android.tv.databinding.DialogTypeBinding;
import com.xingchen.android.tv.ui.adapter.TypeAdapter;
import com.xingchen.android.tv.ui.adapter.TypeDialogAdapter;

import java.util.List;

public class TypeDialog extends BaseBottomSheetDialog implements TypeAdapter.OnClickListener {

    private DialogTypeBinding binding;
    private TypeAdapter.OnClickListener listener;
    private List<Class> items;

    public static TypeDialog create() {
        return new TypeDialog();
    }

    public TypeDialog items(List<Class> items) {
        this.items = items;
        return this;
    }

    public void show(Fragment fragment) {
        for (Fragment child : fragment.getChildFragmentManager().getFragments()) {
            if (child instanceof TypeDialog) return;
        }
        this.listener = (TypeAdapter.OnClickListener) fragment;
        show(fragment.getChildFragmentManager(), "TypeDialog");
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogTypeBinding.inflate(inflater, container, false);
    }

    @Override
    protected boolean transparent() {
        return true;
    }

    @Override
    protected void initView() {
        FlexboxLayoutManager manager = new FlexboxLayoutManager(requireContext());
        manager.setFlexDirection(FlexDirection.ROW);
        manager.setFlexWrap(FlexWrap.WRAP);
        binding.recycler.setLayoutManager(manager);
        binding.recycler.setAdapter(new TypeDialogAdapter(this, items));
    }

    @Override
    public void onItemClick(int position, Class item) {
        if (listener != null) listener.onItemClick(position, item);
        dismiss();
    }
}
