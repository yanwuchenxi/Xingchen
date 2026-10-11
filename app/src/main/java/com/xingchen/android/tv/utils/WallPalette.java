package com.xingchen.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import java.io.File;

/**
 * 从壁纸采样不透明面板底色：跟随壁纸色调并压暗，保证白字可读。
 */
public final class WallPalette {

    private static final int FALLBACK = Color.parseColor("#FF1A1E2C");

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
            // 压暗并略提对比，保证不透明且白字清晰
            int rr = clamp((int) (r / count * 0.28f + 18));
            int gg = clamp((int) (g / count * 0.28f + 20));
            int bb = clamp((int) (b / count * 0.32f + 28));
            return Color.argb(255, rr, gg, bb);
        } catch (Throwable ignored) {
            return FALLBACK;
        }
    }

    public static void applyOpaquePanel(View view) {
        if (view == null) return;
        int color = panelColor();
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(new float[]{
                dp(20), dp(20), dp(20), dp(20),
                0, 0, 0, 0
        });
        view.setBackground(gd);
    }

    public static void applyOpaquePanelFull(View view) {
        if (view == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(panelColor());
        gd.setCornerRadius(dp(16));
        view.setBackground(gd);
    }

    private static int clamp(int v) {
        return Math.max(12, Math.min(80, v));
    }

    private static float dp(int v) {
        return ResUtil.dp2px(v);
    }
}
