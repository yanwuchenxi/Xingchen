package com.xingchen.android.tv.subtitle;

import com.xingchen.android.tv.setting.Setting;
import com.xingchen.android.tv.subtitle.model.SubtitleCandidate;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.model.SubtitleQuery;
import com.xingchen.android.tv.subtitle.model.SubtitleQuerySource;
import com.xingchen.android.tv.subtitle.model.SubtitleStrictness;
import com.xingchen.android.tv.subtitle.provider.SubtitleProviderRegistry;

import java.util.Collections;
import java.util.List;

/**
 * 在线字幕搜索入口。
 * 当前启用迅雷（标题搜索）与射手（本地文件 hash）。
 */
public final class OnlineSubtitle {

    private OnlineSubtitle() {
    }

    public static List<SubtitleCandidate> search(String title) {
        return search(title, Setting.getSubtitlePreferredLanguage());
    }

    public static List<SubtitleCandidate> search(String title, String language) {
        if (title == null || title.trim().isEmpty()) return Collections.emptyList();
        try {
            String q = title.trim();
            String lang = language == null || language.isEmpty() ? "zh" : language;
            SubtitleQuery query = new SubtitleQuery(
                    "manual",
                    q,
                    lang,
                    SubtitleQuerySource.MANUAL,
                    SubtitleStrictness.NORMAL,
                    -1,
                    -1,
                    -1
            );
            SubtitleContext context = SubtitleContext.builder()
                    .canonicalTitle(q)
                    .preferredLanguage(lang)
                    .build();
            return SubtitleProviderRegistry.get().search(Collections.singletonList(query), context);
        } catch (Throwable e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
