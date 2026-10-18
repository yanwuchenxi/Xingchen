package com.xingchen.android.tv.ui.controller;

import android.text.TextUtils;

import com.xingchen.android.tv.runtime.XingchenRuntime;
import com.xingchen.android.tv.bean.Site;
import com.xingchen.android.tv.bean.Vod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 点播换源搜索：站点过滤与结果匹配。从 VideoActivity 抽出，与同类型推荐分离。
 */
public final class VideoSourceSwitch {

    private VideoSourceSwitch() {
    }

    /** 可参与换源搜索的站点列表 */
    public static List<Site> collectSites(boolean autoMode) {
        List<Site> sites = new ArrayList<>();
        for (Site item : XingchenRuntime.vod().getSites()) {
            if (autoMode && !item.isChangeable()) continue;
            if (item.isSearchable()) sites.add(item);
        }
        return sites;
    }

    /**
     * 是否丢弃搜索结果中的某条。
     * auto 模式要求片名完全一致；手动模式为包含关系。
     */
    public static boolean mismatch(Vod item, String currentId, Collection<String> broken, String keyword, boolean autoMode) {
        if (item == null) return true;
        if (!TextUtils.isEmpty(currentId) && currentId.equals(item.getId())) return true;
        if (broken != null && item.getId() != null && broken.contains(item.getId())) return true;
        String name = item.getName() == null ? "" : item.getName();
        String kw = keyword == null ? "" : keyword;
        if (autoMode) return !name.equals(kw);
        return !name.contains(kw);
    }
}
