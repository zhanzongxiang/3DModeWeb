# 任务清单 (Task List)

## Phase 1: Infrastructure
- [x] 1. 初始化前端：使用 Vite 创建 Vue 3 + TS 项目，安装并配置 Tailwind CSS、Pinia 和 Vue Router。
- [x] 2. 初始化后端：使用 Spring Initializr 生成 Spring Boot 3 工程，引入 Web, Security, MyBatis-Plus, Redis, MySQL 驱动。
- [ ] 3. 数据库与配置：在本地/开发环境创建 MySQL 数据库，执行初始化 SQL 脚本建立 `sys_user` 和 `tb_model` 表；在 `application.yml` 中配置数据库和 Redis 连接。

## Phase 2: Backend Development
- [x] 4. 鉴权体系开发：实现 `SecurityConfig`，编写 JWT 工具类，实现用户注册与登录接口（密码需 Bcrypt 加密）。
- [x] 5. 防撞库限流：利用 Redis 和 AOP（切面）编写 `@RateLimiter` 注解，限制登录接口的访问频率（如单 IP 1分钟内最多5次）。
- [x] 6. 对象存储开发：编写 `OssService`，实现图片上传至 MinIO/云 OSS 的接口。
- [x] 7. 模型发布开发：编写 `ModelController.upload()` 接收表单数据并持久化到 MySQL。
- [x] 8. 模型检索开发：编写 `ModelController.list()`，使用 MyBatis-Plus `LambdaQueryWrapper` 处理 `name`, `artwork_name`, `type` 的模糊查询，并使用 Spring Cache 或 RedisTemplate 缓存首页结果。

## Phase 3: UI/UX & Layout (MakerWorld 风格重构)
- [x] 9. 样式配置：修改 `tailwind.config.js`。配置干净的中性灰阶（如 `gray-50` 到 `gray-900`），定义主品牌色（如 `brand-green`），并自定义轻量级的 `boxShadow` 用于卡片悬浮效果。
- [x] 10. 全局布局：编写 `Layout.vue`，实现白底+底边微阴影的 Header，Header 中间放置宽幅的模糊查询搜索框，整体页面背景设为极浅的灰色（如 `bg-gray-50`）以凸显白色内容卡片。
- [x] 11. 基础组件：开发 `ModelCard.vue`。要求：顶部大图（带柔和圆角），底部紧凑显示模型名称、作者小头像与名称、以及右侧的“免费/夸克链接”标签。鼠标悬浮时卡片整体微弱上浮并加深柔和阴影。

## Phase 4: Frontend Development
- [x] 12. 封装网络请求。
- [x] **13. 页面渲染逻辑：在主视图（`router-view`）中，编写特定逻辑，确保首页路由能根据数据渲染推荐 Banner。**
- [x] **14. 列表渲染逻辑：确保所有非首页路由仅渲染模型列表，无需 Banner。**
