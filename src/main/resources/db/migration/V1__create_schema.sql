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
    -- ADMIN vê e edita custos; OPERATOR mexe em produtos, pedidos e clientes
    -- sem enxergar margem. Sem DEFAULT: todo insert declara o papel.
    role VARCHAR(20) NOT NULL,
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
    filament_price_per_kg NUMERIC(12,4) NOT NULL,   -- R$/kg
    power_kw NUMERIC(12,4) NOT NULL,
    energy_rate NUMERIC(12,4) NOT NULL,             -- R$/kWh
    depreciation_per_hour NUMERIC(12,4) NOT NULL,   -- R$/h
    labor_per_hour NUMERIC(12,4) NOT NULL,          -- R$/h
    surcharge_pct NUMERIC(12,4) NOT NULL,           -- %
    markup NUMERIC(12,4) NOT NULL,
    marketplace_commission_pct NUMERIC(12,4) NOT NULL,
    fixed_fee NUMERIC(12,4) NOT NULL,               -- R$
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
    size VARCHAR(20) NOT NULL DEFAULT '',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    observations VARCHAR(255),
    grams NUMERIC(12,2),
    print_time_hours NUMERIC(12,4),
    labor_minutes NUMERIC(12,2),
    supplies NUMERIC(12,2),
    packaging NUMERIC(12,2),
    catalog_price NUMERIC(12,2),
    exact_time BOOLEAN NOT NULL DEFAULT TRUE,
    -- Foto legada de quando o produto tinha uma imagem só; as atuais vivem em
    -- product_photo. Mantida porque a importação de projetos antigos ainda a lê.
    photo TEXT,
    origin TEXT,
    printer VARCHAR(255),
    filament VARCHAR(255),
    -- Campos de loja/SEO
    slug VARCHAR(255),
    lead_time_days INTEGER,
    sort_order INTEGER,
    material VARCHAR(255),
    part_dimensions VARCHAR(255),
    package_weight NUMERIC(12,2),
    package_dimensions VARCHAR(255),
    published BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    long_description TEXT,
    meta_description TEXT,
    license VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT REFERENCES app_user(id),
    updated_by_id BIGINT REFERENCES app_user(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    -- num+tam é o sufixo do SKU; único por categoria incluindo linhas inativas,
    -- para que um SKU excluído continue reservado.
    CONSTRAINT uq_product_sku UNIQUE (category_id, num, size)
);

CREATE INDEX idx_product_category ON product (category_id);

-- Slug é a URL do produto na loja; único para que os links nunca colidam.
CREATE UNIQUE INDEX uq_product_slug ON product (slug) WHERE slug IS NOT NULL;

-- Metadado da imagem; hash é o endereço de conteúdo (SHA-256) usado pelo
-- frontend. Os bytes ficam em disco, em <MEDIA_PATH>/<2 do hash>/<hash> — no
-- banco eles inflariam o dump sem ganho nenhum.
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(64) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
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
    free_shipping_from NUMERIC(12,2),
    hero_title TEXT,
    hero_text TEXT,
    trust_badges JSONB,
    process JSONB,
    faq JSONB,
    footer TEXT,
    order_notes TEXT,
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
    (filament_price_per_kg, power_kw, energy_rate, depreciation_per_hour, labor_per_hour,
     surcharge_pct, markup, marketplace_commission_pct, fixed_fee, created_at, updated_at)
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
INSERT INTO store_config (instagram, whatsapp, free_shipping_from, hero_title, hero_text,
                          trust_badges, process, faq, footer, order_notes, created_at, updated_at)
VALUES (
    'argila_lab', '', 250,
    'A ferramenta que você desenhou na cabeça e nunca achou pra comprar.',
    'Costelas, calibres, moldes e carimbos feitos sob encomenda, um a um, com precisão de décimo de milímetro. Enviamos para todo o Brasil.',
    '[]'::jsonb, '[]'::jsonb, '[]'::jsonb, '', '', now(), now());
