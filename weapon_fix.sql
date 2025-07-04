-- 修復 weapon 表結構
-- 確保使用複合主鍵 (name, weapon) 而不是單一 ID 欄位

-- 1. 檢查當前表結構
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'weapon' 
ORDER BY ordinal_position;

-- 2. 如果存在 id 欄位，移除它
ALTER TABLE weapon DROP COLUMN IF EXISTS id;

-- 3. 確保複合主鍵存在
-- 先移除可能存在的舊主鍵約束
ALTER TABLE weapon DROP CONSTRAINT IF EXISTS weapon_pkey;
ALTER TABLE weapon DROP CONSTRAINT IF EXISTS weapon_name_weapon_key;

-- 4. 添加複合主鍵約束
ALTER TABLE weapon ADD CONSTRAINT weapon_pkey PRIMARY KEY (name, weapon);

-- 5. 驗證表結構
SELECT 
    tc.constraint_name, 
    tc.constraint_type, 
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'weapon' 
    AND tc.constraint_type = 'PRIMARY KEY';

-- 6. 顯示最終表結構
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'weapon' 
ORDER BY ordinal_position; 