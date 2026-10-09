package com.xingchen.android.tv;

import android.content.Context;
import android.os.Build;
import android.view.DisplayCutout;

import com.xingchen.android.tv.bean.Style;
import com.xingchen.android.tv.setting.PlayerSetting;
import com.xingchen.android.tv.utils.ResUtil;

public class Product {

    public static int getDeviceType() {
        return 1;
    }

    public static int getColumn(Context context) {
        // 默认 size=2 → 竖屏 3 列（与影视仓接近）；限制在 2~6，避免一列铺满
        int count = ResUtil.isLand(context) ? 7 : 5;
        count = count + (ResUtil.isPad() ? 1 : 0);
        int column = Math.abs(PlayerSetting.getSize() - count);
        return Math.max(2, Math.min(6, column));
    }

    public static int getColumn(Context context, Style style) {
        int column = getColumn(context);
        if (style.isLand()) column = Math.max(2, column - 1);
        return column;
    }

    public static int[] getSpec(Context context) {
        return getSpec(context, Style.rect());
    }

    public static int[] getSpec(Context context, Style style) {
        int column = getColumn(context, style);
        // 左右留白与 item 间距贴近影视仓（更满、海报更大）
        int space = ResUtil.dp2px(20) + ResUtil.dp2px(8 * (column - 1)) + getCutout(context);
        if (style.isOval()) space += ResUtil.dp2px(column * 12);
        return getSpec(context, space, column, style);
    }

    private static int[] getSpec(Context context, int space, int column, Style style) {
        int base = ResUtil.getScreenWidth(context) - space;
        int width = base / column;
        int height = (int) (width / style.getRatio());
        return new int[]{width, height};
    }

    private static int getCutout(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0;
        DisplayCutout cutout = ResUtil.getDisplay(context).getCutout();
        if (cutout == null) return 0;
        int left = cutout.getSafeInsetLeft();
        int right = cutout.getSafeInsetRight();
        return left | right;
    }
}
