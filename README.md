# 💰 Finanças API

![CI](https://github.com/RodrigodSantos/financas-api/actions/workflows/ci.yml/badge.svg)

API REST de finanças pessoais para controlar contas, categorias, receitas e despesas, com relatórios mensais.

> 🚧 Em desenvolvimento. Veja o [roadmap](#-roadmap).

## 🛠️ Stack
- **Java 17** + **Spring Boot 3**
- **Spring Data JPA** + **PostgreSQL 16**
- **Flyway** para versionar o schema
- **springdoc-openapi** (Swagger UI)
- **JUnit 5** + **Testcontainers** (testes contra um Postgres real)
- **Docker** / **Docker Compose**
- **GitHub Actions** (CI) + **JaCoCo** (cobertura)

## 🚀 Como rodar

Pré-requisitos: Java 17, Maven e Docker.

```bash
# 1. Sobe o Postgres
docker compose up -d

# 2. Sobe a API
mvn spring-boot:run
```

Ou tudo em container:

```bash
docker compose --profile app up --build
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Testes
```bash
mvn verify
```
O Docker precisa estar rodando: o Testcontainers sobe um Postgres temporário para os testes.
Relatório de cobertura: `target/site/jacoco/index.html`.

## 🗂️ Modelo de dados

```mermaid
erDiagram
    USUARIO ||--o{ CONTA : possui
    USUARIO ||--o{ CATEGORIA : cria
    CONTA ||--o{ TRANSACAO : registra
    CATEGORIA ||--o{ TRANSACAO : classifica

    USUARIO { bigint id string nome string email }
    CONTA { bigint id string nome string tipo decimal saldo_inicial }
    CATEGORIA { bigint id string nome string tipo }
    TRANSACAO { bigint id string descricao decimal valor string tipo date data }
```

## 📁 Estrutura

O código é organizado **por funcionalidade**, não por camada:

```
src/main/java/br/com/financas
├── categoria/          # controller, service, repository, entidade e DTOs
├── config/             # configurações (OpenAPI)
└── shared/exception/   # exceções e handler global (RFC 7807)
```

## 📡 Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/categorias?tipo=&page=&size=` | Lista categorias (paginado) |
| GET | `/api/categorias/{id}` | Busca uma categoria |
| POST | `/api/categorias` | Cria uma categoria |
| PUT | `/api/categorias/{id}` | Atualiza uma categoria |
| DELETE | `/api/categorias/{id}` | Exclui uma categoria |

A collection do Postman está em [`postman/`](postman/financas-api.postman_collection.json).

### Formato de erro
Todos os erros seguem o padrão [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807):
```json
{
  "type": "about:blank",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos são inválidos",
  "campos": { "nome": "é obrigatório" }
}
```

## 🧠 Decisões técnicas
- **Pacotes por funcionalidade**: cada parte do domínio fica isolada e fácil de encontrar.
- **Flyway + `ddl-auto: validate`**: o schema é versionado, e o Hibernate apenas confere se as entidades batem com ele.
- **Testcontainers em vez de H2**: os testes rodam no mesmo banco da produção.
- **Records para DTOs**: imutáveis e sem código repetitivo.
- **Categorias globais** (`usuario_id` nulo) + categorias próprias do usuário.

## 🗺️ Roadmap
- [x] Estrutura, Docker, Flyway, Swagger, CI
- [x] CRUD de categorias
- [ ] CRUD de contas e transações
- [ ] Autenticação JWT e isolamento por usuário
- [ ] Filtros e relatório mensal
- [ ] Exportação CSV
- [ ] Deploy
