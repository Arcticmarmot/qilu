# 业务文件存储 MinIO

原 `minio/minio:RELEASE.2025-09-07T16-13-09Z` 已无法公开拉取，
官方 Quay 同版本也不可用。这里使用
[Dappros 维护的同版本镜像副本](https://hub.docker.com/r/dappros/minio)，
并固定多架构 manifest digest，支持 Linux amd64/arm64。
这是第三方镜像仓库；MinIO 服务版本仍为 `RELEASE.2025-09-07T16-13-09Z`。

已验证 amd64 镜像的 `/usr/bin/minio` SHA256 与
[MinIO 官方 GitHub Release 的校验文件](https://github.com/minio/minio/releases/download/RELEASE.2025-09-07T16-13-09Z/minio.linux-amd64.RELEASE.2025-09-07T16-13-09Z.sha256sum)
一致：

```text
7c5bd8512c6e966455b1d198209358b2d191c77a83ab377c4073281065fb855f
```

保留原有 `qilu-minio` 容器名、`minio-data` 命名卷、9000/9001 端口、
凭证与 `/data` 路径，不涉及存储版本降级或数据迁移。
需要使用自有镜像仓库时，可以用 `MINIO_IMAGE` 环境变量覆盖完整镜像地址。

在项目根目录启动和检查：

```bash
docker compose -f deploy/docker/minio/docker-compose.yml up -d --wait --wait-timeout 90
curl -f http://localhost:9000/minio/health/live
```

也可直接使用项目的 `make up`。停止时使用 `down` 会保留命名卷；
不要使用 `down -v` 删除已有文件数据。
