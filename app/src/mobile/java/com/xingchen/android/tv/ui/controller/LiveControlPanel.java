package com.xingchen.android.tv.ui.controller;

import android.view.View;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.databinding.ActivityLiveBinding;

/**
 * 直播播控条显隐与锁定图标。从 LiveActivity 抽出。
 */
public final class LiveControlPanel {

    private LiveControlPanel() {
    }

    /**
     * @param playerEmpty 当前无播放内容
     * @param embedded    竖屏嵌入（上播放器+下列表）
     * @param locked      锁屏态
     */
    public static void show(ActivityLiveBinding binding, boolean playerEmpty, boolean embedded, boolean locked, Runnable hideInfo, Runnable scheduleHide) {
        if (binding == null || binding.control == null) return;
        binding.control.info.setVisibility(playerEmpty ? View.GONE : View.VISIBLE);
        binding.control.cast.setVisibility(playerEmpty || embedded ? View.GONE : View.VISIBLE);
        binding.control.right.rotate.setVisibility(locked ? View.GONE : View.VISIBLE);
        binding.control.center.setVisibility(locked ? View.GONE : View.VISIBLE);
        binding.control.bottom.setVisibility(locked || embedded ? View.GONE : View.VISIBLE);
        if (binding.control.action != null) {
            binding.control.action.getRoot().setVisibility(embedded ? View.GONE : View.VISIBLE);
        }
        binding.control.back.setVisibility(locked ? View.GONE : View.VISIBLE);
        binding.control.top.setVisibility(locked ? View.GONE : View.VISIBLE);
        binding.control.getRoot().setBackgroundResource(embedded ? R.color.transparent : R.color.black_20);
        binding.control.getRoot().setVisibility(View.VISIBLE);
        if (hideInfo != null) hideInfo.run();
        if (scheduleHide != null) scheduleHide.run();
    }

    public static void hide(ActivityLiveBinding binding, Runnable cancelHide) {
        if (binding == null || binding.control == null) return;
        binding.control.getRoot().setVisibility(View.GONE);
        if (cancelHide != null) App.removeCallbacks(cancelHide);
    }

    public static void showInfoPip(ActivityLiveBinding binding, boolean inPip, Runnable scheduleHide, Runnable hideControl, Runnable setInfo) {
        if (binding == null || binding.widget == null) return;
        binding.widget.infoPip.setVisibility(inPip ? View.VISIBLE : View.GONE);
        binding.widget.info.setVisibility(inPip ? View.GONE : View.VISIBLE);
        if (scheduleHide != null) scheduleHide.run();
        if (hideControl != null) hideControl.run();
        if (setInfo != null) setInfo.run();
    }

    public static void hideInfo(ActivityLiveBinding binding, Runnable cancelHide) {
        if (binding == null || binding.widget == null) return;
        binding.widget.infoPip.setVisibility(View.GONE);
        binding.widget.info.setVisibility(View.GONE);
        if (cancelHide != null) App.removeCallbacks(cancelHide);
    }

    public static void applyLockIcon(ActivityLiveBinding binding, boolean locked) {
        if (binding == null || binding.control == null || binding.control.right == null) return;
        binding.control.right.lock.setImageResource(locked ? R.drawable.ic_control_lock_on : R.drawable.ic_control_lock_off);
    }

    public static void setSizeText(ActivityLiveBinding binding, String text) {
        if (binding == null || binding.control == null || binding.control.size == null) return;
        String t = text == null ? "" : text;
        binding.control.size.setText(t);
        binding.control.size.setVisibility(t.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
