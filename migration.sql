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