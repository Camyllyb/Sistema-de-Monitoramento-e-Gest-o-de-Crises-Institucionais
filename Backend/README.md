# Sistema de Monitoramento e Gestão de Crises Institucionais

Este projeto entrega a base profissional do backend para a plataforma acadêmica, com Spring Boot 3, Java 21, PostgreSQL, Flyway, Spring Security preparado para JWT, Swagger/OpenAPI e Docker.

## Estrutura inicial

- Arquitetura em camadas: controller, service, repository, entity e DTOs
- Padrão de resposta comum via ApiResponseDTO
- Tratamento global de exceções
- Endpoint inicial: GET /api/health
- Migrações Flyway para PostgreSQL
- Suporte a debug remoto na porta 5005

## Requisitos

- Docker
- Docker Compose
- Java 21
- Maven

## Subir o banco com Docker

```bash
docker compose up -d postgres
```

## Subir o backend localmente

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:
- http://localhost:8080/api/health

## Executar com Docker Compose

```bash
docker compose up --build
```

O backend ficará disponível em:
- http://localhost:8080/api/health
- Swagger em: http://localhost:8080/swagger-ui.html

## Debug remoto

A porta 5005 está exposta no container. No seu cliente de Debug remoto, use:
- host: localhost
- port: 5005

## Swagger/OpenAPI

Após subir o backend, acesse:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

## Comandos Maven úteis

```bash
mvn clean test
mvn spring-boot:run
mvn package
```

## Variáveis de ambiente

As configurações do PostgreSQL e JWT usam as seguintes variáveis, com valores padrão:

### Banco de dados (PostgreSQL)

- `PGHOST` - Host do PostgreSQL (padrão: `localhost`)
- `PGPORT` - Porta do PostgreSQL (padrão: `5432`)
- `PGDATABASE` - Nome do banco (padrão: `gestaocrise_db`)
- `PGUSER` - Usuário do banco (padrão: `gestaocrise`)
- `PGPASSWORD` - Senha do banco (padrão: `gestaocrise`)

### JWT

- `APPLICATION_SECURITY_JWT_SECRET_KEY` - Chave secreta para assinatura JWT (padrão definido em application.yml)
- `APPLICATION_SECURITY_JWT_EXPIRATION` - Tempo de expiração em ms (padrão: `86400000` - 24h)

### CORS

- `CORS_ALLOWED_ORIGINS` - Origins permitidas para CORS (padrão: `http://localhost:4200`)

### Servidor

- `SERVER_PORT` - Porta do servidor (padrão: `8080`)
