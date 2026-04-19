MYSQL_DOCKER_COMPOSE_FILE = deploy/docker/mysql/docker-compose.yml
KAFKA_DOCKER_COMPOSE_FILE = deploy/docker/kafka/docker-compose.yml

up:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) up -d
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) up -d

down:
	docker compose -f $(MYSQL_DOCKER_COMPOSE_FILE) down
	docker compose -f $(KAFKA_DOCKER_COMPOSE_FILE) down