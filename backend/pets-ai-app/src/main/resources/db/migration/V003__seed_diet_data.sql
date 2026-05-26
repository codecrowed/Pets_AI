-- 主粮库种子数据
INSERT INTO staple_foods (name, brand, food_type, target_species, target_breed, target_age, target_size, 
    crude_protein_pct, crude_fat_pct, crude_ash_pct, crude_fiber_pct, moisture_pct, description) VALUES
-- 皇家 Royal Canin
('皇家柴犬成犬粮', '皇家Royal Canin', 'COMPLETE_DOG', 'DOG', '柴犬', 'ADULT', 'MEDIUM', 25.00, 14.00, 6.50, 2.80, 10.00, '专为柴犬成犬设计，支持皮肤健康和理想体重'),
('皇家小型犬成犬粮', '皇家Royal Canin', 'COMPLETE_DOG', 'DOG', NULL, 'ADULT', 'SMALL', 26.00, 16.00, 6.00, 2.50, 10.00, '小型犬专用配方，高能量密度'),
('皇家英短成猫粮', '皇家Royal Canin', 'COMPLETE_CAT', 'CAT', '英国短毛猫', 'ADULT', 'ALL', 34.00, 17.00, 7.50, 4.00, 8.00, '专为英短设计，支持心脏健康'),
('皇家室内猫粮', '皇家Royal Canin', 'INDOOR', 'CAT', NULL, 'ADULT', 'ALL', 32.00, 14.00, 7.00, 5.00, 8.00, '室内猫专用，控制体重，减少毛球'),

-- 渴望 Orijen
('渴望六种鱼全犬粮', '渴望Orijen', 'COMPLETE_DOG', 'DOG', NULL, 'ALL', 'ALL', 38.00, 18.00, 8.00, 4.00, 12.00, '六种新鲜鱼类，高蛋白无谷配方'),
('渴望鸡肉全犬粮', '渴望Orijen', 'COMPLETE_DOG', 'DOG', NULL, 'ALL', 'ALL', 38.00, 18.00, 8.00, 5.00, 12.00, '新鲜鸡肉配方，仿生自然饮食'),
('渴望六种鱼全猫粮', '渴望Orijen', 'COMPLETE_CAT', 'CAT', NULL, 'ALL', 'ALL', 40.00, 20.00, 8.00, 3.00, 10.00, '六种野生捕捞鱼类，高蛋白配方'),

-- 爱肯拿 Acana
('爱肯拿草原盛宴全犬粮', '爱肯拿Acana', 'COMPLETE_DOG', 'DOG', NULL, 'ALL', 'ALL', 33.00, 17.00, 7.00, 5.00, 12.00, '草原家禽配方，新鲜肉类'),
('爱肯拿海洋盛宴全犬粮', '爱肯拿Acana', 'COMPLETE_DOG', 'DOG', NULL, 'ALL', 'ALL', 33.00, 17.00, 7.00, 5.00, 12.00, '野生捕捞海鱼配方'),
('爱肯拿草原盛宴全猫粮', '爱肯拿Acana', 'COMPLETE_CAT', 'CAT', NULL, 'ALL', 'ALL', 37.00, 20.00, 7.50, 3.00, 10.00, '草原家禽配方，高蛋白'),

-- 纽顿 Nutram
('纽顿T25无谷三文鱼全犬粮', '纽顿Nutram', 'COMPLETE_DOG', 'DOG', NULL, 'ALL', 'ALL', 30.00, 18.00, 9.00, 5.00, 10.00, '三文鱼和鳟鱼配方，Omega脂肪酸丰富'),
('纽顿S5成犬粮', '纽顿Nutram', 'COMPLETE_DOG', 'DOG', NULL, 'ADULT', 'ALL', 26.00, 16.00, 8.00, 4.00, 10.00, '鸡肉和燕麦配方，均衡营养'),

-- 比瑞吉 Bridge
('比瑞吉全价成犬粮', '比瑞吉Bridge', 'COMPLETE_DOG', 'DOG', NULL, 'ADULT', 'ALL', 26.00, 14.00, 10.00, 5.00, 10.00, '鸡肉配方，高性价比'),
('比瑞吉全价成猫粮', '比瑞吉Bridge', 'COMPLETE_CAT', 'CAT', NULL, 'ADULT', 'ALL', 32.00, 14.00, 10.00, 5.00, 10.00, '鸡肉和鱼肉配方'),

-- 幼犬粮/幼猫粮
('皇家小型犬幼犬粮', '皇家Royal Canin', 'PUPPY', 'DOG', NULL, 'PUPPY', 'SMALL', 29.00, 20.00, 7.00, 2.20, 10.00, '小型犬幼犬专用，支持免疫系统发育'),
('渴望幼犬粮', '渴望Orijen', 'PUPPY', 'DOG', NULL, 'PUPPY', 'ALL', 38.00, 20.00, 8.00, 5.00, 12.00, '高蛋白幼犬配方，支持骨骼发育'),
('皇家幼猫粮', '皇家Royal Canin', 'KITTEN', 'CAT', NULL, 'PUPPY', 'ALL', 36.00, 18.00, 7.50, 3.20, 8.00, '幼猫专用，支持免疫系统'),

-- 处方粮
('皇家低敏水解蛋白处方粮', '皇家Royal Canin', 'PRESCRIPTION', 'DOG', NULL, 'ALL', 'ALL', 22.00, 14.00, 7.00, 2.00, 10.00, '食物过敏和不耐受犬专用'),
('皇家肾脏处方粮', '皇家Royal Canin', 'PRESCRIPTION', 'CAT', NULL, 'ALL', 'ALL', 27.00, 22.00, 5.50, 1.80, 8.00, '肾脏疾病猫专用，低磷配方');

-- 食物库种子数据（主粮关联）
INSERT INTO foods (name, icon, category, is_staple_food, staple_food_id, kcal_per_100g, protein_per_100g, fat_per_100g, carb_per_100g, description)
SELECT 
    sf.name,
    CASE 
        WHEN sf.target_species = 'DOG' THEN '🐕'
        WHEN sf.target_species = 'CAT' THEN '🐈'
        ELSE '🐾'
    END,
    'STAPLE_FOOD',
    TRUE,
    sf.id,
    CASE 
        WHEN sf.target_species = 'DOG' THEN 350
        ELSE 380
    END,
    sf.crude_protein_pct,
    sf.crude_fat_pct,
    GREATEST(0, 100 - sf.crude_protein_pct - sf.crude_fat_pct - COALESCE(sf.crude_ash_pct, 8) - COALESCE(sf.moisture_pct, 10)),
    sf.description
FROM staple_foods sf;

-- 食物库种子数据（普通食材）
INSERT INTO foods (name, icon, category, is_staple_food, kcal_per_100g, protein_per_100g, fat_per_100g, carb_per_100g, calcium_mg, phosphorus_mg, description) VALUES
-- 肉类
('鸡胸肉（熟）', '🍗', 'MEAT', FALSE, 165, 31.00, 3.60, 0.00, 15.00, 228.00, '高蛋白低脂肪，宠物最佳蛋白来源之一'),
('鸡腿肉（熟）', '🍗', 'MEAT', FALSE, 209, 26.00, 10.90, 0.00, 12.00, 185.00, '蛋白质丰富，口感好'),
('牛肉（熟）', '🥩', 'MEAT', FALSE, 250, 26.00, 15.00, 0.00, 12.00, 200.00, '富含铁和锌，高蛋白'),
('羊肉（熟）', '🍖', 'MEAT', FALSE, 294, 25.00, 21.00, 0.00, 12.00, 168.00, '温补，适合体质虚弱的宠物'),
('猪里脊（熟）', '🥓', 'MEAT', FALSE, 143, 27.00, 3.50, 0.00, 5.00, 230.00, '瘦肉，蛋白质丰富'),
('鸭肉（熟）', '🦆', 'MEAT', FALSE, 201, 23.50, 11.20, 0.00, 10.00, 170.00, '富含B族维生素'),
('三文鱼（熟）', '🐟', 'MEAT', FALSE, 208, 20.00, 13.40, 0.00, 15.00, 215.00, '富含Omega-3脂肪酸，对皮毛有益'),
('鳕鱼（熟）', '🐟', 'MEAT', FALSE, 105, 23.00, 0.90, 0.00, 18.00, 175.00, '低脂肪高蛋白，易消化'),
('金枪鱼（熟）', '🐟', 'MEAT', FALSE, 144, 30.00, 1.00, 0.00, 12.00, 254.00, '高蛋白，但不宜过多食用'),
('虾仁（熟）', '🦐', 'MEAT', FALSE, 99, 24.00, 0.30, 0.00, 52.00, 265.00, '高蛋白低脂肪，富含钙'),
('鸡蛋（全熟）', '🥚', 'MEAT', FALSE, 155, 12.60, 10.60, 1.12, 50.00, 172.00, '完美蛋白来源，营养均衡'),
('鸡肝（熟）', '🫀', 'MEAT', FALSE, 167, 24.50, 6.30, 0.90, 8.00, 314.00, '富含维生素A和铁，每周1-2次为宜'),

-- 蔬菜
('胡萝卜（熟）', '🥕', 'VEGETABLE', FALSE, 41, 0.90, 0.20, 9.60, 33.00, 35.00, '富含β-胡萝卜素，对视力有益'),
('南瓜（熟）', '🎃', 'VEGETABLE', FALSE, 26, 1.00, 0.10, 6.50, 21.00, 44.00, '高纤维，有助消化'),
('西兰花（熟）', '🥦', 'VEGETABLE', FALSE, 35, 2.80, 0.40, 7.20, 47.00, 66.00, '富含维生素C和纤维'),
('红薯（熟）', '🍠', 'VEGETABLE', FALSE, 86, 1.60, 0.10, 20.10, 30.00, 47.00, '复杂碳水来源，富含纤维'),
('菠菜（熟）', '🥬', 'VEGETABLE', FALSE, 23, 2.90, 0.40, 3.60, 136.00, 56.00, '富含铁和维生素K，少量食用'),
('黄瓜', '🥒', 'VEGETABLE', FALSE, 16, 0.70, 0.10, 3.60, 16.00, 24.00, '水分高，适合夏季补水'),
('生菜', '🥬', 'VEGETABLE', FALSE, 15, 1.40, 0.20, 2.90, 36.00, 29.00, '低卡路里，高水分'),

-- 水果（少量）
('苹果（去核）', '🍎', 'FRUIT', FALSE, 52, 0.30, 0.20, 13.80, 6.00, 11.00, '富含纤维和维生素C，去核后食用'),
('蓝莓', '🫐', 'FRUIT', FALSE, 57, 0.70, 0.30, 14.50, 6.00, 12.00, '富含抗氧化物质'),
('香蕉', '🍌', 'FRUIT', FALSE, 89, 1.10, 0.30, 22.80, 5.00, 22.00, '富含钾，适量食用'),
('西瓜', '🍉', 'FRUIT', FALSE, 30, 0.60, 0.20, 7.60, 7.00, 11.00, '高水分，适合夏季'),

-- 营养品/零食
('冻干鸡肉', '🍖', 'SNACK', FALSE, 380, 85.00, 5.00, 0.00, 25.00, 350.00, '高蛋白零食，适合训练奖励'),
('冻干三文鱼', '🐟', 'SNACK', FALSE, 400, 75.00, 12.00, 0.00, 20.00, 280.00, '富含Omega-3'),
('鱼油', '💊', 'SUPPLEMENT', FALSE, 900, 0.00, 100.00, 0.00, 0.00, 0.00, 'Omega-3补充剂，改善皮毛'),
('钙片', '💊', 'SUPPLEMENT', FALSE, 0, 0.00, 0.00, 0.00, 400.00, 200.00, '钙质补充，按需使用'),
('益生菌', '💊', 'SUPPLEMENT', FALSE, 5, 0.00, 0.00, 1.00, 0.00, 0.00, '肠道健康，改善消化'),
('卵磷脂', '💊', 'SUPPLEMENT', FALSE, 500, 5.00, 40.00, 30.00, 0.00, 0.00, '改善皮毛，促进脂肪代谢');
