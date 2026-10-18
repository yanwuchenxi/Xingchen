package com.xingchen.android.tv.runtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xingchen.android.tv.bean.Result;
import com.xingchen.android.tv.bean.Site;

import java.util.HashMap;
import java.util.List;

/**
 * 点播运行时边界（B 阶段）。
 * UI / ViewModel 逐步改为依赖本接口，避免直接依赖 SiteApi / Spider。
 * 订阅协议仍兼容 FongMi / webhtv 源。
 */
public interface VodRuntime {

    @NonNull
    List<Site> getSites();

    @Nullable
    Site getHome();

    void setHome(@NonNull Site site);

    @NonNull
    Result homeContent(@NonNull Site site) throws Exception;

    @NonNull
    Result categoryContent(@NonNull String siteKey, @NonNull String tid, @NonNull String page, boolean filter, @NonNull HashMap<String, String> extend) throws Exception;

    @NonNull
    Result detailContent(@NonNull String siteKey, @NonNull String id) throws Exception;

    @NonNull
    Result playerContent(@NonNull String siteKey, @NonNull String flag, @NonNull String id) throws Exception;

    @NonNull
    Result searchContent(@NonNull Site site, @NonNull String keyword, boolean quick, @NonNull String page) throws Exception;

    @NonNull
    Result action(@NonNull String siteKey, @NonNull String act) throws Exception;
}
