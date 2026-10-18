package com.xingchen.android.tv.ui.controller;

import com.xingchen.android.tv.runtime.XingchenRuntime;
import com.xingchen.android.tv.bean.Class;
import com.xingchen.android.tv.bean.Result;
import com.xingchen.android.tv.bean.Site;
import com.xingchen.android.tv.bean.Vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 点播详情「同类型推荐」：与换源搜索分离，供 VideoActivity 调用。
 */
public final class VideoTypeRecommend {

    private VideoTypeRecommend() {
    }

    public static List<Vod> fetch(String siteKey, String vodId, String typeName, String name) {
        List<Vod> out = new ArrayList<>();
        try {
            Site site = XingchenRuntime.vod().getSite(siteKey);
            if (site == null || site.getKey() == null || site.getKey().isEmpty()) return out;
            String tn = typeName == null ? "" : typeName.trim();
            String nm = name == null ? "" : name.trim();
            Result result = null;
            boolean fromCategory = false;
            String tid = resolveTypeId(site, tn);
            if (tid != null && !tid.isEmpty()) {
                try {
                    result = XingchenRuntime.vod().categoryContent(siteKey, tid, "1", false, new HashMap<>());
                    fromCategory = result != null && !result.getList().isEmpty();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if ((result == null || result.getList().isEmpty()) && site.isSearchable()) {
                String kw = !tn.isEmpty() ? tn : nm;
                if (!kw.isEmpty()) {
                    try {
                        result = XingchenRuntime.vod().searchContent(site, kw, true, "1");
                        fromCategory = false;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            if (result == null) return out;
            for (Vod v : result.getList()) {
                if (v == null) continue;
                if (vodId != null && !vodId.isEmpty() && vodId.equals(v.getId())) continue;
                if (!fromCategory && !tn.isEmpty()) {
                    String vt = v.getTypeName() == null ? "" : v.getTypeName().trim();
                    if (!vt.isEmpty() && !vt.contains(tn) && !tn.contains(vt)) continue;
                }
                if (v.getSite() == null) v.setSite(site);
                out.add(v);
                if (out.size() >= 24) break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return out;
    }

    private static String resolveTypeId(Site site, String typeName) {
        if (typeName == null || typeName.isEmpty()) return null;
        try {
            Result home = XingchenRuntime.vod().homeContent(site);
            if (home == null || home.getTypes() == null) return null;
            String exact = null, soft = null;
            for (Class c : home.getTypes()) {
                if (c == null) continue;
                String tn = c.getTypeName() == null ? "" : c.getTypeName().trim();
                if (tn.isEmpty()) continue;
                if (tn.equals(typeName)) {
                    exact = c.getTypeId();
                    break;
                }
                if (soft == null && (tn.contains(typeName) || typeName.contains(tn))) soft = c.getTypeId();
            }
            return exact != null ? exact : soft;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
