package com.xingchen.android.tv.ui.controller;

import android.view.View;
import android.view.ViewGroup;

import com.xingchen.android.tv.bean.Channel;
import com.xingchen.android.tv.bean.Epg;
import com.xingchen.android.tv.bean.EpgData;
import com.xingchen.android.tv.bean.Group;
import com.xingchen.android.tv.bean.Live;
import com.xingchen.android.tv.utils.ResUtil;

/**
 * 直播分组/频道/EPG 列宽计算。从 LiveActivity 抽出。
 */
public final class LiveWidthHelper {

    private LiveWidthHelper() {
    }

    public static int groupColumnWidth(Live live) {
        if (live == null) return 0;
        int padding = ResUtil.dp2px(48);
        if (live.getWidth() == 0) {
            for (Group item : live.getGroups()) {
                live.setWidth(Math.max(live.getWidth(), ResUtil.getTextWidth(item.getName(), 14)));
            }
        }
        return live.getWidth() == 0 ? 0 : Math.min(live.getWidth() + padding, ResUtil.getScreenWidth() / 4);
    }

    public static int channelColumnWidth(Group group) {
        if (group == null) return 0;
        int logo = ResUtil.dp2px(56);
        int padding = ResUtil.dp2px(60);
        if (group.isKeep()) group.setWidth(0);
        if (group.getWidth() == 0) {
            for (Channel item : group.getChannel()) {
                group.setWidth(Math.max(group.getWidth(), (item.getLogo().isEmpty() ? 0 : logo) + ResUtil.getTextWidth(item.getNumber() + item.getName(), 14)));
            }
        }
        return group.getWidth() == 0 ? 0 : Math.min(group.getWidth() + padding, ResUtil.getScreenWidth() / 2);
    }

    public static int epgColumnWidth(Epg epg) {
        if (epg == null || epg.getList().isEmpty()) return 0;
        int padding = ResUtil.dp2px(48);
        int minWidth = ResUtil.getTextWidth(epg.getList().get(0).getTime(), 12);
        if (epg.getWidth() == 0) {
            for (EpgData item : epg.getList()) {
                epg.setWidth(Math.max(epg.getWidth(), ResUtil.getTextWidth(item.getTitle(), 14)));
            }
        }
        return epg.getWidth() == 0 ? 0 : Math.min(Math.max(epg.getWidth(), minWidth) + padding, ResUtil.getScreenWidth() / 2);
    }

    public static void apply(View view, int width) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null || params.width == width) return;
        params.width = width;
        view.setLayoutParams(params);
    }
}
