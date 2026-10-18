package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.LiveApi;
import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.bean.Channel;
import com.xingchen.android.tv.bean.Epg;
import com.xingchen.android.tv.bean.EpgData;
import com.xingchen.android.tv.bean.Live;
import com.xingchen.android.tv.bean.Result;

import java.time.ZoneId;

/**
 * 直播运行时边界（B1）。
 * ViewModel 应依赖本接口，而不是直接调用 LiveApi。
 */
public interface LiveRuntime {

    @Nullable
    Live getHome();

    void setHome(@Nullable Live live);

    void parse(@NonNull Live item) throws Exception;

    boolean parseXml(@NonNull Live item);

    @NonNull
    Epg getEpg(@NonNull Channel item, @NonNull ZoneId zoneId);

    @NonNull
    Result getUrl(@NonNull Channel item) throws Exception;

    @NonNull
    Result getUrl(@NonNull Channel item, @NonNull EpgData data) throws Exception;

    /** 兼容实现：委托 LiveConfig + LiveApi */
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

        @Override
        public void parse(@NonNull Live item) throws Exception {
            LiveApi.parse(item);
        }

        @Override
        public boolean parseXml(@NonNull Live item) {
            return LiveApi.parseXml(item);
        }

        @NonNull
        @Override
        public Epg getEpg(@NonNull Channel item, @NonNull ZoneId zoneId) {
            return LiveApi.getEpg(item, zoneId);
        }

        @NonNull
        @Override
        public Result getUrl(@NonNull Channel item) throws Exception {
            return LiveApi.getUrl(item);
        }

        @NonNull
        @Override
        public Result getUrl(@NonNull Channel item, @NonNull EpgData data) throws Exception {
            return LiveApi.getUrl(item, data);
        }
    }
}
