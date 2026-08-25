# ARGILA LAB — frontend

App de catálogo + custos + loja, servido pelo próprio backend em
`src/main/resources/static/argilalabapp.html`.

## Origem e port

Esta é a versão **v4** do app (catálogo, custos, loja pública, carrinho,
múltiplas fotos, slug/SEO). A v4 vinha com **Supabase** como backend (auth +
tabela `projeto` com o projeto inteiro em JSON + Storage para as fotos). Aqui
essa camada foi **substituída pela API do simao-api**:

| v4 original (Supabase) | agora |
|---|---|
| login e-mail/senha | sem login (mesma origem, MVP) |
| `POST /rest/v1/projeto` (blob JSON) | CRUD por recurso: `/api/products`, `/api/categories`, `/api/cost-parameters`, `/api/store` |
| fotos no Storage | `POST /api/media` (dedupe por SHA-256) → `/api/media/{hash}` |
| `catalogo.json` publicado no bucket | `GET /api/catalog` (catálogo vivo) |
| "Enviar/Baixar da nuvem" | salva a cada edição; botão "Recarregar do servidor" |

O SKU e o custo são calculados **no servidor**; o modal mantém uma prévia de
custo client-side só para feedback enquanto você digita.

## Deploy

O HTML entra no fat jar, então ele acompanha a imagem: commit na `main` → o CI
publica no GHCR → o Watchtower atualiza o container no servidor. Veja
`deploy/docker-compose.yml`.

## Acessar

- **Deploy:** `http://<host>:8080/argilalabapp.html` (por ora só pela tailnet —
  não há autenticação ainda)
- **Local (dev):** `http://localhost:8080/argilalabapp.html`

`API` fica vazio quando servido pelo app (mesma origem, sem CORS); só cai em
`http://localhost:8080` se você abrir o arquivo direto do disco.

## Fluxo

- **Produtos** — criar/editar/duplicar/excluir vão para a API; o SKU
  (`AL-<cat>-<num>[-<tam>]`) e o slug são gerados no servidor.
- **Fotos** — várias por produto, ordenáveis; a primeira é a capa. Imagens
  enviadas do disco são comprimidas no navegador e sobem para `/api/media` no
  momento de salvar o produto.
- **Parâmetros de custo e Loja** — salvam sozinhos (debounce) e recalculam tudo.
- **Importar projeto** — envia um `argilalab.json` (qualquer versão) para
  `POST /api/import`: upsert por SKU, imagens embutidas viram `/api/media`.
- **Site da loja** — *Pré-visualizar* abre o storefront; *Baixar site* gera um
  `index.html` que pode buscar o catálogo vivo (`/api/catalog`) ou levar o
  catálogo embutido (offline).
- **CSV / Catálogo PDF / Etiquetas** — client-side, com os custos da API.
