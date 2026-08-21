-- ARGILA LAB catalog: product catalog + cost calculator backend.
-- Category is an editable lookup; cost_parameters is a single global row that
-- drives every product's computed cost; product holds the raw fields (SKU is
-- derived as AL-<category.code>-<num:3>[-<tam>]).

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

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
    active BOOLEAN NOT NULL DEFAULT TRUE
);

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
    foto TEXT,
    origem TEXT,
    impressora VARCHAR(255),
    filamento VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    -- num+tam is the SKU suffix; keep it unique per category (inactive rows
    -- included, so a deleted SKU stays reserved).
    CONSTRAINT uq_product_sku UNIQUE (category_id, num, tam)
);

CREATE INDEX idx_product_category ON product (category_id);

-- Seed the 9 cost parameters (single row) with the app's defaults.
INSERT INTO cost_parameters
    (fil_preco, potencia, tarifa, deprec, mdo, acresc, markup, comissao, taxa_fixa, created_at, updated_at)
VALUES
    (89.00, 0.150, 0.75, 0.58, 10.00, 10, 2.0, 25.5, 4.00, now(), now());

-- Seed the 5 categories from the app.
INSERT INTO category (code, name, created_at, updated_at) VALUES
    ('MOL', 'Moldes e Formas', now(), now()),
    ('COR', 'Corte e Laminação', now(), now()),
    ('TEX', 'Texturas e Carimbos', now(), now()),
    ('MOD', 'Modelagem e Acabamento', now(), now()),
    ('EQP', 'Equipamentos e Suportes', now(), now());
