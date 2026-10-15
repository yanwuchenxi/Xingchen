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
