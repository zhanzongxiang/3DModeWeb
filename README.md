# 3D Model Hub - 3D 模型分享平台

## 1. 产品需求文档 (PRD)
本项目是一个高性能的 3D 模型分享社区，允许用户上传和检索 3D 模型资源。
### 核心功能
* **用户模块**：注册、登录、个人中心。
* **模型发布**：用户可主动上传模型数据。
    * 字段包括：展示图片、模型类型、模型名称、作品名、网盘链接（主推夸克网盘分享链接）。
    * 收费设置：用户可勾选模型是“免费”或“付费/需要某种条件”。
* **模型检索**：列表页支持按“类型”、“名称”、“作品名”进行多维度的**模糊查询**。
* **安全与并发保障**：需要防范爬虫撞库（Credential Stuffing）及突发的高并发访问。

## 2. 架构设计与技术栈
本项目采用前后端分离架构，结合分布式缓存应对高并发，并建立纵深防御体系保障数据安全。
* * **前端 (Frontend)**：
  * 框架：Vue 3 (Composition API) + Vite + TypeScript
  * 状态管理 & 路由：Pinia + Vue Router
  * **UI/UX 与视觉风格 (极致致敬 MakerWorld)：**
    * **核心布局（新增）**：采用类似 MakerWorld 的左右分栏结构。左侧是一个包含 Logo、图标导航、底部社交链接和页脚的可收缩侧边栏（Sidebar/Nav Rail），支持展开与收起；右侧为主内容区。
    * **页面特定逻辑（新增）**：
      * **“推荐页”（首页）**：顶部必须有一个突出的模型 Banner（类似截图中的 Parrot Puzzle 示例），下方是模型列表。
      * **“所有其他页面”**（如分类页、搜索页）：直接展示模型列表，无需顶部的 Banner。
    * **视觉规范**：白色或极浅灰色（bg-gray-50）背景，大圆角卡片（Card UI），大量的留白，悬浮时的柔和阴影，干净现代。
  * 样式库：Tailwind CSS。
* **后端 (Backend)**：
    * 核心框架：Java 17/21 + Spring Boot 3
    * 安全防范：Spring Security + JWT
    * ORM 与持久层：MyBatis-Plus (极简实现多条件动态模糊查询)
    * 数据库：MySQL 8.x
* **高并发 & 防撞库中间件**：
    * 缓存与限流：Redis (用于高频首页列表缓存、接口限流 Rate Limiting、登录失败次数锁定机制)
* **对象存储**：
    * 标准 S3 兼容 OSS (如 MinIO、阿里云 OSS 或腾讯云 COS) - 用于存储模型展示图片

## 3. 核心机制设计
* **防撞库机制**：前端接入行为验证码（如 Cloudflare Turnstile 或 极验），后端利用 Redis 记录单 IP / 单账号的错误尝试次数，触发阈值自动锁定并限制请求。
* **高并发查询机制**：模型列表查询采用 Redis 缓存第一页热门数据，结合 MyBatis-Plus 的分页插件处理深分页；写操作（上传）异步化或直接落库。

## 4. 数据表设计 (MySQL)
**sys_user (用户表)**
* `id`: BIGINT (PK)
* `username`: VARCHAR (Unique)
* `password_hash`: VARCHAR (Bcrypt 强加密)
* `create_time`: DATETIME

**tb_model (模型表)**
* `id`: BIGINT (PK)
* `user_id`: BIGINT (FK)
* `name`: VARCHAR (模型名称)
* `artwork_name`: VARCHAR (作品名)
* `type`: VARCHAR (模型类型)
* `image_urls`: JSON/VARCHAR (OSS 图片链接)
* `disk_link`: VARCHAR (夸克网盘链接)
* `is_free`: TINYINT (0-收费, 1-免费)
* `create_time`: DATETIME
* *索引设计*：对 `type`, `name`, `artwork_name` 建立适当的索引以优化模糊查询。

## 5. 已生成项目结构
```text
myweb/
├─ frontend/   # Vue3 + Vite + TS + Tailwind + Pinia + Router
├─ backend/    # Spring Boot 3 + Security + JWT + MyBatis-Plus + Redis + OSS(S3)
├─ docker-compose.yml
├─ PLAN.md
├─ TODO.md
└─ README.md
```

## 6. 本地启动
### 前端
```bash
cd frontend
npm install
npm run dev
```

### 后端
```bash
cd backend
mvn spring-boot:run
```

> 注意：后端是 Spring Boot 3，需要 **JDK 17+**。

## 7. Docker 启动（可选）
```bash
docker compose up -d --build
```
