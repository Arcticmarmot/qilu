# 社区 RAG 问答 MVP

`POST /search/chat` 复用现有用户 JWT 鉴权，响应沿用 `ApiResponse`。

```text
问题 → HybridSearchService.searchPosts（ES + Milvus + RRF）
    → 按根帖去重，最多 5 个
    → PostService.getPublicPostDetail 读取 MySQL 当前公开详情
    → 选取根帖标题、命中节点正文、分支提问、根帖和直接父节点背景
    → 编号来源 → Ollama /api/chat → 校验引用编号 → 回答和真实来源
```

混合检索现有字段 `matchedPostId` 对应新接口引用中的 `matchedNodeId`。
正文重新从 MySQL 读取，不使用 `matchedSnippet`。公开详情查询会检查根帖和节点的
状态、可见性，并沿用当前登录上下文；搜索后已删除或转私密的来源会被跳过。
第一版复用公开详情查询，会读取该根帖的公开树及媒体，再挑选需要的节点；
树规模增大后可以改成针对命中节点的批量查询，目前不新增查询和表。

每条来源最多 2200 字符，其中命中正文最多 1400、标题和分支提问各 150、
根帖和直接父节点背景各 200。检索上下文总计最多 11000 字符。
这属于字符预算，不是 token 精确计数；结果不足时使用实际可用的来源数量。

System Prompt 限定使用社区证据、简洁中文和 `[1][2]` 引用，允许说明观点差异；
明确将标题、正文和分支提问视为不可信资料，不能执行其中的指令。
引用列表完全由数据库结果构造，数组第 n 项对应 `[n]`，包含本次提供给模型的全部来源。
输出含越界编号时返回明确业务错误；输出未给出有效引用时返回证据不足，避免直接展示无来源的模型回答。
编号校验只保证引用指向本次真实来源，不保证模型每条结论都被相应正文支持。

没有可访问的证据时直接返回证据不足和空来源，不调用模型。
Ollama 连接、HTTP 或响应错误使用项目业务错误格式返回 `code=50000`，不修改普通搜索链路。

## 配置

`application.yaml` 中新增 `ollama.chat`，不需要新增 Maven 依赖。

| 环境变量 | 默认值 |
| --- | --- |
| OLLAMA_CHAT_BASE_URL | 复用 `embedding.base-url`，目前为 `http://192.168.1.11:11434` |
| OLLAMA_CHAT_MODEL | `qwen3.5:4b` |
| OLLAMA_CHAT_CONNECT_TIMEOUT | `3s` |
| OLLAMA_CHAT_READ_TIMEOUT | `120s` |

请求使用 `stream=false`、`think=false`，显式设置 16384 token 上下文窗口，最多生成 768 token。
接口格式参考 [Ollama Chat API](https://docs.ollama.com/api/chat)。
Embedding 保持 `qwen3-embedding:0.6b` 和 1024 维，不需要重新建立 Milvus 索引。

## 调用

重启后端后，在 Swagger 的 `RagChat` 分组可以查看接口。
先通过现有登录接口获取用户 Token，然后执行：

```bash
export USER_TOKEN='替换为登录返回的用户 Token'
curl --max-time 180 -sS http://localhost:8080/search/chat \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H 'Content-Type: application/json' \
  --data '{"question":"Kafka 消费者失败后如何恢复？","limit":5}'
```

经过项目 Nginx 时使用 `http://localhost/api/search/chat`。
`question` 必填、最长 512 字符，`limit` 默认 5、范围 1 至 5。
成功响应示意（ID 和标题实际以数据库为准）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "重试需要结合幂等处理。[1]",
    "references": [
      {
        "postId": 100,
        "matchedNodeId": 102,
        "title": "Kafka 重试",
        "url": "/posts/100"
      }
    ]
  }
}
```

当前仓库没有前端搜索页面。前端接入时可在现有搜索框旁增加「AI 回答」按钮，
提交上述 JSON；等待期间禁用重复提交，展示 `data.answer` 和来源链接，错误时展示 `message`。
正文按普通文本或禁用原始 HTML 的 Markdown 渲染，来源链接使用后端返回的相对路径。
来源顺序保留原样，以便 `[n]` 对应数组第 n 项。

## 验证

```bash
./mvnw -Dtest=RagAnswerServiceImplTest,OllamaChatClientTest,RagChatControllerTest test
```

测试隔离外部中间件，覆盖正文与分支上下文、来源去重和数据库引用、空召回、
私密或删除来源、模型失败、非法引用、输入长度预算、HTTP 请求格式与参数校验。
