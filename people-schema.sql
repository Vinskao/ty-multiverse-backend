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
    personally VARCHAR(255),
    main_weapon VARCHAR(255),
    sub_weapon VARCHAR(255),
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
    hei VARCHAR(255),
    HRRatio VARCHAR(255),
    physicsFallout4 VARCHAR(255)
);

CREATE TABLE weapon (
    name VARCHAR(255),
    weapon VARCHAR(255) PRIMARY KEY,
    attributes VARCHAR(255),
    base_damage INT,
    bonus_damage INT,
    bonus_attributes TEXT[],
    state_attributes TEXT[]
);
-- 移除舊的唯一約束
ALTER TABLE weapon DROP CONSTRAINT IF EXISTS weapon_name_key;
-- 添加新的複合唯一約束
ALTER TABLE weapon ADD CONSTRAINT weapon_name_weapon_key UNIQUE (name, weapon);

CREATE TABLE gallery (
    id SERIAL PRIMARY KEY,                 
    image_base64 TEXT NOT NULL,            
    upload_time TIMESTAMP DEFAULT NOW()  
);


CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

ALTER TABLE people ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE gallery ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE users ADD COLUMN version BIGINT DEFAULT 0;

create table ckeditor(
	editor VARCHAR(20) primary key,
	content TEXT
)