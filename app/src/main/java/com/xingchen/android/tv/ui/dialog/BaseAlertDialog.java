package com.xingchen.android.tv.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class BaseAlertDialog extends DialogFragment {

    protected abstract ViewBinding getBinding();

    protected abstract MaterialAlertDialogBuilder getBuilder();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = getBuilder().create();
        initView();
        initEvent();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        applyDarkWindow();
    }

    /**
     * 强制深色圆角窗体：覆盖 Dynamic Color 的浅色 surface，
     * 避免「奶油外框 + 深色内容」叠层导致字看不清。
     */
    private void applyDarkWindow() {
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#E6121828"));
        gd.setCornerRadius(ResUtil.dp2px(18));
        window.setBackgroundDrawable(gd);
    }

    protected MaterialAlertDialogBuilder builder() {
        return new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_Alert);
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
