package com.xingchen.android.tv.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.xingchen.android.tv.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 全仓库唯一允许的 Alert 入口。
 * 禁止：{@code new MaterialAlertDialogBuilder(context)} 无主题写法。
 * 必须：{@link #builder(Context)} 或 {@link #show(MaterialAlertDialogBuilder)}。
 */
public final class DarkAlert {

    private static final int PANEL = Color.parseColor("#E6121828");

    private DarkAlert() {
    }

    @NonNull
    public static MaterialAlertDialogBuilder builder(@NonNull Context context) {
        return new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_App_Alert);
    }

    public static void applyWindow(@NonNull AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(PANEL);
        gd.setCornerRadius(ResUtil.dp2px(18));
        window.setBackgroundDrawable(gd);
        // 列表/标题/正文强制白字，避免主题 attr 在深色底上发灰或发黑
        try {
            int white = Color.WHITE;
            int soft = Color.parseColor("#E6FFFFFF");
            android.widget.TextView title = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (title != null) title.setTextColor(white);
            android.widget.TextView msg = dialog.findViewById(android.R.id.message);
            if (msg != null) msg.setTextColor(soft);
            android.widget.ListView list = dialog.getListView();
            if (list != null) {
                list.setCacheColorHint(Color.TRANSPARENT);
                list.setSelector(android.R.color.transparent);
            }
        } catch (Throwable ignored) {
        }
    }

    /** create + 深色窗体，不 show（供 DialogFragment） */
    @NonNull
    public static AlertDialog create(@NonNull MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> applyWindow(dialog));
        return dialog;
    }

    @NonNull
    public static AlertDialog show(@NonNull MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = create(builder);
        dialog.show();
        return dialog;
    }
}
