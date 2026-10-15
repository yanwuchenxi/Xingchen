package com.xingchen.android.tv.ui.controller;

import android.view.View;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.databinding.ActivityVideoBinding;

/**
 * 点播播控条显隐策略。从 VideoActivity 抽出。
 */
public final class VideoControlPanel {

    private VideoControlPanel() {
    }

    public static final class State {
        public boolean locked;
        public boolean fullscreen;
        public boolean playerEmpty;
        public boolean hasHistory;
        public boolean useParse;
        public boolean haveDanmaku;

        public State locked(boolean v) { locked = v; return this; }
        public State fullscreen(boolean v) { fullscreen = v; return this; }
        public State playerEmpty(boolean v) { playerEmpty = v; return this; }
        public State hasHistory(boolean v) { hasHistory = v; return this; }
        public State useParse(boolean v) { useParse = v; return this; }
        public State haveDanmaku(boolean v) { haveDanmaku = v; return this; }
    }

    public static void show(ActivityVideoBinding binding, State s, Runnable afterShow) {
        if (binding == null || binding.control == null || s == null) return;
        binding.control.danmaku.setVisibility(s.locked || !s.haveDanmaku ? View.GONE : View.VISIBLE);
        binding.control.setting.setVisibility(!s.hasHistory || s.fullscreen ? View.GONE : View.VISIBLE);
        binding.control.right.rotate.setVisibility(s.fullscreen && !s.locked ? View.VISIBLE : View.GONE);
        binding.control.fullscreen.setVisibility(s.locked ? View.GONE : View.VISIBLE);
        binding.control.keep.setVisibility(!s.hasHistory || s.fullscreen ? View.GONE : View.VISIBLE);
        binding.control.parse.setVisibility(s.fullscreen && s.useParse ? View.VISIBLE : View.GONE);
        binding.control.action.getRoot().setVisibility(s.fullscreen ? View.VISIBLE : View.GONE);
        binding.control.right.lock.setVisibility(s.fullscreen ? View.VISIBLE : View.GONE);
        binding.control.info.setVisibility(s.playerEmpty ? View.GONE : View.VISIBLE);
        binding.control.cast.setVisibility(s.playerEmpty ? View.GONE : View.VISIBLE);
        binding.control.center.setVisibility(s.locked ? View.GONE : View.VISIBLE);
        binding.control.bottom.setVisibility(s.locked ? View.GONE : View.VISIBLE);
        binding.control.back.setVisibility(s.locked ? View.GONE : View.VISIBLE);
        binding.control.top.setVisibility(s.locked ? View.GONE : View.VISIBLE);
        binding.control.getRoot().setVisibility(View.VISIBLE);
        if (afterShow != null) afterShow.run();
    }

    public static void hide(ActivityVideoBinding binding, Runnable cancelHide) {
        if (binding == null || binding.control == null) return;
        binding.control.getRoot().setVisibility(View.GONE);
        if (cancelHide != null) App.removeCallbacks(cancelHide);
    }
}
