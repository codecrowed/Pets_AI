# PawPal 宠物 AI 助手 — 前端项目

## 项目简介

PawPal 是一款面向宠物主人的智能助手 Web 应用，提供 AI 对话、宠物档案管理、饮食记录与营养分析等核心功能。前端基于 React 19 + TypeScript + Vite 构建，采用纯 CSS 自定义设计系统，无第三方 UI 框架依赖。

## 功能概览

| 模块 | 功能描述 |
|------|----------|
| **AI 对话** | 与 AI 助手实时对话（SSE 流式响应），支持 Markdown 渲染、文件附件、对话历史管理 |
| **宠物档案** | 宠物信息 CRUD（品种、生日、体重、健康状况等），支持头像上传（OSS 直传） |
| **饮食记录** | 每日餐食/饮水记录、营养摄入统计、周热量趋势图、AI 饮食分析 |
| **用户认证** | 登录/注册（邀请码机制）、记住登录状态、JWT Token 管理 |

## 快速开始

### 环境要求

- Node.js >= 18
- npm >= 9

### 安装与运行

```bash
cd frontend-pet/web

# 安装依赖
npm install

# 复制环境配置
cp .env.example .env

# 启动开发服务器
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`。

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `VITE_API_BASE_URL` | 后端 API 地址 | `http://localhost:8081/pet-ai-app` |
| `VITE_PET_NAME` | 展示用宠物名称 | `小橘子` |

### 可用脚本

| 命令 | 说明 |
|------|------|
| `npm run dev` | 启动 Vite 开发服务器（HMR） |
| `npm run build` | TypeScript 编译 + Vite 生产构建 |
| `npm run preview` | 本地预览生产构建 |
| `npm run lint` | ESLint 代码检查 |

## 项目结构

```
frontend-pet/
├── backend_api.md              # 后端 API 接口文档
├── README.md                   # 项目文档（本文件）
├── TECHNICAL.md                # 技术文档
└── web/                        # 主应用目录
    ├── index.html              # HTML 入口
    ├── package.json            # 依赖与脚本
    ├── vite.config.ts          # Vite 配置
    ├── tsconfig.json           # TypeScript 项目引用
    ├── tsconfig.app.json       # 应用 TS 配置
    ├── tsconfig.node.json      # Node TS 配置
    ├── eslint.config.js        # ESLint 配置
    ├── .env.example            # 环境变量模板
    ├── public/                 # 静态资源
    ├── dist/                   # 构建产物
    └── src/
        ├── main.tsx            # 应用入口
        ├── App.tsx             # 根组件（路由、全局状态、鉴权）
        ├── index.css           # 全局样式与设计 Token
        ├── components/         # 页面与 UI 组件
        │   ├── AppShell.tsx    # 布局容器（侧边栏）
        │   ├── TopBar.tsx      # 顶部导航栏
        │   ├── LoginPage.tsx   # 登录/注册页
        │   ├── AskAiPage.tsx   # AI 对话页
        │   ├── PetProfilePage.tsx  # 宠物档案页
        │   ├── DietRecordPage.tsx  # 饮食记录页
        │   └── MarkdownMessage.tsx # Markdown 消息渲染
        ├── hooks/              # 自定义 Hooks
        │   └── useDebouncedValue.ts
        └── lib/                # 工具库与 API 层
            ├── api-client.ts   # HTTP 请求核心
            ├── auth-api.ts     # 认证 API
            ├── auth-storage.ts # Token 本地存储
            ├── auth-keys.ts    # 存储 Key 常量
            ├── chat-api.ts     # 对话 API
            ├── chat-types.ts   # 对话类型定义
            ├── file-api.ts     # 文件上传 API
            ├── sse-parse.ts    # SSE 流解析器
            ├── pet-api.ts      # 宠物 API
            ├── pet-types.ts    # 宠物类型定义
            ├── pet-storage.ts  # 宠物本地存储
            ├── diet-api.ts     # 饮食 API
            └── format.ts       # 格式化工具
```

## 部署

### 构建生产版本

```bash
cd frontend-pet/web
npm run build
```

构建产物输出至 `web/dist/` 目录，可部署到任意静态资源服务器（Nginx、CDN 等）。

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name pawpal.example.com;
    root /path/to/frontend-pet/web/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /pet-ai-app/ {
        proxy_pass http://backend-server:8081;
    }
}
```

## 协作约定

- 代码风格由 ESLint 统一管控，提交前请运行 `npm run lint`
- TypeScript 严格模式：`noUnusedLocals`、`noUnusedParameters`
- 组件文件使用 PascalCase 命名，工具文件使用 kebab-case 命名
- API 层统一通过 `lib/api-client.ts` 发起请求，禁止组件内直接 `fetch`
