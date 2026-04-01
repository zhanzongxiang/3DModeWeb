# AGENTS.md

## 目的

本文档用于约束 Codex 及其他协作型 AI 在本仓库中的工作方式。

本项目为前后端分离结构，必须按目录边界维护：

- `frontend/`：Vue 3 + Vite + TypeScript + Tailwind CSS + Pinia + Vue Router + Element Plus
- `backend/`：Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + Redis + OSS（S3 兼容）

不要把前端与后端代码混写到同一应用结构中。

## 需求来源

当用户提到“按 md 文件要求修改”“根据文档继续做”时，以下文件视为当前需求来源：

- `README.md`：产品需求、架构说明、功能定义
- `PLAN.md`：阶段规划、里程碑、完成状态
- `TODO.md`：可执行任务清单

工作规则：

- 开始改代码前，先阅读与任务相关的 `.md` 文件。
- 用户更新了 `.md` 要求后，必须先重新读取对应文档，再开始修改代码。
- 未经用户确认，不要擅自改变文档原意。

## 新增需求的文档同步规则

只要出现“新增需求”“新增页面”“新增接口”“新增约束”“新增验收条件”，无论需求来自用户口头补充还是文档追加，都必须同步记录到以下文件：

- `README.md`：补充需求说明、页面/接口能力、架构或使用说明
- `PLAN.md`：补充所属阶段、里程碑或路线图状态
- `TODO.md`：补充具体待办项，确保可以执行和追踪

执行要求：

- 先实现需求，再回写完成状态。
- 如果只是新增需求但尚未实现，必须写入文档但不能打勾。
- 只有“代码已实现 + 基本验证已完成”后，才能把 `PLAN.md`、`TODO.md` 中对应项改成 `[x]`。
- 如果需求会影响使用方式或项目结构，也要同步更新 `README.md`。
- 不要只改代码不改文档，也不要只勾选清单不落实现。

## 仓库结构

- `frontend/src/views/`：页面级视图
- `frontend/src/components/`：可复用组件
- `frontend/src/api/`：HTTP 封装与接口模块
- `frontend/src/router/index.ts`：路由与鉴权守卫
- `frontend/src/stores/auth.ts`：前端登录态与本地持久化
- `frontend/src/types/index.ts`：前端共享类型
- `backend/src/main/java/com/modelhub/backend/controller/`：接口入口层
- `backend/src/main/java/com/modelhub/backend/service/`：业务层
- `backend/src/main/java/com/modelhub/backend/mapper/`：MyBatis-Plus Mapper
- `backend/src/main/java/com/modelhub/backend/entity/`：数据库实体
- `backend/src/main/java/com/modelhub/backend/dto/`：请求/响应 DTO
- `backend/sql/init.sql`：初始化 SQL
- `docker-compose.yml`：容器编排
- `jenkins/`、`Jenkinsfile`：CI/CD 相关文件

## 本地运行说明

### 前端

运行：

```bash
cd frontend
npm install
npm run dev
```

构建：

```bash
cd frontend
npm run build
```

已知事实：

- Vite 开发端口为 `9080`
- 前端默认通过 `/api` 访问后端
- 代理配置位于 `frontend/vite.config.ts`
- 代理目标来自 `VITE_DEV_API_PROXY_TARGET`
- 当前 `.env.example` 默认开发代理为 `http://36.150.237.57:8080`

### 后端

运行：

```bash
cd backend
mvn spring-boot:run
```

打包：

```bash
cd backend
mvn -DskipTests package
```

重要说明：

- 后端基于 Spring Boot 3，必须使用 `JDK 17+`
- 未确认 Maven 实际使用的是 JDK 17 或更高版本前，不要宣称后端构建成功
- 当前机器历史上出现过 Maven 仍使用 Java 8 的情况，会导致代码正确但编译失败

### Swagger / OpenAPI

后端正常启动后，可通过以下路径访问接口文档：

- `/api/swagger-ui.html`
- `/api/docs`

例如：

- `http://localhost:8080/api/swagger-ui.html`
- `http://localhost:8080/api/docs`

### Docker

```bash
docker compose up -d --build
```

## 前端修改规则

- 使用现有 Vue 3 SFC、`script setup`、TypeScript 风格
- 优先复用 `frontend/src/api/http.ts` 与 `frontend/src/api/modules/*`
- 不要在页面或组件里随意新建 `fetch` 或临时 Axios 实例
- 页面放在 `frontend/src/views/`，共享模块放在 `frontend/src/components/`
- 受保护页面继续使用 `meta.requiresAuth` 和现有路由守卫
- 登录态继续通过 `frontend/src/stores/auth.ts` 管理
- 后端地址统一由 API 层与代理层处理，不要在组件中硬编码主机地址
- 保持当前 MakerWorld 风格的视觉方向，不要随意替换成无关的通用 UI
- 优先复用 `tailwind.config.ts` 与 `src/style.css` 中已有设计 token
- Element Plus 已用于表单与上传相关界面，风格一致时优先复用

### 当前前端路由

- `/`
- `/hall`
- `/models/:id`
- `/collections`
- `/login`
- `/register`
- `/upload`

除非用户明确要求，不要随意重命名或删除现有路由。

### 前端常见坑

- 损坏或乱码的 `.vue` 文件，可能触发 Vite 误导性报错：
  `Failed to parse source for import analysis... Install @vitejs/plugin-vue...`
- 遇到该错误时，先检查：
  - 目标 `.vue` 文件内容是否损坏
  - 编码是否异常
  - 模板标签是否错乱
  - 是否仍有旧的 Vite 进程在提供过期内容
- 如果浏览器报错疑似陈旧缓存，先结束旧 `node` / Vite 进程，再重新执行 `npm run dev`
- 修改 Vite 代理时，要确认上游接口是否要求保留 `/api` 前缀，错误 rewrite 很容易表现成前端故障

## 后端修改规则

- 保持现有分层：`Controller -> Service -> Mapper -> Entity/DTO`
- 对外响应继续统一使用 `ApiResponse`
- 公共 API 路径继续保持在 `/api` 下
- 请求校验优先写在 DTO 上，使用 `jakarta.validation`
- 认证相关接口继续遵循 JWT + Spring Security 方案
- 新增公开接口时，必须同步检查并更新 `SecurityConfig`
- 新增鉴权接口时，确认前端继续按现有 Bearer Token 方式调用
- MyBatis-Plus 已启用驼峰映射，字段命名要与现有模式一致
- Swagger 注解可继续使用，但框架内部参数如 `Authentication` 应避免暴露到文档中

### 当前后端控制器边界

- `AuthController`
- `ModelController`
- `OssController`
- `MakeController`
- `CollectionController`
- `HealthController`

新增接口优先并入现有边界清晰的模块，不要无理由新建控制器。

## 接口与数据约束

- 前端本地登录态存储键：
  - `model_hub_token`
  - `model_hub_username`
  - `model_hub_user_id`
- 模型相关前端类型集中在 `frontend/src/types/index.ts`
- 当前模型字段包含打印参数与协议字段，前后端命名必须保持一致：
  - `printLayerHeight`
  - `printInfill`
  - `printSupport`
  - `printMaterial`
  - `licenseType`
- 当前社区能力包括：
  - `makes`
  - `collections`
- 如果改动请求或响应结构，必须同步修改：
  - 后端 DTO
  - 后端业务映射
  - 前端类型定义
  - 前端接口模块
  - 受影响的页面与组件

## 文档与勾选规则

- 任务来源于 `README.md`、`PLAN.md`、`TODO.md` 时，代码实现必须与文档保持一致
- 用户新增要求后，必须把新增内容同步补充到 `README.md`、`PLAN.md`、`TODO.md`
- 只有在实现完成并完成基本验证后，才能把对应清单标记为已完成
- 如果只是创建了结构、文案或占位页面，但功能未完成，不要打勾
- 不要静默回退用户对 `.md` 文件的修改

## 验证要求

默认验证方式如下。

### 前端

```bash
cd frontend
npm run build
```

### 后端

仅在确认切换到 JDK 17+ 后执行：

```bash
cd backend
mvn -DskipTests package
```

### 冒烟检查

- `GET /api/health`
- 打开 Swagger UI：`/api/swagger-ui.html`
- 如果改动了认证或接口，确认前端仍能通过 `/api` 正常调用目标接口

## 项目已知风险

- 本机 Maven 可能仍默认使用 Java 8，而项目需要 Java 17+
- 旧的 Vite 进程可能持续提供过期页面内容
- 损坏的 `.vue` 文件会触发误导性的 `plugin-vite:import-analysis` 报错
- 开发代理目标与服务器 Nginx 行为可能不一致，修改代理前要先验证真实上游
- 用户如果要求“前后台都改”，必须同时检查 `frontend/` 与 `backend/`，不要默认只改前端

## 编辑约束

- 修改范围必须围绕用户当前需求
- 不要回退与当前任务无关的用户改动
- 优先沿用现有模式，不要额外引入平行实现
- 新增文件保持聚焦、简洁
- 只要触及 `.md` 需求文件，就必须如实反映完成状态

## 推荐工作流程

- 任务涉及需求时，先读 `README.md`、`PLAN.md`、`TODO.md`
- 先定位目标模块，再做最小必要修改
- 同步更新受影响文档
- 运行与任务相关的验证命令
- 明确汇报阻塞项，尤其是：
  - JDK 版本不匹配
  - Vite 旧进程或缓存
  - 代理路径不匹配
  - `.vue` 文件损坏或乱码
