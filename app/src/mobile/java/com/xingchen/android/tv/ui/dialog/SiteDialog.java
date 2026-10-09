package com.xingchen.android.tv.ui.dialog;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.bean.Site;
import com.xingchen.android.tv.databinding.DialogSiteBinding;
import com.xingchen.android.tv.impl.SiteListener;
import com.github.catvod.utils.Prefers;
import com.xingchen.android.tv.ui.adapter.SiteAdapter;
import com.xingchen.android.tv.ui.custom.SpaceItemDecoration;
import com.xingchen.android.tv.utils.ResUtil;

public class SiteDialog extends BaseAlertDialog implements SiteAdapter.OnClickListener {

    private static final String PREF_SITE_SPAN = "site_dialog_span";

    private DialogSiteBinding binding;
    private SiteListener listener;
    private SiteAdapter adapter;
    private SpaceItemDecoration decoration;
    private boolean search;
    private boolean change;
    private int spanCount = 1;

    public static SiteDialog create() {
        return new SiteDialog();
    }

    public SiteDialog search() {
        search = true;
        return this;
    }

    public SiteDialog change() {
        change = true;
        return this;
    }

    public void show(Fragment fragment) {
        show(fragment.getChildFragmentManager(), null);
        if (fragment instanceof SiteListener) listener = (SiteListener) fragment;
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogSiteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        spanCount = Math.max(1, Math.min(2, Prefers.getInt(PREF_SITE_SPAN, 1)));
        adapter = new SiteAdapter(this);
        adapter.search(search).change(change);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        applySpan();
        binding.recycler.post(() -> binding.recycler.scrollToPosition(Math.max(VodConfig.getHomeIndex(), 0)));
        updateSpanIcon();
    }

    @Override
    protected void initEvent() {
        binding.spanToggle.setOnClickListener(v -> {
            spanCount = spanCount == 1 ? 2 : 1;
            Prefers.put(PREF_SITE_SPAN, spanCount);
            applySpan();
            updateSpanIcon();
        });
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applySpan() {
        if (decoration != null) binding.recycler.removeItemDecoration(decoration);
        decoration = new SpaceItemDecoration(spanCount, 8);
        binding.recycler.addItemDecoration(decoration);
        if (spanCount == 1) {
            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        } else {
            binding.recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        }
    }

    private void updateSpanIcon() {
        binding.spanToggle.setImageResource(spanCount == 1 ? R.drawable.ic_site_span_1 : R.drawable.ic_site_span_2);
    }

    @Override
    public void onTextClick(Site item) {
        if (listener != null) listener.setSite(item);
        dismiss();
    }

    @Override
    public void onSearchClick(int position, Site item) {
        item.setSearchable(!item.isSearchable()).save();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onChangeClick(int position, Site item) {
        item.setChangeable(!item.isChangeable()).save();
        adapter.notifyItemChanged(position);
    }

    @Override
    public boolean onSearchLongClick(Site item) {
        boolean result = !item.isSearchable();
        adapter.getItems().forEach(site -> site.setSearchable(result).save());
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        return true;
    }

    @Override
    public boolean onChangeLongClick(Site item) {
        boolean result = !item.isChangeable();
        adapter.getItems().forEach(site -> site.setChangeable(result).save());
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() == 0) dismiss();
        else if (ResUtil.isLand(requireContext())) setWidth(0.5f);
    }
}
