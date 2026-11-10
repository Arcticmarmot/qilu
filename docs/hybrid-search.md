# 混合召回 MVP

当前实现 RAG 的检索部分：ES 关键词召回与 Milvus 语义召回并行执行，
返回帖子和命中节点，供搜索页面或后续生成回答使用。

## 索引链路

```text
帖子写入 MySQL
  → 事务提交后发布 post-search-index-events
      ├─ qilu-post-search-index-group → ES 索引
      └─ qilu-post-vector-index-group → MySQL 回查 → Ollama /api/embed → Milvus
```

沿用现有帖子索引事件和 `SYNC / DELETE` 动作，两个消费者使用独立消费组。
每个帖子树节点对应一条向量，以 `post_id` 为主键，`root_id` 为动态字段，
正文拼接格式为 `标题 + 分支问题 + 完整正文`。
仅正常、公开的节点写入向量；同步发现节点私密、删除或不存在时删除旧向量。
重复同步按同一主键 upsert。

首次同步或查询自动建集合和向量索引，默认维度 1024、距离度量 COSINE。
MVP 使用一节点一向量，不做分块；超出模型上下文时 embedding 调用报错，
不静默截断。换模型或维度时，使用新集合名并重新回填。

## 查询链路

```text
查询文本
  ├─ ES multi_match，保留命中节点 ID
  └─ Ollama 查询向量 → Milvus 相似度搜索
      → MySQL 校验命中节点状态、可见性和当前 rootId
      → MySQL 查询公开正常的根帖卡片
      → 每路按 rootId 去重
      → RRF 融合排序 → 返回 Top K
```

每路最多召回 50 个候选，每棵树在每一路只贡献一次排名：

```text
RRF = 1 / (60 + ES排名) + 1 / (60 + 向量排名)
```

未命中某一路时，该路贡献为零。每路超时或异常会记日志并按空候选处理，
Milvus 关闭时仅使用 ES。数据回表在请求线程执行，保留当前用户的点赞状态。

## 配置和启动

在 Milvus、现有应用依赖和 Ollama 模型可用后启动应用：

```bash
MILVUS_ENABLED=true \
EMBEDDING_BASE_URL=http://192.168.1.11:11434 \
./mvnw spring-boot:run
```

| 配置环境变量 | 默认值 |
| --- | --- |
| MILVUS_ENABLED | true |
| MILVUS_COLLECTION_NAME | qilu_post_vector |
| EMBEDDING_BASE_URL | http://192.168.1.11:11434 |
| EMBEDDING_MODEL | qwen3-embedding:0.6b |
| EMBEDDING_DIMENSION | 1024 |
| EMBEDDING_CONNECT_TIMEOUT | 3s |
| EMBEDDING_READ_TIMEOUT | 30s |

客户端检查 embedding 数量、向量维度和数值有效性。

混合检索分支默认启用向量链路；仅使用 ES 时设置 `MILVUS_ENABLED=false`。
启动日志会打印向量链路开关和消费者运行状态；消费者收到事件后立即记录
`consume post vector index event received`，完成后记录 `success`，失败时记录异常。
修改开关需要重启应用，启动 Docker 中的 Milvus 不会自动改变 Spring 配置。

## 接口

### 存量回填

首次接入时直接调用，无需 Token：

```bash
curl -X POST http://localhost:8080/admin/search/vector-index/rebuild
```

经过项目 Nginx 时，地址为 `http://localhost/api/admin/search/vector-index/rebuild`。
接口始终显示在 Swagger 的 `AdminVectorIndex` 分组下；向量功能关闭时，
返回业务错误并提示设置 `MILVUS_ENABLED=true` 后重启应用。

按 ID 游标每批处理 20 个节点，返回处理节点数（包括删除失效向量的节点）。
该接口同步执行，适用于 MVP 小规模数据。它会刷新现有集合，也可在新集合上执行；
不会修改帖子数据。Kafka 历史保留消息不能代替这一步。

### 混合搜索

```bash
curl -G http://localhost:8080/search/posts/hybrid \
  -H "Authorization: Bearer $USER_TOKEN" \
  --data-urlencode 'keyword=如何避免 Kafka 消息重复消费' \
  --data-urlencode 'limit=10'
```

返回字段：

| 字段 | 含义 |
| --- | --- |
| post | 根帖卡片，沿用 PostPageItemVO |
| matchedPostId | 具体命中的节点 ID |
| matchedSnippet | 从 MySQL 当前正文生成的前 160 字符预览 |
| sources | ES、VECTOR，或两者 |
| score | RRF 排名分数 |

`limit` 范围 1 至 50，默认 10。接口返回有限候选结果，不提供深分页或全局 total。
原有 `GET /search/posts` 继续提供 ES 分页搜索。

本次按要求只进行 Maven 编译检查，未运行测试或中间件联调。
