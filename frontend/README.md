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

### Como a UI fica acessível

O proxy do Coolify **não** é usado neste servidor: não há `coolify-proxy`
rodando e a máquina está atrás de NAT (o IP público é do roteador). Por isso
atribuir um domínio no Coolify não funciona aqui.

Em vez disso, a porta do `app` é publicada direto na **interface da Tailscale**.
No Coolify, em *Environment Variables*, defina:

```
APP_BIND_IP=100.127.213.86      # IP do servidor na tailnet
```

O compose usa `${APP_BIND_IP:-127.0.0.1}:8080:8080`, então:

- com a variável definida → acessível de qualquer aparelho da tailnet;
- sem ela → fica só em `127.0.0.1` (nada exposto por engano).

## Acessar

- **Deploy (tailnet):** `http://100.127.213.86:8080/argilalabapp.html` — de
  qualquer dispositivo logado na tailnet (PC, celular).
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
