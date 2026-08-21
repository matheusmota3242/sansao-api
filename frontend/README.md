# ARGILA LAB — frontend

O app (`argilalabapp.html`) fala com o backend simao-api
(`/api/products`, `/api/categories`, `/api/cost-parameters`, `/api/import`).

## Onde ele vive

O arquivo é servido pelo **próprio backend**, como recurso estático em
`src/main/resources/static/argilalabapp.html`. Ele entra no fat jar (via
`deploy/Dockerfile`) e é publicado no GHCR pelo CI — ou seja, **deploya junto
com o app**, sem container extra. Não há nada a configurar no docker-compose:
o serviço `app` já o serve na porta 8080.

## Acessar

- **No notebook (deploy):** `http://<host>:8080/argilalabapp.html`. Como as portas
  ficam em `127.0.0.1`, exponha pela tailnet com `tailscale serve 8080` e acesse
  `https://rodrigo.tail….ts.net/argilalabapp.html`.
- **Local (dev):** com o `docker compose up` rodando, abra
  `http://localhost:8080/argilalabapp.html`.

A constante `API` no topo do `<script>` fica vazia quando servido pelo app
(mesma origem, sem CORS). Só cai em `http://localhost:8080` se você abrir o
arquivo direto do disco (`file://`).

## Fluxo

- **Novo / Editar / Duplicar / Excluir** → chamam a API; o SKU (`AL-<cat>-<num>`)
  é gerado no servidor.
- **Parâmetros de custo** → `PUT /api/cost-parameters` (debounce) e recálculo de
  todos os produtos.
- **Importar projeto** → envia um `argilalab.json` antigo para
  `POST /api/import` (upsert idempotente por SKU) e semeia/atualiza o banco.
- **CSV / Catálogo PDF / Etiquetas** → client-side, usando os custos já
  computados pela API.
