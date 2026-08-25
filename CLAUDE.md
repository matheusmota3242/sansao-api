# ARGILA LAB — Claude Code Guide

Catálogo, custos, loja pública e gestão (clientes, pedidos, compras).
O bot de WhatsApp foi removido nesta branch — se você encontrar referência a
WAHA, interações de chat, tarefas, notas, automações ou trackers, é resíduo e
deve sair.

## Stack
- Java 25, Spring Boot 4
- Maven (NÃO Gradle)
- PostgreSQL + migrations Flyway
- MapStruct para mapeamento
- Jackson para serialização

## Convenções de teste
- JUnit 5 via `spring-boot-starter-test` (scope: test)
- Sem @SpringBootTest enquanto não houver Testcontainers — só testes unitários
- Testes em src/test/java espelhando o pacote de main
- Services são testados com Mockito sobre os repositories

## Maven — não invente artifact IDs
A única dependência de teste necessária:
- groupId: org.springframework.boot
- artifactId: spring-boot-starter-test
- scope: test

Artefatos como spring-boot-starter-data-jpa-test não existem. Nunca adicione uma
dependência de teste que já não esteja no pom.xml sem confirmar que ela existe
no Maven Central.

## Idioma: código em inglês, contrato em português

Campos, métodos e colunas são em inglês. O **JSON da API continua em
português**, preso por `@JsonProperty` nos DTOs — o `argilalabapp.html` e os
sites de loja já gerados dependem dessas chaves. Ou seja: `catalogPrice` no
Java e no banco, `"catalogo"` no JSON.

Não remova essas anotações achando que são sobra: apagá-las quebra o frontend
em silêncio, porque nenhum teste cobre aquele HTML. Virar o contrato para
inglês é uma decisão separada, e o frontend tem de ir no mesmo commit.

Português permanece em: mensagens ao usuário, o formato externo `argilalab.json`
(inclusive o prefixo `midia:`) e os códigos de status do produto
(`"ativo"`/`"dev"`/`"off"`, via `@JsonValue`).

## Segurança
- Login por formulário + sessão. `AppUserDetails` carrega o `User` junto para
  que o `AuditorProvider` preencha `created_by`/`updated_by` sem ir ao banco.
- Público é só `GET /api/catalog` e `GET /api/media/{hash}` — o que a loja
  estática consome. O resto exige sessão; custo e margem exigem ADMIN.
- CSRF ligado com cookie legível. Toda escrita precisa do header
  `X-XSRF-TOKEN`; o `api()` e o form de login do admin já mandam.
- `/api/**` responde 401/403 em vez de redirecionar — um `fetch()` seguiria o
  redirect e tentaria ler o HTML de login como JSON. O
  `DelegatingAuthenticationEntryPoint` é montado à mão porque, com um único
  mapeamento, `defaultAuthenticationEntryPointFor` passa a valer para tudo e o
  matcher é ignorado.
- Exceção conhecida: escrita vinda de um cliente **sem cookie nenhum** volta 302
  (o Spring está emitindo o token de CSRF na mesma resposta). A escrita é
  rejeitada igual; com qualquer cookie presente, vem 401.

## Arquitetura

### Catálogo
- `Product` tem SKU derivado: `AL-<category.code>-<num:3>[-<tam>]`, montado por
  `SkuUtil`. A unicidade é garantida por `uq_product_sku (category_id, num, tam)`,
  incluindo linhas inativas — um SKU excluído continua reservado.
- `CostParameters` é uma linha única e global: mexer nela recalcula o custo de
  todos os produtos. O cálculo vive no servidor (`CostCalculatorService`); o
  modal do frontend só faz uma prévia para dar feedback enquanto se digita.
- `Media` é endereçada por conteúdo (SHA-256). Subir a mesma imagem duas vezes
  devolve o mesmo hash e grava os bytes uma vez só.
- **Os bytes ficam em disco**, em `MEDIA_PATH/<2 do hash>/<hash>`; no banco fica
  só o metadado. `MediaStorage` grava de forma atômica e valida que o hash é
  hexadecimal antes de montar caminho — sem isso um `../..` na URL leria
  qualquer arquivo do servidor. O `MediaService` grava o arquivo **antes** da
  linha: linha sem arquivo vira foto quebrada no catálogo.
- `ProductPhoto` é ordenada por `position`; a primeira é a capa.

### Loja pública
- `GET /api/catalog` é o feed lido pelo storefront: produtos publicados +
  `StoreConfig`. É só leitura, e o checkout é link `wa.me` — nenhum dado de
  cliente entra pela API.

### Gestão
- Telas no admin: **Clientes** e **Compras de insumo**, em
  `<details class="painel">` como o resto. O JS vive no mesmo arquivo, na seção
  "gestão", e é carregado por `carregarGestao()` no start.
- `GET /api/me` diz quem está logado; o frontend usa `canSeeCosts` só para não
  desenhar campo que não pode ver. **A resposta da API já omite o dado** — nunca
  confie na tela para esconder dinheiro.
- `CustomerService.resolveByNameOrId` aceita id ou nome: entrada só de dígitos é
  id e falha se não existir; qualquer outra coisa casa por nome (ignorando caixa)
  ou cria um cliente novo.
- Exclusão de cliente é soft delete: mantém o histórico consistente e o cadastro
  some das listas do mesmo jeito.
- Erros de negócio sobem como `ResponseStatusException` com mensagem em
  português, que é o que os controllers do catálogo já fazem.

## Migrations
- `V1__create_schema.sql` é o esquema inteiro: app_user, catálogo, loja e
  gestão. As antigas V1–V12 foram consolidadas nela porque o banco ainda não
  estava populado.
- Daqui em diante, **nunca edite uma migration aplicada** — o checksum do Flyway
  quebra. Adicione uma Vn nova.
- Toda tabela tem `created_by_id`/`updated_by_id` apontando para `app_user`,
  espelhando os campos de `BaseModel`. Ninguém os preenche ainda: falta o login.

## Convenções de nomenclatura
- Classes de teste: `[Classe]Test`
- Mensagens ao usuário em português, código em inglês
- A tabela de usuários é `app_user`, não `user` — USER é palavra reservada
