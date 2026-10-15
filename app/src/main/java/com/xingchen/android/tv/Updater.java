package com.xingchen.android.tv;

import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.xingchen.android.tv.impl.UpdateListener;
import com.xingchen.android.tv.setting.Setting;
import com.xingchen.android.tv.ui.dialog.UpdateDialog;
import com.xingchen.android.tv.utils.DarkAlert;
import com.xingchen.android.tv.utils.Download;
import com.xingchen.android.tv.utils.FileUtil;
import com.xingchen.android.tv.utils.Github;
import com.xingchen.android.tv.utils.Notify;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.Task;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import org.json.JSONObject;

import java.io.File;

public class Updater implements Download.Callback, UpdateListener {

    private final Download download;
    private UpdateDialog dialog;
    private AlertDialog checking;

    private Updater() {
        this.download = Download.create(getApk(), getFile());
    }

    public static Updater create() {
        return new Updater();
    }

    private File getFile() {
        return Path.cache("update.apk");
    }

    private String getJson() {
        return Github.getJson(BuildConfig.FLAVOR_mode);
    }

    private String getApk() {
        return Github.getApk(BuildConfig.FLAVOR_mode + "-" + BuildConfig.FLAVOR_abi);
    }

    public Updater force() {
        Setting.putUpdate(true);
        return this;
    }

    public void start(FragmentActivity activity) {
        if (!Setting.getUpdate()) return;
        App.post(() -> showChecking(activity));
        Task.execute(() -> doInBackground(activity));
    }

    private void showChecking(FragmentActivity activity) {
        try {
            dismissChecking();
            if (activity == null || activity.isFinishing()) return;
            View root = LayoutInflater.from(activity).inflate(R.layout.dialog_update_check, null);
            android.widget.ImageView icon = root.findViewById(R.id.icon);
            android.widget.TextView title = root.findViewById(R.id.title);
            android.widget.TextView hint = root.findViewById(R.id.hint);
            if (icon != null) icon.setImageResource(R.mipmap.ic_launcher);
            if (title != null) title.setText(R.string.update_check);
            if (hint != null) hint.setText(R.string.app_name);
            checking = DarkAlert.create(DarkAlert.builder(activity).setView(root).setCancelable(true));
            checking.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void dismissChecking() {
        try {
            if (checking != null) checking.dismiss();
        } catch (Exception ignored) {
        }
        checking = null;
    }

    private void doInBackground(FragmentActivity activity) {
        try {
            JSONObject object = new JSONObject(OkHttp.string(getJson()));
            String name = object.optString("name");
            String desc = object.optString("desc");
            int code = object.optInt("code");
            if (code <= BuildConfig.VERSION_CODE) {
                App.post(() -> {
                    dismissChecking();
                    Notify.show(R.string.update_latest);
                });
                return;
            }
            App.post(() -> {
                dismissChecking();
                show(activity, name, desc);
            });
        } catch (Exception e) {
            e.printStackTrace();
            App.post(() -> {
                dismissChecking();
                Notify.show(R.string.update_check_fail);
            });
        }
    }

    private void show(FragmentActivity activity, String version, String desc) {
        dismiss();
        dialog = UpdateDialog.create().title(ResUtil.getString(R.string.update_version, version)).desc(desc).listener(this).show(activity);
    }

    @Override
    public void onConfirm(View view) {
        view.setEnabled(false);
        download.start(this);
    }

    @Override
    public void onCancel(View view) {
        Setting.putUpdate(false);
        download.cancel();
        dismiss();
    }

    private void dismiss() {
        try {
            if (dialog != null) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void progress(int progress) {
        if (dialog != null) dialog.setProgress(progress);
    }

    @Override
    public void error(String msg) {
        Notify.show(msg);
        dismiss();
    }

    @Override
    public void success(File file) {
        FileUtil.openFile(file);
        dismiss();
    }
}
