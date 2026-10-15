package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.widget.TextView;

/**
 * 直播 EPG/频道列表显隐。进度/错误见 {@link PlayerOverlayUi}。
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

    /** @deprecated 使用 {@link PlayerOverlayUi#showProgress} */
    @Deprecated
    public static void showProgress(View progressRoot, TextView errorView, Runnable trafficTick) {
        PlayerOverlayUi.showProgress(progressRoot, errorView, trafficTick);
    }

    /** @deprecated 使用 {@link PlayerOverlayUi#hideProgress} */
    @Deprecated
    public static void hideProgress(View progressRoot, Runnable trafficTick) {
        PlayerOverlayUi.hideProgress(progressRoot, trafficTick);
    }

    /** @deprecated 使用 {@link PlayerOverlayUi#showError} */
    @Deprecated
    public static void showError(TextView errorView, String text, View progressRoot, Runnable trafficTick) {
        PlayerOverlayUi.showError(errorView, text, progressRoot, trafficTick);
    }

    /** @deprecated 使用 {@link PlayerOverlayUi#hideError} */
    @Deprecated
    public static void hideError(TextView errorView) {
        PlayerOverlayUi.hideError(errorView);
    }
}
