# targetSdk / Media3 说明（星辰）

## targetSdk

| 版本 | 说明 |
|------|------|
| 原 28 | 与 webhtv 5.5.2 基座一致，新机行为偏差大 |
| **现 34** | Android 14；保留 FGS mediaPlayback、通知权限、安装包权限 |

### 已配套
- `POST_NOTIFICATIONS` + 首页启动 `PermissionUtil.requestNotify`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` + PlaybackService `foregroundServiceType`
- `REQUEST_INSTALL_PACKAGES` + FileProvider 应用内更新
- API 33+：`READ_MEDIA_VIDEO/AUDIO/IMAGES` 作为未开「所有文件」时的降级

### 验证清单
1. 冷启动权限：存储/媒体 + 通知
2. 后台播放通知与锁屏控制
3. 应用内更新安装
4. 本地字幕/文件选择

## Media3

| 组件 | 当前 |
|------|------|
| media3 | **1.11.0-alpha01-fongmi**（third_party 本地 maven） |
| nextlib | 1.10.0-0.12.1-fongmi-softload-av3a-avs3-ffmpeg901-r5 |

**策略：不自动追上游正式版。** alpha-fongmi 带 softload/av3a 等补丁，换官方稳定版需整包回归。

升级条件建议：
1. webhtv / FongMi 出现同能力的非 alpha 标签
2. CI 冒烟：HLS / 直播 / 字幕 / 后台播放
3. 同步更新 `third_party/media-lock.json` 与 aar

## 后续（未做）
- targetSdk 35：预测返回手势、更严的后台限制
- 逐步减少对 `MANAGE_EXTERNAL_STORAGE` 的依赖（上架商店时必做）
