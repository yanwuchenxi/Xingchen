package com.xingchen.android.tv.ui.dialog;

import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.utils.DarkAlert;

public class WebDialog {

    private final AlertDialog dialog;

    public static WebDialog create(View view) {
        return new WebDialog(view);
    }

    private WebDialog(View view) {
        this.dialog = DarkAlert.create(DarkAlert.builder(App.activity()).setView(view));
        this.dialog.setOnDismissListener((DialogInterface.OnDismissListener) view);
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
        dialog.show();
    }
}
