# ARGILA LAB — frontend

O app (`argilalabapp.html`) fala com o backend simao-api
(`/api/products`, `/api/categories`, `/api/cost-parameters`, `/api/import`).

## Onde ele vive

O arquivo é servido pelo **próprio backend**, como recurso estático em
`src/main/resources/static/argilalabapp.html`. Ele entra no fat jar que o
`deploy/Dockerfile` compila — ou seja, **deploya junto com o app**, sem
container extra. Nada a configurar no compose: o serviço `app` já o serve na
porta 8080.

## Deploy (Coolify)

O Coolify builda o `app` a partir de `deploy/Dockerfile` (ver
`docker-compose.coolify.yml`). Fluxo: commit na `master` → *Redeploy* no Coolify.
Como o build vem do repo, o front vai junto automaticamente.

Para acessar a UI de fora, **atribua um domínio ao serviço `app`** no Coolify
(porta 8080), do mesmo jeito que o `waha` tem. Aí:
`https://<dominio-do-app>/argilalabapp.html` — e a API responde na mesma origem
(`/api/...`), sem CORS.

## Acessar

- **Deploy:** `https://<dominio-do-app>/argilalabapp.html`.
- **Local (dev):** com `docker compose up` rodando, abra
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
