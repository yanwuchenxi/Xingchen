package com.xingchen.android.tv.ui.controller;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.utils.ResUtil;

/**
 * 直播竖屏「上播放器+下列表」与横屏侧栏列表的布局切换。从 LiveActivity 抽出。
 */
public final class LiveLayoutHelper {

    private LiveLayoutHelper() {
    }

    /** 竖屏：列表挂在内容根下，播放器固定 16:9 高度 */
    public static void applyEmbedded(Context context, ViewGroup content, View video, View recycler, View liveContent, View homeNav) {
        ensureRecyclerInRoot(content, video, recycler);
        int videoH = Math.round(ResUtil.getScreenWidth(context) * 9f / 16f);
        video.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, videoH));
        recycler.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        recycler.setVisibility(View.VISIBLE);
        if (liveContent != null && homeNav != null) {
            RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            clp.addRule(RelativeLayout.ABOVE, homeNav.getId());
            liveContent.setLayoutParams(clp);
        }
        if (homeNav != null) homeNav.setVisibility(View.VISIBLE);
    }

    /** 横屏/全屏：列表叠在播放器左侧 */
    public static void applyOverlay(Context context, ViewGroup video, View recycler, View liveContent, View homeNav) {
        if (homeNav != null) homeNav.setVisibility(View.GONE);
        if (liveContent != null) {
            RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            liveContent.setLayoutParams(clp);
        }
        ensureRecyclerOverlayOnVideo(context, video, recycler);
        video.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
    }

    public static void ensureRecyclerInRoot(ViewGroup content, View video, View recycler) {
        if (content == null || recycler == null || video == null) return;
        if (recycler.getParent() == content) return;
        ViewGroup parent = (ViewGroup) recycler.getParent();
        if (parent != null) parent.removeView(recycler);
        int index = content.indexOfChild(video);
        content.addView(recycler, index < 0 ? -1 : index + 1,
                new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 14f));
    }

    public static void ensureRecyclerOverlayOnVideo(Context context, ViewGroup video, View recycler) {
        if (video == null || recycler == null) return;
        if (recycler.getParent() == video) return;
        ViewGroup parent = (ViewGroup) recycler.getParent();
        if (parent != null) parent.removeView(recycler);
        int sw = ResUtil.getScreenWidth(context);
        int w = Math.max(ResUtil.dp2px(280), Math.round(sw * 0.42f));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        video.addView(recycler, lp);
        recycler.setBackgroundResource(R.color.transparent);
    }
}
