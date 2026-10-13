package com.xingchen.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.ui.SubtitleView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.bean.Sub;
import com.xingchen.android.tv.databinding.AdapterOnlineSubtitleBinding;
import com.xingchen.android.tv.databinding.DialogPlayerSubtitleBinding;
import com.xingchen.android.tv.player.PlayerManager;
import com.xingchen.android.tv.subtitle.OnlineSubtitle;
import com.xingchen.android.tv.subtitle.model.SubtitleAsset;
import com.xingchen.android.tv.subtitle.model.SubtitleCandidate;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.provider.SubtitleProviderRegistry;
import com.xingchen.android.tv.utils.FileChooser;
import com.xingchen.android.tv.utils.Notify;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.Task;
import com.xingchen.android.tv.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 播放页字幕面板（移植星影：偏移 + 搜索 + 在线列表 + 本地选择）。
 * 搜索走现有 OnlineSubtitle（迅雷/射手），应用走 PlayerManager.setSub / setTextOffsetMs。
 */
public final class PlayerSubtitleDialog extends BaseBottomSheetDialog {

    private static final long STEP_MS = 500L;

    private final List<SubtitleCandidate> items = new ArrayList<>();
    private DialogPlayerSubtitleBinding binding;
    private Adapter adapter;
    private PlayerManager player;
    private SubtitleView subtitleView;
    private String seedTitle = "";
    private boolean busy;
    private long offsetMs;

    public static PlayerSubtitleDialog create() {
        return new PlayerSubtitleDialog();
    }

    public PlayerSubtitleDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public PlayerSubtitleDialog subtitleView(SubtitleView view) {
        this.subtitleView = view;
        return this;
    }

    public PlayerSubtitleDialog title(String title) {
        this.seedTitle = title == null ? "" : title.trim();
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
            if (f instanceof PlayerSubtitleDialog) return;
        }
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected boolean transparent() {
        return true;
    }

    @Override
    protected float sheetWidthRatio() {
        return ResUtil.isLand(requireContext()) ? 0.55f : 1f;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogPlayerSubtitleBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        adapter = new Adapter();
        binding.recycler.setAdapter(adapter);
        if (player != null) {
            try {
                offsetMs = player.getTextOffsetMs();
            } catch (Throwable ignored) {
                offsetMs = 0L;
            }
        }
        updateOffsetLabel();
        if (!TextUtils.isEmpty(seedTitle)) {
            binding.keyword.setText(seedTitle);
            binding.status.setText(R.string.online_subtitle_providers);
            binding.getRoot().post(this::doSearch);
        } else {
            binding.status.setText(R.string.online_subtitle_providers);
        }
    }

    @Override
    protected void initEvent() {
        binding.offsetMinus.setOnClickListener(v -> adjustOffset(-STEP_MS));
        binding.offsetPlus.setOnClickListener(v -> adjustOffset(STEP_MS));
        binding.offsetReset.setOnClickListener(v -> {
            offsetMs = 0L;
            applyOffset();
        });
        binding.search.setOnClickListener(v -> doSearch());
        binding.choose.setOnClickListener(v -> FileChooser.from(launcher).show());
        binding.style.setOnClickListener(v -> {
            dismiss();
            if (getActivity() instanceof FragmentActivity activity) {
                SubtitleDialog.create().view(subtitleView).player(player).show(activity);
            }
        });
        binding.keyword.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                doSearch();
                return true;
            }
            return false;
        });
    }

    private void adjustOffset(long delta) {
        offsetMs += delta;
        applyOffset();
    }

    private void applyOffset() {
        if (player != null) {
            try {
                player.setTextOffsetMs(offsetMs);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        updateOffsetLabel();
    }

    private void updateOffsetLabel() {
        double sec = offsetMs / 1000.0;
        String text = String.format(Locale.getDefault(), "%+.1fs", sec);
        binding.offsetValue.setText(text);
    }

    private void doSearch() {
        if (busy) return;
        String q = binding.keyword.getText() == null ? "" : binding.keyword.getText().toString().trim();
        if (q.isEmpty()) {
            binding.status.setText(R.string.online_subtitle_hint);
            return;
        }
        busy = true;
        binding.search.setEnabled(false);
        binding.status.setText(R.string.online_subtitle_searching);
        items.clear();
        adapter.notifyDataSetChanged();
        Util.hideKeyboard(binding.keyword);
        Task.execute(() -> {
            try {
                List<SubtitleCandidate> result = OnlineSubtitle.search(q);
                App.post(() -> onSearchDone(result, null));
            } catch (Throwable e) {
                App.post(() -> onSearchDone(null, e));
            }
        });
    }

    private void onSearchDone(@Nullable List<SubtitleCandidate> result, @Nullable Throwable error) {
        busy = false;
        binding.search.setEnabled(true);
        items.clear();
        if (error != null) {
            String msg = error.getMessage() == null ? error.toString() : error.getMessage();
            binding.status.setText(ResUtil.getString(R.string.online_subtitle_error, msg));
        } else if (result == null || result.isEmpty()) {
            binding.status.setText(R.string.online_subtitle_empty);
        } else {
            items.addAll(result);
            binding.status.setText(ResUtil.getString(R.string.online_subtitle_title) + " · " + items.size());
        }
        adapter.notifyDataSetChanged();
    }

    private void applyCandidate(SubtitleCandidate candidate) {
        if (busy || player == null || candidate == null) return;
        busy = true;
        binding.status.setText(R.string.online_subtitle_applying);
        Task.execute(() -> {
            try {
                SubtitleContext context = SubtitleContext.builder()
                        .canonicalTitle(seedTitle)
                        .build();
                SubtitleAsset asset = SubtitleProviderRegistry.get().resolve(candidate, context);
                if (asset == null || TextUtils.isEmpty(asset.getUri())) {
                    throw new IllegalStateException("empty asset");
                }
                Sub sub = Sub.create(
                        firstNonEmpty(asset.getDisplayName(), candidate.getDisplayName()),
                        firstNonEmpty(asset.getLanguage(), candidate.getLanguage()),
                        asset.getUri(),
                        firstNonEmpty(asset.getMimeType(), candidate.getFormat())
                );
                if (asset.getSelectionFlag() != 0) sub.setFlag(asset.getSelectionFlag());
                App.post(() -> {
                    player.setSub(sub);
                    applyOffset();
                    busy = false;
                    Notify.show(R.string.online_subtitle_applied);
                    dismiss();
                });
            } catch (Throwable e) {
                App.post(() -> {
                    busy = false;
                    binding.status.setText(R.string.online_subtitle_apply_fail);
                    Notify.show(R.string.online_subtitle_apply_fail);
                    e.printStackTrace();
                });
            }
        });
    }

    private static String firstNonEmpty(String a, String b) {
        if (!TextUtils.isEmpty(a)) return a;
        return b == null ? "" : b;
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null)
                    return;
                if (player == null) return;
                player.setSub(Sub.from(FileChooser.getPathFromUri(result.getData().getData())));
                applyOffset();
                Notify.show(R.string.online_subtitle_applied);
                dismiss();
            });

    private class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(AdapterOnlineSubtitleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            SubtitleCandidate item = items.get(position);
            holder.binding.name.setText(item.getDisplayName());
            String meta = item.getProvider() == null ? "" : item.getProvider();
            if (!TextUtils.isEmpty(item.getLanguage())) meta += " · " + item.getLanguage();
            if (!TextUtils.isEmpty(item.getFormat())) meta += " · " + item.getFormat();
            if (!TextUtils.isEmpty(item.getReleaseInfo())) meta += " · " + item.getReleaseInfo();
            holder.binding.meta.setText(meta);
            holder.itemView.setOnClickListener(v -> applyCandidate(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final AdapterOnlineSubtitleBinding binding;

            Holder(AdapterOnlineSubtitleBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
