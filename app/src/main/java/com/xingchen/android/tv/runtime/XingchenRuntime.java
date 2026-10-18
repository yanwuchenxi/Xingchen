package com.xingchen.android.tv.runtime;

/**
 * 星辰运行时总入口（B0）。
 * 后续 UI 优先：
 * {@code XingchenRuntime.vod()} / {@code XingchenRuntime.live()}
 */
public final class XingchenRuntime {

    private XingchenRuntime() {
    }

    public static VodRuntime vod() {
        return VodRuntimeCompat.get();
    }

    public static LiveRuntime live() {
        return LiveRuntime.Compat.get();
    }

    public static PlayRuntime play() {
        return PlayRuntime.Compat.get();
    }

    public static LoaderRuntime loader() {
        return LoaderRuntime.Compat.get();
    }
}
