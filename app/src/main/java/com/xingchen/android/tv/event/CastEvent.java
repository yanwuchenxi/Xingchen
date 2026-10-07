package com.xingchen.android.tv.event;

import com.xingchen.android.tv.bean.Config;
import com.xingchen.android.tv.bean.Device;
import com.xingchen.android.tv.bean.History;

import org.greenrobot.eventbus.EventBus;

public record CastEvent(Config config, Device device, History history) {

    public static void post(Config config, Device device, History history) {
        EventBus.getDefault().post(new CastEvent(config, device, history));
    }
}
