-- 主粮表
CREATE TABLE IF NOT EXISTS staple_foods (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(256) NOT NULL,
    brand               VARCHAR(128) NOT NULL,
    food_type           VARCHAR(64) NOT NULL,
    target_species      VARCHAR(32) NOT NULL,
    target_breed        VARCHAR(128),
    target_age          VARCHAR(64),
    target_size         VARCHAR(64),
    
    -- 关键营养指标（宠物粮标签标准）
    crude_protein_pct   DECIMAL(5,2) NOT NULL,
    crude_fat_pct       DECIMAL(5,2) NOT NULL,
    crude_ash_pct       DECIMAL(5,2),
    crude_fiber_pct     DECIMAL(5,2),
    moisture_pct        DECIMAL(5,2),
    
    -- 其他信息
    net_weight_g        INT,
    price_yuan          DECIMAL(10,2),
    barcode             VARCHAR(64),
    image_url           VARCHAR(512),
    description         TEXT,
    ingredients         TEXT,
    feeding_guide       TEXT,
    
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE staple_foods IS '主粮库';
COMMENT ON COLUMN staple_foods.name IS '主粮名称';
COMMENT ON COLUMN staple_foods.brand IS '品牌';
COMMENT ON COLUMN staple_foods.food_type IS '主粮类型: COMPLETE_DOG/COMPLETE_CAT/PRESCRIPTION/PUPPY/KITTEN/SENIOR/INDOOR/WEIGHT_CONTROL';
COMMENT ON COLUMN staple_foods.target_species IS '适用物种: DOG/CAT/ALL';
COMMENT ON COLUMN staple_foods.target_breed IS '适用品种';
COMMENT ON COLUMN staple_foods.target_age IS '适用年龄段: PUPPY/ADULT/SENIOR/ALL';
COMMENT ON COLUMN staple_foods.target_size IS '适用体型: SMALL/MEDIUM/LARGE/ALL';
COMMENT ON COLUMN staple_foods.crude_protein_pct IS '粗蛋白质(%)';
COMMENT ON COLUMN staple_foods.crude_fat_pct IS '粗脂肪(%)';
COMMENT ON COLUMN staple_foods.crude_ash_pct IS '粗灰分(%)';
COMMENT ON COLUMN staple_foods.crude_fiber_pct IS '粗纤维(%)';
COMMENT ON COLUMN staple_foods.moisture_pct IS '水分(%)';
COMMENT ON COLUMN staple_foods.barcode IS '条形码';
COMMENT ON COLUMN staple_foods.ingredients IS '配料表';
COMMENT ON COLUMN staple_foods.feeding_guide IS '喂食指南';

CREATE INDEX IF NOT EXISTS idx_staple_foods_brand ON staple_foods(brand);
CREATE INDEX IF NOT EXISTS idx_staple_foods_type ON staple_foods(food_type);
CREATE INDEX IF NOT EXISTS idx_staple_foods_species ON staple_foods(target_species);

-- 食物库表
CREATE TABLE IF NOT EXISTS foods (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(256) NOT NULL,
    icon                VARCHAR(64),
    category            VARCHAR(64) NOT NULL,
    is_staple_food      BOOLEAN NOT NULL DEFAULT FALSE,
    staple_food_id      BIGINT REFERENCES staple_foods(id),
    
    -- 基础核心指标（每100g）
    kcal_per_100g       INT,
    protein_per_100g    DECIMAL(10,2),
    fat_per_100g        DECIMAL(10,2),
    carb_per_100g       DECIMAL(10,2),
    
    -- 微量元素（每100g，mg）
    calcium_mg          DECIMAL(10,2),
    phosphorus_mg       DECIMAL(10,2),
    sodium_mg           DECIMAL(10,2),
    iron_mg             DECIMAL(10,2),
    zinc_mg             DECIMAL(10,2),
    
    -- 其他营养成分
    moisture_pct        DECIMAL(5,2),
    taurine_mg          DECIMAL(10,2),
    omega3_pct          DECIMAL(5,2),
    omega6_pct          DECIMAL(5,2),
    
    description         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE foods IS '食物库';
COMMENT ON COLUMN foods.name IS '食物名称';
COMMENT ON COLUMN foods.icon IS 'emoji图标';
COMMENT ON COLUMN foods.category IS '分类: STAPLE_FOOD/MEAT/VEGETABLE/FRUIT/SNACK/SUPPLEMENT/OTHER';
COMMENT ON COLUMN foods.is_staple_food IS '是否为主粮';
COMMENT ON COLUMN foods.staple_food_id IS '关联主粮表ID';
COMMENT ON COLUMN foods.kcal_per_100g IS '热量(kcal/100g)';
COMMENT ON COLUMN foods.protein_per_100g IS '蛋白质(g/100g)';
COMMENT ON COLUMN foods.fat_per_100g IS '脂肪(g/100g)';
COMMENT ON COLUMN foods.carb_per_100g IS '碳水化合物(g/100g)';
COMMENT ON COLUMN foods.calcium_mg IS '钙(mg/100g)';
COMMENT ON COLUMN foods.phosphorus_mg IS '磷(mg/100g)';
COMMENT ON COLUMN foods.sodium_mg IS '钠(mg/100g)';
COMMENT ON COLUMN foods.iron_mg IS '铁(mg/100g)';
COMMENT ON COLUMN foods.zinc_mg IS '锌(mg/100g)';
COMMENT ON COLUMN foods.moisture_pct IS '水分(%)';
COMMENT ON COLUMN foods.taurine_mg IS '牛磺酸(mg/100g)';
COMMENT ON COLUMN foods.omega3_pct IS 'Omega-3(%)';
COMMENT ON COLUMN foods.omega6_pct IS 'Omega-6(%)';

CREATE INDEX IF NOT EXISTS idx_foods_name ON foods(name);
CREATE INDEX IF NOT EXISTS idx_foods_category ON foods(category);
CREATE INDEX IF NOT EXISTS idx_foods_is_staple ON foods(is_staple_food);

-- 宠物饮食记录表
CREATE TABLE IF NOT EXISTS pet_diet_records (
    id                  BIGSERIAL PRIMARY KEY,
    pet_id              BIGINT NOT NULL,
    food_id             BIGINT REFERENCES foods(id),
    food_name           VARCHAR(256) NOT NULL,
    weight              INT NOT NULL,
    meal_type           VARCHAR(32) NOT NULL,
    meal_time           TIMESTAMP NOT NULL,
    estimated_kcal      INT,
    protein_g           DECIMAL(10,2),
    fat_g               DECIMAL(10,2),
    carb_g              DECIMAL(10,2),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP
);

COMMENT ON TABLE pet_diet_records IS '宠物饮食记录';
COMMENT ON COLUMN pet_diet_records.pet_id IS '宠物ID';
COMMENT ON COLUMN pet_diet_records.food_id IS '关联食物库ID';
COMMENT ON COLUMN pet_diet_records.food_name IS '食物名称（冗余存储）';
COMMENT ON COLUMN pet_diet_records.weight IS '重量(克)';
COMMENT ON COLUMN pet_diet_records.meal_type IS '餐次类型: BREAKFAST/LUNCH/DINNER/SNACK/SUPPLEMENT';
COMMENT ON COLUMN pet_diet_records.meal_time IS '用餐时间';
COMMENT ON COLUMN pet_diet_records.estimated_kcal IS '估算热量(kcal)';
COMMENT ON COLUMN pet_diet_records.protein_g IS '蛋白质(g)';
COMMENT ON COLUMN pet_diet_records.fat_g IS '脂肪(g)';
COMMENT ON COLUMN pet_diet_records.carb_g IS '碳水(g)';
COMMENT ON COLUMN pet_diet_records.deleted_at IS '软删除时间';

CREATE INDEX IF NOT EXISTS idx_diet_records_pet_time ON pet_diet_records(pet_id, meal_time);
CREATE INDEX IF NOT EXISTS idx_diet_records_pet_date ON pet_diet_records(pet_id, DATE(meal_time));
CREATE INDEX IF NOT EXISTS idx_diet_records_food ON pet_diet_records(food_id);

-- 宠物饮水记录表
CREATE TABLE IF NOT EXISTS pet_water_records (
    id                  BIGSERIAL PRIMARY KEY,
    pet_id              BIGINT NOT NULL,
    water_amount        INT NOT NULL,
    record_time         TIMESTAMP NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP
);

COMMENT ON TABLE pet_water_records IS '宠物饮水记录';
COMMENT ON COLUMN pet_water_records.pet_id IS '宠物ID';
COMMENT ON COLUMN pet_water_records.water_amount IS '饮水量(ml)';
COMMENT ON COLUMN pet_water_records.record_time IS '记录时间';
COMMENT ON COLUMN pet_water_records.deleted_at IS '软删除时间';

CREATE INDEX IF NOT EXISTS idx_water_records_pet_time ON pet_water_records(pet_id, record_time);

-- 用户常用食物表
CREATE TABLE IF NOT EXISTS user_frequent_foods (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    food_id             BIGINT NOT NULL REFERENCES foods(id),
    use_count           INT NOT NULL DEFAULT 1,
    last_used_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, food_id)
);

COMMENT ON TABLE user_frequent_foods IS '用户常用食物';
COMMENT ON COLUMN user_frequent_foods.user_id IS '用户ID';
COMMENT ON COLUMN user_frequent_foods.food_id IS '食物ID';
COMMENT ON COLUMN user_frequent_foods.use_count IS '使用次数';
COMMENT ON COLUMN user_frequent_foods.last_used_at IS '最后使用时间';

CREATE INDEX IF NOT EXISTS idx_freq_foods_user_count ON user_frequent_foods(user_id, use_count DESC);
