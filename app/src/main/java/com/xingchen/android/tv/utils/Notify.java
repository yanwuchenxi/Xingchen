package com.xingchen.android.tv.utils;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.databinding.ViewProgressBinding;

public class Notify {

    public static final String DEFAULT = "default";
    public static final int ID = 9527;
    private AlertDialog mDialog;
    private Toast mToast;

    private static class Loader {
        static volatile Notify INSTANCE = new Notify();
    }

    private static Notify get() {
        return Loader.INSTANCE;
    }

    public static void createChannel() {
        NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(App.get());
        notifyMgr.createNotificationChannel(new NotificationChannelCompat.Builder(DEFAULT, NotificationManagerCompat.IMPORTANCE_LOW).setName("TV").build());
    }

    public static String getError(int resId, Throwable e) {
        if (TextUtils.isEmpty(e.getMessage())) return ResUtil.getString(resId);
        return ResUtil.getString(resId) + "\n" + e.getMessage();
    }

    public static void show(Notification notification) {
        if (ContextCompat.checkSelfPermission(App.get(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(App.get()).notify(ID, notification);
    }

    public static void show(int resId) {
        if (resId != 0) show(ResUtil.getString(resId));
    }

    public static void show(String text) {
        if (!TextUtils.isEmpty(text)) get().makeText(text);
    }

    public static void progress(Context context) {
        dismiss();
        get().create(context);
    }

    public static void dismiss() {
        try {
            if (get().mDialog != null) get().mDialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    private void create(Context context) {
        ViewProgressBinding binding = ViewProgressBinding.inflate(LayoutInflater.from(context));
        mDialog = DarkAlert.create(DarkAlert.builder(context).setView(binding.getRoot()));
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.show();
    }

    /** 自定义 Toast：左侧固定星辰图标，避免系统默认立方体等无关图标 */
    private void makeText(String text) {
        try {
            if (mToast != null) mToast.cancel();
            Context context = App.get();
            View root = LayoutInflater.from(context).inflate(R.layout.view_toast, null);
            ImageView icon = root.findViewById(R.id.icon);
            TextView tv = root.findViewById(R.id.text);
            if (icon != null) icon.setImageResource(R.mipmap.ic_launcher);
            if (tv != null) tv.setText(text);
            mToast = new Toast(context);
            mToast.setView(root);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, (int) (context.getResources().getDisplayMetrics().density * 96));
            mToast.show();
        } catch (Throwable e) {
            // 降级：系统 Toast（无自定义图标）
            try {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(App.get(), text, Toast.LENGTH_LONG);
                mToast.show();
            } catch (Throwable ignored) {
            }
        }
    }
}
