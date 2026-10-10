package com.xingchen.android.tv.subtitle.provider;

import android.util.Log;

import com.xingchen.android.tv.subtitle.model.SubtitleAsset;
import com.xingchen.android.tv.subtitle.model.SubtitleCandidate;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.model.SubtitleQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** 在线字幕 Provider 注册与并行搜索（Silent 适配精简版） */
public final class SubtitleProviderRegistry {

    private static final String TAG = "SubtitleMatch";
    private static final int MAX_SEARCH_THREADS = 8;
    private static final SubtitleProviderRegistry INSTANCE = new SubtitleProviderRegistry();

    private final Map<String, SubtitleProvider> providers = new LinkedHashMap<>();

    public SubtitleProviderRegistry() {
        registerLegacyProviders();
    }

    public static SubtitleProviderRegistry get() {
        return INSTANCE;
    }

    SubtitleProviderRegistry(SubtitleProvider... items) {
        if (items != null) for (SubtitleProvider p : items) register(p);
    }

    private void registerLegacyProviders() {
        register(new XunleiSubtitleProvider());
        register(new ShooterSubtitleProvider());
    }

    public void register(SubtitleProvider provider) {
        if (provider == null) return;
        providers.put(provider.getName(), provider);
    }

    public void destroy() {
        providers.clear();
    }

    public List<SubtitleProvider> enabledProviders() {
        List<SubtitleProvider> items = new ArrayList<>();
        for (SubtitleProvider provider : providers.values()) if (provider.isEnabled()) items.add(provider);
        return items;
    }

    public List<SubtitleCandidate> search(List<SubtitleQuery> queries, SubtitleContext context) {
        List<SubtitleCandidate> items = new ArrayList<>();
        if (queries == null || queries.isEmpty()) return items;
        List<SubtitleProvider> enabled = enabledProviders();
        if (enabled.isEmpty()) return items;

        List<Future<List<SubtitleCandidate>>> futures = new ArrayList<>();
        int threads = Math.max(1, Math.min(MAX_SEARCH_THREADS, enabled.size() * queries.size()));
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (SubtitleProvider provider : enabled) {
                for (SubtitleQuery query : queries) {
                    if (query == null) continue;
                    futures.add(executor.submit(() -> {
                        try {
                            return provider.search(query, context);
                        } catch (Throwable e) {
                            Log.w(TAG, provider.getName() + " search failed: " + e.getMessage());
                            return new ArrayList<>();
                        }
                    }));
                }
            }
            for (Future<List<SubtitleCandidate>> f : futures) {
                try {
                    List<SubtitleCandidate> part = f.get();
                    if (part != null) items.addAll(part);
                } catch (ExecutionException | InterruptedException e) {
                    Log.w(TAG, "search task failed: " + e.getMessage());
                }
            }
        } finally {
            executor.shutdownNow();
        }
        return items;
    }

    public SubtitleAsset resolve(SubtitleCandidate candidate, SubtitleContext context) throws Exception {
        if (candidate == null) return null;
        SubtitleProvider provider = providers.get(candidate.getProvider());
        if (provider == null) {
            for (SubtitleProvider p : providers.values()) {
                if (p.getName().equalsIgnoreCase(candidate.getProvider())) {
                    provider = p;
                    break;
                }
            }
        }
        if (provider == null) throw new IllegalStateException("provider not found: " + candidate.getProvider());
        return provider.resolve(candidate, context);
    }
}
