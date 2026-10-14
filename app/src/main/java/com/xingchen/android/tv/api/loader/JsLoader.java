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

    public JsLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        Module.get().clear();
        spiders.clear();
        recent = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        Spider cached = spiders.get(key);
        if (cached != null && !(cached instanceof SpiderNull)) return cached;
        // 失败不长期缓存 SpiderNull，避免一次网络失败后整站永久空数据（影视仓可重试）
        spiders.remove(key);
        try {
            SpiderDebug.log("js", "init key=%s api=%s", key, api);
            Spider spider = loader.spider(api, BaseLoader.get().dex(jar));
            spider.siteKey = key;
            spider.init(App.get(), ext == null ? "" : ext);
            spiders.put(key, spider);
            SpiderDebug.log("js", "init ok key=%s", key);
            return spider;
        } catch (Throwable e) {
            SpiderDebug.log("js", "init fail key=%s api=%s err=%s", key, api, e.getMessage());
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
