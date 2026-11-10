MYSQL_DOCKER_COMPOSE_FILE = deploy/docker/mysql/docker-compose.yml
REDIS_DOCKER_COMPOSE_FILE = deploy/docker/redis/docker-compose.yml
KAFKA_DOCKER_COMPOSE_FILE = deploy/docker/kafka/docker-compose.yml
MINIO_DOCKER_COMPOSE_FILE = deploy/docker/minio/docker-compose.yml
ELASTICSEARCH_DOCKER_COMPOSE_FILE = deploy/docker/elasticsearch/docker-compose.yml
MILVUS_DOCKER_COMPOSE_FILE = deploy/docker/milvus/docker-compose.yml
NGINX_DOCKER_COMPOSE_FILE = deploy/docker/nginx/docker-compose.yml

.PHONY: up down milvus-up milvus-down milvus-logs milvus-status

up:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(REDIS_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(MINIO_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(ELASTICSEARCH_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(NGINX_DOCKER_COMPOSE_FILE) up -d

down:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) down
	docker compose -f $(REDIS_DOCKER_COMPOSE_FILE) down
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) down
	docker compose -f $(MINIO_DOCKER_COMPOSE_FILE) down
	docker compose -f $(ELASTICSEARCH_DOCKER_COMPOSE_FILE) down
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) down
	docker compose -f $(NGINX_DOCKER_COMPOSE_FILE) down

milvus-up:
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) up -d --wait --wait-timeout 180

milvus-down:
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) down

milvus-logs:
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) logs -f standalone

milvus-status:
	docker compose -f $(MILVUS_DOCKER_COMPOSE_FILE) ps

nginx-up:
	docker compose -f $(NGINX_DOCKER_COMPOSE_FILE) up -d

nginx-down:
	docker compose -f $(NGINX_DOCKER_COMPOSE_FILE) down

nginx-logs:
	docker logs -f qilu-nginx

nginx-reload:
	docker exec qilu-nginx nginx -s reload
