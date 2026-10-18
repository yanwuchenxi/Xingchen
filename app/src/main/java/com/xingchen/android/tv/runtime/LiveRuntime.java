package com.xingchen.android.tv.runtime;

import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.bean.Live;

/**
 * 直播运行时边界（B 阶段薄封装）。
 */
public interface LiveRuntime {

    @Nullable
    Live getHome();

    void setHome(@Nullable Live live);

    /** 当前兼容实现 */
    final class Compat implements LiveRuntime {
        private static final Compat INSTANCE = new Compat();

        public static LiveRuntime get() {
            return INSTANCE;
        }

        private Compat() {
        }

        @Nullable
        @Override
        public Live getHome() {
            return LiveConfig.get().getHome();
        }

        @Override
        public void setHome(@Nullable Live live) {
            if (live != null) LiveConfig.get().setHome(live);
        }
    }
}
