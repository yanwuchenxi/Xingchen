package com.xingchen.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.utils.DarkAlert;
import com.xingchen.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Alert 基类：强制 {@link DarkAlert} 主题，禁止无主题 Builder。
 */
public abstract class BaseAlertDialog extends DialogFragment {

    protected abstract ViewBinding getBinding();

    protected abstract MaterialAlertDialogBuilder getBuilder();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = DarkAlert.create(getBuilder());
        initView();
        initEvent();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof AlertDialog alert) {
            DarkAlert.applyWindow(alert);
        }
    }

    /** 唯一 Builder 入口 */
    protected MaterialAlertDialogBuilder builder() {
        return DarkAlert.builder(requireActivity());
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void setWidth(float factor) {
        if (getDialog() == null || getDialog().getWindow() == null) return;
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (ResUtil.getScreenWidth() * factor);
        getDialog().getWindow().setAttributes(params);
    }
}
