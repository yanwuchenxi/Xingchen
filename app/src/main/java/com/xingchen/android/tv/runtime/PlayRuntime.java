package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaMetadata;

import com.xingchen.android.tv.bean.Result;
import com.xingchen.android.tv.bean.Vod;
import com.xingchen.android.tv.player.PlayerManager;
import com.xingchen.android.tv.player.Source;

/**
 * 播放运行时边界（B2）。
 * UI 经此创建播放器、构建元数据、停止嗅探源；实现仍为现有 PlayerManager / Source。
 */
public interface PlayRuntime {

    @NonNull
    MediaMetadata buildMetadata(@Nullable String title, @Nullable String artist, @Nullable String artUri);

    @NonNull
    PlayerManager createPlayer(@NonNull PlayerManager.Callback callback);

    void parseVodFlags(@NonNull Vod vod) throws Exception;

    @NonNull
    String fetchUrl(@NonNull Result result) throws Exception;

    void stopExtractors();

    void exitExtractors();

    final class Compat implements PlayRuntime {
        private static final Compat INSTANCE = new Compat();

        public static PlayRuntime get() {
            return INSTANCE;
        }

        private Compat() {
        }

        @NonNull
        @Override
        public MediaMetadata buildMetadata(@Nullable String title, @Nullable String artist, @Nullable String artUri) {
            return PlayerManager.buildMetadata(
                    title == null ? "" : title,
                    artist == null ? "" : artist,
                    artUri == null ? "" : artUri);
        }

        @NonNull
        @Override
        public PlayerManager createPlayer(@NonNull PlayerManager.Callback callback) {
            return new PlayerManager(callback);
        }

        @Override
        public void parseVodFlags(@NonNull Vod vod) throws Exception {
            Source.get().parse(vod);
        }

        @NonNull
        @Override
        public String fetchUrl(@NonNull Result result) throws Exception {
            return Source.get().fetch(result);
        }

        @Override
        public void stopExtractors() {
            Source.get().stop();
        }

        @Override
        public void exitExtractors() {
            Source.get().exit();
        }
    }
}
