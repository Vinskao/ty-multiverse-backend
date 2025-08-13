-- Default schema objects (excluding people and weapon which are defined in people.sql)

-- ======================
-- Table: ckeditor
-- ======================
CREATE TABLE IF NOT EXISTS ckeditor (
    editor VARCHAR(20) PRIMARY KEY,
    content TEXT
);

-- ======================
-- Table: gallery
-- ======================
CREATE TABLE IF NOT EXISTS gallery (
    id SERIAL PRIMARY KEY,
    image_base64 TEXT,
    upload_time TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT
);

-- ======================
-- Table: livestock
-- ======================
CREATE TABLE IF NOT EXISTS livestock (
    id SERIAL PRIMARY KEY,
    livestock VARCHAR(255) NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    melee INT NOT NULL,
    magicka INT NOT NULL,
    ranged INT NOT NULL,
    selling_price NUMERIC(19, 4),
    buying_price NUMERIC(19, 4),
    deal_price NUMERIC(19, 4),
    buyer VARCHAR(255),
    owner VARCHAR(255) NOT NULL,
    version BIGINT
);

CREATE INDEX IF NOT EXISTS idx_livestock_owner ON livestock(owner);
CREATE INDEX IF NOT EXISTS idx_livestock_buyer ON livestock(buyer);
CREATE INDEX IF NOT EXISTS idx_livestock_name ON livestock(livestock);

-- ======================
-- Table: people_image
-- ======================
CREATE TABLE IF NOT EXISTS people_image (
    id VARCHAR(255) PRIMARY KEY,
    version BIGINT,
    "codeName" VARCHAR(255),
    image TEXT
);

CREATE INDEX IF NOT EXISTS idx_people_image_codename ON people_image("codeName");


