CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    lft INTEGER,
    rgt INTEGER,
    depth INTEGER,
    active BOOLEAN DEFAULT TRUE,
    listing_allowed BOOLEAN NOT NULL DEFAULT TRUE,
    review_required BOOLEAN NOT NULL DEFAULT FALSE,
    c2c_allowed BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS leftIndex ON category (lft);
CREATE INDEX IF NOT EXISTS depthNameLeftIndex ON category (depth, name, lft);

CREATE TABLE IF NOT EXISTS feature (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    category_id BIGINT
);

CREATE TABLE IF NOT EXISTS feature_option (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    feature_id BIGINT,
    FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS variant_type (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    status VARCHAR(255),
    code VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS variant_option (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    code VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    variant_type_id BIGINT NOT NULL,
    FOREIGN KEY (variant_type_id) REFERENCES variant_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS variant_option_unit (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    variant_type_id BIGINT,
    FOREIGN KEY (variant_type_id) REFERENCES variant_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    category_id VARCHAR(255) NOT NULL,
    listing_condition VARCHAR(255),
    moderation_note VARCHAR(500),
    status VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS media (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    type VARCHAR(255),
    path VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS product_media (
    product_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, media_id),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_description (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(120) NOT NULL,
    title VARCHAR(100),
    description TEXT,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_variant (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL,
    uuid VARCHAR(255) UNIQUE,
    status VARCHAR(255) NOT NULL,
    product_id BIGINT,
    UNIQUE (sku, product_id),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_variant_media (
    variant_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    PRIMARY KEY (variant_id, media_id),
    FOREIGN KEY (variant_id) REFERENCES product_variant(id) ON DELETE CASCADE,
    FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_variant_description (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    name VARCHAR(120) NOT NULL,
    title VARCHAR(100),
    description TEXT,
    product_variant_id BIGINT NOT NULL,
    FOREIGN KEY (product_variant_id) REFERENCES product_variant(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_feature (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    product_variant_id BIGINT,
    feature_id BIGINT,
    feature_option_id BIGINT,
    FOREIGN KEY (product_variant_id) REFERENCES product_variant(id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE,
    FOREIGN KEY (feature_option_id) REFERENCES feature_option(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_variant_option (
    variant_option_id VARCHAR(255) NOT NULL,
    variant_type_id VARCHAR(255) NOT NULL,
    variant_id BIGINT NOT NULL,
    PRIMARY KEY (variant_option_id, variant_type_id, variant_id),
    FOREIGN KEY (variant_id) REFERENCES product_variant(id) ON DELETE CASCADE
);
