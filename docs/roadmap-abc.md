# 星辰 A → B → C 自研路线

> 原则：**兼容现有源协议不中断**；用绞杀者（Strangler）方式把实现换成星辰边界，而不是一次性推倒重写。

## 阶段总览

| 阶段 | 目标 | 状态 |
|------|------|------|
| **A 产品自研** | 品牌 / UI / 交互独立 | **基本完成**（持续打磨） |
| **B 代码自研** | 架构与模块归星辰所有，协议仍兼容 FongMi/webhtv 源 | **进行中** |
| **C 生态自研** | 自有源格式 + 运行时 + 播放栈可替换 | **未开始**（依赖 B 稳定） |

---

## A · 产品自研（维持）

已完成（节选）：

- [x] 包名 `com.xingchen.android.tv`、应用名「星辰」、图标
- [x] 首页 / 直播 / 详情 / 设置玻璃拟态与 YingKe 向交互
- [x] 在线字幕、检测更新 Toast、targetSdk 34 等产品能力
- [ ] 设计规范文档化、无障碍与动画统一（可选）

验收：用户感知为「星辰」，而非 webhtv 换皮。

---

## B · 代码自研（当前主战场）

### B0 边界定义（本提交）

- 包 `com.xingchen.android.tv.runtime`：对外稳定门面
  - `VodRuntime`：点播站点与内容
  - `LiveRuntime`：直播列表与取流入口（薄封装）
  - `PlayRuntime`：播放会话抽象（薄封装）
- 实现类 `*Compat`：**仅委托**现有 `VodConfig` / `SiteApi` / `LiveConfig` / `PlayerManager`，行为与现在一致

### B1 绞杀迁移（进行中）

顺序建议（风险从低到高）：

1. **设置 / 备份 / 更新 / 主题** — 已较独立，继续纯星辰
2. **UI controller** — 已拆 Live/Video 控制器，继续抽
3. **ViewModel / 搜索 / 推荐 / Browse** — ✅ `SiteViewModel`、`SearchTask`、`LiveViewModel`、`VideoTypeRecommend`、`VodBrowse` 已走 runtime
4. **详情 Activity 内残留 SiteApi** — PUSH 常量等可保留；业务经 ViewModel
5. **播放与解析** — ✅ `PlayRuntime`：元数据 / 创建播放器 / 嗅探源退出；PlaybackService + Live/Video/Home 已接入
6. **Loader（Jar/JS/Py）** — ✅ `LoaderRuntime`
7. **Config / UI 收敛（B4）** — ✅
8. **B5 扫尾** — ✅ 手机/TV 主路径 Activity·Fragment·Dialog·Adapter 尽量走 runtime（Doh 等次要仍可直接 Config）

每完成一块：上层只依赖 `runtime.*`，测试源站仍可用。

### B2 实现可替换

- `VodRuntime` 可有 `VodRuntimeCompat`（当前）与未来 `VodRuntimeNative`
- 播放：`PlayRuntime` 下可切换 Engine，不改 Activity

验收：核心路径（首页→详情→播放、直播切台）不直接 import `catvod.crawler` / `SiteApi`（除 Compat 实现包外）。

---

## C · 生态自研

### C0（已落地）

- [x] `docs/schema-xingchen-config.md` 草案
- [x] `ConfigNormalizer`：识别 `xingchen/1`，别名展开后走现有解析
- [x] 无 schema 的 FongMi JSON **行为不变**

### C1（后续）



1. 从内存配置 **导出** `schema: xingchen/1` JSON
2. 自研或精简 Spider ABI（或 WASM/JS 单一运行时）
3. 官方 Media3 稳定版或自有 softload 策略
4. 公共源迁移工具（FongMi JSON → 星辰 schema）

验收：无 FongMi 源时应用仍可用；有转换器时可导入旧源。

---

## 节奏建议

| 周期 | 焦点 |
|------|------|
| 近 1～2 周 | B0 落地 + UI 新代码只走 runtime；补文档 |
| 1～2 月 | B1：首页/详情/直播调用迁到 runtime |
| 其后 | B2 播放与 Loader 接口稳定 |
| 再后 | 启动 C 的 schema 与转换器原型 |

**禁止**：在 C 未设计前删除 Jar/JS 兼容或强行换掉 Media3-fongmi（会导致现网源不可用）。
