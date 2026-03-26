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

### 2. 架构设计与技术栈
* **前端 (Frontend)**：Vue 3 + Vite + TS + Tailwind CSS
* **像素级 UI 规范 (基于 MakerWorld 截图严格提取)**：
  * **全局色彩 (Colors)**：
    * 主背景色 (Main Canvas)：极浅灰 `bg-[#f4f5f7]`，用于衬托纯白色的卡片。
    * 侧边栏/顶栏背景：纯白 `bg-white`。
    * 文本主色：深黑灰 `text-[#1c1e21]` (标题)；中灰 `text-[#8a8d93]` (作者、数据统计等次要信息)。
    * 品牌高亮/点缀：鲜活绿 `text-[#00AE42]` / `bg-[#00AE42]` (参考截图中的绿色箭头和图标)。
    * 激活状态块 (Active Pill)：深黑 `bg-[#252525]` + 白字 `text-white` (参考“推荐”分类按钮)。
  * **全局圆角 (Border Radius)**：
    * 模型卡片/Banner 主图：大圆角 `rounded-[16px]` 或 `rounded-2xl`。
    * 搜索框/分类 Tag/按钮：全圆角 `rounded-full`。
  * **布局尺寸 (Layout Metrics)**：
    * 左侧边栏 (Sidebar)：展开宽度约 `w-60` (240px)，内边距 `p-4`，菜单项间距紧凑 `space-y-1`。
    * 顶部导航 (Header)：高度约 `h-16`，左侧对齐搜索框，搜索框极宽 (占据约 60% 剩余空间)。
  * **字体排版 (Typography)**：
    * 导航与分类文字：小号粗体 `text-[14px] font-semibold`。
    * 卡片标题：`text-[15px] font-medium leading-tight`，最多显示两行，超出省略 (`line-clamp-2`)。
    * 数据统计：极小号 `text-[12px]`，结合极简线面图标。
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
* `print_layer_height`: DECIMAL(3,2) (推荐层高)
* `print_infill`: INT (填充率 0-100)
* `print_support`: TINYINT (0-不需要, 1-需要)
* `print_material`: VARCHAR (建议材质，如 PLA/PETG)
* `license_type`: VARCHAR (CC 协议代码，如 CC-BY-NC)
* *索引设计*：对 `type`, `name`, `artwork_name` 建立适当的索引以优化模糊查询。

**tb_make (作品秀表 - New)**
* `id`: BIGINT (PK)
* `model_id`: BIGINT (关联模型)
* `user_id`: BIGINT (上传者)
* `image_url`: VARCHAR (实物图链接)
* `description`: TEXT (心得描述)
* `create_time`: DATETIME

**tb_collection (收藏表 - New)**
* `id`: BIGINT (PK)
* `user_id`: BIGINT
* `model_id`: BIGINT
* `create_time`: DATETIME

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
