package com.xingchen.android.tv.utils;

/**
 * 应用内更新下载地址。
 * 约定：GitHub Release 的 latest 资源中包含
 *   - mobile.json / leanback.json（版本信息）
 *   - mobile-arm64_v8a.apk 等（安装包）
 *
 * 私有仓库需公开 Release，或把仓库设为 Public，否则客户端无法无鉴权下载。
 */
public class Github {

    public static final String OWNER = "yanwuchenxi";
    public static final String REPO = "Xingchen";
    /** latest Release 资源根路径 */
    public static final String URL = "https://github.com/" + OWNER + "/" + REPO + "/releases/latest/download";

    private static String getUrl(String name) {
        return URL + "/" + name;
    }

    public static String getJson(String name) {
        return getUrl(name + ".json");
    }

    public static String getApk(String name) {
        return getUrl(name + ".apk");
    }
}
