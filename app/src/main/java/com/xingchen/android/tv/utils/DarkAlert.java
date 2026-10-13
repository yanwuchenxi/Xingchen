package com.xingchen.android.tv.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.xingchen.android.tv.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** 与直播弹窗一致的深色 Material Alert */
public final class DarkAlert {

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
        gd.setColor(Color.parseColor("#E6121828"));
        gd.setCornerRadius(ResUtil.dp2px(18));
        window.setBackgroundDrawable(gd);
    }

    public static AlertDialog show(@NonNull MaterialAlertDialogBuilder builder) {
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> applyWindow(dialog));
        dialog.show();
        return dialog;
    }
}
