package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xingchen.android.tv.api.SiteApi;
import com.xingchen.android.tv.api.config.VodConfig;
import com.xingchen.android.tv.bean.Parse;
import com.xingchen.android.tv.bean.Result;
import com.xingchen.android.tv.bean.Site;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * B0 兼容实现：100% 委托现有 VodConfig + SiteApi，保证源站行为不变。
 */
public final class VodRuntimeCompat implements VodRuntime {

    private static final VodRuntimeCompat INSTANCE = new VodRuntimeCompat();

    public static VodRuntime get() {
        return INSTANCE;
    }

    private VodRuntimeCompat() {
    }

    @NonNull
    @Override
    public List<Site> getSites() {
        List<Site> sites = VodConfig.get().getSites();
        return sites == null ? Collections.emptyList() : sites;
    }

    @Nullable
    @Override
    public Site getHome() {
        return VodConfig.get().getHome();
    }

    @Override
    public void setHome(@NonNull Site site) {
        VodConfig.get().setHome(site);
    }

    @Nullable
    @Override
    public Site getSite(@NonNull String key) {
        return VodConfig.get().getSite(key);
    }

    @NonNull
    @Override
    public List<Parse> getParses() {
        List<Parse> list = VodConfig.get().getParses();
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public void setParse(@Nullable Parse parse) {
        VodConfig.get().setParse(parse);
    }

    @NonNull
    @Override
    public String pushKey() {
        return SiteApi.PUSH;
    }

    @NonNull
    @Override
    public Result homeContent(@NonNull Site site) throws Exception {
        return SiteApi.homeContent(site);
    }

    @NonNull
    @Override
    public Result categoryContent(@NonNull String siteKey, @NonNull String tid, @NonNull String page, boolean filter, @NonNull HashMap<String, String> extend) throws Exception {
        return SiteApi.categoryContent(siteKey, tid, page, filter, extend);
    }

    @NonNull
    @Override
    public Result detailContent(@NonNull String siteKey, @NonNull String id) throws Exception {
        return SiteApi.detailContent(siteKey, id);
    }

    @NonNull
    @Override
    public Result playerContent(@NonNull String siteKey, @NonNull String flag, @NonNull String id) throws Exception {
        return SiteApi.playerContent(siteKey, flag, id);
    }

    @NonNull
    @Override
    public Result searchContent(@NonNull Site site, @NonNull String keyword, boolean quick, @NonNull String page) throws Exception {
        return SiteApi.searchContent(site, keyword, quick, page);
    }

    @NonNull
    @Override
    public Result action(@NonNull String siteKey, @NonNull String act) throws Exception {
        return SiteApi.action(siteKey, act);
    }
}
