package com.fongmi.quickjs.crawler;

import android.content.Context;

import com.fongmi.quickjs.bean.Res;
import com.fongmi.quickjs.method.Console;
import com.fongmi.quickjs.method.Global;
import com.fongmi.quickjs.method.Local;
import com.fongmi.quickjs.utils.Async;
import com.fongmi.quickjs.utils.JSUtil;
import com.fongmi.quickjs.utils.Module;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Asset;
import com.github.catvod.utils.Json;
import com.github.catvod.utils.UriUtil;
import com.github.catvod.utils.Util;
import com.whl.quickjs.wrapper.JSArray;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dalvik.system.DexClassLoader;

public class Spider extends com.github.catvod.crawler.Spider {

    private final ExecutorService executor;
    private final DexClassLoader dex;
    private final String api;

    private QuickJSContext ctx;
    private JSObject jsObject;
    private boolean cat;

    public Spider(String api, DexClassLoader dex) {
        this.executor = Executors.newSingleThreadExecutor();
        this.api = api;
        this.dex = dex;
    }

    private <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    private Object call(String func, Object... args) throws Exception {
        Object result = submit(() -> Async.run(jsObject, func, args)).get().get();
        if (result == null) return null;
        if (result instanceof String) return result;
        if (result instanceof JSObject) {
            try {
                return submit(() -> ((JSObject) result).stringify()).get();
            } catch (Throwable e) {
                return result.toString();
            }
        }
        return String.valueOf(result);
    }

    @Override
    public void init(Context context, String extend) throws Exception {
        final String resolved = resolveExtend(extend == null ? "" : extend);
        // 初始化与 init 调用放同一条 JS 线程任务，避免跨线程碰 QuickJS 对象
        submit(() -> {
            createCtx();
            createFun();
            createObj();
            Object extArg = getExt(resolved);
            Async.run(jsObject, "init", extArg).get();
            return null;
        }).get();
    }

    private String resolveExtend(String extend) {
        if (extend == null || extend.isEmpty()) return "";
        String e = extend.trim();
        if (e.startsWith("http://") || e.startsWith("https://") || e.startsWith("file://")) {
            try {
                String body = Module.get().fetch(e.startsWith("file://") ? e : e);
                if (body != null && !body.isEmpty()) {
                    SpiderDebug.log("js", "ext prefetched len=%s url=%s", body.length(), e);
                    return body;
                }
                SpiderDebug.log("js", "ext prefetch empty, pass url url=%s", e);
            } catch (Throwable t) {
                SpiderDebug.log("js", "ext prefetch fail url=%s err=%s", e, t.getMessage());
            }
        }
        return e;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        Object r = call("home", filter);
        String s = r == null ? "" : String.valueOf(r);
        SpiderDebug.log("js", "home api=%s len=%s", api, s.length());
        return s;
    }

    @Override
    public String homeVideoContent() throws Exception {
        return (String) call("homeVod");
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        JSObject obj = submit(() -> JSUtil.toObject(ctx, extend)).get();
        return (String) call("category", tid, pg, filter, obj);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        return (String) call("detail", ids.get(0));
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return (String) call("search", key, quick);
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return (String) call("search", key, quick, pg);
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSArray array = submit(() -> JSUtil.toArray(ctx, vipFlags)).get();
        return (String) call("play", flag, id, array);
    }

    @Override
    public String liveContent(String url) throws Exception {
        return (String) call("live", url);
    }

    @Override
    public boolean manualVideoCheck() throws Exception {
        return (Boolean) call("sniffer");
    }

    @Override
    public boolean isVideoFormat(String url) throws Exception {
        return (Boolean) call("isVideo", url);
    }

    @Override
    public Object[] proxy(Map<String, String> params) throws Exception {
        return "catvod".equals(params.get("from")) ? proxy2(params) : proxy1(params);
    }

    @Override
    public String action(String action) throws Exception {
        return (String) call("action", action);
    }

    @Override
    public void destroy() {
        try {
            call("destroy");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            releaseJS();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }

    private void releaseJS() throws Exception {
        submit(() -> {
            jsObject.release();
            ctx.destroy();
            return null;
        }).get();
    }

    private void initializeJS() throws Exception {
        submit(() -> {
            createCtx();
            createFun();
            createObj();
            return null;
        }).get();
    }

    private void createCtx() {
        ctx = QuickJSContext.create();
        ctx.setConsole(new Console());
        ctx.evaluate(Asset.read("js/lib/http.js"));
        ctx.getGlobalObject().setProperty("local", Local.class);
        ctx.setModuleLoader(new QuickJSContext.BytecodeModuleLoader() {
            @Override
            public String moduleNormalizeName(String baseModuleName, String moduleName) {
                return UriUtil.resolve(baseModuleName, moduleName);
            }

            @Override
            public byte[] getModuleBytecode(String moduleName) {
                return ctx.compileModule(Module.get().fetch(moduleName), moduleName);
            }
        });
    }

    private void createFun() {
        try {
            Global.create(ctx, executor);
            Class<?> clz = dex.loadClass("com.github.catvod.js.Function");
            clz.getDeclaredConstructor(QuickJSContext.class).newInstance(ctx);
        } catch (Throwable ignored) {
        }
    }

    private void createObj() {
        String spider = "__JS_SPIDER__";
        String global = "globalThis." + spider;
        String content = Module.get().fetch(api);
        if (content == null || content.isEmpty()) {
            throw new IllegalStateException("js empty: " + api);
        }
        cat = content.contains("__jsEvalReturn");
        ctx.evaluateModule(content.replace(spider, global), api);
        ctx.evaluateModule(Asset.read("js/lib/spider.js").replace("%s", api));
        jsObject = (JSObject) ctx.getProperty(ctx.getGlobalObject(), spider);
        if (jsObject == null) {
            throw new IllegalStateException("js spider object null: " + api);
        }
    }

    private Object getExt(String ext) {
        if (!cat) return Json.isObj(ext) ? ctx.parse(ext) : ext;
        JSObject obj = ctx.createNewJSObject();
        obj.setProperty("stype", 3);
        obj.setProperty("skey", siteKey);
        if (!Json.isObj(ext)) obj.setProperty("ext", ext);
        else obj.setProperty("ext", (JSObject) ctx.parse(ext));
        return obj;
    }

    private Object[] proxy1(Map<String, String> params) throws Exception {
        JSObject obj = submit(() -> JSUtil.toObject(ctx, params)).get();
        JSArray proxy = (JSArray) call("proxy", obj);
        String json = submit(proxy::stringify).get();
        JSONArray array = new JSONArray(json);
        Map<String, String> headers = array.length() > 3 ? Json.toMap(array.optString(3)) : null;
        boolean base64 = array.length() > 4 && array.optInt(4) == 1;
        Object[] result = new Object[4];
        result[0] = array.optInt(0);
        result[1] = array.optString(1);
        result[2] = getStream(array.opt(2), base64);
        result[3] = headers;
        return result;
    }

    private Object[] proxy2(Map<String, String> params) throws Exception {
        String url = params.get("url");
        String header = params.get("header");
        JSArray array = submit(() -> JSUtil.toArray(ctx, Arrays.asList(url.split("/")))).get();
        Object object = submit(() -> ctx.parse(header)).get();
        String proxy = (String) call("proxy", array, object);
        Res res = Res.objectFrom(proxy);
        Object[] result = new Object[3];
        result[0] = res.getCode();
        result[1] = res.getContentType();
        result[2] = res.getStream();
        return result;
    }

    private ByteArrayInputStream getStream(Object o, boolean base64) {
        if (o instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) o);
        } else {
            String content = o.toString();
            if (base64 && content.contains("base64,")) content = content.split("base64,")[1];
            return new ByteArrayInputStream(base64 ? Util.decode(content) : content.getBytes());
        }
    }
}
