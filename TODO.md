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

## Phase 3: 像素级 UI 还原 (MakerWorld 风格)
- [x] 4. 配置 `tailwind.config.js`：添加自定义颜色 `canvas: '#f4f5f7'`, `primary-text: '#1c1e21'`, `muted: '#8a8d93'`, `brand: '#00AE42'`；添加自定义圆角 `card: '16px'`。
- [x] 5. 编写基础 Layout：实现固定宽度 (`w-[240px]`) 的左侧边栏（包含顶部的侧边栏收缩图标 `[]`、Logo 和左对齐的图标菜单），和右侧的 `flex-1` 主体区域。整体采用 `h-screen overflow-hidden`，右侧主体区域允许 `overflow-y-auto`。
- [x] 6. 编写 Header & 分类栏：在右侧主体顶部实现白底 Header。中心放置高度约 `40px`、`rounded-full`、背景色为 `bg-gray-100` 的超宽搜索栏（带左侧放大镜，右侧相机图标）。Header 下方实现一行横向滚动的分类 Tags（推荐、热门等），选中项使用 `bg-[#252525] text-white`，未选中项使用 `bg-gray-100 text-gray-600`。
- [x] 7. 编写首页 Banner 模块：主内容区顶部实现左右结构 Banner。左半部分为占宽约 65% 的大图轮播，`rounded-[16px]`；右半部分为占宽约 35%、背景为浅绿色渐变的区域，包含“探索更多”标题和 2x2 网格排列的圆角小块（如 MakerLab、创客宝库等）。
- [x] 8. 编写 ModelCard 组件：容器 `rounded-[16px] bg-white overflow-hidden`。图片区占满宽度；信息区包含深色粗体标题 (`line-clamp-2`)，底部采用 `flex justify-between items-center` 布局，左侧放置圆角头像 `w-5 h-5` 和作者名，右侧放置下载/点赞微小图标和灰色数字 (`text-[12px]`)。左上角支持叠放角标（如模型类型 icon）。

## Phase 4: Frontend Development
- [x] 12. 封装网络请求。
- [x] **13. 页面渲染逻辑：在主视图（`router-view`）中，编写特定逻辑，确保首页路由能根据数据渲染推荐 Banner。**
- [x] **14. 列表渲染逻辑：确保所有非首页路由仅渲染模型列表，无需 Banner。**

## Phase 5: 基础体验增强 (Current)
- [x] **15. 数据库 DDL 更新**:
    - [x] 为 `tb_model` 增加 `print_layer_height`, `print_infill`, `print_support`, `license_type` 字段。
    - [x] 创建 `tb_make` 表用于存储实物作品秀数据。
- [x] **16. 后端 Entity 与 DTO 同步**:
    - [x] 修改 `TbModel.java` 实体类，增加 MyBatis-Plus 对应映射。
    - [x] 更新 `ModelUploadRequest.java` 接收前端新增的打印参数字段。
- [x] **17. 前端上传表单改造**:
    - [x] 在 `UploadView.vue` 引入 `el-form-item` 组，添加 Slider (填充率) 和 Select (材质/协议)。
    - [x] 预设 CC 协议选项列表：`["CC BY", "CC BY-NC", "CC BY-ND", "CC BY-SA"]`。
- [x] **18. 模型展示增强**:
    - [x] 在 `ModelCard.vue` 底部角落增加一个微小的协议图标提示。
    - [x] 开发详情页组件 `PrintSpec.vue`，用于展示层高、支撑等参数。

## Phase 6: 社区互动 (Next)
- [x] **19. 作品秀逻辑**: 编写 `MakeController`，支持用户上传实物打印图并关联模型。
- [x] **20. 收藏功能**: 封装收藏 API，实现前端点击爱心按钮的高亮切换。

## Phase 7: Safety & Ops
- [x] **21. 验证码接入**: 前端登录页集成 Cloudflare 验证码。

## Phase 8: Admin Governance（用户 / 角色 / 组织）
- [x] **22. 数据库结构扩展**: 新增组织、角色、用户角色关联、角色菜单关联、角色接口关联、角色数据范围关联等表结构及索引。
- [x] **23. 组织管理后端能力**: 新建 `OrgController` / `OrgService`，实现组织树查询、组织节点新增、编辑、删除、启停用接口。
- [x] **24. 用户管理后端能力**: 新建管理端用户查询与维护接口，覆盖分页检索、创建、编辑、启停用、重置密码、组织归属更新。
- [x] **25. 角色管理后端能力**: 新建角色查询与维护接口，覆盖角色新增、编辑、删除（或停用）、角色成员查看、角色编码唯一校验。
- [x] **26. 授权能力实现**: 实现角色菜单授权、角色接口授权、角色数据范围授权（`ALL`/`ORG_AND_CHILDREN`/`ORG_ONLY`/`SELF`/`CUSTOM_ORG`）。
- [x] **27. 权限计算与校验改造**: 支持用户多角色并集计算，接入菜单可见性、接口鉴权、数据范围过滤。
- [ ] **28. 后台审计能力**: 增加登录日志、操作日志与关键权限变更审计记录能力。
- [x] **29. 管理后台页面开发**: 新增组织管理、用户管理、角色管理、授权配置页面，并完成联调。
- [ ] **30. 验证与验收**: 完成权限回归测试与数据范围测试，确认越权访问被正确拦截。
