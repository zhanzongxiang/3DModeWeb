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

### 7.1 后台基础账号与角色初始化 SQL
后台治理能力初始化可执行：

```bash
backend/sql/admin_seed_basic.sql
```

默认会初始化：
* 超级管理员账号 `superadmin`
* 基础角色：`SUPER_ADMIN`、`PLATFORM_ADMIN`、`OPS_OPERATOR`、`SEC_AUDITOR`
* 超级管理员与基础角色授权关系

## 8. 后台治理能力规划（用户 / 角色 / 组织）

为支持平台级后台运营，新增“用户管理、角色管理、组织管理”能力，作为后端管理域新模块，规范如下：

### 8.1 范围边界
* **管理形态**：平台级后台（平台超管统一管理全量用户、角色、组织）。
* **权限模型**：菜单权限 + 接口权限 + 数据范围权限。
* **组织模型**：树形组织结构（支持父子级）。
* **角色分配**：用户可绑定多个角色，权限按并集合并。
* **一期约束**：先采用“一个用户一个主组织”，暂不做“同一用户在不同组织下配置不同角色”。

### 8.2 功能拆分
* **用户管理**：用户分页检索、创建、编辑、启停用、重置密码、组织归属、角色绑定。
* **角色管理**：角色创建/编辑/停用、菜单授权、接口授权、数据范围授权、角色成员查看。
* **组织管理**：组织树查询、节点新增/编辑/删除、启停用、负责人设置、组织排序。
* **权限资源管理**：菜单资源、接口资源、权限码统一维护。
* **审计与安全**：登录日志、操作日志、越权访问记录。

### 8.3 数据范围规范
* `ALL`：全量数据
* `ORG_AND_CHILDREN`：本组织及下级组织
* `ORG_ONLY`：仅本组织
* `SELF`：仅本人数据
* `CUSTOM_ORG`：自定义组织范围

### 8.4 权限码规范
* 命名格式：`sys:<resource>:<action>`
* 示例：`sys:user:list`、`sys:user:create`、`sys:role:assign`、`sys:org:update`

### 8.5 落地原则
* 新增后台治理能力时，必须同步更新 `README.md`、`PLAN.md`、`TODO.md`。
* 仅在“代码实现 + 基本验证完成”后，才能将对应计划项和任务项打勾。

## 9. 模型论坛基础能力补全清单（新）

为形成“内容发布-互动-治理-运营”闭环，新增以下基础能力规划：

### 9.1 P0（优先级最高）
* **账号体系完善**：找回密码、修改密码、邮箱/手机验证、账号注销。
* **评论与回复**：模型详情评论、楼中回复、@提醒、评论删除。
* **通知中心**：评论回复通知、被收藏通知、审核结果通知、系统通知。
* **内容治理**：举报入口、审核队列、违规下架、用户封禁。
* **下载闭环**：下载记录、下载统计、失效链接检测。
* **检索增强**：标签体系、排序（最新/最热/下载量）、多条件筛选。

### 9.2 P1（论坛化增强）
* **话题/帖子模块**：经验贴、求助帖、教程帖。
* **模型版本管理**：版本号、更新日志、历史版本。
* **用户主页**：发布列表、收藏列表、关注关系。
* **评分体系**：评分提交、评分分布展示、评分排序。

### 9.3 P2（运营增长）
* **积分等级系统**：上传、评论、收藏等行为积分。
* **榜单与推荐**：周榜、新人榜、个性化推荐。
* **运营看板**：DAU、上传转化、下载转化、举报率等指标。
