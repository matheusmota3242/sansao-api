# ARGILA LAB — front conectado à API

`argilalabapp.html` é a versão do app que fala com o backend simao-api
(`/api/products`, `/api/categories`, `/api/cost-parameters`, `/api/import`),
em vez de guardar tudo em memória + `argilalab.json`.

## Configurar

Edite a constante no topo do `<script>`:

```js
const API = "http://localhost:8080";
```

Aponte para onde o backend está (ex.: o host/porta do túnel Tailscale). O
backend libera CORS em `/api/**`.

## Usar

- Abra o `argilalabapp.html` no navegador. Ele carrega categorias, parâmetros e
  produtos da API ao iniciar.
- **Novo produto / Editar / Duplicar / Excluir** → chamam a API e recarregam.
  O SKU (`AL-<cat>-<num>`) é gerado no servidor.
- **Parâmetros de custo** → salvam via `PUT /api/cost-parameters` (debounce) e
  recalculam todos os produtos.
- **Importar projeto** → escolha um `argilalab.json` antigo; ele é enviado para
  `POST /api/import` (upsert idempotente por SKU) para semear/atualizar o banco.
- **CSV / Catálogo PDF / Etiquetas** → seguem client-side, usando os custos já
  computados pela API.

## Notas

- A prévia de custo no modal usa um cálculo client-side que espelha o backend,
  só para dar feedback instantâneo enquanto você digita. O valor persistido/
  exibido na lista é sempre o computado pelo servidor.
