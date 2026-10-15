package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.databinding.ActivityVideoBinding;

/**
 * 点播详情播控图标状态（收藏/锁定/全屏/弹幕）。从 VideoActivity 抽出。
 */
public final class VideoChromeUi {

    private VideoChromeUi() {
    }

    public static void applyKeep(ActivityVideoBinding binding, boolean kept) {
        if (binding == null) return;
        int icon = kept ? R.drawable.ic_control_keep_on : R.drawable.ic_control_keep_off;
        if (binding.control != null && binding.control.keep != null) {
            binding.control.keep.setImageResource(icon);
        }
        if (binding.detailKeepIcon != null) {
            binding.detailKeepIcon.setImageResource(icon);
        }
        if (binding.detailKeepText != null) {
            binding.detailKeepText.setText(kept ? R.string.keep_del : R.string.keep);
        }
    }

    public static void applyLock(ImageView lockView, boolean locked) {
        if (lockView == null) return;
        lockView.setImageResource(locked ? R.drawable.ic_control_lock_on : R.drawable.ic_control_lock_off);
    }

    public static void applyFullscreen(ImageView fullscreenView, boolean fullscreen) {
        if (fullscreenView == null) return;
        fullscreenView.setImageResource(fullscreen ? R.drawable.ic_control_fullscreen_exit : R.drawable.ic_control_fullscreen);
    }

    public static void applyDanmaku(ImageView danmakuView, boolean show) {
        if (danmakuView == null) return;
        danmakuView.setImageResource(show ? R.drawable.ic_control_danmaku_on : R.drawable.ic_control_danmaku_off);
    }

    public static void setQualityVisible(View qualityText, View quality, boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        if (qualityText != null) qualityText.setVisibility(v);
        if (quality != null) quality.setVisibility(v);
    }
}
