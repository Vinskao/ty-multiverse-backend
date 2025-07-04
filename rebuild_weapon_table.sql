-- 重建 weapon 表 - 只使用 name 作為主鍵
-- 請先備份現有數據！

-- 1. 刪除現有的 weapon 表
DROP TABLE IF EXISTS weapon CASCADE;

-- 2. 重新創建 weapon 表
CREATE TABLE weapon (
    name VARCHAR(255) PRIMARY KEY,
    weapon_type VARCHAR(255),
    attributes VARCHAR(255),
    base_damage INT,
    bonus_damage INT,
    bonus_attributes TEXT[],
    state_attributes TEXT[],
    -- Single embedding field for all search
    embedding VECTOR(1536),           -- OpenAI text-embedding-ada-002 dimension
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 3. 創建索引
CREATE INDEX idx_weapon_weapon_type ON weapon(weapon_type);
CREATE INDEX idx_weapon_base_damage ON weapon(base_damage);
CREATE INDEX idx_weapon_bonus_damage ON weapon(bonus_damage);

-- 4. 創建向量索引
CREATE INDEX idx_weapon_embedding ON weapon USING ivfflat (embedding vector_cosine_ops);

-- 5. 創建觸發器
CREATE TRIGGER update_weapon_updated_at 
    BEFORE UPDATE ON weapon 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 6. 添加註釋
COMMENT ON TABLE weapon IS 'Weapon table with vector embedding support for semantic search';
COMMENT ON COLUMN weapon.embedding IS 'Vector embedding of all weapon fields for semantic search';
COMMENT ON COLUMN weapon.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN weapon.updated_at IS 'Timestamp when the record was last updated';

-- 7. 驗證表結構
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'weapon' 
ORDER BY ordinal_position;

-- 8. 驗證主鍵
SELECT 
    tc.constraint_name, 
    tc.constraint_type, 
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'weapon' 
    AND tc.constraint_type = 'PRIMARY KEY'; 