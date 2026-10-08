package com.xingchen.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.xingchen.android.tv.bean.Channel;
import com.xingchen.android.tv.bean.Epg;
import com.xingchen.android.tv.bean.EpgData;
import com.xingchen.android.tv.databinding.DialogLiveProgramBinding;
import com.xingchen.android.tv.ui.adapter.LiveProgramAdapter;
import com.xingchen.android.tv.ui.adapter.LiveProgramDateAdapter;
import com.xingchen.android.tv.utils.Formatters;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class LiveProgramDialog extends BaseBottomSheetDialog implements LiveProgramDateAdapter.OnClickListener, LiveProgramAdapter.OnClickListener {

    private DialogLiveProgramBinding binding;
    private LiveProgramDateAdapter dateAdapter;
    private LiveProgramAdapter programAdapter;
    private Consumer<EpgData> listener;
    private Channel channel;
    private ZoneId zoneId;

    public static LiveProgramDialog create() {
        return new LiveProgramDialog();
    }

    public LiveProgramDialog channel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public LiveProgramDialog zoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public LiveProgramDialog listener(Consumer<EpgData> listener) {
        this.listener = listener;
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            if (fragment instanceof LiveProgramDialog) return;
        }
        show(activity.getSupportFragmentManager(), "LiveProgramDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        configureWindow(dialog);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        configureWindow(getDialog());
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogLiveProgramBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        if (channel == null) return;
        if (zoneId == null) zoneId = ZoneId.systemDefault();
        binding.title.setText(channel.getShow());
        binding.date.setHasFixedSize(false);
        binding.date.setItemAnimator(null);
        binding.program.setHasFixedSize(false);
        binding.program.setItemAnimator(null);
        binding.date.setAdapter(dateAdapter = new LiveProgramDateAdapter(this));
        binding.program.setAdapter(programAdapter = new LiveProgramAdapter(this));
        List<Epg> items = new ArrayList<>(channel.getDataList());
        items.sort(Comparator.comparing(Epg::getDate));
        String today = LocalDate.now(zoneId).format(Formatters.DATE);
        dateAdapter.setItems(items, today);
        showProgram(dateAdapter.getSelected());
    }

    @Override
    public void onDateClick(Epg epg) {
        showProgram(epg);
    }

    @Override
    public void onProgramClick(EpgData item) {
        if (listener != null) listener.accept(item);
        dismiss();
    }

    private void showProgram(Epg epg) {
        if (epg == null) return;
        epg.selected();
        programAdapter.setEpg(epg);
        binding.program.post(() -> scrollToPosition(binding.program, Math.max(programAdapter.getSelected(), 0)));
    }

    private void scrollToPosition(RecyclerView view, int position) {
        if (view.getLayoutManager() != null) view.getLayoutManager().scrollToPosition(position);
    }

    private void configureWindow(Dialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setDimAmount(0f);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        WindowCompat.setDecorFitsSystemWindows(window, true);
    }

    @Override
    protected boolean transparent() {
        return true;
    }

    @Override
    protected void setBehavior(BottomSheetDialog dialog) {
        super.setBehavior(dialog);
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        sheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }
}
