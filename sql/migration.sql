-- 遷移腳本：更新 people 表結構
-- 將 personally 字段改名為 personality
-- 移除不需要的字段

-- 1. 重命名 personally 字段為 personality
ALTER TABLE people RENAME COLUMN personally TO personality;

-- 2. 移除不需要的字段
ALTER TABLE people DROP COLUMN IF EXISTS hei;
ALTER TABLE people DROP COLUMN IF EXISTS "HRRatio";
ALTER TABLE people DROP COLUMN IF EXISTS "physicsFallout4";
ALTER TABLE people DROP COLUMN IF EXISTS version;

-- 3. 驗證更改
-- 檢查表結構是否正確
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'people' 
ORDER BY ordinal_position;

-- Migration script to add embedding fields to existing tables

-- Enable pgvector extension if not already enabled
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding and timestamp fields to people table
ALTER TABLE people ADD COLUMN IF NOT EXISTS embedding VECTOR(1536);
ALTER TABLE people ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE people ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- Add embedding and timestamp fields to weapon table
ALTER TABLE weapon ADD COLUMN IF NOT EXISTS embedding VECTOR(1536);
ALTER TABLE weapon ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE weapon ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- Create vector indexes for embedding search
CREATE INDEX IF NOT EXISTS idx_people_embedding ON people USING ivfflat (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_weapon_embedding ON weapon USING ivfflat (embedding vector_cosine_ops);

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
DROP TRIGGER IF EXISTS update_people_updated_at ON people;
CREATE TRIGGER update_people_updated_at 
    BEFORE UPDATE ON people 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_weapon_updated_at ON weapon;
CREATE TRIGGER update_weapon_updated_at 
    BEFORE UPDATE ON weapon 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 