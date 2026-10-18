package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.api.config.WallConfig;
import com.xingchen.android.tv.bean.Config;
import com.xingchen.android.tv.impl.Callback;

/**
 * 订阅/配置加载边界（B4/B5）。
 */
public interface ConfigRuntime {

    void initHome(@Nullable Callback vodCallback);

    void clearVodAndLive();

    void loadVod(@Nullable Callback callback);

    void loadLive();

    void loadLive(@Nullable Callback callback);

    void loadVodConfig(@NonNull Config config, @Nullable Callback callback);

    void loadLiveConfig(@NonNull Config config, @Nullable Callback callback);

    int getVodCid();

    @NonNull
    String getVodDesc();

    @NonNull
    String getLiveDesc();

    boolean hasLiveUrl();

    boolean isLiveEmpty();

    boolean isLiveOnly();

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

        @Override
        public void loadVodConfig(@NonNull Config config, @Nullable Callback callback) {
            VodConfig.load(config, callback);
        }

        @Override
        public void loadLiveConfig(@NonNull Config config, @Nullable Callback callback) {
            LiveConfig.load(config, callback);
        }

        @Override
        public int getVodCid() {
            return VodConfig.getCid();
        }

        @NonNull
        @Override
        public String getVodDesc() {
            String d = VodConfig.getDesc();
            return d == null ? "" : d;
        }

        @NonNull
        @Override
        public String getLiveDesc() {
            String d = LiveConfig.getDesc();
            return d == null ? "" : d;
        }

        @Override
        public boolean hasLiveUrl() {
            return LiveConfig.hasUrl();
        }

        @Override
        public boolean isLiveEmpty() {
            return LiveConfig.isEmpty();
        }

        @Override
        public boolean isLiveOnly() {
            return LiveConfig.isOnly();
        }
    }
}
