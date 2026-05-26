# PawPal 后端接口规划

本文档按 **登录**、**问 AI 助手（对话）**、**宠物档案**、**饮食记录** 四个产品页面梳理接口需求，并与仓库内 Spring Boot 实现对齐。基础路径相对于 **`{API_BASE}`**（例如 `http://localhost:8081/pet-ai-app`）。成功响应统一为 `ApiResponse<T>`：`{ code, message, success, timestamp, data }`。

**鉴权**：除注册/登录/刷新外，均需请求头 `Authorization: Bearer <access_token>`（与主站 `pets_ai_access_token` 一致）。

**图例**：`✅ 已实现` · `🟡 部分实现 / 占位` · `📋 规划中`

---

## 页面与接口总览

| 页面 | 主要能力 | 涉及接口分组 |
|------|----------|----------------|
| 登录 / 注册 | 邮箱登录、邀请码注册、记住我（前端存储策略）、登出 | §1 认证 |
| 登录后账号 | 个人资料、改密（与「忘记密码」运营流程可分离） | §2 当前用户 |
| 问 AI 助手 | 会话列表、搜索、新对话、消息流、SSE、附件、反馈 | §3～§6 |
| 宠物档案 | 宠物列表、增删改查、头像上传、健康字段 | §7 宠物 |
| 饮食记录 | 按日/周汇总、餐次条目、饮水、趋势图、食物库（可选） | §8 饮食 |

---

## 1. 认证（登录 / 注册页）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `POST` | `/api/v1/auth/register` | Body：`inviteCode`, `username`（昵称）, `email`, `password`（≥8 位） | ✅ |
| `POST` | `/api/v1/auth/login` | Body：`email`, `password`；返回 `accessToken`, `refreshToken`, `expiresIn`, `user` | ✅ |
| `POST` | `/api/v1/auth/refresh` | Body：`refreshToken` | ✅ |
| `POST` | `/api/v1/auth/logout` | Body：`refreshToken`；可选带 `Authorization` 作废 access | ✅ |

**说明**：「记住我」仅为前端选择 `localStorage` / `sessionStorage` 存储 token，**不需要**单独后端接口。

---

## 2. 当前用户（登录后个人账号）

与登录页「忘记密码」配合：若产品走「邮件重置链接」，需后续增加 `POST /api/v1/auth/forgot-password` 等，此处不展开。

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/api/v1/users/me` | 当前用户 `UserInfoDto`：`uid`, `username`, `email`, `avatarUrl`, `plan` | ✅ |
| `PUT` | `/api/v1/users/me` | Body：`username?`, `email?` | ✅ |
| `PUT` | `/api/v1/users/me/password` | Body：`oldPassword`, `newPassword`（≥8 位） | ✅ |

---

## 3. 会话（AI 对话页 · 左侧历史 + 新对话）

| 方法 | 路径 | 查询参数 / Body | 说明 | 状态 |
|------|------|-----------------|------|------|
| `GET` | `/api/v1/chat/listChats` | `page`, `size` | 分组会话列表 `groups[]`（`dateLabel` + `chats[]`） | ✅ |
| `GET` | `/api/v1/chat/search` | **`q`**（必填）, `page`, `size` | 搜索历史 | ✅ |
| `POST` | `/api/v1/chat/createChat` | `{ title?, model?, petId? }` | 新对话；**`petId` 建议与档案联动**（见 §7），当前实现可忽略该字段 | ✅ / 📋 `petId` |
| `GET` | `/api/v1/chat/{chatId}` | — | 会话摘要 | ✅ |
| `PUT` | `/api/v1/chat/{chatId}` | `{ title?, model? }` | 重命名等 | ✅ |
| `DELETE` | `/api/v1/chat/{chatId}` | — | 删除会话 | ✅ |

**`ChatSummaryDto`（摘要）**：`id`, `title`, `updatedAt`。若列表需展示「最后一条预览」，需扩展字段（如 `lastMessagePreview`）。

#### 3.1 会话绑定 `petId`（规划中）：表结构示例

若会话持久化在表 **`chats`**（或等价名）中，可增加可空外键列，与 §7 `pets.id` 关联：

```sql
-- 示例：在已有会话表上增加列（表名、主键类型以实际为准）
ALTER TABLE chats
  ADD COLUMN pet_id BIGINT UNSIGNED NULL COMMENT '可选，关联 pets.id' AFTER user_id,
  ADD KEY idx_chats_pet (pet_id);
-- 若需外键且库支持：
-- ALTER TABLE chats ADD CONSTRAINT fk_chats_pet FOREIGN KEY (pet_id) REFERENCES pets (id);
```

```sql
-- 示例：将某会话关联到宠物 1（仅演示）
UPDATE chats SET pet_id = 1 WHERE id = 'chat-demo-001' LIMIT 1;
```

---

## 4. 消息（AI 对话页 · 主区域）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/api/v1/chats/{chatId}/messages` | `cursor?`, `size`；响应 `messages`, `nextCursor`, `hasMore` | ✅ |
| `POST` | `/api/v1/chats/{chatId}/messages` | Body：`{ content, attachmentIds[]? }`；同步返回助手消息 | ✅ |
| `POST` | `/api/v1/chats/{chatId}/messages/stream` | 同上；**SSE**（`Accept: text/event-stream`） | ✅ |

### 4.1 SSE 事件约定

| event | data（JSON） | 含义 |
|-------|----------------|------|
| `message_start` | `{ messageId, role, model }` | 助手消息开始 |
| `content_delta` | `{ delta }` | 增量正文 |
| `message_end` | `{ messageId, finishReason }` | 结束 |
| `error` | `{ code, message }` | 流错误 |

### 4.2 `MessageDto` 字段

`id`, `role`（`USER` / `ASSISTANT` / `SYSTEM`）, `content`, `codeBlock`, `feedbackType`, `model`, `createdAt`。结构化卡片若需独立块，可后续增加 `blocks` / `metadata`。

---

## 5. 文件（对话页附件）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `POST` | `/api/v1/files/upload` | `multipart/form-data`，字段名 **`file`** → `id`, `name`, `contentType`, `fileSize`, `status` | ✅ |
| `GET` | `/api/v1/files/{fileId}` | 元数据 | ✅ |
| `GET` | `/api/v1/files/{fileId}/download` | 下载 | ✅ |
| `DELETE` | `/api/v1/files/{fileId}` | 删除 | ✅ |

发消息时将返回的 **`id`** 填入 `attachmentIds`。

---

## 6. 反馈（对话页点赞 / 点踩，可选）

| 方法 | 路径 | Body | 状态 |
|------|------|------|------|
| `POST` | `/api/v1/chats/{chatId}/messages/{msgId}/feedback` | `{ type }` | 📋 按产品开关 |
| `DELETE` | 同上 | 移除反馈 | 📋 |

**反馈 Body（建议）**：`type` — `like` / `dislike`（或 `UP` / `DOWN`）。

#### 6.1 建表与示例数据（规划中）

```sql
CREATE TABLE chat_message_feedback (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  chat_id         VARCHAR(64) NOT NULL,
  message_id      VARCHAR(64) NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  feedback_type   VARCHAR(32) NOT NULL COMMENT 'like/dislike 等',
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_chat_msg_user (chat_id, message_id, user_id),
  KEY idx_feedback_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单条消息反馈';
```

```sql
INSERT INTO chat_message_feedback (chat_id, message_id, user_id, feedback_type)
VALUES ('chat-demo-001', 'msg-1001', 1, 'like');
```

---

## 7. 宠物档案（`pawpal-web.html` · `page-register`）

原型字段：**基础信息**（头像 emoji 或照片、昵称、类型、品种、生日、体重、性别、绝育、芯片）、**健康信息**（过敏史、慢性疾病、主食品牌、常去医院、备注）。

### 7.1 已实现

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/api/v1/pets` | 当前用户宠物列表 | 🟡 返回空数组占位，待 `PetSummaryDto` 真实数据 |

### 7.2 规划接口（建议）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/v1/pets` | 创建档案；Body 见下表 **PetUpsert** |
| `GET` | `/api/v1/pets/{petId}` | 单条详情（含健康信息） |
| `PUT` | `/api/v1/pets/{petId}` | 更新档案 |
| `DELETE` | `/api/v1/pets/{petId}` | 删除（需二次确认，前端已预留交互） |
| `POST` | `/api/v1/pets/{petId}/avatar` | 可选：`multipart/form-data` 上传照片；或与统一文件服务结合，返回 `avatarUrl` 写入宠物 |
| `GET` | `/api/v1/users/me/current-pet` | 可选：查询/设置「当前选中宠物」（顶栏、对话上下文）；也可用 `localStorage` + `petId` 仅前端 |

**PetUpsert / PetDetail（建议字段）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | string | 昵称，必填 |
| `species` | enum | `dog` / `cat` / `rabbit` / `hamster` / `bird` / `fish` / `other` 等，与原型下拉一致 |
| `breed` | string? | 品种 |
| `birthday` | date | 生日 |
| `weightKg` | number? | 体重 |
| `gender` | enum? | `male` / `female` |
| `neutered` | boolean? | 是否绝育 |
| `microchipped` | boolean? | 是否已打芯片 |
| `avatarUrl` | string? | 照片 URL；若为 emoji 可用 `avatarEmoji` 或约定前缀 |
| `allergies` | string? | 过敏史 |
| `chronicConditions` | string? | 慢性疾病 |
| `mainFoodBrand` | string? | 主食品牌 |
| `vetHospital` | string? | 常去医院 |
| `notes` | string? | 备注 |

#### 7.3 建表与示例数据（MySQL 8 示例，可按实际库调整）

以下为与上表字段对齐的 **`pets`** 表；`user_id` 需与现有用户表主键类型一致（示例假定 `BIGINT`）。

```sql
CREATE TABLE pets (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT UNSIGNED NOT NULL COMMENT '所属用户 ID',
  name            VARCHAR(64) NOT NULL COMMENT '昵称',
  species         VARCHAR(32) NOT NULL COMMENT 'dog/cat/rabbit/hamster/bird/fish/other',
  breed           VARCHAR(128) NULL,
  birthday        DATE NULL,
  weight_kg       DECIMAL(6, 2) NULL,
  gender          VARCHAR(16) NULL COMMENT 'male/female',
  neutered        TINYINT(1) NULL COMMENT '是否绝育 0/1',
  microchipped    TINYINT(1) NULL COMMENT '是否芯片 0/1',
  avatar_url      VARCHAR(512) NULL,
  avatar_emoji    VARCHAR(16) NULL COMMENT 'emoji 头像，可与 avatar_url 并存',
  allergies       VARCHAR(512) NULL,
  chronic_conditions VARCHAR(512) NULL,
  main_food_brand VARCHAR(256) NULL,
  vet_hospital    VARCHAR(256) NULL,
  notes           TEXT NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      DATETIME NULL COMMENT '软删除',
  KEY idx_pets_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物档案';
```

```sql
-- 假定已有用户 id=1；插入两只宠物示例
INSERT INTO pets (user_id, name, species, breed, birthday, weight_kg, gender, neutered, microchipped, avatar_emoji, allergies, chronic_conditions, main_food_brand, vet_hospital, notes)
VALUES
(1, '小橘子', 'dog', '柴犬', '2024-01-15', 5.20, 'male', 1, 1, '🐕', NULL, NULL, '皇家柴犬专用粮', '博爱动物医院', '活泼，喜欢追球。'),
(1, '咪咪', 'cat', '英短', '2023-06-01', 4.50, 'female', 1, 0, '🐈', '鱼类', NULL, NULL, NULL, NULL);
```

**可选：当前选中宠物（对应 `GET/PATCH .../users/me/current-pet`）**

```sql
CREATE TABLE user_pet_current (
  user_id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  pet_id  BIGINT UNSIGNED NOT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_upc_pet FOREIGN KEY (pet_id) REFERENCES pets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_pet_current (user_id, pet_id) VALUES (1, 1);
```

---

## 8. 饮食记录（`pawpal-web.html` · `page-diet`）

原型能力：**按宠物 + 日期** 展示三餐/零食/营养品的条目列表、**当日营养摘要**（热量、蛋白/脂/碳水、目标进度）、**饮水**、**近 7 天热量趋势**、添加/编辑单条记录（食物名、重量 g、时间、餐次类型）、快速食物 chip、（后续）扫码。

### 8.1 规划接口（建议统一前缀）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/v1/pets/{petId}/diet/days/{date}` | `date` = `yyyy-MM-dd`；返回当日 **日汇总** + **按餐次分组**的条目列表 |
| `POST` | `/api/v1/pets/{petId}/diet/entries` | 新增一条饮食记录；Body 见 **DietEntryCreate** |
| `PUT` | `/api/v1/pets/{petId}/diet/entries/{entryId}` | 更新 |
| `DELETE` | `/api/v1/pets/{petId}/diet/entries/{entryId}` | 删除 |
| `GET` | `/api/v1/pets/{petId}/diet/summary/week` | `from`, `to` 或 `weekOffset`；返回每日总热量等，供 **近7天柱状图** |
| `PUT` | `/api/v1/pets/{petId}/diet/water` | Body：`date`, `amountMl`；当日饮水（若按日一条） |
| `GET` | `/api/v1/pets/{petId}/diet/targets` | 可选：每日目标热量（如按体重/物种计算），供进度条 |

**DietEntryCreate / DietEntryDto（建议字段）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `mealSlot` | enum | `breakfast` / `lunch` / `dinner` / `snack` / `supplement` 与原型餐次一致 |
| `foodName` | string | 食物名称 |
| `amountG` | number | 重量（克） |
| `recordedAt` | time 或 datetime | 用餐时间 |
| `estimatedKcal` | number? | 服务端或规则引擎估算热量；前端可先传估算值校验 |
| `proteinG` / `fatG` / `carbG` | number? | 可选宏量，便于展示「今日营养摘要」 |

**日汇总 DTO（建议）**：`date`, `totalKcal`, `proteinG`, `fatG`, `carbG`, `targetKcal`, `waterMl`, `streakDays?`（连续记录天数），`entriesByMeal[]`。

### 8.2 可选扩展

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/v1/foods/search` | `q` 食物名称搜索（快速 chip、自动完成） |
| `POST` | `/api/v1/pets/{petId}/diet/analyze` | Body：当日或单条 entry，返回 AI 营养分析文案（与原型「AI 营养分析」按钮对应） |

#### 8.3 建表与示例数据（MySQL 8 示例）

**单条饮食记录 `pet_diet_entries`**（宏量与热量可服务端计算后写入，便于列表直接汇总）。

```sql
CREATE TABLE pet_diet_entries (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id          BIGINT UNSIGNED NOT NULL,
  meal_slot       VARCHAR(32) NOT NULL COMMENT 'breakfast/lunch/dinner/snack/supplement',
  food_name       VARCHAR(256) NOT NULL,
  amount_g        DECIMAL(10, 2) NOT NULL,
  recorded_at     DATETIME NOT NULL COMMENT '用餐时间（含日期）',
  estimated_kcal  DECIMAL(10, 2) NULL,
  protein_g       DECIMAL(10, 2) NULL,
  fat_g           DECIMAL(10, 2) NULL,
  carb_g          DECIMAL(10, 2) NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_diet_pet_time (pet_id, recorded_at),
  CONSTRAINT fk_diet_pet FOREIGN KEY (pet_id) REFERENCES pets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食明细';
```

**按日饮水与目标 `pet_diet_day_stats`**（与 `PUT .../diet/water`、`targets` 对应；`streakDays` 建议查询时计算）。

```sql
CREATE TABLE pet_diet_day_stats (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id       BIGINT UNSIGNED NOT NULL,
  stat_date    DATE NOT NULL COMMENT '本地日',
  water_ml     INT NULL,
  target_kcal  INT NULL COMMENT '当日目标热量',
  UNIQUE KEY uk_pet_day (pet_id, stat_date),
  CONSTRAINT fk_day_pet FOREIGN KEY (pet_id) REFERENCES pets (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食日维：饮水与目标';
```

```sql
-- 假定 pet_id=1；2026-04-07 当天两条记录 + 日统计
INSERT INTO pet_diet_day_stats (pet_id, stat_date, water_ml, target_kcal)
VALUES (1, '2026-04-07', 280, 480);

INSERT INTO pet_diet_entries (pet_id, meal_slot, food_name, amount_g, recorded_at, estimated_kcal, protein_g, fat_g, carb_g)
VALUES
(1, 'breakfast', '皇家柴犬粮', 80.00, '2026-04-07 07:30:00', 130.00, 18.00, 4.50, 12.00),
(1, 'breakfast', '胡萝卜条', 20.00, '2026-04-07 08:00:00', 32.00, 0.60, 0.10, 6.00),
(1, 'lunch', '鸡胸肉（熟）', 50.00, '2026-04-07 12:00:00', 85.00, 16.00, 2.00, 0.00),
(1, 'lunch', '皇家柴犬粮', 60.00, '2026-04-07 12:00:00', 97.00, 14.00, 3.50, 10.00);
```

---

## 9. 通知与其它（侧栏角标、顶栏）

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| `GET` | `/api/v1/notifications` | 未读数、疫苗提醒等（侧栏「3天」类角标） | 📋 视产品优先级 |

**通知 DTO（建议字段）**：`id`, `title`, `body`, `type`, `readAt`, `createdAt`。

#### 9.1 建表与示例数据（规划中）

```sql
CREATE TABLE notifications (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT UNSIGNED NOT NULL,
  title       VARCHAR(255) NOT NULL,
  body        TEXT NULL,
  type        VARCHAR(32) NULL COMMENT 'reminder/system/...',
  read_at     DATETIME NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notif_user_unread (user_id, read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';
```

```sql
INSERT INTO notifications (user_id, title, body, type, read_at)
VALUES
(1, '疫苗提醒', '狗狗狂犬疫苗将在 3 天后到期，记得预约。', 'reminder', NULL),
(1, '系统通知', '欢迎使用 PawPal 饮食记录功能。', 'system', '2026-04-06 10:00:00');
```

---

## 10. 环境变量（前端）

- `VITE_API_BASE_URL`：与网关一致（默认示例：`http://localhost:8081/pet-ai-app`）。
- React 工程：[frontend-pet/web](web)（Vite + TypeScript）。复制 `web/.env.example` 为 `web/.env` 后执行 `npm run dev`。

---

## 11. 实现状态汇总（便于排期）

| 模块 | 状态 |
|------|------|
| 认证 `auth` | ✅ |
| 当前用户 `users/me` | ✅ |
| 会话 `chat` + 消息 `messages` + SSE | ✅ |
| 文件 `files` | ✅ |
| 宠物列表 `GET /pets` | 🟡 空列表占位 |
| 宠物 CRUD、头像 | 📋 |
| 对话绑定 `petId` | 📋 |
| 饮食记录 `diet/*` | 📋 |
| 消息反馈、通知 | 📋 |

---

*文档版本：覆盖 `pawpal-web.html` 四页面与 `frontend-pet/web` 接入场景；具体实现以 `backend` 源码为准。*
