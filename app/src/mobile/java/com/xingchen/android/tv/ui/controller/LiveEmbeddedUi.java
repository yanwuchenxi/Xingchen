package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.utils.ResUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 直播竖屏嵌入：状态栏 inset + 底栏玻璃样式。从 LiveActivity 抽出。
 */
public final class LiveEmbeddedUi {

    private LiveEmbeddedUi() {
    }

    public static void applyTopInset(View root, BottomNavigationView homeNav) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            if (homeNav != null) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) homeNav.getLayoutParams();
                lp.bottomMargin = bars.bottom + ResUtil.dp2px(14);
                lp.leftMargin = ResUtil.dp2px(20);
                lp.rightMargin = ResUtil.dp2px(20);
                homeNav.setLayoutParams(lp);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
        int top = 0;
        try {
            int resId = root.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) top = root.getResources().getDimensionPixelSize(resId);
        } catch (Throwable ignored) {
        }
        if (root.getPaddingTop() == 0 && top > 0) root.setPadding(0, top, 0, 0);
        styleHomeNav(root, homeNav);
    }

    public static void clearTopInset(View root) {
        if (root == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(root, null);
        root.setPadding(0, 0, 0, 0);
    }

    public static void styleHomeNav(View root, BottomNavigationView homeNav) {
        if (homeNav == null) return;
        homeNav.setBackgroundResource(R.drawable.shape_glass_nav);
        homeNav.setItemBackgroundResource(android.R.color.transparent);
        if (root != null) {
            android.content.res.ColorStateList tint = ContextCompat.getColorStateList(root.getContext(), R.color.selector_ysc_nav);
            if (tint != null) {
                homeNav.setItemIconTintList(tint);
                homeNav.setItemTextColor(tint);
            }
        }
        try {
            homeNav.setItemActiveIndicatorEnabled(true);
        } catch (Throwable ignored) {
        }
    }
}
