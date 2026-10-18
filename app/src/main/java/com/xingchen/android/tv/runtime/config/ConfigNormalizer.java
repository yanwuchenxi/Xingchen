package com.xingchen.android.tv.runtime.config;

import androidx.annotation.NonNull;

import com.github.catvod.utils.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * C0：订阅 JSON 规范化。
 * <ul>
 *   <li>legacy（无 schema / 非 xingchen）→ 原样返回</li>
 *   <li>schema = xingchen/1 → 展开别名，输出与 FongMi 同构的对象供现有解析复用</li>
 * </ul>
 */
public final class ConfigNormalizer {

    public static final String SCHEMA_KEY = "schema";
    public static final String SCHEMA_XINGCHEN_V1 = "xingchen/1";

    private ConfigNormalizer() {
    }

    public enum Format {
        LEGACY,
        XINGCHEN_V1
    }

    @NonNull
    public static Format detect(@NonNull JsonObject root) {
        String schema = Json.safeString(root, SCHEMA_KEY);
        if (SCHEMA_XINGCHEN_V1.equalsIgnoreCase(schema) || "xingchen".equalsIgnoreCase(schema)) {
            return Format.XINGCHEN_V1;
        }
        return Format.LEGACY;
    }

    /**
     * 返回可供 {@code VodConfig} / {@code LiveConfig} 现有逻辑解析的对象。
     * 不修改入参；必要时返回深拷贝后的规范化副本。
     */
    @NonNull
    public static JsonObject normalize(@NonNull JsonObject root) {
        Format format = detect(root);
        if (format == Format.LEGACY) return root;
        JsonObject out = root.deepCopy();
        applyAliases(out);
        // 保留 schema 无妨；下游 Json.safe* 会忽略未知字段
        return out;
    }

    private static void applyAliases(@NonNull JsonObject o) {
        renameArray(o, "sources", "sites");
        renameArray(o, "channels", "lives");
        renameArray(o, "parsers", "parses");
        renameString(o, "wall", "wallpaper");
        renameString(o, "wallpaper_url", "wallpaper");
        // 站点列表内：id → key（仅当缺少 key）
        if (o.has("sites") && o.get("sites").isJsonArray()) {
            for (JsonElement el : o.getAsJsonArray("sites")) {
                if (!el.isJsonObject()) continue;
                JsonObject site = el.getAsJsonObject();
                if (!site.has("key") && site.has("id")) {
                    site.add("key", site.get("id"));
                }
                if ((!site.has("api") || Json.safeString(site, "api").isEmpty()) && site.has("spider")) {
                    site.add("api", site.get("spider"));
                }
            }
        }
    }

    private static void renameArray(@NonNull JsonObject o, @NonNull String from, @NonNull String to) {
        if (o.has(to) || !o.has(from)) return;
        o.add(to, o.get(from));
        o.remove(from);
    }

    private static void renameString(@NonNull JsonObject o, @NonNull String from, @NonNull String to) {
        if (o.has(to) || !o.has(from)) return;
        o.add(to, o.get(from));
        o.remove(from);
    }
}
