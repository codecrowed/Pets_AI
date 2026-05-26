# PawPal 前端技术文档

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| UI 框架 | React | ^19.2.4 |
| 语言 | TypeScript | ~6.0.2 |
| 构建工具 | Vite | ^8.0.4 |
| 样式 | 纯 CSS + CSS Variables | — |
| Markdown 渲染 | react-markdown + remark-gfm + remark-breaks | ^10.1.0 |
| HTTP 通信 | 原生 Fetch API | — |
| 流式通信 | 自定义 SSE 解析器 (ReadableStream) | — |
| 代码检查 | ESLint 9 + typescript-eslint | — |

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────┐
│                  App.tsx                      │
│  (全局状态: auth, pets, page, toast)          │
├─────────────────────────────────────────────┤
│              AppShell.tsx                     │
│  (布局: 侧边栏导航 + 内容区)                   │
├──────────┬──────────┬───────────────────────┤
│ AskAiPage│PetProfile│   DietRecordPage      │
│  (对话)   │  (档案)   │   (饮食)              │
├──────────┴──────────┴───────────────────────┤
│              lib/ (API 层)                    │
│  api-client → auth-api / chat-api / pet-api  │
└─────────────────────────────────────────────┘
```

### 路由方案

项目采用**状态驱动路由**，不使用 React Router。`App.tsx` 通过 `useState<PageName>` 管理当前页面：

```typescript
type PageName = "chat" | "register" | "diet";
```

页面切换通过 `AppShell` 侧边栏导航回调触发，不依赖 URL 变化。

**设计考量：** 作为单一功能聚焦的 SPA，页面数量有限且无需 SEO，状态路由避免了路由库依赖。

### 状态管理

采用 React 原生状态管理，不引入全局状态库：

| 状态层级 | 管理方式 | 典型数据 |
|----------|----------|----------|
| 全局 | `App.tsx` state + props 下传 | `user`, `pets[]`, `currentPage`, `activePet` |
| 页面 | 各页面组件内部 state | 对话消息、表单数据、日期选择 |
| 持久化 | localStorage / sessionStorage | Token、当前宠物 ID、记住登录 |

**跨组件通信：**
- 父子：Callback Props
- 命令式操作：`forwardRef` + `useImperativeHandle`（如 `PetProfilePage.openAddModal()`）
- 全局事件：`CustomEvent("pawpal-toast")` 用于 Toast 通知

### 组件职责划分

| 组件 | 类型 | 职责 |
|------|------|------|
| `App.tsx` | 容器组件 | 鉴权守卫、全局状态编排、页面路由 |
| `AppShell.tsx` | 布局组件 | 侧边栏、移动端覆盖层、导航逻辑 |
| `TopBar.tsx` | 共享 UI | 页面标题、宠物切换、菜单按钮 |
| `LoginPage.tsx` | 页面组件 | 登录/注册表单 |
| `AskAiPage.tsx` | 页面组件 | 对话 UI、历史管理、流式响应 |
| `PetProfilePage.tsx` | 页面组件 | 宠物卡片、编辑模态框、头像上传 |
| `DietRecordPage.tsx` | 页面组件 | 饮食/饮水记录、统计图表、AI 分析 |
| `MarkdownMessage.tsx` | 展示组件 | Markdown 渲染（AI 回复） |

## API 通信层

### 核心设计

所有 HTTP 请求通过 `lib/api-client.ts` 统一处理：

```typescript
// 统一响应信封
interface ApiResponse<T> {
  code: number;
  message: string;
  success: boolean;
  timestamp: number;
  data: T;
}

// 请求头注入
function authHeaders(): Headers {
  // Authorization: Bearer <token>
  // X-Pet-Id: <currentPetId>
}

// 响应解包
async function apiJson<T>(res: Response): Promise<T> {
  // 提取 data 字段，异常时抛出错误
}
```

### API 模块

| 模块 | 文件 | 核心端点 |
|------|------|----------|
| 认证 | `auth-api.ts` | login, register, logout |
| 对话 | `chat-api.ts` | createChat, listChats, sendMessage, stream |
| 文件 | `file-api.ts` | upload (multipart) |
| 宠物 | `pet-api.ts` | CRUD, avatar presigned-url, confirm |
| 饮食 | `diet-api.ts` | diet-records, water-records, diet-stats, foods, ai-analysis |

### SSE 流式通信

AI 对话响应采用 Server-Sent Events 实现实时流式输出：

```typescript
// sse-parse.ts - 手动解析 SSE 流
// 不使用 EventSource API，而是通过 fetch + ReadableStream 实现
// 优势：支持 POST 请求、自定义 Headers、更好的错误处理

async function sendMessageStream(chatId, content, onChunk, onDone) {
  const res = await fetch(url, { method: "POST", headers: authHeaders(), body });
  const reader = res.body.getReader();
  // 逐块解析 SSE data 字段，回调 onChunk
}
```

### 文件上传流程

**头像上传（OSS 直传）：**
1. 前端请求 presigned URL → `GET /pets/{id}/avatar/presigned-url`
2. 前端 PUT 文件至 OSS URL
3. 前端确认上传完成 → `POST /pets/{id}/avatar/confirm`

**聊天附件：**
- 直接 multipart 上传至后端 → `POST /files/upload`

## 样式系统

### 设计 Token

`index.css` 定义了完整的 CSS Variables 设计系统（约 3400+ 行）：

```css
:root {
  --color-primary: #ff6b35;       /* 主色：橙色 */
  --color-primary-hover: #e55a2b;
  --color-bg-main: #faf8f5;       /* 背景：暖白 */
  --color-sidebar-bg: #2d2d2d;    /* 侧边栏：深色 */
  --radius-md: 12px;
  --shadow-card: 0 2px 8px rgba(0,0,0,0.08);
  /* ... */
}
```

### 设计原则

- **暖色系**：橙色主题 + 暖棕辅助色，契合宠物应用定位
- **无框架依赖**：纯 CSS 实现全部 UI，保持包体积最小
- **响应式**：媒体查询适配移动端，侧边栏可收起
- **深色侧边栏**：导航区与内容区形成视觉层次

## 本地存储策略

| Key | 存储位置 | 用途 |
|-----|----------|------|
| `pets_ai_access_token` | localStorage / sessionStorage | JWT 访问令牌 |
| `pets_ai_refresh_token` | localStorage / sessionStorage | JWT 刷新令牌 |
| `pets_ai_user_json` | localStorage / sessionStorage | 缓存用户信息 |
| `pawpal_remember_me` | localStorage | 记住登录偏好 |
| `pawpal_current_pet_id` | localStorage | 当前选中宠物 ID |

"记住登录" 开启时使用 `localStorage`（持久），否则使用 `sessionStorage`（会话级）。

## 构建与编译配置

### TypeScript 配置

```json
{
  "compilerOptions": {
    "target": "ES2023",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "verbatimModuleSyntax": true
  }
}
```

### Vite 配置

最小化配置，仅启用 React 插件：

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()]
})
```

### ESLint 配置

ESLint 9 Flat Config，集成：
- `typescript-eslint` — TypeScript 规则
- `eslint-plugin-react-hooks` — Hooks 规则
- `eslint-plugin-react-refresh` — HMR 兼容性检查

## 依赖清单

### 生产依赖

| 包名 | 版本 | 用途 |
|------|------|------|
| `react` | ^19.2.4 | UI 框架 |
| `react-dom` | ^19.2.4 | DOM 渲染 |
| `react-markdown` | ^10.1.0 | AI 消息 Markdown 渲染 |
| `remark-gfm` | ^4.0.1 | GitHub Flavored Markdown 支持 |
| `remark-breaks` | ^4.0.0 | 单换行转 `<br>` |

### 开发依赖

| 包名 | 用途 |
|------|------|
| `vite` | 开发服务器 & 构建工具 |
| `@vitejs/plugin-react` | React 热更新 |
| `typescript` | 类型检查 |
| `eslint` + 插件 | 代码规范 |
| `@types/react`, `@types/react-dom` | 类型定义 |

## 性能考量

- **零 UI 框架**：不引入 Ant Design / MUI，打包体积极小
- **按需渲染**：状态路由避免未激活页面的 DOM 挂载
- **流式响应**：ReadableStream 逐块渲染 AI 回复，首字延迟低
- **防抖搜索**：`useDebouncedValue` 减少搜索频率
- **预构建产物**：`dist/` 可直接部署，无需 CI 构建

## 已知限制与后续规划

| 项目 | 现状 | 规划 |
|------|------|------|
| URL 路由 | 无 URL 路由，不支持浏览器后退/刷新保持页面 | 可引入 React Router |
| 全局状态 | Props 传递链较长 | 可引入 Zustand 或 Context |
| 错误边界 | 无 ErrorBoundary | 建议添加 |
| 国际化 | 仅中文 | 可引入 i18n 方案 |
| 测试 | 无单元/E2E 测试 | 建议引入 Vitest + Playwright |
| 条码扫描 | 饮食页标记"开发中" | 待实现 |
| 健康中心 | 侧边栏入口仅 Toast 提示 | 待实现 |
