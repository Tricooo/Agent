# RAG Eval Viewer

把 `docs/dev-ops/rag-eval/results/` 下全部 markdown 报告做成可对比可下钻的单页 HTML，
**只读源文件**，所有解析产物落在本目录 `data/` 下。

## 打开

```bash
cd docs/dev-ops/rag-eval/viewer
python3 -m http.server 8765
# 浏览器开 http://localhost:8765/index.html
```

也可以直接 `open index.html`（Safari/Chrome 走 file://）。`build_dataset.py` 同时输出
`data/dataset.js`，因此本地双击打开也能工作，不强依赖 http server。

## 重新生成数据

新增 / 修改 results/ 下任何 markdown 报告后：

```bash
python3 build_dataset.py
```

## 视图

| 视图 | 用途 |
|---|---|
| **矩阵** | 行 = 14 个 RAG case，默认列出完整 14-case run；范围下拉可切到全部 run 或仅 probe。cell 颜色编码 literal_hit / manual_pass，点击 cell 弹下钻 |
| **AB 对比** | 选两个 run，先给全局 diff summary，再按 case 并排列出 completed / literal_hit / retrieved / score_max / empty / duration / top1_source / top1_chunk / rerank_mode 的差异，可展开 answer 全文 |
| **汇总统计** | 全量实验与 probe 分组展示：通过率 / 平均时长 / schema 版本 / 配置 hint |

## Schema 兼容

Parser 识别 3 种 schema 时代：

- **legacy**（Apr 28）：`matched_points / missed_points`，无检索字段
- **triangle**（Apr 30）：加 `literal_hit`，仍无 `retrieved/score/empty`
- **new**（May 1+）：完整 `retrieved/score/empty/literal_hit`，step4+ 有 `rrfScore/vectorScore/keywordScore`，step5+ 有 `rerankScore/rerankMode/rerankRank`

矩阵 cell 自适应：早期 schema 只显示 literal_hit，新 schema 同时显示 retrieved 数 + score。

## 文件清单

- `build_dataset.py` — 解析器（Python，无外部依赖）
- `index.html` — 单页 SPA（vanilla JS / CSS，无 CDN 依赖）
- `data/dataset.json` — JSON 主数据
- `data/dataset.js` — 同样内容包装成 `window.RAG_EVAL_DATASET`（用于 file:// 访问）
- `qa/` — Playwright 验证截图

## 源文件保护

本目录工具 **从不写** results/ 下任何文件，只读取后输出到 `data/`。
