package com.xingchen.android.tv.utils;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * 深色半透明面板（与直播弹窗一致，略降透明度）。
 */
public final class WallPalette {

    private static final int PANEL = Color.parseColor("#E6121828");

    private WallPalette() {
    }

    public static int panelColor() {
        return PANEL;
    }

    public static void applyOpaquePanel(View view) {
        apply(view, true);
    }

    public static void applyOpaquePanelFull(View view) {
        apply(view, false);
    }

    private static void apply(View view, boolean topOnly) {
        if (view == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(PANEL);
        float r = ResUtil.dp2px(22);
        if (topOnly) gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        else gd.setCornerRadius(ResUtil.dp2px(18));
        view.setBackground(gd);
    }
}
