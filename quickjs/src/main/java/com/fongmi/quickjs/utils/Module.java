package com.fongmi.quickjs.utils;

import android.text.TextUtils;
import android.util.LruCache;

import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Asset;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.Util;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class Module {

    private static final int MAX_SIZE = 80;
    /** 磁盘缓存最长 7 天，过期后重新拉取 */
    private static final long MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000;
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

        // http 模块：内存未命中则读磁盘
        if (name.startsWith("http")) {
            content = readDisk(name);
            if (!TextUtils.isEmpty(content)) {
                cache.put(name, content);
                return content;
            }
        }

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
        if (name.startsWith("http")) writeDisk(name, content);
        return content;
    }

    private String readDisk(String name) {
        try {
            File file = diskFile(name);
            if (!file.exists() || file.length() == 0) return "";
            if (System.currentTimeMillis() - file.lastModified() > MAX_AGE_MS) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                return "";
            }
            String body = Path.read(file);
            body = stripBom(body);
            if (!TextUtils.isEmpty(body)) {
                SpiderDebug.log("js-module", "disk hit name=%s len=%s", name, body.length());
            }
            return body;
        } catch (Throwable e) {
            return "";
        }
    }

    private void writeDisk(String name, String content) {
        try {
            File file = diskFile(name);
            Path.write(file, content.getBytes(StandardCharsets.UTF_8));
            SpiderDebug.log("js-module", "disk save name=%s len=%s", name, content.length());
        } catch (Throwable e) {
            SpiderDebug.log("js-module", "disk save fail name=%s err=%s", name, e.getMessage());
        }
    }

    private File diskFile(String name) {
        return Path.js(Util.md5(name) + ".js");
    }

    private static String stripBom(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }

    public void clear() {
        cache.evictAll();
    }

    /** 清除 JS 磁盘缓存（设置/调试用） */
    public void clearDisk() {
        try {
            File dir = Path.js();
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File f : files) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        } catch (Throwable ignored) {
        }
    }

    private static class Loader {
        static volatile Module INSTANCE = new Module();
    }
}
