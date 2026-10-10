package com.xingchen.android.tv.subtitle;

import com.xingchen.android.tv.subtitle.model.ResolvedMediaIdentity;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.model.SubtitleRequest;

/**
 * 轻量上下文构建：不依赖 TMDB / MediaTitle 学习模块。
 */
public final class SubtitleContextBuilder {

    private final SubtitleTitleParser parser;

    public SubtitleContextBuilder() {
        this.parser = new SubtitleTitleParser();
    }

    public SubtitleContext build(SubtitleRequest request) {
        return build(request, false);
    }

    public SubtitleContext build(SubtitleRequest request, boolean forceAi) {
        if (request == null) {
            return SubtitleContext.builder().build();
        }
        String vodName = request.getVodName() == null ? "" : request.getVodName();
        String cleaned = parser.cleanTitle(vodName);
        ResolvedMediaIdentity identity = ResolvedMediaIdentity.builder()
                .canonicalTitle(cleaned)
                .build();
        return SubtitleContext.builder()
                .playbackKey(request.getPlaybackKey())
                .siteKey(request.getSiteKey())
                .vodId(request.getVodId())
                .canonicalTitle(cleaned)
                .originalTitle(vodName)
                .identity(identity)
                .preferredLanguage(request.getPreferredLanguage())
                .build();
    }
}
