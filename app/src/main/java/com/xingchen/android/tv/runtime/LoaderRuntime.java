package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.loader.BaseLoader;
import com.github.catvod.crawler.Spider;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Spider / Jar / JS / Py 加载边界（B3）。
 * 业务与 bean 经此访问，实现仍委托 {@link BaseLoader}。
 */
public interface LoaderRuntime {

    void clear();

    @NonNull
    Spider getSpider(@NonNull String key, @NonNull String api, @Nullable String ext, @Nullable String jar);

    @NonNull
    Spider getSpider(@NonNull String key);

    @Nullable
    String getLastError();

    void setRecent(@NonNull String key, @NonNull String api, @Nullable String jar);

    @Nullable
    Object[] proxy(@NonNull Map<String, String> params) throws Exception;

    void parseJar(@Nullable String jar, boolean recent);

    @Nullable
    DexClassLoader dex(@Nullable String jar);

    @NonNull
    JSONObject jsonExt(@NonNull String key, @NonNull LinkedHashMap<String, String> jxs, @NonNull String url) throws Throwable;

    @NonNull
    JSONObject jsonExtMix(@NonNull String flag, @NonNull String key, @NonNull String name,
                          @NonNull LinkedHashMap<String, HashMap<String, String>> jxs, @NonNull String url) throws Throwable;

    final class Compat implements LoaderRuntime {
        private static final Compat INSTANCE = new Compat();

        public static LoaderRuntime get() {
            return INSTANCE;
        }

        private Compat() {
        }

        private BaseLoader base() {
            return BaseLoader.get();
        }

        @Override
        public void clear() {
            base().clear();
        }

        @NonNull
        @Override
        public Spider getSpider(@NonNull String key, @NonNull String api, @Nullable String ext, @Nullable String jar) {
            return base().getSpider(key, api, ext == null ? "" : ext, jar == null ? "" : jar);
        }

        @NonNull
        @Override
        public Spider getSpider(@NonNull String key) {
            return base().getSpider(key);
        }

        @Nullable
        @Override
        public String getLastError() {
            return base().getLastError();
        }

        @Override
        public void setRecent(@NonNull String key, @NonNull String api, @Nullable String jar) {
            base().setRecent(key, api, jar == null ? "" : jar);
        }

        @Nullable
        @Override
        public Object[] proxy(@NonNull Map<String, String> params) throws Exception {
            return base().proxy(params);
        }

        @Override
        public void parseJar(@Nullable String jar, boolean recent) {
            base().parseJar(jar, recent);
        }

        @Nullable
        @Override
        public DexClassLoader dex(@Nullable String jar) {
            return base().dex(jar);
        }

        @NonNull
        @Override
        public JSONObject jsonExt(@NonNull String key, @NonNull LinkedHashMap<String, String> jxs, @NonNull String url) throws Throwable {
            return base().jsonExt(key, jxs, url);
        }

        @NonNull
        @Override
        public JSONObject jsonExtMix(@NonNull String flag, @NonNull String key, @NonNull String name,
                                     @NonNull LinkedHashMap<String, HashMap<String, String>> jxs, @NonNull String url) throws Throwable {
            return base().jsonExtMix(flag, key, name, jxs, url);
        }
    }
}
