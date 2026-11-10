# Qilu Kubernetes Local Setup

这套清单是第一阶段的 K8s 学习配置：Spring Boot 应用跑在 Kubernetes，MySQL、Redis、Kafka、MinIO、Elasticsearch 暂时继续使用 `deploy/docker` 里的 Docker Compose。

## 1. 确认 kubectl 连到了真正的 Kubernetes

先执行：

```bash
kubectl config current-context
kubectl cluster-info
```

如果 `kubectl apply` 报下面这个错误：

```text
failed to download openapi: Get "http://localhost:8080/openapi/v2?timeout=32s": dial tcp [::1]:8080: connect: connection refused
```

这不是 qilu 应用的 8080 端口问题，而是 `kubectl` 没拿到有效的 Kubernetes 集群配置，于是默认去连 `localhost:8080` 这个 API Server 地址。

常见处理方式：

```bash
kubectl config get-contexts
kubectl config use-context docker-desktop
```

如果你用 Docker Desktop，要先在 Docker Desktop 设置里启用 Kubernetes，并等它状态变成 running。

## 2. 启动本地依赖

```bash
make up
```

注意 Kafka 当前 Docker Compose 里 `KAFKA_ADVERTISED_LISTENERS` 是 `localhost:9092`。如果应用跑在 Kubernetes Pod 里，Kafka 客户端可能会拿到 `localhost:9092` 并连回 Pod 自己。第一次验证时如果遇到 Kafka 连接失败，先把 Kafka 也迁到 K8s，或者单独调整 Kafka 的 advertised listener。

## 3. 构建应用镜像

Docker Desktop Kubernetes 通常可以直接使用本机 Docker 镜像：

```bash
docker build -t qilu:local .
```

如果你用的是 Minikube：

```bash
eval $(minikube docker-env)
docker build -t qilu:local .
```

如果你用的是 kind：

```bash
docker build -t qilu:local .
kind load docker-image qilu:local
```

## 4. 部署到 Kubernetes

```bash
kubectl apply -k deploy/k8s
```

查看状态：

```bash
kubectl get pods -n qilu
kubectl get svc -n qilu
kubectl logs -f deploy/qilu -n qilu
```

本地访问：

```bash
kubectl port-forward svc/qilu 8080:8080 -n qilu
```

然后打开：

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/actuator/health
```

## 5. Ingress 访问

如果你的集群已经安装了 Nginx Ingress Controller，可以把下面内容加入 `/etc/hosts`：

```text
127.0.0.1 qilu.local
```

然后访问：

```text
http://qilu.local/swagger-ui.html
```

如果没有 Ingress Controller，先用 `kubectl port-forward`。

## 6. Lens 里重点看什么

- `qilu` namespace
- `qilu` Deployment 的副本状态
- Pod events
- Pod logs
- ConfigMap 和 Secret 是否挂到了 Pod 环境变量
- readiness/liveness probe 是否通过
- CPU 和内存使用量

## 7. 后续扩展方向

第一阶段先只部署应用。下一步可以逐个把依赖迁到 K8s：

1. Redis：Deployment 或 StatefulSet + PVC
2. MySQL：StatefulSet + PVC + Secret
3. MinIO：StatefulSet + PVC
4. Kafka：单节点学习版 StatefulSet，重点处理 advertised listener
5. Elasticsearch：单节点学习版 StatefulSet，注意内存和 vm.max_map_count
