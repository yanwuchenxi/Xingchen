package com.xingchen.android.tv.bean;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.player.PlayerHelper;
import com.xingchen.android.tv.utils.UrlUtil;
import com.github.catvod.utils.Trans;
import com.google.gson.annotations.SerializedName;

public class Sub {

    @SerializedName("url")
    private String url;
    @SerializedName("name")
    private String name;
    @SerializedName("lang")
    private String lang;
    @SerializedName("format")
    private String format;
    @SerializedName("flag")
    private int flag;


    public static Sub create(String name, String lang, String url, String format) {
        Sub sub = new Sub();
        sub.name = name;
        sub.lang = lang;
        sub.url = url;
        sub.format = format;
        sub.flag = C.SELECTION_FLAG_DEFAULT;
        return sub;
    }

    public Sub setUrl(String url) { this.url = url; return this; }
    public Sub setName(String name) { this.name = name; return this; }
    public Sub setLang(String lang) { this.lang = lang; return this; }
    public Sub setFormat(String format) { this.format = format; return this; }
    public Sub setFlag(int flag) { this.flag = flag; return this; }

    public static Sub from(String path) {
        Sub sub = new Sub();
        sub.url = path;
        sub.name = UrlUtil.path(path);
        sub.flag = C.SELECTION_FLAG_FORCED;
        sub.format = PlayerHelper.getSubtitleMimeType(sub.name);
        return sub;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public String getLang() {
        return TextUtils.isEmpty(lang) ? "" : lang;
    }

    public String getFormat() {
        return TextUtils.isEmpty(format) ? "" : format;
    }

    public int getFlag() {
        return flag == 0 ? C.SELECTION_FLAG_DEFAULT : flag;
    }

    public void trans() {
        if (Trans.pass()) return;
        this.name = Trans.s2t(name);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Sub it)) return false;
        return getUrl().equals(it.getUrl());
    }

    @NonNull
    @Override
    public String toString() {
        return App.gson().toJson(this);
    }
}
