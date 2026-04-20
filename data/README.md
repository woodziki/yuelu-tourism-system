# Ctrip Comment Crawler

基于 `requests` 的携程评论 API 爬虫示例，支持多景点遍历、分页抓取、限速与 CSV 追加写入。

## 1. 安装依赖

```bash
pip install -r requirements.txt
```

## 2. 运行

```bash
python crawler.py
```

## 3. 输出文件

脚本会在当前目录生成/追加写入 `comments.csv`，表头为：

`['景点名称', '评分', '评论内容', '发布时间']`

## 4. 关键动态参数

- 景点 ID：`payload["arg"]["poiId"]`
- 页码：`payload["arg"]["pageIndex"]`

## 5. 防反爬限速

- 每个景点抓取前 3 页（可改）
- 每页后延迟：`time.sleep(random.uniform(2.5, 3.5))`
- 每个景点完成后延迟：`time.sleep(random.uniform(9.0, 11.0))`

## 6. 注意事项

抓包参数（如 Cookie、`_fxpcqlniredt`、`x-traceID`、`x-ctx-wclient-req`）可能会过期，请及时更新为你最新抓包值。
