# 星辰订阅配置 Schema（C0 草案）

## 目标

- **写出**：逐步支持 `schema: "xingchen/1"` 的星辰格式  
- **读入**：**完全兼容** 现有 FongMi / webhtv / 影视仓 JSON（无 schema 字段）  
- 不破坏现网订阅源

## 识别规则

| 特征 | 判定 |
|------|------|
| 无 `schema` 或未知 | **legacy**（FongMi 系），原样解析 |
| `"schema": "xingchen/1"` | **星辰 v1**，先经 `ConfigNormalizer` 再解析 |

## 星辰 v1 字段（与 legacy 对齐的核心）

```json
{
  "schema": "xingchen/1",
  "name": "可选显示名",
  "spider": "可选全局 jar 地址",
  "sites": [ { "key", "name", "type", "api", "searchable", "quickSearch", "changeable", "ext", "jar" } ],
  "lives": [ { "name", "type", "url", "epg", "logo", "pass" } ],
  "parses": [ { "name", "type", "url" } ],
  "wallpaper": "壁纸 url",
  "flags": [],
  "ads": [],
  "hosts": [],
  "headers": [],
  "proxy": [],
  "rules": [],
  "doh": []
}
```

### 别名（仅星辰 v1 读入时展开）

| 星辰别名 | 规范化为 |
|----------|----------|
| `sources` | `sites` |
| `channels` | `lives` |
| `parsers` | `parses` |
| `wall` / `wallpaper_url` | `wallpaper` |

站点项别名：`id`→`key`，`spider`→`api`（当 api 为空且为 js/py/csp 时）——**C0 仅做文档预留，实现以安全别名为主**。

## 兼容策略

1. Legacy JSON：**零改动**进入现有 `VodConfig.parseConfig`  
2. 星辰 JSON：规范化成与 legacy **同构**的 `JsonObject` 再进入同一解析路径  
3. 写出（后续 C1）：可从当前内存配置导出 `schema: xingchen/1`

## 非目标（C0）

- 不替换 Spider ABI  
- 不强制用户改订阅  
- 不移除 jar/js/py  


## 导出（C1）

- 代码：`ConfigExporter.build()` / `writeToTvDir()`
- 设置页：长按「备份」→ 写入应用 `TV/` 目录下的 `xingchen-config-时间戳.json`
- 可用 `ConfigNormalizer` 再读回（别名与标准字段均支持）
