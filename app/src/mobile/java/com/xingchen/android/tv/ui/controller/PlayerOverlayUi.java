package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.widget.TextView;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.utils.Traffic;

/**
 * 点播/直播共用：进度与错误浮层。
 */
public final class PlayerOverlayUi {

    private PlayerOverlayUi() {
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
