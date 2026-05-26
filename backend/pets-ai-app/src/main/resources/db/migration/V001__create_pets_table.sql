-- 宠物档案表
CREATE TABLE IF NOT EXISTS pets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(64) NOT NULL,
    species         VARCHAR(32) NOT NULL,
    breed           VARCHAR(128),
    birthday        DATE,
    weight_kg       DECIMAL(6, 2),
    gender          VARCHAR(16),
    neutered        BOOLEAN DEFAULT FALSE,
    microchipped    BOOLEAN DEFAULT FALSE,
    avatar_url      VARCHAR(512),
    avatar_emoji    VARCHAR(16),
    allergies       VARCHAR(512),
    chronic_conditions VARCHAR(512),
    main_food_brand VARCHAR(256),
    vet_hospital    VARCHAR(256),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP
);

COMMENT ON TABLE pets IS '宠物档案';
COMMENT ON COLUMN pets.user_id IS '所属用户ID';
COMMENT ON COLUMN pets.name IS '宠物昵称';
COMMENT ON COLUMN pets.species IS '宠物类型: dog/cat';
COMMENT ON COLUMN pets.breed IS '品种';
COMMENT ON COLUMN pets.birthday IS '生日';
COMMENT ON COLUMN pets.weight_kg IS '体重(kg)';
COMMENT ON COLUMN pets.gender IS '性别: male/female';
COMMENT ON COLUMN pets.neutered IS '是否绝育';
COMMENT ON COLUMN pets.microchipped IS '是否已打芯片';
COMMENT ON COLUMN pets.avatar_url IS '头像URL';
COMMENT ON COLUMN pets.avatar_emoji IS 'emoji头像';
COMMENT ON COLUMN pets.allergies IS '过敏史';
COMMENT ON COLUMN pets.chronic_conditions IS '慢性疾病';
COMMENT ON COLUMN pets.main_food_brand IS '主食品牌';
COMMENT ON COLUMN pets.vet_hospital IS '常去医院';
COMMENT ON COLUMN pets.notes IS '备注';
COMMENT ON COLUMN pets.deleted_at IS '软删除时间';

CREATE INDEX IF NOT EXISTS idx_pets_user ON pets(user_id);
CREATE INDEX IF NOT EXISTS idx_pets_user_active ON pets(user_id) WHERE deleted_at IS NULL;
