package com.fongmi.quickjs.utils;

import android.text.TextUtils;
import android.util.LruCache;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Asset;

public class Module {

    private static final int MAX_SIZE = 80;
    private final LruCache<String, String> cache;

    public Module() {
        cache = new LruCache<>(MAX_SIZE);
    }

    public static Module get() {
        return Loader.INSTANCE;
    }

    public String fetch(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String content = cache.get(name);
        if (!TextUtils.isEmpty(content)) return content;
        try {
            if (name.startsWith("http")) content = OkHttp.string(name);
            else if (name.startsWith("assets")) content = Asset.read(name);
            else if (name.startsWith("lib/")) content = Asset.read("js/" + name);
            else content = "";
        } catch (Throwable e) {
            SpiderDebug.log("js-module", "fetch fail name=%s err=%s", name, e.getMessage());
            content = "";
        }
        content = stripBom(content);
        if (TextUtils.isEmpty(content)) {
            SpiderDebug.log("js-module", "empty content name=%s", name);
            return "";
        }
        cache.put(name, content);
        return content;
    }

    private static String stripBom(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }

    public void clear() {
        cache.evictAll();
    }

    private static class Loader {
        static volatile Module INSTANCE = new Module();
    }
}
