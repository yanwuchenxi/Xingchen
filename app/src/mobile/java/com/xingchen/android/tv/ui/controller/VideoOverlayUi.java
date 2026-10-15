package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.widget.TextView;

/**
 * 点播进度/错误浮层，委托 {@link PlayerOverlayUi}。
 */
public final class VideoOverlayUi {

    private VideoOverlayUi() {
    }

    public static void showProgress(View progressRoot, TextView errorView, Runnable trafficTick) {
        PlayerOverlayUi.showProgress(progressRoot, errorView, trafficTick);
    }

    public static void hideProgress(View progressRoot, Runnable trafficTick) {
        PlayerOverlayUi.hideProgress(progressRoot, trafficTick);
    }

    public static void showError(TextView errorView, String text, View progressRoot, Runnable trafficTick) {
        PlayerOverlayUi.showError(errorView, text, progressRoot, trafficTick);
    }

    public static void hideError(TextView errorView) {
        PlayerOverlayUi.hideError(errorView);
    }
}
