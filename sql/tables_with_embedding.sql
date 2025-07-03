-- Enable pgvector extension for vector operations
CREATE EXTENSION IF NOT EXISTS vector;

-- People table with single embedding field
CREATE TABLE people (
    name_original VARCHAR(255),
    code_name VARCHAR(255),
    name VARCHAR(255) PRIMARY KEY,
    physic_power INT,
    magic_power INT,
    utility_power INT,
    dob DATE,
    race VARCHAR(255),
    attributes VARCHAR(255),
    gender VARCHAR(255),
    ass_size VARCHAR(255),
    boobs_size VARCHAR(255),
    height_cm INT,
    weight_kg INT,
    profession VARCHAR(255),
    combat VARCHAR(255),
    favorite_foods VARCHAR(255),
    job VARCHAR(255),
    physics VARCHAR(255),
    known_as VARCHAR(255),
    personality VARCHAR(255),
    interest VARCHAR(255),
    likes VARCHAR(255),
    dislikes VARCHAR(255),
    concubine VARCHAR(255),
    faction VARCHAR(255),
    army_id INT,
    army_name VARCHAR(255),
    dept_id INT,
    dept_name VARCHAR(255),
    origin_army_id INT,
    origin_army_name VARCHAR(255),
    gave_birth BOOLEAN,
    email VARCHAR(255),
    age INT,
    proxy VARCHAR(255),
    -- Single embedding field for all search
    embedding VECTOR(1536),           -- OpenAI text-embedding-ada-002 dimension
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Weapon table with single embedding field
CREATE TABLE weapon (
    name VARCHAR(255),
    weapon VARCHAR(255),
    attributes VARCHAR(255),
    base_damage INT,
    bonus_damage INT,
    bonus_attributes TEXT[],
    state_attributes TEXT[],
    -- Single embedding field for all search
    embedding VECTOR(1536),           -- OpenAI text-embedding-ada-002 dimension
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    -- Primary key and constraints
    PRIMARY KEY (name, weapon)
);

-- Create indexes for people table
CREATE INDEX idx_people_code_name ON people(code_name);
CREATE INDEX idx_people_race ON people(race);
CREATE INDEX idx_people_gender ON people(gender);
CREATE INDEX idx_people_faction ON people(faction);
CREATE INDEX idx_people_army_id ON people(army_id);
CREATE INDEX idx_people_dept_id ON people(dept_id);

-- Create indexes for weapon table
CREATE INDEX idx_weapon_name ON weapon(name);
CREATE INDEX idx_weapon_weapon ON weapon(weapon);
CREATE INDEX idx_weapon_base_damage ON weapon(base_damage);
CREATE INDEX idx_weapon_bonus_damage ON weapon(bonus_damage);

-- Create vector indexes for embedding search
CREATE INDEX idx_people_embedding ON people USING ivfflat (embedding vector_cosine_ops);
CREATE INDEX idx_weapon_embedding ON weapon USING ivfflat (embedding vector_cosine_ops);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_people_updated_at 
    BEFORE UPDATE ON people 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_weapon_updated_at 
    BEFORE UPDATE ON weapon 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE people IS 'People table with vector embedding support for semantic search';
COMMENT ON COLUMN people.embedding IS 'Vector embedding of all people fields for semantic search';
COMMENT ON COLUMN people.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN people.updated_at IS 'Timestamp when the record was last updated';

COMMENT ON TABLE weapon IS 'Weapon table with vector embedding support for semantic search';
COMMENT ON COLUMN weapon.embedding IS 'Vector embedding of all weapon fields for semantic search';
COMMENT ON COLUMN weapon.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN weapon.updated_at IS 'Timestamp when the record was last updated'; 