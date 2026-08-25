# ARGILA LAB — loja e admin

Catálogo, cálculo de custo, loja pública e gestão de pedidos e compras da
ARGILA LAB, em Spring Boot + PostgreSQL.

O frontend (admin) é servido pelo próprio jar em `/argilalabapp.html`.

## Stack

- Java 25 + Spring Boot 4
- PostgreSQL + Flyway
- Docker Compose

## O que tem aqui

### Catálogo e custos
Produtos com SKU derivado (`AL-<cat>-<num>[-<tam>]`), categorias editáveis,
fotos deduplicadas por SHA-256 e um conjunto único de parâmetros que recalcula
o custo de todos os produtos.

### Loja pública
`GET /api/catalog` devolve o catálogo vivo (produtos publicados + configuração
da loja). O checkout é um link `wa.me` — nenhum dado de cliente passa pela API.

### Gestão
Clientes, fila de impressão (com status e prioridade contígua) e compras de
insumos.

## API

| Recurso | Endpoint |
|---|---|
| Catálogo público | `GET /api/catalog` |
| Produtos | `/api/products` |
| Categorias | `/api/categories` |
| Parâmetros de custo | `/api/cost-parameters` |
| Configuração da loja | `/api/store` |
| Mídia | `POST /api/media`, `GET /api/media/{hash}` |
| Importação | `POST /api/import` |

Clientes, pedidos e compras ainda não têm controller: os services existem
(`CustomerService`, `OrderService`, `PurchaseService`), falta expô-los.

## Rodando

### Pré-requisitos

- Docker + Docker Compose

### Variáveis de ambiente

Crie o `.app_env` (nunca commitado):

```env
DATABASE_URL=jdbc:postgresql://postgres-app:5432/simao_db
DATABASE_USERNAME=matheus
DATABASE_PASSWORD=sua_senha
```

E o `.postgres_app.env`:

```env
POSTGRES_DB=simao_db
POSTGRES_USER=matheus
POSTGRES_PASSWORD=sua_senha
```

### Subir

```bash
docker compose up -d
```

O Flyway roda as migrations no boot. O admin fica em
`http://localhost:8080/argilalabapp.html`.

## Deploy

`.github/workflows/deploy.yml` builda a partir de `deploy/Dockerfile` e publica
a imagem no GHCR a cada push na `main`. No servidor, o
`deploy/docker-compose.notebook.yml` puxa essa imagem e o Watchtower atualiza o
container sozinho.

## Segurança

> **Ainda não há autenticação.** `spring-boot-starter-security` está comentado
> no `pom.xml` e o CORS em `CatalogCorsConfig` aceita qualquer origem. Enquanto
> isso não mudar, as portas ficam em `127.0.0.1` e o acesso é só pela tailnet —
> qualquer exposição pública permitiria escrita anônima na API.
