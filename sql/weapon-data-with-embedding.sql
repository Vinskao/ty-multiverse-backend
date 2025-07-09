-- Weapon data with single embedding field
-- Note: Embedding field will be populated by application logic

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Sayuri', '斷魔', '闇', 1529, 450, ARRAY['闇','血','氣'], ARRAY['出血', '沉默'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Hitomi', '噬人', '波', 1453, 399, ARRAY['波','光'], ARRAY['擊退'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Chiaki', 'Lord of Anal Beads', '淫', 142, 1840, ARRAY['淫','氣'], ARRAY['無力','噴汁','噴屎','腿軟','噴尿'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Samui', '冰の女王', '冰', 103, 543, ARRAY['冰'], ARRAY['冷凍'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Yuko', 'Goddess Eye', '知',298, 2259, ARRAY['知'], ARRAY['穿刺'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Etsuko', 'Suppressor', '風',1881, 105, ARRAY['風'], ARRAY['爆擊'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Miyako', 'Genade All Rounder', '無',495, 0, ARRAY['無'], ARRAY['炸裂'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Kalidasa', 'The Buddha', '淫',50, 2122, ARRAY['淫','佛'], ARRAY['腿軟','噴汁','噴尿','暈眩','四肢麻痺','屁股抽筋','噴乳汁'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Wavo', 'Giant Penis', '淫',1, 8564, ARRAY['淫'], ARRAY['噴屎','麻痺','崩壞','噴尿','痙攣','屁股抽筋','噴乳汁','暈吐','癱軟','中邪'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Siyu', '龍骨劍', '龍',149,34, ARRAY['龍'], ARRAY['見血'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Siyu', '龍骨盾', '龍',46, 142, ARRAY['龍'], ARRAY['見血'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Mesoyei', '屠宰刀', '血',232, 48, ARRAY['血'], ARRAY['抽乾'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Z', '突擊步槍', '無',55, 0, ARRAY['無'], ARRAY['破洞'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Aile', '', '無',55, 0, ARRAY['無'], ARRAY['破洞'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW(); 

INSERT INTO weapon(owner, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('Sorane', 'The Great Sword', '電',432, 1552, ARRAY['電'], ARRAY['露點','露水','麻痺','噴尿','噴屎','噴乳汁','噴汁','噴精'])
ON CONFLICT (weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();