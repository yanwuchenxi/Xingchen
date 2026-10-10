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
import com.xingchen.android.tv.ui.adapter.TrackAdapter;
import com.xingchen.android.tv.ui.custom.SpaceItemDecoration;
import com.xingchen.android.tv.utils.FileChooser;
import com.xingchen.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 轨道面板（参照 Silent）：列表 + 工具栏图标（本地/在线/样式/偏移）
 */
public final class TrackDialog extends BaseBottomSheetDialog implements TrackAdapter.OnClickListener {

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

    private boolean hasSearch() {
        return hasChoose();
    }

    private boolean hasStyle() {
        return type == C.TRACK_TYPE_TEXT && subtitleView != null;
    }

    private boolean hasOffset() {
        return player != null && (type == C.TRACK_TYPE_TEXT || type == C.TRACK_TYPE_AUDIO)
                && player.haveTrack(type);
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
        binding.recycler.addItemDecoration(new SpaceItemDecoration(1, 12));
        binding.title.setText(ResUtil.getStringArray(R.array.select_track)[type - 1]);
        binding.recycler.post(() -> binding.recycler.scrollToPosition(adapter.getSelected()));
        boolean empty = adapter.getItemCount() == 0;
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyHint.setVisibility(empty && hasChoose() ? View.VISIBLE : View.GONE);
        binding.choose.setVisibility(hasChoose() ? View.VISIBLE : View.GONE);
        binding.search.setVisibility(hasSearch() ? View.VISIBLE : View.GONE);
        binding.subtitle.setVisibility(hasStyle() ? View.VISIBLE : View.GONE);
        binding.offset.setVisibility(hasOffset() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initEvent() {
        binding.choose.setOnClickListener(this::onChoose);
        binding.search.setOnClickListener(this::onSearch);
        binding.subtitle.setOnClickListener(this::onSubtitleStyle);
        binding.offset.setOnClickListener(this::onOffset);
    }

    private void onChoose(View view) {
        FileChooser.from(launcher).show(new String[]{MimeTypes.APPLICATION_SUBRIP, MimeTypes.TEXT_SSA, MimeTypes.TEXT_VTT, MimeTypes.APPLICATION_TTML, "audio/*", "text/*", "application/octet-stream"});
        player.pause();
    }

    private void onSearch(View view) {
        openOnlineSearch();
        dismiss();
    }

    private void onSubtitleStyle(View view) {
        FragmentActivity activity = requireActivity();
        SubtitleDialog.create()
                .view(subtitleView)
                .player(player)
                .search(this::openOnlineSearch)
                .show(activity);
        dismiss();
    }

    private void onOffset(View view) {
        OffsetDialog.create().player(player).type(type).show(requireActivity());
        dismiss();
    }

    private void openOnlineSearch() {
        String seed = vodTitle;
        if ((seed == null || seed.isEmpty()) && player != null && player.getCurrentMediaItem() != null
                && player.getCurrentMediaItem().mediaMetadata.title != null) {
            seed = player.getCurrentMediaItem().mediaMetadata.title.toString();
        }
        OnlineSubtitleDialog.create().player(player).title(seed).show(requireActivity());
    }

    private List<Track> getTrack() {
        List<Track> items = new ArrayList<>();
        if (player == null) return items;
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
        return items;
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
