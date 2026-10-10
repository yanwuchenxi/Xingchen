package com.xingchen.android.tv.ui.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.bean.Sub;
import com.xingchen.android.tv.databinding.AdapterOnlineSubtitleBinding;
import com.xingchen.android.tv.databinding.DialogOnlineSubtitleBinding;
import com.xingchen.android.tv.player.PlayerManager;
import com.xingchen.android.tv.subtitle.OnlineSubtitle;
import com.xingchen.android.tv.subtitle.model.SubtitleAsset;
import com.xingchen.android.tv.subtitle.model.SubtitleCandidate;
import com.xingchen.android.tv.subtitle.model.SubtitleContext;
import com.xingchen.android.tv.subtitle.provider.SubtitleProviderRegistry;
import com.xingchen.android.tv.utils.Notify;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.Task;
import com.xingchen.android.tv.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 在线字幕搜索面板：搜索 → 选中 → resolve → PlayerManager.setSub
 */
public final class OnlineSubtitleDialog extends BaseBottomSheetDialog {

    private final List<SubtitleCandidate> items = new ArrayList<>();
    private DialogOnlineSubtitleBinding binding;
    private PlayerManager player;
    private String seedTitle = "";
    private Adapter adapter;
    private boolean busy;

    public static OnlineSubtitleDialog create() {
        return new OnlineSubtitleDialog();
    }

    public OnlineSubtitleDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public OnlineSubtitleDialog title(String title) {
        this.seedTitle = title == null ? "" : title.trim();
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
            if (f instanceof OnlineSubtitleDialog) return;
        }
        show(activity.getSupportFragmentManager(), "online_subtitle");
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogOnlineSubtitleBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        adapter = new Adapter();
        binding.recycler.setAdapter(adapter);
        if (!TextUtils.isEmpty(seedTitle)) {
            binding.keyword.setText(seedTitle);
            binding.keyword.setSelection(seedTitle.length());
            binding.status.setText(R.string.online_subtitle_searching);
            // 有默认片名时自动搜一次
            binding.getRoot().post(this::doSearch);
        } else {
            binding.status.setText(R.string.online_subtitle_hint);
        }
    }

    @Override
    protected void initEvent() {
        binding.search.setOnClickListener(v -> doSearch());
        binding.keyword.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                doSearch();
                return true;
            }
            return false;
        });
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
            binding.status.setText(ResUtil.getString(R.string.online_subtitle_error, error.getMessage() == null ? error.toString() : error.getMessage()));
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
            String meta = item.getProvider();
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
