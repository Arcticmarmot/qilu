MYSQL_DOCKER_COMPOSE_FILE = deploy/docker/mysql/docker-compose.yml
REDIS_DOCKER_COMPOSE_FILE = deploy/docker/redis/docker-compose.yml
KAFKA_DOCKER_COMPOSE_FILE = deploy/docker/kafka/docker-compose.yml
MINIO_DOCKER_COMPOSE_FILE = deploy/docker/minio/docker-compose.yml
ELASTICSEARCH_DOCKER_COMPOSE_FILE = deploy/docker/elasticsearch/docker-compose.yml

up:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(REDIS_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(MINIO_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(ELASTICSEARCH_DOCKER_COMPOSE_FILE) up -d

down:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) down
	docker compose -f $(REDIS_DOCKER_COMPOSE_FILE) down
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) down
	docker compose -f $(MINIO_DOCKER_COMPOSE_FILE) down
	docker compose -f $(ELASTICSEARCH_DOCKER_COMPOSE_FILE) down