package com.xingchen.android.tv.runtime.config;

import androidx.annotation.NonNull;

import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.bean.Live;
import com.xingchen.android.tv.bean.Parse;
import com.xingchen.android.tv.bean.Site;
import com.github.catvod.utils.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * C1：将当前内存中的点播/直播配置导出为 {@code schema: xingchen/1} JSON。
 */
public final class ConfigExporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ConfigExporter() {
    }

    @NonNull
    public static JsonObject build() {
        JsonObject root = new JsonObject();
        root.addProperty(ConfigNormalizer.SCHEMA_KEY, ConfigNormalizer.SCHEMA_XINGCHEN_V1);
        String name = VodConfig.getDesc();
        if (name == null || name.isEmpty()) name = LiveConfig.getDesc();
        if (name != null && !name.isEmpty()) root.addProperty("name", name);

        String spider = pickSpider();
        if (!spider.isEmpty()) root.addProperty("spider", spider);

        root.add("sites", exportSites());
        root.add("lives", exportLives());
        root.add("parses", exportParses());

        String wall = VodConfig.get().getWall();
        if (wall != null && !wall.isEmpty()) root.addProperty("wallpaper", wall);

        addStringList(root, "flags", VodConfig.get().getFlags());
        addStringList(root, "ads", VodConfig.get().getAds());
        return root;
    }

    @NonNull
    public static String toJson() {
        return GSON.toJson(build());
    }

    /** 写入应用 TV 目录，返回文件 */
    @NonNull
    public static File writeToTvDir() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        File file = new File(Path.tv(), "xingchen-config-" + stamp + ".json");
        Path.write(file, toJson().getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @NonNull
    private static String pickSpider() {
        for (Site site : VodConfig.get().getSites()) {
            if (site != null && !site.getJar().isEmpty()) return site.getJar();
        }
        for (Live live : LiveConfig.get().getLives()) {
            if (live != null && !live.getJar().isEmpty()) return live.getJar();
        }
        return "";
    }

    @NonNull
    private static JsonArray exportSites() {
        JsonArray arr = new JsonArray();
        for (Site site : VodConfig.get().getSites()) {
            if (site == null || site.getKey().isEmpty()) continue;
            JsonObject o = new JsonObject();
            o.addProperty("key", site.getKey());
            o.addProperty("name", site.getName());
            o.addProperty("type", site.getType());
            o.addProperty("api", site.getApi());
            if (!site.getExt().isEmpty()) o.addProperty("ext", site.getExt());
            if (!site.getJar().isEmpty()) o.addProperty("jar", site.getJar());
            if (site.getSearchable() != null) o.addProperty("searchable", site.getSearchable());
            if (site.getQuickSearch() != null) o.addProperty("quickSearch", site.getQuickSearch());
            if (site.getChangeable() != null) o.addProperty("changeable", site.getChangeable());
            if (site.isHide()) o.addProperty("hide", 1);
            if (site.getTimeout() > 0) o.addProperty("timeout", site.getTimeout());
            if (!site.getPlayUrl().isEmpty()) o.addProperty("playUrl", site.getPlayUrl());
            Map<String, String> header = site.getHeader();
            if (header != null && !header.isEmpty()) o.add("header", GSON.toJsonTree(header));
            arr.add(o);
        }
        return arr;
    }

    @NonNull
    private static JsonArray exportLives() {
        JsonArray arr = new JsonArray();
        for (Live live : LiveConfig.get().getLives()) {
            if (live == null || live.getName().isEmpty()) continue;
            JsonObject o = new JsonObject();
            o.addProperty("name", live.getName());
            if (!live.getUrl().isEmpty()) o.addProperty("url", live.getUrl());
            if (!live.getApi().isEmpty()) o.addProperty("api", live.getApi());
            if (!live.getExt().isEmpty()) o.addProperty("ext", live.getExt());
            if (!live.getJar().isEmpty()) o.addProperty("jar", live.getJar());
            if (!live.getLogo().isEmpty()) o.addProperty("logo", live.getLogo());
            if (!live.getEpg().isEmpty()) o.addProperty("epg", live.getEpg());
            arr.add(o);
        }
        return arr;
    }

    @NonNull
    private static JsonArray exportParses() {
        JsonArray arr = new JsonArray();
        for (Parse parse : VodConfig.get().getParses()) {
            if (parse == null) continue;
            // 跳过内置「超级解析」
            if (parse.getType() == 4) continue;
            JsonObject o = new JsonObject();
            o.addProperty("name", parse.getName());
            o.addProperty("type", parse.getType());
            o.addProperty("url", parse.getUrl());
            arr.add(o);
        }
        return arr;
    }

    private static void addStringList(JsonObject root, String key, java.util.List<String> list) {
        if (list == null || list.isEmpty()) return;
        JsonArray arr = new JsonArray();
        for (String s : list) if (s != null && !s.isEmpty()) arr.add(s);
        if (arr.size() > 0) root.add(key, arr);
    }
}
