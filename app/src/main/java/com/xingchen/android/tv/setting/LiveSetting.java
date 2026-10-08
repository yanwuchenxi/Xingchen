package com.xingchen.android.tv.setting;

import com.github.catvod.utils.Prefers;

public class LiveSetting {

    public static final int LIST_STYLE_CLASSIC = 0; // 易读
    public static final int LIST_STYLE_GLASS = 1;   // 通透

    public static boolean isBoot() {
        return Prefers.getBoolean("boot_live");
    }

    public static void putBoot(boolean boot) {
        Prefers.put("boot_live", boot);
    }

    public static boolean isAcross() {
        return Prefers.getBoolean("across", true);
    }

    public static void putAcross(boolean across) {
        Prefers.put("across", across);
    }

    public static boolean isChange() {
        return Prefers.getBoolean("change", true);
    }

    public static void putChange(boolean change) {
        Prefers.put("change", change);
    }

    public static boolean isInvert() {
        return Prefers.getBoolean("invert");
    }

    public static void putInvert(boolean invert) {
        Prefers.put("invert", invert);
    }

    public static int getScale() {
        return Prefers.getInt("scale_live", PlayerSetting.getScale());
    }

    public static void putScale(int scale) {
        Prefers.put("scale_live", scale);
    }

    public static int getListStyle() {
        int style = Prefers.getInt("live_list_style", LIST_STYLE_GLASS);
        return style == LIST_STYLE_CLASSIC ? LIST_STYLE_CLASSIC : LIST_STYLE_GLASS;
    }

    public static boolean isListStyleClassic() {
        return getListStyle() == LIST_STYLE_CLASSIC;
    }

    public static void putListStyle(int style) {
        Prefers.put("live_list_style", style == LIST_STYLE_CLASSIC ? LIST_STYLE_CLASSIC : LIST_STYLE_GLASS);
    }

    public static void putListStyleClassic(boolean classic) {
        putListStyle(classic ? LIST_STYLE_CLASSIC : LIST_STYLE_GLASS);
    }

    /** 屏显：时间/网速/分辨率/标题/参数 */
    public static boolean[] getLiveDisplayChecked() {
        String raw = Prefers.getString("live_display_flags", "1,1,1,1,0");
        String[] parts = raw.split(",");
        boolean[] out = new boolean[5];
        for (int i = 0; i < out.length; i++) {
            out[i] = i < parts.length && "1".equals(parts[i].trim());
        }
        return out;
    }

    public static void putLiveDisplayChecked(boolean[] checked) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i > 0) sb.append(',');
            sb.append(i < checked.length && checked[i] ? '1' : '0');
        }
        Prefers.put("live_display_flags", sb.toString());
    }
}
