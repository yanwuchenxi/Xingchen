package com.xingchen.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.DarkAlert;

public class WebDialog {

    private final AlertDialog dialog;

    private WebDialog(View view) {
        this.dialog = DarkAlert.create(DarkAlert.builder(App.activity()).setView(view));
        this.dialog.setOnDismissListener((DialogInterface.OnDismissListener) view);
    }

    public static WebDialog create(View view) {
        return new WebDialog(view);
    }

    public WebDialog show() {
        initDialog();
        return this;
    }

    public void dismiss() {
        dialog.setOnDismissListener(null);
        dialog.dismiss();
    }

    private void initDialog() {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.height = (int) (ResUtil.getScreenHeight() * 0.8f);
        params.width = (int) (ResUtil.getScreenWidth() * 0.8f);
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }
}
