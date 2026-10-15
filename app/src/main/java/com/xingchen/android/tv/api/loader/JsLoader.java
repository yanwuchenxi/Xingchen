package com.xingchen.android.tv.api.loader;

import com.xingchen.android.tv.App;
import com.fongmi.quickjs.crawler.Loader;
import com.fongmi.quickjs.utils.Module;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private final Loader loader;
    private volatile String recent;
    /** 最近一次初始化失败原因，供 UI 展示 */
    private volatile String lastError;

    public JsLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public String getLastError() {
        return lastError;
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        Module.get().clear();
        spiders.clear();
        recent = null;
        lastError = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        Spider cached = spiders.get(key);
        if (cached != null && !(cached instanceof SpiderNull)) return cached;
        spiders.remove(key);
        lastError = null;
        try {
            SpiderDebug.log("js", "init key=%s api=%s", key, api);
            if (api == null || api.isEmpty()) {
                lastError = "站点 api 为空";
                return new SpiderNull();
            }
            Spider spider = loader.spider(api, BaseLoader.get().dex(jar));
            spider.siteKey = key;
            spider.init(App.get(), ext == null ? "" : ext);
            spiders.put(key, spider);
            SpiderDebug.log("js", "init ok key=%s", key);
            return spider;
        } catch (Throwable e) {
            lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            SpiderDebug.log("js", "init fail key=%s api=%s err=%s", key, api, lastError);
            e.printStackTrace();
            return new SpiderNull();
        }
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
