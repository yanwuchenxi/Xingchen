package com.xingchen.android.tv.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.xingchen.android.tv.App;
import com.xingchen.android.tv.Constant;
import com.xingchen.android.tv.R;
import com.xingchen.android.tv.api.config.LiveConfig;
import com.xingchen.android.tv.bean.CastVideo;
import com.xingchen.android.tv.bean.Channel;
import com.xingchen.android.tv.bean.Config;
import com.xingchen.android.tv.bean.Epg;
import com.xingchen.android.tv.bean.EpgData;
import com.xingchen.android.tv.bean.Group;
import com.xingchen.android.tv.bean.Keep;
import com.xingchen.android.tv.bean.Live;
import com.xingchen.android.tv.bean.Result;
import com.xingchen.android.tv.bean.Track;
import com.xingchen.android.tv.databinding.ActivityLiveBinding;
import com.xingchen.android.tv.event.RefreshEvent;
import com.xingchen.android.tv.impl.Callback;
import com.xingchen.android.tv.impl.ConfigListener;
import com.xingchen.android.tv.impl.CustomTarget;
import com.xingchen.android.tv.impl.LiveListener;
import com.xingchen.android.tv.impl.PassListener;
import com.xingchen.android.tv.model.LiveViewModel;
import com.xingchen.android.tv.player.PlayerHelper;
import com.xingchen.android.tv.player.PlayerManager;
import com.xingchen.android.tv.player.Source;
import com.xingchen.android.tv.service.PlaybackService;
import com.xingchen.android.tv.setting.LiveSetting;
import com.xingchen.android.tv.setting.PlayerSetting;
import com.xingchen.android.tv.ui.adapter.ChannelAdapter;
import com.xingchen.android.tv.ui.adapter.EpgDataAdapter;
import com.xingchen.android.tv.ui.adapter.GroupAdapter;
import com.xingchen.android.tv.ui.custom.CustomKeyDown;
import com.xingchen.android.tv.ui.custom.CustomSeekView;
import com.xingchen.android.tv.ui.dialog.CastDialog;
import com.xingchen.android.tv.ui.dialog.HistoryDialog;
import com.xingchen.android.tv.ui.dialog.InfoDialog;
import com.xingchen.android.tv.ui.activity.HomeActivity;
import com.xingchen.android.tv.ui.dialog.LiveDialog;
import com.xingchen.android.tv.ui.dialog.LiveControlDialog;
import com.xingchen.android.tv.ui.dialog.LiveEpgDialog;
import com.xingchen.android.tv.ui.dialog.LiveLineDialog;
import com.xingchen.android.tv.ui.dialog.LiveProgramDialog;
import com.xingchen.android.tv.setting.LiveEpgSetting;
import com.xingchen.android.tv.ui.dialog.PassDialog;
import com.xingchen.android.tv.ui.dialog.SubtitleDialog;
import com.xingchen.android.tv.ui.dialog.TrackDialog;
import com.xingchen.android.tv.utils.Biometric;
import com.xingchen.android.tv.utils.ImgUtil;
import com.xingchen.android.tv.utils.Notify;
import com.xingchen.android.tv.utils.PiP;
import com.xingchen.android.tv.utils.ResUtil;
import com.xingchen.android.tv.utils.Traffic;
import com.xingchen.android.tv.utils.Util;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LiveActivity extends PlaybackActivity implements CustomKeyDown.Listener, TrackDialog.Listener, Biometric.Callback, PassListener, ConfigListener, LiveListener, GroupAdapter.OnClickListener, ChannelAdapter.OnClickListener, EpgDataAdapter.OnClickListener, CastDialog.Listener, InfoDialog.Listener, LiveControlDialog.Listener, LiveEpgDialog.Listener {

    private ActivityLiveBinding mBinding;
    private ChannelAdapter mChannelAdapter;
    private EpgDataAdapter mEpgDataAdapter;
    private Observer<Result> mObserveUrl;
    private GroupAdapter mGroupAdapter;
    private Observer<Epg> mObserveEpg;
    private LiveViewModel mViewModel;
    private CustomKeyDown mKeyDown;
    private float lastTapX;
    private boolean mHomeNavGuard;
    private List<Group> mHides;
    private String mPlaybackKey;
    private Channel mChannel;
    private Channel lastLineClickChannel;
    private long lastLineClickTime;
    private boolean pendingShowProgram;
    private Group mGroup;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    private boolean rotate;
    private int count;
    private PiP mPiP;

    public static void start(Context context) {
        context.startActivity(new Intent(context, LiveActivity.class).putExtra("empty", LiveConfig.isEmpty()));
    }

    private boolean isEmpty() {
        return getIntent().getBooleanExtra("empty", true);
    }

    private Group getKeep() {
        return mGroupAdapter.get(0);
    }

    private Live getHome() {
        return LiveConfig.get().getHome();
    }

    @Override
    protected boolean customWall() {
        // 直播页跟随全局壁纸，列表区域保持透明
        return true;
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityLiveBinding.inflate(getLayoutInflater());
    }

    @Override
    protected PlaybackService.NavigationCallback getNavigationCallback() {
        return mNavigationCallback;
    }

    @Override
    protected String getPlaybackKey() {
        return mPlaybackKey;
    }

    @Override
    protected PlayerView getExoView() {
        return mBinding.exo;
    }

    @Override
    protected CustomSeekView getSeekView() {
        return mBinding.control.seek;
    }

    @Override
    protected void onServiceConnected() {
        try {
            if (mBinding.exo != null) player().setDanmakuController(mBinding.exo.getDanmakuController());
        } catch (Throwable ignored) {}
        if (mBinding.control != null && mBinding.control.action != null) {
            mBinding.control.action.decode.setText(player().getDecodeText());
            mBinding.control.action.speed.setText(player().getSpeedText());
        }
        checkLive();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 竖屏显示状态栏，横屏/旋转全屏再沉浸
        applySystemUiForOrientation();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mKeyDown = CustomKeyDown.create(this, mBinding.exo);
        setPadding(mBinding.control.getRoot());
        // 竖屏下列表不要套用 cutout 左 padding（会把整列挤向右侧）
        noPadding(mBinding.recycler);
        mObserveEpg = this::setEpg;
        mObserveUrl = this::start;
        mHides = new ArrayList<>();
        mR1 = this::hideControl;
        mR2 = this::setTraffic;
        mR3 = this::hideInfo;
        mPiP = new PiP();
        setRecyclerView();
        setVideoView();
        setViewModel();
        applyLiveListStyle();
        // 避免在 init 同步改方向/改父布局触发崩溃，等布局完成后再应用
        mBinding.getRoot().post(() -> {
            try {
                if (!isFinishing() && !isDestroyed()) applyOrientationLayout();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
        mBinding.control.back.setOnClickListener(view -> onBack());
        mBinding.control.cast.setOnClickListener(view -> onCast());
        mBinding.control.info.setOnClickListener(view -> onInfo());
        mBinding.control.play.setOnClickListener(view -> checkPlay());
        mBinding.control.next.setOnClickListener(view -> nextChannel());
        mBinding.control.prev.setOnClickListener(view -> prevChannel());
        mBinding.control.right.lock.setOnClickListener(view -> onLock());
        mBinding.control.right.rotate.setOnClickListener(view -> onRotate());
        if (mBinding.homeNav != null) {
            mBinding.homeNav.setOnItemSelectedListener(item -> {
                if (mHomeNavGuard) return true;
                int id = item.getItemId();
                if (id == R.id.live) return true;
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (id == R.id.vod) intent.putExtra("tab", 0);
                else if (id == R.id.setting) intent.putExtra("tab", 1);
                intent.putExtra("from_live", true);
                startActivity(intent);
                finish();
                return true;
            });
        }
        if (mBinding.liveSource != null) {
            mBinding.liveSource.setOnClickListener(v -> LiveDialog.show(this));
        }
        if (mBinding.liveSetting != null) {
            mBinding.liveSetting.setOnClickListener(v -> onLiveSetting());
        }
        mBinding.control.action.text.setOnClickListener(this::onTrack);
        mBinding.control.action.audio.setOnClickListener(this::onTrack);
        mBinding.control.action.video.setOnClickListener(this::onTrack);
        mBinding.control.action.home.setOnClickListener(view -> onHome());
        mBinding.control.action.line.setOnClickListener(view -> onLine());
        if (mBinding.liveCurrent != null) mBinding.liveCurrent.setOnClickListener(view -> onLiveProgram());
        if (mBinding.liveProgram != null) mBinding.liveProgram.setOnClickListener(view -> onLiveProgram());
        if (mBinding.liveProgramNext != null) mBinding.liveProgramNext.setOnClickListener(view -> onLiveProgram());
        mBinding.control.action.scale.setOnClickListener(view -> onScale());
        mBinding.control.action.speed.setOnClickListener(view -> onSpeed());
        mBinding.control.action.config.setOnClickListener(view -> onConfig());
        mBinding.control.action.invert.setOnClickListener(view -> onInvert());
        mBinding.control.action.across.setOnClickListener(view -> onAcross());
        mBinding.control.action.change.setOnClickListener(view -> onChange());
        mBinding.control.action.player.setOnClickListener(view -> onChoose());
        mBinding.control.action.decode.setOnClickListener(view -> onDecode());
        mBinding.control.action.text.setOnLongClickListener(view -> onTextLong());
        mBinding.control.action.speed.setOnLongClickListener(view -> onSpeedLong());
        mBinding.control.action.getRoot().setOnTouchListener(this::onActionTouch);
        mBinding.video.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) lastTapX = event.getX();
            return mKeyDown.onTouchEvent(event);
        });
    }

    private void setRecyclerView() {
        mBinding.group.setItemAnimator(null);
        mBinding.channel.setItemAnimator(null);
        mBinding.epgData.setItemAnimator(null);
        mBinding.group.setAdapter(mGroupAdapter = new GroupAdapter(this));
        mBinding.channel.setAdapter(mChannelAdapter = new ChannelAdapter(this));
        mBinding.epgData.setAdapter(mEpgDataAdapter = new EpgDataAdapter(this));
    }

    private void setVideoView() {
        setScale(LiveSetting.getScale());
        mBinding.control.action.invert.setSelected(LiveSetting.isInvert());
        mBinding.control.action.across.setSelected(LiveSetting.isAcross());
        mBinding.control.action.change.setSelected(LiveSetting.isChange());
        mBinding.video.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> mPiP.update(this, view));
    }

    private void setDecode() {
        mBinding.control.action.decode.setText(player().getDecodeText());
    }

    private void setScale(int scale) {
        LiveSetting.putScale(scale);
        mBinding.exo.setResizeMode(scale);
        mBinding.control.action.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        mViewModel.url().observeForever(mObserveUrl);
        mViewModel.xml().observe(this, this::setEpg);
        mViewModel.epg().observeForever(mObserveEpg);
        mViewModel.live().observe(this, live -> {
            mViewModel.parseXml(live);
            setGroup(live);
            setWidth(live);
        });
    }

    private void checkLive() {
        if (isEmpty()) {
            LiveConfig.get().init().load(getCallback());
        } else {
            getLive();
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success() {
                getLive();
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
            }
        };
    }

    private void getLive() {
        Live home = getHome();
        if (home == null) home = new Live();
        String name = home.getName();
        mBinding.control.action.home.setText(LiveConfig.isOnly() || name == null || name.isEmpty()
                ? getString(R.string.live_refresh) : name);
        mViewModel.parse(home);
        showProgress();
    }

    private void setGroup(Live live) {
        List<Group> items = new ArrayList<>();
        for (Group group : live.getGroups()) (group.isHidden() ? mHides : items).add(group);
        mGroupAdapter.addAll(items);
        setPosition(LiveConfig.get().findKeepPosition(items));
    }

    private void setWidth(Live live) {
        int padding = ResUtil.dp2px(48);
        if (live.getWidth() == 0) for (Group item : live.getGroups()) live.setWidth(Math.max(live.getWidth(), ResUtil.getTextWidth(item.getName(), 14)));
        int width = live.getWidth() == 0 ? 0 : Math.min(live.getWidth() + padding, ResUtil.getScreenWidth() / 4);
        setWidth(mBinding.group, width);
    }

    @Override
    public void setWidth(Group group) {
        int logo = ResUtil.dp2px(56);
        int padding = ResUtil.dp2px(60);
        if (group.isKeep()) group.setWidth(0);
        if (group.getWidth() == 0) for (Channel item : group.getChannel()) group.setWidth(Math.max(group.getWidth(), (item.getLogo().isEmpty() ? 0 : logo) + ResUtil.getTextWidth(item.getNumber() + item.getName(), 14)));
        int width = group.getWidth() == 0 ? 0 : Math.min(group.getWidth() + padding, ResUtil.getScreenWidth() / 2);
        setWidth(mBinding.channel, width);
    }

    private void setWidth(Epg epg) {
        int padding = ResUtil.dp2px(48);
        if (epg.getList().isEmpty()) return;
        int minWidth = ResUtil.getTextWidth(epg.getList().get(0).getTime(), 12);
        if (epg.getWidth() == 0) for (EpgData item : epg.getList()) epg.setWidth(Math.max(epg.getWidth(), ResUtil.getTextWidth(item.getTitle(), 14)));
        int width = epg.getWidth() == 0 ? 0 : Math.min(Math.max(epg.getWidth(), minWidth) + padding, ResUtil.getScreenWidth() / 2);
        setWidth(mBinding.epgData, width);
    }

    private void setWidth(View view, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params.width == width) return;
        params.width = width;
        view.setLayoutParams(params);
    }

    private void setPosition(int[] position) {
        if (position[0] == -1) return;
        int size = mGroupAdapter.getItemCount();
        if (size == 1 || position[0] >= size) return;
        mGroup = mGroupAdapter.get(position[0]);
        mGroup.setPosition(position[1]);
        onItemClick(mGroup);
        onItemClick(mGroup.current());
    }

    private void setPosition() {
        if (mChannel == null) return;
        mGroup = mChannel.getGroup();
        int position = mGroupAdapter.indexOf(mGroup);
        boolean change = mGroupAdapter.getPosition() != position;
        if (change) mGroupAdapter.setSelected(position);
        if (change) mChannelAdapter.addAll(mGroup.getChannel());
        if (change) mChannelAdapter.setSelected(mGroup.getPosition());
        scrollToPosition(mBinding.channel, mGroup.getPosition());
        scrollToPosition(mBinding.group, position);
    }

    private void onBack() {
        finish();
    }

    private void onCast() {
        CastDialog.create().video(new CastVideo(mBinding.control.title.getText().toString(), player().getUrl(), androidx.media3.common.C.TIME_UNSET, player().getHeaders())).fm(false).show(this);
    }

    private void onInfo() {
        InfoDialog.create().title(mBinding.control.title.getText()).headers(player().getHeaders()).url(player().getUrl()).show(this);
    }

    private void onLock() {
        setLock(!isLock());
        setRequestedOrientation(getLockOrient());
        mKeyDown.setLock(isLock());
        checkLockImg();
        showControl();
    }

    private void onRotate() {
        setR1Callback();
        setRotate(!isRotate());
        setRequestedOrientation(isRotate()
            ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 等方向真正切换后再调布局；先按目标状态预应用
        mBinding.getRoot().post(this::applyOrientationLayout);
    }

    private void checkPlay() {
        if (player().isPlaying()) onPaused();
        else onPlay();
    }

    private void onTrack(View view) {
        TrackDialog.create().type(Integer.parseInt(view.getTag().toString())).player(player()).show(this);
        hideControl();
    }

    private void onHome() {
        if (LiveConfig.isOnly()) setLive(getHome());
        else LiveDialog.show(this);
        hideControl();
    }

    private void onLine() {
        if (mChannel != null && !mChannel.isOnly()) showLineDialog(mChannel);
        else nextLine(false);
    }

    private void onScale() {
        int index = LiveSetting.getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        if (mKeyDown.getScale() != 1.0f) mKeyDown.resetScale();
        else setScale(index == array.length - 1 ? 0 : ++index);
        setR1Callback();
    }

    private void onSpeed() {
        mBinding.control.action.speed.setText(player().addSpeed());
        setR1Callback();
    }

    private boolean onSpeedLong() {
        mBinding.control.action.speed.setText(player().toggleSpeed());
        setR1Callback();
        return true;
    }

    private void onConfig() {
        HistoryDialog.create().live().readOnly().show(this);
        hideControl();
    }

    private void onInvert() {
        setR1Callback();
        LiveSetting.putInvert(!LiveSetting.isInvert());
        mBinding.control.action.invert.setSelected(LiveSetting.isInvert());
    }

    private void onAcross() {
        setR1Callback();
        LiveSetting.putAcross(!LiveSetting.isAcross());
        mBinding.control.action.across.setSelected(LiveSetting.isAcross());
    }

    private void onChange() {
        setR1Callback();
        LiveSetting.putChange(!LiveSetting.isChange());
        mBinding.control.action.change.setSelected(LiveSetting.isChange());
    }

    private void onDecode() {
        player().toggleDecode();
        setR1Callback();
        setDecode();
    }

    private void onChoose() {
        PlayerHelper.choose(this, player().getUrl(), player().getHeaders(), player().isVod(), player().getPosition(), mBinding.control.title.getText());
        setRedirect(true);
    }

    private boolean onTextLong() {
        if (!player().haveTrack(C.TRACK_TYPE_TEXT)) return false;
        onSubtitleClick();
        return true;
    }

    private boolean onActionTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) setR1Callback();
        return false;
    }

    private int getLockOrient() {
        if (isLock()) {
            return ResUtil.getScreenOrientation(this);
        } else if (isRotate()) {
            // 旋转开关：竖屏下进入横屏全屏
            return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        } else {
            // 默认竖屏（上播放器 + 下频道列表）
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    private void hideUI() {
        // 竖屏嵌入模式（上播放器+下列表）时不隐藏频道列表
        if (isEmbeddedLiveUi()) {
            keepLiveMenuVisible();
            setPosition();
            return;
        }
        if (isGone(mBinding.recycler)) return;
        mBinding.recycler.setVisibility(View.GONE);
        setPosition();
    }

    private void showUI() {
        if (isEmbeddedLiveUi()) {
            keepLiveMenuVisible();
            setPosition();
            return;
        }
        if (mGroupAdapter.getItemCount() == 0) return;
        ensureRecyclerOverlayOnVideo();
        mBinding.recycler.setVisibility(View.VISIBLE);
        keepLiveMenuVisible();
        mBinding.channel.requestFocus();
        setPosition();
        hideEpg();
    }


    /** 竖屏：上播放器+下列表；横屏：播放器全屏，列表侧栏叠在画面上 */
    private void applyOrientationLayout() {
        if (mBinding == null || mBinding.video == null || mBinding.recycler == null) return;
        try {
            applySystemUiForOrientation();
            boolean embedded = isEmbeddedLiveUi();
            if (embedded) {
                ensureRecyclerInRoot();
                noPadding(mBinding.recycler);
                // 按 16:9 固定播放器高度，消除画面与列表之间的黑边空白
                int videoH = Math.round(ResUtil.getScreenWidth(this) * 9f / 16f);
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams vp =
                        new androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, videoH);
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams rp =
                        new androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
                mBinding.video.setLayoutParams(vp);
                mBinding.recycler.setLayoutParams(rp);
                mBinding.recycler.setVisibility(View.VISIBLE);
                keepLiveMenuVisible();
                hideEpg();
                if (mBinding.homeNav != null) {
                    mBinding.homeNav.setVisibility(View.VISIBLE);
                    // 避免 setSelected 触发误跳转
                    mHomeNavGuard = true;
                    mBinding.homeNav.setSelectedItemId(R.id.live);
                    mHomeNavGuard = false;
                }
            } else {
                if (mBinding.homeNav != null) mBinding.homeNav.setVisibility(View.GONE);
                ensureRecyclerOverlayOnVideo();
                androidx.appcompat.widget.LinearLayoutCompat.LayoutParams vp =
                        new androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                mBinding.video.setLayoutParams(vp);
                if (!isVisible(mBinding.control.getRoot())) {
                    mBinding.recycler.setVisibility(View.GONE);
                }
            }
            if (isVisible(mBinding.control.getRoot())) showControl();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void ensureRecyclerInRoot() {
        ViewGroup root = (ViewGroup) mBinding.getRoot();
        View recycler = mBinding.recycler;
        if (recycler.getParent() == root) return;
        ViewGroup parent = (ViewGroup) recycler.getParent();
        if (parent != null) parent.removeView(recycler);
        int index = root.indexOfChild(mBinding.video);
        root.addView(recycler, index < 0 ? -1 : index + 1,
                new androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 14f));
    }

    private void ensureRecyclerOverlayOnVideo() {
        ViewGroup video = mBinding.video;
        View recycler = mBinding.recycler;
        if (recycler.getParent() == video) return;
        ViewGroup parent = (ViewGroup) recycler.getParent();
        if (parent != null) parent.removeView(recycler);
        int sw = ResUtil.getScreenWidth(this);
        int w = Math.max(ResUtil.dp2px(280), Math.round(sw * 0.42f));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START);
        video.addView(recycler, lp);
        recycler.setBackgroundResource(R.color.transparent);
    }

    /** YingKe：竖屏且非旋转全屏、非 PiP 时为嵌入式列表 UI */
    private boolean isEmbeddedLiveUi() {
        return !ResUtil.isLand(this) && !isRotate() && !isInPictureInPictureMode();
    }

    private void keepLiveMenuVisible() {
        mBinding.recycler.setVisibility(View.VISIBLE);
        mBinding.group.setVisibility(View.VISIBLE);
        mBinding.channel.setVisibility(View.VISIBLE);
    }

    private void showEpg(Channel item) {
        if (mChannel == null || mChannel.getData(mViewModel.getZoneId()).getList().isEmpty() || mEpgDataAdapter.getItemCount() == 0 || !mChannel.equals(item) || !mChannel.getGroup().equals(mGroup)) return;
        scrollToPosition(mBinding.epgData, item.getData(mViewModel.getZoneId()).getSelected());
        mBinding.epgData.setVisibility(View.VISIBLE);
        mBinding.channel.setVisibility(View.GONE);
        mBinding.group.setVisibility(View.GONE);
    }

    private void hideEpg() {
        mBinding.channel.setVisibility(View.VISIBLE);
        mBinding.group.setVisibility(View.VISIBLE);
        mBinding.epgData.setVisibility(View.GONE);
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(mR2, 0);
        hideError();
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR2);
        Traffic.reset();
    }

    private void showError(String text) {
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.error.setText(text);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.error.setText("");
    }

    private void showControl() {
        if (service() == null || isInPictureInPictureMode()) return;
        boolean embedded = isEmbeddedLiveUi();
        // 竖屏嵌入：不盖住下列表，隐藏底部 action 条；仅在播放器区域显示顶栏/中控
        mBinding.control.info.setVisibility(player().isEmpty() ? View.GONE : View.VISIBLE);
        mBinding.control.cast.setVisibility(player().isEmpty() || embedded ? View.GONE : View.VISIBLE);
        mBinding.control.right.rotate.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        mBinding.control.center.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        mBinding.control.bottom.setVisibility(isLock() || embedded ? View.GONE : View.VISIBLE);
        if (mBinding.control.action != null) {
            mBinding.control.action.getRoot().setVisibility(embedded ? View.GONE : View.VISIBLE);
        }
        mBinding.control.back.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        mBinding.control.top.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        // 嵌入模式下播控背景更轻，避免整屏压黑
        mBinding.control.getRoot().setBackgroundResource(embedded ? R.color.transparent : R.color.black_20);
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        setR1Callback();
        hideInfo();
    }

    private void hideControl() {
        mBinding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR1);
    }

    private void showInfo() {
        mBinding.widget.infoPip.setVisibility(isInPictureInPictureMode() ? View.VISIBLE : View.GONE);
        mBinding.widget.info.setVisibility(isInPictureInPictureMode() ? View.GONE : View.VISIBLE);
        setR3Callback();
        hideControl();
        setInfo();
    }

    private void hideInfo() {
        mBinding.widget.infoPip.setVisibility(View.GONE);
        mBinding.widget.info.setVisibility(View.GONE);
        App.removeCallbacks(mR3);
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.progress.traffic);
        App.post(mR2, 1000);
    }

    private void setR1Callback() {
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    private void setR3Callback() {
        App.post(mR3, Constant.INTERVAL_HIDE);
    }

    private void onToggle() {
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else if (isVisible(mBinding.recycler)) hideUI();
        else showUI();
        hideInfo();
    }

    private void resetPass() {
        this.count = 0;
    }

    private void setArtwork() {
        if (mChannel == null) return;
        ImgUtil.load(this, mChannel.getLogo(), new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mBinding.exo.setDefaultArtwork(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mBinding.exo.setDefaultArtwork(errorDrawable);
            }
        });
    }

    @Override
    public void onItemClick(Group item) {
        mGroupAdapter.setSelected(mGroup = item);
        mChannelAdapter.addAll(item.getChannel());
        mChannelAdapter.setSelected(item.getPosition());
        scrollToPosition(mBinding.channel, Math.max(item.getPosition(), 0));
        if (!item.isKeep() || ++count < 5 || mHides.isEmpty()) return;
        if (Biometric.enable()) Biometric.show(this);
        else PassDialog.create().show(this);
        resetPass();
    }

    @Override
    public void onItemClick(Channel item) {
        if (item.isSelected() && mChannel != null && mChannel.equals(item) && mChannel.getGroup().equals(mGroup) && isLineDoubleClick(item)) {
            showLineDialog(item);
            return;
        }
        if (!item.getData(mViewModel.getZoneId()).getList().isEmpty() && item.isSelected() && mChannel != null && mChannel.equals(item) && mChannel.getGroup().equals(mGroup)) {
            showEpg(item);
            rememberLineClick(item);
        } else if (mGroup != null) {
            mGroup.setPosition(mChannelAdapter.setSelected(item.group(mGroup)));
            mChannel = item;
            setArtwork();
            showInfo();
            hideUI();
            fetch();
            rememberLineClick(item);
        }
    }

    @Override
    public boolean onLongClick(Channel item) {
        if (mGroup.isHidden()) return false;
        boolean exist = Keep.exist(item.getName());
        Notify.show(exist ? R.string.keep_del : R.string.keep_add);
        if (exist) delKeep(item);
        else addKeep(item);
        return true;
    }

    @Override
    public void onItemClick(EpgData item) {
        if (item.isSelected()) {
            fetch(item);
        } else if (mChannel.hasCatchup() || mChannel.isRtsp()) {
            mBinding.control.title.setText(getString(R.string.detail_title, mChannel.getShow(), item.getTitle()));
            Notify.show(getString(R.string.play_ready, item.getTitle()));
            mEpgDataAdapter.setSelected(item);
            fetch(item);
        }
    }

    private void addKeep(Channel item) {
        getKeep().add(item);
        Keep keep = new Keep();
        keep.setKey(item.getName());
        keep.setType(1);
        keep.save();
    }

    private void delKeep(Channel item) {
        if (mGroup.isKeep()) mChannelAdapter.remove(item);
        getKeep().getChannel().remove(item);
        Keep.delete(item.getName());
    }

    private void setInfo() {
        if (mChannel == null) return;
        mViewModel.getEpg(mChannel);
        mBinding.widget.play.setText("");
        mBinding.widget.name.setMaxEms(48);
        mChannel.loadLogo(mBinding.widget.logo);
        mBinding.control.title.setSelected(true);
        mBinding.widget.line.setText(mChannel.getLine());
        mBinding.widget.name.setText(mChannel.getShow());
        mBinding.control.title.setText(mChannel.getShow());
        if (mBinding.liveTitle != null) {
            mBinding.liveTitle.setText(mChannel.getShow());
        }
        setSizeText();
        mBinding.widget.namePip.setText(mChannel.getShow());
        mBinding.widget.number.setText(mChannel.getNumber());
        mBinding.widget.numberPip.setText(mChannel.getNumber());
        mBinding.widget.line.setVisibility(mChannel.getLineVisible());
        mBinding.control.action.line.setText(mBinding.widget.line.getText());
        mBinding.control.action.line.setVisibility(mBinding.widget.line.getVisibility());
    }

    private void setEpg(Epg epg) {
        if (mChannel == null || !mChannel.getTvgId().equals(epg.getKey())) {
            pendingShowProgram = false;
            return;
        }
        EpgData data = epg.getEpgData();
        boolean hasTitle = !data.getTitle().isEmpty();
        mEpgDataAdapter.addAll(epg.getList());
        if (pendingShowProgram) {
            pendingShowProgram = false;
            showLiveProgram();
        }
        if (hasTitle) mBinding.control.title.setText(getString(R.string.detail_title, mChannel.getShow(), data.getTitle()));
        mBinding.widget.name.setMaxEms(hasTitle ? 12 : 48);
        mBinding.widget.play.setText(data.format());
        if (mBinding.liveProgram != null) {
            mBinding.liveProgram.setText(data.format());
        }
        if (mBinding.liveProgramNext != null) {
            String nextText = getString(R.string.live_no_epg);
            if (epg.getList() != null && epg.getList().size() > 1) {
                int idx = epg.getList().indexOf(data);
                int nextIdx = (idx >= 0 ? idx + 1 : 1);
                if (nextIdx >= 0 && nextIdx < epg.getList().size()) {
                    nextText = epg.getList().get(nextIdx).format();
                }
            }
            mBinding.liveProgramNext.setText(nextText);
        }
        setWidth(epg);
        setMetadata();
    }

    private void setEpg(boolean success) {
        if (mChannel != null && success)
            mViewModel.getEpg(mChannel);
    }

    private void fetch(EpgData item) {
        if (mChannel == null) return;
        mViewModel.getUrl(mChannel, item);
        player().clear();
        player().stop();
        hideUI();
    }

    private void fetch() {
        if (mChannel == null) return;
        LiveConfig.get().setKeep(mChannel);
        mViewModel.getUrl(mChannel);
        player().clear();
        player().stop();
        showProgress();
    }

    private void start(Result result) {
        if (result == null) return;
        mPlaybackKey = result.getRealUrl();
        long timeout = getHome() != null ? getHome().getTimeout() : 0;
        startPlayer(mPlaybackKey, result, false, timeout, buildMetadata());
    }

    private void checkControl() {
        if (isVisible(mBinding.control.getRoot())) showControl();
    }

    private void checkLockImg() {
        mBinding.control.right.lock.setImageResource(isLock() ? R.drawable.ic_control_lock_on : R.drawable.ic_control_lock_off);
    }

    private void setSizeText() {
        String text = player().getSizeText();
        mBinding.control.size.setText(text);
        mBinding.control.size.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void resetAdapter() {
        mBinding.control.action.line.setVisibility(View.GONE);
        mBinding.control.title.setText("");
        mBinding.control.size.setText("");
        mBinding.control.size.setVisibility(View.GONE);
        mEpgDataAdapter.clear();
        mChannelAdapter.clear();
        mGroupAdapter.clear();
        mHides.clear();
        mChannel = null;
        mGroup = null;
    }

    private final PlaybackService.NavigationCallback mNavigationCallback = new PlaybackService.NavigationCallback() {
        @Override
        public void onNext() {
            nextChannel();
        }

        @Override
        public void onPrev() {
            prevChannel();
        }

        @Override
        public void onStop() {
            finish();
        }

        @Override
        public void onAudio() {
            moveTaskToBack(true);
            setAudioOnly(true);
        }
    };

    @Override
    protected void onPrepare() {
        setDecode();
    }

    @Override
    protected void onTracksChanged() {
        setTrackVisible();
    }

    @Override
    protected void onError(String msg) {
        Track.delete(player().getKey());
        player().resetTrack();
        player().reset();
        player().stop();
        showError(msg);
        startFlow();
    }

    @Override
    protected void onReclaim() {
        // onResume 回收播放权时 mChannel 可能仍为 null，避免 getShow NPE
        if (mChannel == null) return;
        Result result = mViewModel != null ? mViewModel.url().getValue() : null;
        if (result != null) start(result);
    }

    @Override
    protected void onStateChanged(int state) {
        switch (state) {
            case Player.STATE_BUFFERING:
                showProgress();
                break;
            case Player.STATE_READY:
                hideProgress();
                checkControl();
                player().reset();
                break;
            case Player.STATE_ENDED:
                checkEnded();
                break;
        }
    }

    @Override
    protected void onSizeChanged(VideoSize size) {
        setSizeText();
    }

    @Override
    protected void onPlayingChanged(boolean isPlaying) {
        if (isPlaying) {
            mPiP.update(this, true);
            mBinding.control.play.setImageResource(androidx.media3.ui.R.drawable.exo_icon_pause);
        } else if (isPaused()) {
            mPiP.update(this, false);
            mBinding.control.play.setImageResource(androidx.media3.ui.R.drawable.exo_icon_play);
        }
    }

    @Override
    public void onSubtitleClick() {
        SubtitleDialog.create().view(mBinding.exo.getSubtitleView()).show(this);
        hideControl();
    }

    @Override
    public void setConfig(Config config) {
        Config current = LiveConfig.get().getConfig();
        LiveConfig.load(config, getCallback(current));
    }

    private Callback getCallback(Config config) {
        return new Callback() {
            @Override
            public void start() {
                showProgress();
            }

            @Override
            public void success() {
                setLive(getHome());
            }

            @Override
            public void error(String msg) {
                LiveConfig.load(config, new Callback());
                Notify.show(msg);
                hideProgress();
            }
        };
    }

    @Override
    public void setLive(Live item) {
        if (item.isSelected()) item.getGroups().clear();
        LiveConfig.get().setHome(item);
        player().reset();
        player().clear();
        player().stop();
        resetAdapter();
        hideControl();
        getLive();
    }

    @Override
    public void setPass(String pass) {
        unlock(pass);
    }

    @Override
    public void onBiometricSuccess() {
        unlock(null);
    }

    private void unlock(String pass) {
        boolean first = true;
        Iterator<Group> iterator = mHides.iterator();
        while (iterator.hasNext()) {
            Group item = iterator.next();
            if (pass != null && !pass.equals(item.getPass())) continue;
            mGroupAdapter.add(item);
            if (first) onItemClick(item);
            iterator.remove();
            first = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        switch (event.getType()) {
            case LIVE -> setLive(getHome());
            case PLAYER -> fetch();
        }
    }

    private void checkEnded() {
        if (player().isLive()) {
            checkNext();
        } else {
            nextChannel();
        }
    }

    private void setTrackVisible() {
        mBinding.control.action.text.setVisibility(player().haveTrack(C.TRACK_TYPE_TEXT) || player().isVod() ? View.VISIBLE : View.GONE);
        mBinding.control.action.audio.setVisibility(player().haveTrack(C.TRACK_TYPE_AUDIO) ? View.VISIBLE : View.GONE);
        mBinding.control.action.video.setVisibility(player().haveTrack(C.TRACK_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
        mBinding.control.action.speed.setVisibility(player().isVod() ? View.VISIBLE : View.GONE);
    }

    private MediaMetadata buildMetadata() {
        if (mChannel == null) {
            return PlayerManager.buildMetadata("", null, null);
        }
        String artist = "";
        try {
            if (mBinding != null && mBinding.widget != null && mBinding.widget.play != null
                    && mBinding.widget.play.getText() != null) {
                artist = mBinding.widget.play.getText().toString();
            }
        } catch (Throwable ignored) {
        }
        return PlayerManager.buildMetadata(mChannel.getShow(), artist, mChannel.getLogo());
    }

    private void setMetadata() {
        if (mChannel == null || player() == null) return;
        player().setMetadata(buildMetadata());
    }

    private void startFlow() {
        if (!LiveSetting.isChange()) return;
        if (!mChannel.isLast()) nextLine(true);
    }

    private boolean prevGroup() {
        int position = mGroupAdapter.getPosition() - 1;
        if (position < 0) position = mGroupAdapter.getItemCount() - 1;
        if (mGroup.equals(mGroupAdapter.get(position))) return false;
        mGroup = mGroupAdapter.get(position);
        mGroupAdapter.setSelected(position);
        if (mGroup.skip()) return prevGroup();
        mChannelAdapter.addAll(mGroup.getChannel());
        mGroup.setPosition(mGroup.getChannel().size() - 1);
        return true;
    }

    private boolean nextGroup() {
        int position = mGroupAdapter.getPosition() + 1;
        if (position > mGroupAdapter.getItemCount() - 1) position = 0;
        if (mGroup.equals(mGroupAdapter.get(position))) return false;
        mGroup = mGroupAdapter.get(position);
        mGroupAdapter.setSelected(position);
        if (mGroup.skip()) return nextGroup();
        mChannelAdapter.addAll(mGroup.getChannel());
        mGroup.setPosition(0);
        return true;
    }

    private void prevChannel() {
        if (mGroup == null) return;
        int position = mGroup.getPosition() - 1;
        boolean limit = position < 0;
        if (LiveSetting.isAcross() & limit) prevGroup();
        else mGroup.setPosition(limit ? mChannelAdapter.getItemCount() - 1 : position);
        if (!mGroup.isEmpty()) onItemClick(mGroup.current());
    }

    private void nextChannel() {
        if (mGroup == null) return;
        int position = mGroup.getPosition() + 1;
        boolean limit = position > mChannelAdapter.getItemCount() - 1;
        if (LiveSetting.isAcross() && limit) nextGroup();
        else mGroup.setPosition(limit ? 0 : position);
        if (!mGroup.isEmpty()) onItemClick(mGroup.current());
    }

    private void checkNext() {
        int current = mChannel.getData(mViewModel.getZoneId()).getInRange();
        int position = mChannel.getData(mViewModel.getZoneId()).getSelected() + 1;
        boolean hasNext = position <= current && position > 0;
        if (hasNext) onItemClick(mChannel.getData(mViewModel.getZoneId()).getList().get(position));
        else fetch();
    }

    private void nextLine(boolean show) {
        if (mChannel == null || mChannel.isOnly()) return;
        mChannel.switchLine(true);
        if (show) showInfo();
        else setInfo();
        fetch();
    }

    private void setLine(int position) {
        if (mChannel == null || position < 0 || position >= mChannel.getUrls().size()) return;
        if (mChannel.getIndex() == position) return;
        mChannel.setIndex(position);
        setInfo();
        fetch();
    }

    private boolean isLineDoubleClick(Channel item) {
        long now = System.currentTimeMillis();
        boolean result = lastLineClickChannel != null && lastLineClickChannel.equals(item)
                && now - lastLineClickTime <= ViewConfiguration.getDoubleTapTimeout();
        rememberLineClick(item, now);
        return result && !item.isOnly();
    }

    private void rememberLineClick(Channel item) {
        rememberLineClick(item, System.currentTimeMillis());
    }

    private void rememberLineClick(Channel item, long time) {
        lastLineClickChannel = item;
        lastLineClickTime = time;
    }

    private void showLineDialog(Channel item) {
        if (item == null || item.isOnly()) return;
        hideControl();
        LiveLineDialog.create().channel(item).listener(this::setLine).show(this);
    }

    private void onLiveProgram() {
        if (mChannel == null) return;
        if (!mChannel.getDataList().isEmpty()) {
            showLiveProgram();
            return;
        }
        pendingShowProgram = true;
        mViewModel.getEpg(mChannel);
        Notify.show(R.string.live_program_empty);
    }

    private void showLiveProgram() {
        if (mChannel == null || mChannel.getDataList().isEmpty()) {
            Notify.show(R.string.live_program_empty);
            return;
        }
        LiveProgramDialog.create().channel(mChannel).zoneId(mViewModel.getZoneId()).listener(this::onItemClick).show(this);
        hideControl();
    }

    private void onPaused() {
        controller().pause();
    }

    private void onPlay() {
        controller().play();
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
        if (rotate) {
            noPadding(mBinding.recycler);
            noPadding(mBinding.control.getRoot());
        } else {
            // 竖屏嵌入列表保持左右对称；横屏侧栏再按需处理
            if (isEmbeddedLiveUi()) noPadding(mBinding.recycler);
            else setPadding(mBinding.recycler, true);
            setPadding(mBinding.control.getRoot());
        }
    }

    private void scrollToPosition(RecyclerView view, int position) {
        view.post(() -> view.scrollToPosition(position));
    }

    @Override
    public void onCasted() {
        player().stop();
    }

    @Override
    public void onSpeedUp() {
        if (player().isLive()) return;
        if (!player().isPlaying()) return;
        mBinding.widget.speed.setVisibility(View.VISIBLE);
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward));
        mBinding.control.action.speed.setText(player().setSpeed(PlayerSetting.getSpeed()));
    }

    @Override
    public void onSpeedEnd() {
        mBinding.widget.speed.clearAnimation();
        mBinding.control.action.speed.setText(player().setSpeed(1.0f));
    }

    @Override
    public void onBright(int progress) {
        mBinding.widget.bright.setVisibility(View.VISIBLE);
        mBinding.widget.brightProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_low);
        else if (progress < 70) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_medium);
        else mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_high);
    }

    @Override
    public void onVolume(int progress) {
        mBinding.widget.volume.setVisibility(View.VISIBLE);
        mBinding.widget.volumeProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_low);
        else if (progress < 70) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_medium);
        else mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_high);
    }

    @Override
    public void onFlingUp() {
        if (LiveSetting.isInvert()) nextChannel();
        else prevChannel();
    }

    @Override
    public void onFlingDown() {
        if (LiveSetting.isInvert()) prevChannel();
        else nextChannel();
    }

    @Override
    public void onSeeking(long time) {
        if (player().isLive()) return;
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        mBinding.widget.time.setText(player().getPositionTime(time));
        mBinding.widget.seek.setVisibility(View.VISIBLE);
        hideProgress();
    }

    @Override
    public void onSeekEnd(long time) {
        if (player().isLive()) return;
        seekTo(time);
    }

    @Override
    public void onSingleTap() {
        // 竖屏嵌入：单击播放器切换播控（不必双击）
        if (isEmbeddedLiveUi()) {
            if (isVisible(mBinding.control.getRoot())) hideControl();
            else showControl();
            hideInfo();
            return;
        }
        // 横屏：左侧频道列表，右侧播控
        if (ResUtil.isLand(this) || isRotate()) {
            int half = ResUtil.getScreenWidth(this) / 2;
            if (lastTapX < half) {
                if (isVisible(mBinding.control.getRoot())) hideControl();
                if (isVisible(mBinding.recycler)) hideUI();
                else showUI();
            } else {
                if (isVisible(mBinding.recycler)) hideUI();
                if (isVisible(mBinding.control.getRoot())) hideControl();
                else showControl();
            }
            hideInfo();
            return;
        }
        onToggle();
    }

    @Override
    public void onDoubleTap() {
        if (isVisible(mBinding.recycler)) hideUI();
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else showControl();
    }

    @Override
    public void onTouchEnd() {
        mBinding.widget.seek.setVisibility(View.GONE);
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.bright.setVisibility(View.GONE);
        mBinding.widget.volume.setVisibility(View.GONE);
    }

    @Override
    public void onShare(CharSequence title) {
        PlayerHelper.share(this, player().getUrl(), player().getHeaders(), title);
        setRedirect(true);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isRedirect()) return;
        if (isLock()) App.post(this::onLock, 500);
        if (service() != null && player().haveTrack(C.TRACK_TYPE_VIDEO)) mPiP.enter(this, player().getVideoWidth(), player().getVideoHeight(), LiveSetting.getScale());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            hideControl();
            hideInfo();
            hideUI();
        } else {
            hideInfo();
            if (isStop()) finish();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applySystemUiForOrientation();
        applyOrientationLayout();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applySystemUiForOrientation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setAudioOnly(false);
        setStop(false);
        if (!isRotate()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAudioOnly()) setStop(true);
    }

    @Override
    protected void onBackInvoked() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
            return;
        }
        if (isVisible(mBinding.widget.info)) {
            hideInfo();
            return;
        }
        // 横屏全屏：先退回竖屏嵌入，再按返回才退出直播页
        if (isRotate() || ResUtil.isLand(this)) {
            setRotate(false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            applyOrientationLayout();
            return;
        }
        // 横屏叠加列表（非嵌入）可先收列表
        if (!isEmbeddedLiveUi() && isVisible(mBinding.recycler)) {
            hideUI();
            return;
        }
        if (!isLock()) {
            // 竖屏嵌入下列表常显，直接结束回到 HomeActivity
            if (isTaskRoot()) {
                startActivity(new Intent(this, HomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        Source.get().exit();
        App.removeCallbacks(mR1, mR2, mR3);
        mViewModel.url().removeObserver(mObserveUrl);
        mViewModel.epg().removeObserver(mObserveEpg);
        super.onDestroy();
    }

    private void onLiveSetting() {
        LiveControlDialog.create().parent(mBinding).show(this);
    }

    private void applyLiveListStyle() {
        boolean classic = LiveSetting.isListStyleClassic();
        if (mBinding.recycler != null) {
            mBinding.recycler.setBackgroundResource(classic ? R.color.transparent : R.color.transparent);
        }
        if (mBinding.liveCurrent != null) {
            mBinding.liveCurrent.setBackgroundResource(classic ? R.drawable.shape_live_classic : R.drawable.shape_live_current);
        }
        if (mGroupAdapter != null) mGroupAdapter.notifyDataSetChanged();
        if (mChannelAdapter != null) mChannelAdapter.notifyDataSetChanged();
        if (mEpgDataAdapter != null) mEpgDataAdapter.notifyDataSetChanged();
    }

    /** 屏显：时间/网速/分辨率/标题/参数 → widget 控件显隐 */
    private void applyLiveDisplay() {
        boolean[] d = LiveSetting.getLiveDisplayChecked();
        // 0 时间 1 网速 2 分辨率 3 标题 4 参数
        try {
            if (mBinding.widget.time != null) {
                mBinding.widget.time.setVisibility(d[0] ? View.VISIBLE : View.GONE);
            }
            if (mBinding.progress != null && mBinding.progress.traffic != null) {
                mBinding.progress.traffic.setVisibility(d[1] ? View.VISIBLE : View.INVISIBLE);
            }
            if (mBinding.control != null && mBinding.control.size != null) {
                mBinding.control.size.setVisibility(d[2] ? View.VISIBLE : View.GONE);
            }
            if (mBinding.widget.name != null) {
                mBinding.widget.name.setVisibility(d[3] ? View.VISIBLE : View.GONE);
            }
            if (mBinding.liveTitle != null) {
                mBinding.liveTitle.setVisibility(d[3] ? View.VISIBLE : View.GONE);
            }
            // 参数：无独立 OSD 时用 info 行近似
            if (mBinding.widget.play != null) {
                // 保留 EPG 文本，不因参数开关隐藏
            }
        } catch (Exception ignored) {}
    }

    private void dismissLiveControlDialog() {
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof LiveControlDialog) {
                ((LiveControlDialog) fragment).dismissAllowingStateLoss();
            }
        }
    }

    @Override
    public void onLiveConfigPanel() {
        dismissLiveControlDialog();
        onConfig();
    }

    @Override
    public void onLiveSourcePanel() {
        onHome();
    }

    @Override
    public void onLiveEpgPanel() {
        dismissLiveControlDialog();
        LiveEpgDialog.create().show(this);
        hideControl();
        hideInfo();
    }

    @Override
    public void onLiveEpgSelected(String url) {
        LiveEpgSetting.putUrl(url == null ? "" : url);
        Live home = getHome();
        if (home != null) LiveEpgSetting.apply(home);
        if (mChannel != null) {
            LiveEpgSetting.apply(home, mChannel);
            if (LiveEpgSetting.isGlobalXmlUrl(LiveEpgSetting.getUrl()) || (LiveEpgSetting.getUrl().isEmpty() && home != null && !home.getEpgXml().isEmpty())) {
                try { mViewModel.parseXml(home); } catch (Exception e) {
                    mViewModel.getEpg(mChannel);
                }
            } else {
                mViewModel.getEpg(mChannel);
            }
        }
        hideControl();
        hideInfo();
    }

    @Override
    public void onLiveCastPanel() {
        dismissLiveControlDialog();
        onCast();
        hideControl();
    }

    @Override
    public void onLivePiPPanel() {
        dismissLiveControlDialog();
        App.post(() -> {
            try {
                if (service() != null && player().haveTrack(C.TRACK_TYPE_VIDEO)) {
                    mPiP.enter(this, player().getVideoWidth(), player().getVideoHeight(), LiveSetting.getScale());
                }
            } catch (Exception ignored) {}
        }, 100);
    }

    @Override
    public void onLiveBackgroundPanel() {
        dismissLiveControlDialog();
        // 后台播放：退到后台，由 PlaybackService 继续播
        try {
            moveTaskToBack(true);
        } catch (Exception ignored) {}
    }

    @Override
    public void onLiveListStylePanel(boolean classic) {
        LiveSetting.putListStyleClassic(classic);
        applyLiveListStyle();
    }

    @Override
    public void onLiveDisplayChanged() {
        applyLiveDisplay();
    }

    @Override
    public void onLiveScalePanel(int scale) {
        setScale(scale);
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        if (scale >= 0 && scale < array.length) {
            mBinding.control.action.scale.setText(array[scale]);
        }
    }

    @Override
    public void onLiveTrackPanel(int type) {
        dismissLiveControlDialog();
        try {
            if (type == 1) mBinding.control.action.audio.performClick();
            else if (type == 2) mBinding.control.action.video.performClick();
            else if (type == 3) mBinding.control.action.text.performClick();
        } catch (Exception ignored) {}
    }

}
