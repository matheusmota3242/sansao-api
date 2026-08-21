-- ARGILA LAB v4: product gains storefront/SEO fields, photos become an ordered
-- list, images are stored once (deduped by content hash), and the public store
-- gets its own single-row config.

ALTER TABLE product ADD COLUMN IF NOT EXISTS slug VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS prazo INTEGER;
ALTER TABLE product ADD COLUMN IF NOT EXISTS ordem INTEGER;
ALTER TABLE product ADD COLUMN IF NOT EXISTS material VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS dim_peca VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS emb_peso NUMERIC(12,2);
ALTER TABLE product ADD COLUMN IF NOT EXISTS emb_dim VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS publicado BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE product ADD COLUMN IF NOT EXISTS destaque BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE product ADD COLUMN IF NOT EXISTS desc_longa TEXT;
ALTER TABLE product ADD COLUMN IF NOT EXISTS meta_desc TEXT;
ALTER TABLE product ADD COLUMN IF NOT EXISTS licenca VARCHAR(255);

-- Backfill defaults matching the frontend migrar(): publish what is active,
-- order by the SKU number, standard material/lead time.
-- The column defaults to TRUE, so every existing row would publish; align it
-- with the current status instead (only active products go to the store).
UPDATE product SET publicado = (status = 'ATIVO');
UPDATE product SET ordem = num * 10 WHERE ordem IS NULL;
UPDATE product SET prazo = 5 WHERE prazo IS NULL;
UPDATE product SET material = 'PLA rígido' WHERE material IS NULL;

-- Slug is the storefront URL for a product; unique so links never collide.
CREATE UNIQUE INDEX IF NOT EXISTS uq_product_slug ON product (slug) WHERE slug IS NOT NULL;

-- Image bytes stored once; hash is the content address used by the frontend.
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    hash VARCHAR(64) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    bytes BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Ordered photos per product. url is either an external URL or /api/media/<hash>.
CREATE TABLE product_photo (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_product_photo_product ON product_photo (product_id, position);

-- Existing single foto becomes the first photo of the product.
INSERT INTO product_photo (product_id, url, position, created_at, updated_at)
SELECT id, foto, 0, now(), now() FROM product WHERE foto IS NOT NULL AND foto <> '';

-- Public store config (hero, trust badges, process, faq...): a single row.
-- The repeated blocks are content lists, so they live as JSONB.
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
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO store_config (instagram, whatsapp, frete_gratis, hero_titulo, hero_texto,
                          confianca, processo, faq, rodape, obs_pedido, created_at, updated_at)
VALUES (
    'argila_lab', '', 250,
    'A ferramenta que você desenhou na cabeça e nunca achou pra comprar.',
    'Costelas, calibres, moldes e carimbos feitos sob encomenda, um a um, com precisão de décimo de milímetro. Enviamos para todo o Brasil.',
    '[]'::jsonb, '[]'::jsonb, '[]'::jsonb, '', '', now(), now());
