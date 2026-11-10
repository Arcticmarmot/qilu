# Milvus 本地开发环境

固定版本：Milvus `2.6.26`、Java SDK `2.6.27`，版本搭配见
[官方兼容表](https://github.com/milvus-io/milvus/releases/tag/v2.6.26)。
部署结构参考[官方 2.6 Standalone Compose](https://github.com/milvus-io/milvus/blob/v2.6.26/deployments/docker/standalone/docker-compose.yml)。

## 组成与端口

| 服务 | 用途 | 宿主机端口 |
| --- | --- | --- |
| standalone | 向量数据库，供 Java SDK 访问 | 19530 |
| standalone HTTP | 健康检查、指标和 WebUI | 9091 |
| etcd | Milvus 元数据 | 不暴露 |
| minio | Milvus 持久化对象存储 | 不暴露 |

Milvus 使用独立的 MinIO 和数据卷，不复用媒体文件的 `qilu-minio`。
因此不会占用已有的 `9000/9001`，也不需要先启动其他 Compose。
三个服务各有命名卷，`down` 后数据保留。
当前 Compose 是本地开发配置，Milvus 未开启访问鉴权。

## 启停与检查

在项目根目录执行：

```bash
make milvus-up
make milvus-status
curl -f http://localhost:9091/healthz
make milvus-logs
make milvus-down
```

`milvus-up` 会等待服务健康，首次启动需要拉取镜像。
项目原有的 `make up` / `make down` 也已包含 Milvus。
WebUI 地址为 <http://localhost:9091/webui/>。

可通过环境变量 `MILVUS_PORT` / `MILVUS_HTTP_PORT` 修改宿主机端口。
修改 SDK 端口时，也要同步设置应用的 `MILVUS_URI`。
如需修改内部 MinIO 凭证，在启动 Compose 前设置
`MILVUS_MINIO_ACCESS_KEY` / `MILVUS_MINIO_SECRET_KEY`；两个容器使用同一组值。

## 应用配置

`application.yaml` 的 `milvus` 配置由 `MilvusProperties` 绑定。
当前默认 `MILVUS_ENABLED=false`，不创建客户端，保持尚未使用向量检索时的启动方式。
启动 Milvus 后，可通过以下命令启用客户端：

```bash
MILVUS_ENABLED=true ./mvnw spring-boot:run
```

| 环境变量 | 默认值 | 含义 |
| --- | --- | --- |
| MILVUS_ENABLED | false | 是否创建 MilvusClientV2 Bean |
| MILVUS_URI | http://localhost:19530 | SDK 连接地址 |
| MILVUS_TOKEN | 空 | 开启鉴权后的 username:password 或云服务 token |
| MILVUS_DATABASE_NAME | default | 已存在的数据库名称 |
| MILVUS_CONNECT_TIMEOUT | 5s | 建立连接的超时时间 |
| MILVUS_RPC_DEADLINE | 10s | RPC 调用超时时间 |
| MILVUS_COLLECTION_NAME | qilu_post_vector | 后续向量索引使用的集合名 |

启用后客户端会在初始化时连接服务，应用关闭时调用 `close()` 释放连接。
本机运行应用使用 `localhost`；容器或 K8s 中运行时，需要将 `MILVUS_URI`
指向从应用容器可达的 Milvus 地址。

本次只接入基础设施，不自动创建数据库、集合或索引。
集合的向量维度、距离度量和索引参数应在确定 embedding 模型后再设置。
后续可让 ES 做关键词召回、Milvus 做语义召回，在搜索服务中按 `postId/rootId`
融合结果，并回 MySQL 校验可见性、获取展示内容。
