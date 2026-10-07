# 星辰 (Xingchen)

基于 WebHomeTV / FongMi 生态的 Android 影音应用（手机 + 电视）。

- 包名：`com.xingchen.android.tv`
- 应用名：星辰
- 基座：webhtv 5.5.2（含 `androidx.media3:*-1.10.1-fongmi` 本地 Maven）

## 构建

```bash
./gradlew assembleMobileRelease
# 或
./gradlew assembleLeanbackRelease
```

APK 输出到 `app/build/outputs/apk/` 与 `Release/apk/`。

CI：push 到 `main` 自动构建手机版 Release。
