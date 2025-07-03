-- Weapon data with single embedding field
-- Note: Embedding field will be populated by application logic

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('sayuri', '斷魔', '闇', 1529, 450, ARRAY['闇','血','氣'], ARRAY['出血', '沉默'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('hitomi', '噬人', '波', 1453, 399, ARRAY['波','光'], ARRAY['擊退'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('chiaki', 'Lord of Anal Beads', '淫', 142, 1840, ARRAY['淫','氣'], ARRAY['無力','噴汁','噴屎','腿軟','噴尿'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('samui', '冰の女王', '冰', 103, 543, ARRAY['冰'], ARRAY['冷凍'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('yuko', 'Goddess Eye', '知',298, 2259, ARRAY['知'], ARRAY['穿刺'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('etsuko', 'Suppressor', '風',1881, 105, ARRAY['風'], ARRAY['爆擊'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('miyako', 'Genade All Rounder', '無',495, 0, ARRAY['無'], ARRAY['炸裂'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('kalidasa', 'The Buddha', '淫',50, 2122, ARRAY['淫','佛'], ARRAY['腿軟','噴汁','噴尿','暈眩','四肢麻痺','屁股抽筋','噴乳汁'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('wavo', 'Giant Penis', '淫',1, 8564, ARRAY['淫'], ARRAY['噴屎','麻痺','崩壞','噴尿','痙攣','屁股抽筋','噴乳汁','暈吐','癱軟','中邪'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('siyu', '龍骨劍', '龍',149,34, ARRAY['龍'], ARRAY['見血'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('siyu', '龍骨盾', '龍',46, 142, ARRAY['龍'], ARRAY['見血'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('mesoyei', '屠宰刀', '血',232, 48, ARRAY['血'], ARRAY['抽乾'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('z', '突擊步槍', '無',55, 0, ARRAY['無'], ARRAY['破洞'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW();

INSERT INTO weapon(name, weapon, attributes, base_damage, bonus_damage, bonus_attributes, state_attributes)
VALUES ('aile', '', '無',55, 0, ARRAY['無'], ARRAY['破洞'])
ON CONFLICT (name, weapon) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    base_damage = EXCLUDED.base_damage,
    bonus_damage = EXCLUDED.bonus_damage,
    bonus_attributes = EXCLUDED.bonus_attributes,
    state_attributes = EXCLUDED.state_attributes,
    updated_at = NOW(); 