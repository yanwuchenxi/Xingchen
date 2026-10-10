package com.xingchen.android.tv.subtitle;

import com.xingchen.android.tv.subtitle.model.SubtitleCandidate;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.model.SubtitleQuery;
import com.xingchen.android.tv.subtitle.model.SubtitleQuerySource;
import com.xingchen.android.tv.subtitle.model.SubtitleStrictness;
import com.xingchen.android.tv.subtitle.provider.SubtitleProviderRegistry;

import java.util.Collections;
import java.util.List;

/**
 * 在线字幕搜索入口（自 Silent1566/webhtv 适配）。
 * 当前启用迅雷（标题搜索）与射手（本地文件 hash）。
 */
public final class OnlineSubtitle {

    private OnlineSubtitle() {
    }

    public static List<SubtitleCandidate> search(String title) {
        if (title == null || title.trim().isEmpty()) return Collections.emptyList();
        try {
            String q = title.trim();
            SubtitleQuery query = new SubtitleQuery(
                    "manual",
                    q,
                    "zh",
                    SubtitleQuerySource.MANUAL,
                    SubtitleStrictness.NORMAL,
                    -1,
                    -1,
                    -1
            );
            SubtitleContext context = SubtitleContext.builder()
                    .canonicalTitle(q)
                    .build();
            return SubtitleProviderRegistry.get().search(Collections.singletonList(query), context);
        } catch (Throwable e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
