package com.xingchen.android.tv.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.bean.Live;
import com.xingchen.android.tv.databinding.DialogLiveBinding;
import com.xingchen.android.tv.impl.LiveListener;
import com.xingchen.android.tv.ui.adapter.LiveAdapter;
import com.xingchen.android.tv.utils.ResUtil;

public class LiveDialog extends BaseBottomSheetDialog implements LiveAdapter.OnClickListener {

    private DialogLiveBinding binding;
    private LiveListener listener;
    private LiveAdapter adapter;

    public static void show(FragmentActivity activity) {
        new LiveDialog().show(activity.getSupportFragmentManager(), "live_source");
    }

    public static void show(Fragment fragment) {
        new LiveDialog().show(fragment.getChildFragmentManager(), "live_source");
    }

    private boolean isFull() {
        return getParentFragment() == null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = isFull() ? (LiveListener) context : (LiveListener) getParentFragment();
    }

    @Override
    protected boolean transparent() {
        return true;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogLiveBinding.inflate(inflater, container, false);
    }

    @Override
    protected void setBehavior(BottomSheetDialog dialog) {
        super.setBehavior(dialog);
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        sheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setDimAmount(0.35f);
        WindowCompat.setDecorFitsSystemWindows(window, true);
    }

    @Override
    protected void initView() {
        adapter = new LiveAdapter(this);
        adapter.setAction(false);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recycler.setMaxHeight(ResUtil.getScreenHeight(requireContext()) * 50 / 100);
        binding.recycler.post(() -> binding.recycler.scrollToPosition(Math.max(LiveConfig.getHomeIndex(), 0)));
    }

    @Override
    public void onItemClick(Live item) {
        if (listener != null) listener.setLive(item);
        dismiss();
    }

    @Override
    public void onBootClick(int position, Live item) {
        item.boot(!item.isBoot()).save();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onPassClick(int position, Live item) {
        item.pass(!item.isPass()).save();
        adapter.notifyItemChanged(position);
    }

    @Override
    public boolean onBootLongClick(Live item) {
        boolean result = !item.isBoot();
        LiveConfig.get().getLives().forEach(live -> live.boot(result).save());
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        return true;
    }

    @Override
    public boolean onPassLongClick(Live item) {
        boolean result = !item.isPass();
        LiveConfig.get().getLives().forEach(live -> live.pass(result).save());
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        return true;
    }
}
