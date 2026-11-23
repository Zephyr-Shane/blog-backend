# 阿申の小破站 · 后端（blog-backend）

基于小傅哥 DDD 六边形脚手架搭建的 Spring Boot 后端，严格实现《API 契约 v1.0》，供 `zephyr-blog` 前端调用。

## 技术栈与架构

- Java 17 + Spring Boot 3.4.3 + MyBatis + MySQL 8 + JWT（java-jwt）+ Jsoup（正文消毒）
- 六边形分层，依赖方向单向：`trigger → domain ← infrastructure`，`app` 负责装配

| 模块 | 职责 |
|---|---|
| `types` | 响应码、业务异常（无框架依赖的内核常量） |
| `api` | 统一响应 `Response{code,message,data}`、分页 `PageResult` |
| `domain` | 实体/值对象、仓储端口（Port）、领域服务；业务逻辑核心，不依赖具体存储 |
| `infrastructure` | MyBatis Mapper、仓储实现（Adapter）、JWT、上传、HTML 消毒 |
| `trigger` | HTTP Controller、鉴权拦截器、CORS、全局异常处理 |
| `app` | 启动类、配置、MyBatis 扫描 |

**关键设计**：
- 泛型仓储 `ContentRepository<T>` + 抽象基类 `AbstractContentRepository`：四类内容（post/moment/interview/link）共用一套 CRUD，标签批量装配避免 N+1
- `AdminResourceService` 用策略映射把 7 种后台资源统一到一个 CRUD 入口，无重复样板代码
- 所有写操作经列白名单（`SqlColumns`）生成 SQL，防注入；PUT 为部分更新语义，避免前端只提交表单字段时丢数据
- 正文入库前经 `HtmlSanitizer` 消毒，防存储型 XSS
- 时间以 ISO8601 字符串存储，跨库/跨端零时区转换

## 快速启动

```bash
# 1. 初始化数据库（会创建 zephyr_blog 库并写入种子数据）
mysql -h127.0.0.1 -uroot -p < docs/dev-ops/mysql/sql/blog-schema.sql

# 2. 构建并运行
mvn clean package -DskipTests
java -jar blog-backend-app/target/blog-backend-app.jar --spring.profiles.active=dev
```

服务地址：`http://localhost:8080/api`（context-path = `/api`）

数据库账号密码见 `blog-backend-app/src/main/resources/application-dev.yml`。

## 前端对接

前端设置 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api` 即切换到本后端（见前端 `.env.local`）；留空则走 mock。所有请求经前端 `lib/api.ts`（服务端）与 `lib/client-api.ts`（客户端，自动注入 Bearer token）。

- 登录：`POST /api/admin/auth/login`，body `{email,password}` → 返回 `{token,user}`
- 后台接口：请求头带 `Authorization: Bearer <token>`；登录接口除外
- 管理员账号：`admin@zephyr.dev`，密码见 `application-dev.yml` 对应密码（SHA-256 存储，种子 SQL 中）

## 接口一览

前台公开：`GET /feed /posts /posts/{slug} /moments /interviews /links /tags /tags/{slug}/contents /search /site-config /comments`，`POST /comments /likes`

后台鉴权：`POST /admin/auth/login`、`GET /admin/stats`、`GET|PUT /admin/site-config`、`PATCH /admin/comments/{id}/review`、`POST /admin/upload`、`GET /admin/media`、`DELETE /admin/media/{id}`，以及 `GET|POST|PUT|DELETE /admin/{resource}[/{id}]`（resource ∈ post/moment/interview/link/tag/comment/user）

前台公开媒体：`GET /media/{id}`——从数据库直出图片二进制（带 30 天缓存），不依赖文件系统

统一响应：`{ code, message, data }`；分页 data 为 `{ list, total, page, pageSize }`

## 上传资源（二进制落库）

交互产生的上传**统一落 MySQL `blog_upload` 表的 LONGBLOB**，不写文件系统、不做 `/uploads` 静态映射；因此重启/多实例都不丢资源。前端项目自带的 `/images/*.svg` 属于前端静态资源，不由后端存储、不由后端代理。

- `POST /admin/upload`：multipart，字段 `file` + `category`（avatar/cover/content/logo/gallery/other），返回 `{id, category, fileName, mimeType, size, url, createdAt}`
- `url` 由 `blog.upload.public-url` 配置生成完整地址（默认 `http://localhost:8080/api/media`），可直接写进文章字段或 `<img src>`；**改部署地址时须同步修改该配置**
- `GET /media/{id}`：公开读取；`GET /admin/media?category=`：媒体库列表（仅元数据，鉴权）；`DELETE /admin/media/{id}`：删除
- 限制：5MB，png/jpg/jpeg/webp/gif/svg；分类白名单校验

首次部署需执行建表脚本 `docs/dev-ops/mysql/sql/blog-upload.sql`。

## 已知事项

- JWT 密钥为 dev 默认值（`blog.jwt.secret`），生产环境务必通过环境变量覆盖
- 编译需开启 `-parameters`（父 pom 已配置），否则 `@RequestParam` 未命名时会启动失败
- 数据库密码为本地开发值，部署前请修改配置
