# 项目路线图 (Project Roadmap)

## Phase 1: 基础设施与骨架搭建 (当前阶段)
- [x] 初始化前端项目 (Vue 3 + Vite + TS + Tailwind)。
- [x] 初始化后端项目 (Spring Boot 3 + Maven + MySQL + Redis 依赖配置)。
- [x] 数据库 Schema 初始化与实体类 (Entity) 代码生成。

## Phase 2: UI/UX 布局与 Maker 风格全局设定 (Updated)
- [x] 确立全局设计规范 (配置 Tailwind：深灰色文本色板、大圆角 `rounded-xl`, 柔和阴影变量 `shadow-soft`)。
- [x] **搭建核心全局布局 (实现左右分栏：左侧可收缩侧边栏 + 右侧主内容区；顶部 Header 包含大型搜索框，致敬截图布局)。**
- [x] 设计核心 UI 组件 (干净简约的模型卡片 `ModelCard`，瀑布流或网格布局)。

## Phase 3: 后端核心业务 API 开发
- [x] 用户模块：Spring Security + JWT 鉴权配置。
- [x] 安全模块：基于 Redis + 自定义拦截器实现防撞库（登录限流策略）。
- [x] 存储模块：集成 OSS SDK，实现图片上传并返回 URL 的接口。
- [x] 模型模块：实现模型数据的发布 (POST) 接口。
- [x] 查询模块：基于 MyBatis-Plus 编写多条件动态 `LIKE` 查询及分页接口，并加入 Redis 缓存。

## Phase 4: 前端页面与联调
- [x] 封装 Axios 请求及全局响应/异常拦截器。
- [ ] 编写登录与注册页面（集成前端行为验证码）。
- [x] 编写模型上传表单页面。
- [x] 编写模型大厅页面（支持瀑布流、分类 Tab、搜索框模糊检索）。

## Phase 5: 测试与部署
- [ ] 后端 Jmeter 并发查询压测及限流阈值调优。
- [x] Docker 化部署打包 (Dockerfile & docker-compose)。
