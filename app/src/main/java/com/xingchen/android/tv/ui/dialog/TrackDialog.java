package com.xingchen.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Tracks;
import androidx.media3.ui.DefaultTrackNameProvider;
import androidx.media3.ui.SubtitleView;
import androidx.media3.ui.TrackNameProvider;
import androidx.viewbinding.ViewBinding;

import com.xingchen.android.tv.App;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.bean.Sub;
import com.xingchen.android.tv.bean.Track;
import com.xingchen.android.tv.databinding.DialogTrackBinding;
import com.xingchen.android.tv.player.PlayerHelper;
import com.xingchen.android.tv.player.PlayerManager;
import com.xingchen.android.tv.setting.PlayerSetting;
import com.xingchen.android.tv.ui.adapter.TrackAdapter;
import com.xingchen.android.tv.ui.custom.SpaceItemDecoration;
import com.xingchen.android.tv.utils.FileChooser;
import com.xingchen.android.tv.utils.ResUtil;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class TrackDialog extends BaseBottomSheetDialog implements TrackAdapter.OnClickListener {

    private static final long OFFSET_STEP_MS = 100;

    private final TrackNameProvider provider;
    private final TrackAdapter adapter;
    private DialogTrackBinding binding;
    private PlayerManager player;
    private SubtitleView subtitleView;
    private String vodTitle = "";
    private int type;

    public static TrackDialog create() {
        return new TrackDialog();
    }

    public TrackDialog() {
        this.adapter = new TrackAdapter(this);
        this.provider = new DefaultTrackNameProvider(App.get().getResources());
    }

    public TrackDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public TrackDialog subtitleView(SubtitleView subtitleView) {
        this.subtitleView = subtitleView;
        return this;
    }

    public TrackDialog title(String title) {
        this.vodTitle = title == null ? "" : title.trim();
        return this;
    }

    public TrackDialog type(int type) {
        this.type = type;
        return this;
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) if (f instanceof TrackDialog) return;
        show(activity.getSupportFragmentManager(), null);
    }

    private boolean hasChoose() {
        return type == C.TRACK_TYPE_TEXT && player != null && player.isVod();
    }

    private boolean hasStyle() {
        return type == C.TRACK_TYPE_TEXT && subtitleView != null;
    }

    private boolean hasOffset() {
        return player != null && (type == C.TRACK_TYPE_TEXT || type == C.TRACK_TYPE_AUDIO);
    }

    @Override
    protected boolean transparent() {
        return true;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogTrackBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        binding.recycler.setItemAnimator(null);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setAdapter(adapter.addAll(getTrack()));
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 16));
        binding.title.setText(ResUtil.getStringArray(R.array.select_track)[type - 1]);
        binding.recycler.post(() -> binding.recycler.scrollToPosition(adapter.getSelected()));
        boolean empty = adapter.getItemCount() == 0;
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.trackSectionTitle.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyHint.setVisibility(empty && hasChoose() ? View.VISIBLE : View.GONE);
        binding.choose.setVisibility(hasChoose() ? View.VISIBLE : View.GONE);
        binding.online.setVisibility(hasChoose() ? View.VISIBLE : View.GONE);
        binding.styleRow.setVisibility(hasStyle() ? View.VISIBLE : View.GONE);
        bindOffsetSection();
    }

    @Override
    protected void initEvent() {
        binding.choose.setOnClickListener(this::onChoose);
        binding.online.setOnClickListener(this::onOnline);
        binding.large.setOnClickListener(this::onLarge);
        binding.small.setOnClickListener(this::onSmall);
        binding.up.setOnClickListener(this::onUp);
        binding.down.setOnClickListener(this::onDown);
        binding.styleReset.setOnClickListener(this::onStyleReset);
        binding.offsetMinus.setOnClickListener(v -> nudgeOffset(-OFFSET_STEP_MS));
        binding.offsetPlus.setOnClickListener(v -> nudgeOffset(OFFSET_STEP_MS));
        binding.offsetReset.setOnClickListener(v -> applyOffset(0));
        binding.offsetSlider.addOnChangeListener((Slider slider, float value, boolean fromUser) -> {
            if (!fromUser || player == null) return;
            long ms = Math.round(value / OFFSET_STEP_MS) * OFFSET_STEP_MS;
            applyOffset(ms);
        });
    }

    private void bindOffsetSection() {
        if (!hasOffset()) {
            binding.offsetSection.setVisibility(View.GONE);
            return;
        }
        binding.offsetSection.setVisibility(View.VISIBLE);
        boolean text = type == C.TRACK_TYPE_TEXT;
        binding.offsetLabel.setText(text ? R.string.offset_text : R.string.offset_audio);
        long current = text ? player.getTextOffsetMs() : player.getAudioOffsetMs();
        float clamped = Math.max(binding.offsetSlider.getValueFrom(), Math.min(binding.offsetSlider.getValueTo(), current));
        binding.offsetSlider.setValue(clamped);
        binding.offsetValue.setText(formatOffset(clamped));
    }

    private void nudgeOffset(long deltaMs) {
        if (player == null) return;
        long cur = type == C.TRACK_TYPE_TEXT ? player.getTextOffsetMs() : player.getAudioOffsetMs();
        applyOffset(cur + deltaMs);
    }

    private void applyOffset(long offsetMs) {
        if (player == null) return;
        float from = binding.offsetSlider.getValueFrom();
        float to = binding.offsetSlider.getValueTo();
        long clamped = Math.round(Math.max(from, Math.min(to, offsetMs)));
        clamped = Math.round(clamped / (double) OFFSET_STEP_MS) * OFFSET_STEP_MS;
        if (type == C.TRACK_TYPE_TEXT) player.setTextOffsetMs(clamped);
        else if (type == C.TRACK_TYPE_AUDIO) player.setAudioOffsetMs(clamped);
        binding.offsetSlider.setValue(clamped);
        binding.offsetValue.setText(formatOffset(clamped));
    }

    private String formatOffset(float valueMs) {
        return String.format(Locale.getDefault(), "%+.1fs", valueMs / 1000f);
    }

    private void onChoose(View view) {
        FileChooser.from(launcher).show(new String[]{MimeTypes.APPLICATION_SUBRIP, MimeTypes.TEXT_SSA, MimeTypes.TEXT_VTT, MimeTypes.APPLICATION_TTML, "audio/*", "text/*", "application/octet-stream"});
        player.pause();
    }

    private void onOnline(View view) {
        String seed = vodTitle;
        if ((seed == null || seed.isEmpty()) && player != null && player.getCurrentMediaItem() != null
                && player.getCurrentMediaItem().mediaMetadata.title != null) {
            seed = player.getCurrentMediaItem().mediaMetadata.title.toString();
        }
        OnlineSubtitleDialog.create().player(player).title(seed).show(requireActivity());
        dismiss();
    }

    private void onLarge(View view) {
        if (subtitleView == null) return;
        subtitleView.addTextSize(0.002f);
        PlayerSetting.putSubtitleTextSize(subtitleView.getTextSize());
    }

    private void onSmall(View view) {
        if (subtitleView == null) return;
        subtitleView.subTextSize(0.002f);
        PlayerSetting.putSubtitleTextSize(subtitleView.getTextSize());
    }

    private void onUp(View view) {
        if (subtitleView == null) return;
        subtitleView.addPosition(0.005f);
        PlayerSetting.putSubtitlePosition(subtitleView.getPosition());
    }

    private void onDown(View view) {
        if (subtitleView == null) return;
        subtitleView.subPosition(0.005f);
        PlayerSetting.putSubtitlePosition(subtitleView.getPosition());
    }

    private void onStyleReset(View view) {
        if (subtitleView == null) return;
        PlayerSetting.putSubtitleTextSize(0.0f);
        PlayerSetting.putSubtitlePosition(0.0f);
        subtitleView.reset();
    }

    private List<Track> getTrack() {
        List<Track> items = new ArrayList<>();
        addTrack(items);
        return items;
    }

    private void addTrack(List<Track> items) {
        if (player == null) return;
        List<Tracks.Group> groups = player.getCurrentTracks().getGroups();
        for (int i = 0; i < groups.size(); i++) {
            Tracks.Group trackGroup = groups.get(i);
            if (trackGroup.getType() != type) continue;
            for (int j = 0; j < trackGroup.length; j++) {
                Format format = trackGroup.getTrackFormat(j);
                String name = provider.getTrackName(format);
                Track item = new Track(type, name, PlayerHelper.describeFormat(format));
                item.setSelected(trackGroup.isTrackSelected(j));
                items.add(item);
            }
        }
    }

    @Override
    public void onItemClick(Track item) {
        player.setTrack(Arrays.asList(item.key(player.getKey()).save()));
        dismiss();
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        player.setSub(Sub.from(FileChooser.getPathFromUri(result.getData().getData())));
        dismiss();
    });

    public interface Listener {
        void onSubtitleClick();
    }
}
