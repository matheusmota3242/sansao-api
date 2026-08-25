-- Esquema inicial do ARGILA LAB: usuários do admin, catálogo, loja pública e
-- gestão (clientes, fila de impressão, compras de insumo).
--
-- Consolidada a partir das antigas V1–V12, que criavam também o esquema do bot
-- de WhatsApp e depois o derrubavam. O banco ainda não está populado, então vale
-- reescrever a história; a partir daqui, migration aplicada nunca mais se edita
-- (o checksum do Flyway quebra) — sempre uma Vn nova.

-- ---------------------------------------------------------------- usuários --
-- app_user, não user: USER é palavra reservada no Postgres.
-- created_by_id/updated_by_id apontam para a própria tabela; o primeiro usuário
-- não tem quem o crie, por isso as colunas são nulas.
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,      -- hash BCrypt
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------- catálogo --
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Linha única e global: mexer aqui recalcula o custo de todos os produtos.
CREATE TABLE cost_parameters (
    id BIGSERIAL PRIMARY KEY,
    fil_preco NUMERIC(12,4) NOT NULL,   -- filamento R$/kg
    potencia NUMERIC(12,4) NOT NULL,    -- kW
    tarifa NUMERIC(12,4) NOT NULL,      -- R$/kWh
    deprec NUMERIC(12,4) NOT NULL,      -- depreciacao R$/h
    mdo NUMERIC(12,4) NOT NULL,         -- mao de obra R$/h
    acresc NUMERIC(12,4) NOT NULL,      -- acrescimo %
    markup NUMERIC(12,4) NOT NULL,      -- markup x
    comissao NUMERIC(12,4) NOT NULL,    -- comissao marketplace %
    taxa_fixa NUMERIC(12,4) NOT NULL,   -- taxa fixa R$
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- O SKU é derivado: AL-<category.code>-<num:3>[-<tam>].
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES category(id),
    num INTEGER NOT NULL,
    tam VARCHAR(20) NOT NULL DEFAULT '',
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    obs VARCHAR(255),
    gram NUMERIC(12,2),
    tempo_horas NUMERIC(12,4),
    trab_min NUMERIC(12,2),
    insumos NUMERIC(12,2),
    embalagem NUMERIC(12,2),
    catalogo_preco NUMERIC(12,2),
    tempo_exato BOOLEAN NOT NULL DEFAULT TRUE,
    -- Foto legada de quando o produto tinha uma imagem só; as atuais vivem em
    -- product_photo. Mantida porque a importação de projetos antigos ainda a lê.
    foto TEXT,
    origem TEXT,
    impressora VARCHAR(255),
    filamento VARCHAR(255),
    -- Campos de loja/SEO
    slug VARCHAR(255),
    prazo INTEGER,                       -- dias
    ordem INTEGER,                       -- ordenação na loja
    material VARCHAR(255),
    dim_peca VARCHAR(255),
    emb_peso NUMERIC(12,2),
    emb_dim VARCHAR(255),
    publicado BOOLEAN NOT NULL DEFAULT TRUE,
    destaque BOOLEAN NOT NULL DEFAULT FALSE,
    desc_longa TEXT,
    meta_desc TEXT,
    licenca VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    -- num+tam é o sufixo do SKU; único por categoria incluindo linhas inativas,
    -- para que um SKU excluído continue reservado.
    CONSTRAINT uq_product_sku UNIQUE (category_id, num, tam)
);

CREATE INDEX idx_product_category ON product (category_id);

-- Slug é a URL do produto na loja; único para que os links nunca colidam.
CREATE UNIQUE INDEX uq_product_slug ON product (slug) WHERE slug IS NOT NULL;

-- Bytes guardados uma vez só; hash é o endereço de conteúdo (SHA-256) usado
-- pelo frontend.
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(64) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    bytes BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Fotos ordenadas por produto; a primeira é a capa. url é uma URL externa ou
-- /api/media/<hash>.
CREATE TABLE product_photo (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_product_photo_product ON product_photo (product_id, position);

-- Configuração da loja pública (hero, confiança, processo, faq...): linha única.
-- Os blocos repetidos são listas de conteúdo, por isso JSONB.
CREATE TABLE store_config (
    id BIGSERIAL PRIMARY KEY,
    instagram VARCHAR(255),
    whatsapp VARCHAR(50),
    frete_gratis NUMERIC(12,2),
    hero_titulo TEXT,
    hero_texto TEXT,
    confianca JSONB,
    processo JSONB,
    faq JSONB,
    rodape TEXT,
    obs_pedido TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------------ gestão --
CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- print_order e não order: ORDER é palavra reservada em SQL.
CREATE TABLE print_order (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    print_time_minutes INTEGER,
    -- Posição na fila, 1..n e contígua entre os pedidos ainda nela
    -- (WAITING/RUNNING). NULL quando o pedido sai da fila
    -- (COMPLETED/CANCELLED), para que trabalho encerrado não ocupe posição.
    priority INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    production_cost NUMERIC(12, 2),
    sale_price NUMERIC(12, 2),
    -- Preenchido quando o pedido vai para RUNNING, limpo se voltar a WAITING.
    started_at TIMESTAMP,
    observations TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_print_order_queue ON print_order (status, priority);
CREATE INDEX idx_print_order_customer ON print_order (customer_id);

CREATE TABLE purchase (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    amount INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    source TEXT,
    purchased_at DATE,
    observations TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------------ seeds ---
-- Os 9 parâmetros de custo (linha única) com os padrões do app.
INSERT INTO cost_parameters
    (fil_preco, potencia, tarifa, deprec, mdo, acresc, markup, comissao, taxa_fixa, created_at, updated_at)
VALUES
    (89.00, 0.150, 0.75, 0.58, 10.00, 10, 2.0, 25.5, 4.00, now(), now());

-- As 5 categorias do app.
INSERT INTO category (code, name, created_at, updated_at) VALUES
    ('MOL', 'Moldes e Formas', now(), now()),
    ('COR', 'Corte e Laminação', now(), now()),
    ('TEX', 'Texturas e Carimbos', now(), now()),
    ('MOD', 'Modelagem e Acabamento', now(), now()),
    ('EQP', 'Equipamentos e Suportes', now(), now());

-- Configuração inicial da loja (linha única).
INSERT INTO store_config (instagram, whatsapp, frete_gratis, hero_titulo, hero_texto,
                          confianca, processo, faq, rodape, obs_pedido, created_at, updated_at)
VALUES (
    'argila_lab', '', 250,
    'A ferramenta que você desenhou na cabeça e nunca achou pra comprar.',
    'Costelas, calibres, moldes e carimbos feitos sob encomenda, um a um, com precisão de décimo de milímetro. Enviamos para todo o Brasil.',
    '[]'::jsonb, '[]'::jsonb, '[]'::jsonb, '', '', now(), now());
