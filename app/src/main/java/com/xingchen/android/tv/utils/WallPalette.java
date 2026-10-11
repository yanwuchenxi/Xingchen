package com.xingchen.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import java.io.File;

/**
 * 面板底色：半透明白，略带壁纸色调。
 */
public final class WallPalette {

    private static final int FALLBACK = Color.parseColor("#D9FFFFFF");

    private WallPalette() {
    }

    public static int panelColor() {
        try {
            File cache = FileUtil.getWallCache();
            if (cache == null || !cache.exists()) return FALLBACK;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 16;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeFile(cache.getAbsolutePath(), opts);
            if (bitmap == null) return FALLBACK;
            long r = 0, g = 0, b = 0;
            int count = 0;
            int stepX = Math.max(1, bitmap.getWidth() / 20);
            int stepY = Math.max(1, bitmap.getHeight() / 20);
            for (int y = 0; y < bitmap.getHeight(); y += stepY) {
                for (int x = 0; x < bitmap.getWidth(); x += stepX) {
                    int p = bitmap.getPixel(x, y);
                    r += Color.red(p);
                    g += Color.green(p);
                    b += Color.blue(p);
                    count++;
                }
            }
            bitmap.recycle();
            if (count == 0) return FALLBACK;
            // 高亮半透明白：壁纸色占少量，整体偏白
            int rr = clamp((int) (r / count * 0.12f + 235));
            int gg = clamp((int) (g / count * 0.12f + 235));
            int bb = clamp((int) (b / count * 0.12f + 238));
            return Color.argb(0xD9, rr, gg, bb);
        } catch (Throwable ignored) {
            return FALLBACK;
        }
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
        gd.setColor(panelColor());
        float r = ResUtil.dp2px(20);
        if (topOnly) gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        else gd.setCornerRadius(r);
        view.setBackground(gd);
    }

    private static int clamp(int v) {
        return Math.max(200, Math.min(255, v));
    }
}
