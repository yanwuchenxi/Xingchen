package com.xingchen.android.tv.runtime;

import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.api.config.WallConfig;
import com.xingchen.android.tv.impl.Callback;

/**
 * 订阅/配置加载边界（B4）。
 * Home 启动、清理经此入口，避免 UI 直接堆叠三个 Config 单例。
 */
public interface ConfigRuntime {

    /** 冷启动：点播回调 + 直播/壁纸 init */
    void initHome(@Nullable Callback vodCallback);

    void clearVodAndLive();

    void loadVod(@Nullable Callback callback);

    void loadLive();

    void loadLive(@Nullable Callback callback);

    final class Compat implements ConfigRuntime {
        private static final Compat INSTANCE = new Compat();

        public static ConfigRuntime get() {
            return INSTANCE;
        }

        private Compat() {
        }

        @Override
        public void initHome(@Nullable Callback vodCallback) {
            VodConfig.get().init().load(vodCallback);
            LiveConfig.get().init().load();
            WallConfig.get().init();
        }

        @Override
        public void clearVodAndLive() {
            LiveConfig.get().clear();
            VodConfig.get().clear();
        }

        @Override
        public void loadVod(@Nullable Callback callback) {
            VodConfig.get().init().load(callback);
        }

        @Override
        public void loadLive() {
            LiveConfig.get().init().load();
        }

        @Override
        public void loadLive(@Nullable Callback callback) {
            LiveConfig.get().init().load(callback);
        }
    }
}
