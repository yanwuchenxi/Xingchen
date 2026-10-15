package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.widget.TextView;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.utils.Traffic;

/**
 * 直播页进度/错误/EPG 列表显隐。从 LiveActivity 抽出。
 */
public final class LiveOverlayUi {

    private LiveOverlayUi() {
    }

    public static void keepMenuVisible(View recycler, View group, View channel) {
        if (recycler != null) recycler.setVisibility(View.VISIBLE);
        if (group != null) group.setVisibility(View.VISIBLE);
        if (channel != null) channel.setVisibility(View.VISIBLE);
    }

    public static void showEpg(View epgData, View channel, View group) {
        if (epgData != null) epgData.setVisibility(View.VISIBLE);
        if (channel != null) channel.setVisibility(View.GONE);
        if (group != null) group.setVisibility(View.GONE);
    }

    public static void hideEpg(View epgData, View channel, View group) {
        if (channel != null) channel.setVisibility(View.VISIBLE);
        if (group != null) group.setVisibility(View.VISIBLE);
        if (epgData != null) epgData.setVisibility(View.GONE);
    }

    public static void showProgress(View progressRoot, TextView errorView, Runnable trafficTick) {
        if (progressRoot != null) progressRoot.setVisibility(View.VISIBLE);
        hideError(errorView);
        if (trafficTick != null) App.post(trafficTick, 0);
    }

    public static void hideProgress(View progressRoot, Runnable trafficTick) {
        if (progressRoot != null) progressRoot.setVisibility(View.GONE);
        if (trafficTick != null) App.removeCallbacks(trafficTick);
        Traffic.reset();
    }

    public static void showError(TextView errorView, String text, View progressRoot, Runnable trafficTick) {
        if (errorView != null) {
            errorView.setVisibility(View.VISIBLE);
            errorView.setText(text == null ? "" : text);
        }
        hideProgress(progressRoot, trafficTick);
    }

    public static void hideError(TextView errorView) {
        if (errorView == null) return;
        errorView.setVisibility(View.GONE);
        errorView.setText("");
    }
}
