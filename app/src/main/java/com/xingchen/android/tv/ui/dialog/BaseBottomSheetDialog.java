package com.xingchen.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.R;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment {

    protected abstract ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> setBehavior(dialog));
        Window window = dialog.getWindow();
        if (window == null) return dialog;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Util.isFullscreen(getActivity())) window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getBinding(inflater, container).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initEvent();
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected boolean transparent() {
        // 液态玻璃：默认透明底板，由内容 drawable 承担玻璃背景
        return true;
    }

    /** 底部面板宽度占屏比，1f 为全宽 */
    protected float sheetWidthRatio() {
        return 1f;
    }

    protected void setBehavior(BottomSheetDialog dialog) {
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        if (transparent()) {
            sheet.setBackgroundColor(ResUtil.getColor(R.color.transparent));
            sheet.setBackground(null);
            sheet.setElevation(0f);
            try {
                sheet.setBackgroundTintList(null);
            } catch (Throwable ignored) {
            }
        }
        float ratio = sheetWidthRatio();
        if (ratio > 0f && ratio < 1f) {
            int width = Math.round(ResUtil.getScreenWidth() * ratio);
            ViewGroup.LayoutParams lp = sheet.getLayoutParams();
            lp.width = width;
            sheet.setLayoutParams(lp);
            if (sheet.getParent() instanceof ViewGroup parent) {
                if (parent instanceof android.widget.FrameLayout) {
                    android.widget.FrameLayout.LayoutParams flp = (android.widget.FrameLayout.LayoutParams) sheet.getLayoutParams();
                    flp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
                    sheet.setLayoutParams(flp);
                }
            }
        }
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        behavior.setFitToContents(true);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
