# 深色玻璃 UI 文字规范（星辰）

## 原则
- 深色面板 / 直播控制条 / Alert 上 **禁止** 使用 `?attr/colorOnSurface`、`?attr/colorOnSurfaceVariant` 等会随浅色主题变深的属性。
- 正文用 `@color/glass_text_primary`（`#FFF5F7FF`）
- 次要用 `@color/glass_text_secondary`（`#CCE8ECF8`）
- 提示用 `@color/glass_text_hint`（`#99A8B0C8`）

## Alert
- 唯一入口：`DarkAlert.builder(context)` / `BaseAlertDialog.builder()`
- 主题：`ThemeOverlay.App.Alert`（勿再设 `android:background`，否则 m3_alert_dialog 缺 width 崩溃）

## 扫尾
修改弹窗 layout 后自检：深色底 + 白字；输入框 `textColor`/`hint` 为白系。

## Live 控制器拆分（第 5 步）

- `LiveEmbeddedUi`：竖屏 inset / 底栏
- `LiveOverlayUi`：进度、错误、EPG 列表显隐
- `LiveControlPanel`：播控条 / 信息条 / 锁定图标 / 分辨率文案

## Video 控制器拆分（第 6 步）
- `VideoOverlayUi`：进度、错误
- `VideoControlPanel`：点播播控条显隐（全屏/锁定/弹幕/解析等）
- `VideoTypeRecommend`：同类型推荐（第 2 步）

## Video 换源与图标（第 7 步）
- `VideoSourceSwitch`：换源搜索站点过滤与结果匹配（与同类型推荐分离）
- `VideoChromeUi`：收藏/锁定/全屏/弹幕/清晰度图标状态

## 第 8 步
- `PlayerOverlayUi`：点播/直播共用进度与错误浮层
- `LiveLayoutHelper`：竖屏嵌入 / 横屏侧栏列表布局
