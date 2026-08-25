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

E o primeiro admin, criado no boot quando a tabela de usuários está vazia:

```env
ADMIN_EMAIL=voce@argilalab.com.br
ADMIN_PASSWORD=uma_senha_forte
ADMIN_NAME=Seu Nome
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
`deploy/docker-compose.yml` puxa essa imagem e o Watchtower atualiza o
container sozinho — nada é compilado na máquina de destino.

## Segurança

Login por sessão, com papéis:

| | público | OPERATOR | ADMIN |
|---|---|---|---|
| `GET /api/catalog`, `GET /api/media/{hash}` | sim | sim | sim |
| Produtos, categorias, loja, mídia, importação | não | sim | sim |
| `GET/PUT /api/cost-parameters` (custo e margem) | não | **não** | sim |
| `/argilalabapp.html` | não | sim | sim |

- Senhas em BCrypt; o hash nunca é serializado (`@JsonIgnore`, com teste).
- CSRF ligado: o token vai num cookie legível e volta no header `X-XSRF-TOKEN`.
  O `api()` do admin já faz isso — outro cliente precisa fazer também.
- Sessão só por cookie (sem `;jsessionid=` na URL).
- CORS fechado por padrão. Para a loja estática ler o catálogo de outro domínio,
  defina `STOREFRONT_ORIGINS=https://argilalab.pages.dev` — só isso libera, e só
  em `GET` nos dois endpoints públicos.
- **Sem TLS ainda:** ponha atrás de um proxy com HTTPS antes de expor na internet.
  Sessão por cookie em HTTP puro trafega em texto claro.
