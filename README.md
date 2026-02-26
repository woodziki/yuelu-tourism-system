# 岳麓山景点推荐系统（Yuelu Tourism System）

## 技术栈

- **后端**：Java 18、Spring Boot 2.7+、MyBatis-Plus、MySQL 8.0、Lombok、Hutool
- **前端**：Vue 2、Element UI、Axios（后续步骤实现）
- **算法**：纯 Java 手写实现（PRD 指定 User-based CF，后续步骤实现）

## 当前进度（Step 3）

- **已完成**
  - ✅ Step 1：后端基础工程初始化（`backend`，Spring Boot + Maven）
  - ✅ Step 1：MySQL 连接配置（`backend/src/main/resources/application.yml`，库名 `yuelu_tourism`）
  - ✅ Step 1：数据库建表脚本（`docs/schema.sql`，严格按 PRD 第 5 章，所有表 ID 均为 AUTO_INCREMENT）
  - ✅ Step 1：实体类（`backend/src/main/java/com/yuelu/entity`，对应 PRD 表结构）
  - ✅ Step 2：统一返回类 `Result<T>`（`com.yuelu.common`）
  - ✅ Step 2：Mapper 层（`com.yuelu.mapper`，继承 BaseMapper）
  - ✅ Step 2：Service 层（`com.yuelu.service` 及 `impl`，继承 IService）
  - ✅ Step 2：Controller 层（`com.yuelu.controller`，景点和线路接口）
  - ✅ Step 2：MyBatis-Plus 分页插件配置（`MyBatisPlusConfig.java`）
  - ✅ Step 3：用户认证（注册/登录，密码 BCrypt 加密，登录返回 JWT）
  - ✅ Step 3：JWT 拦截器（JwtInterceptor）+ WebMvcConfig 白名单（/user/login、/user/register、/spot/**、/route/**）
  - ✅ Step 3：全局异常处理（GlobalExceptionHandler，AuthException 返回「请先登录」）

## 数据库设计（PRD 第 5 章）

> 表名与字段名 **必须严格遵循 PRD**，禁止随意修改。

- **t_user（用户表）**
  - id BIGINT（自增主键）
  - username VARCHAR(50)（唯一）
  - password VARCHAR(100)
  - nickname VARCHAR(50)
  - create_time DATETIME

- **t_spot（景点表）**
  - id BIGINT（主键）
  - name VARCHAR(100)
  - intro TEXT
  - content LONGTEXT
  - price DECIMAL(10,2)
  - duration VARCHAR(50)
  - tags VARCHAR(255)
  - view_count INT
  - score DOUBLE

- **t_route（线路表）**
  - id BIGINT（主键）
  - name VARCHAR(100)
  - intro VARCHAR(500)
  - map_img VARCHAR(255)
  - tags VARCHAR(255)

- **t_route_spot（线路-景点关联表）**
  - id BIGINT（主键）
  - route_id BIGINT
  - spot_id BIGINT
  - sort INT

- **t_comment（评论表）**
  - id BIGINT
  - user_id BIGINT
  - spot_id BIGINT
  - content TEXT
  - star INT（1-5）

- **t_favorite（收藏表）**
  - id BIGINT
  - user_id BIGINT
  - spot_id BIGINT

## 已实现的接口（Step 2）

### 景点接口（SpotController）

- **GET `/spot/list`**：分页查询景点列表
  - 参数：`current`（页码，默认 1）、`size`（每页大小，默认 10）、`name`（名称模糊搜索，可选）、`tags`（标签筛选，可选）
  - 返回：分页结果（`Result<IPage<Spot>>`）
  - 示例：`GET /spot/list?current=1&size=10&name=爱晚亭&tags=人文`

- **GET `/spot/{id}`**：查询景点详情
  - 参数：`id`（景点 ID，路径参数）
  - 返回：景点详情（`Result<Spot>`）
  - 示例：`GET /spot/1`

### 线路接口（RouteController）

- **GET `/route/list`**：查询所有线路列表
  - 返回：线路列表（`Result<List<Route>>`）
  - 示例：`GET /route/list`

- **GET `/route/{id}`**：查询线路详情
  - 参数：`id`（线路 ID，路径参数）
  - 返回：线路详情（`Result<Route>`）
  - 注意：当前版本暂不返回关联的景点列表，后续步骤会添加
  - 示例：`GET /route/1`

### 用户认证接口（UserController）

- **POST `/user/register`**：注册
  - 请求体：`{ "username", "password", "nickname" }`
  - 返回：`Result<User>`（用户信息，不含密码）
  - 密码存库前使用 BCrypt 加密

- **POST `/user/login`**：登录
  - 请求体：`{ "username", "password" }`
  - 返回：`Result<LoginVO>`（`token`、`userId`、`username`、`nickname`）
  - 需登录的接口请在请求头携带：`Authorization: Bearer <token>`

### 鉴权说明

- **白名单**（无需 Token）：`/user/login`、`/user/register`、`/spot/**`、`/route/**`
- **需登录**：如后续的 `/favorite/**`、`/comment/**` 等，未带或 Token 无效时返回 `code=401`、`message="请先登录"`

## 下一步计划

- Step 4：实现用户行为接口（收藏/评分/评论）
- Step 5：实现推荐算法工具类 `RecommendUtils.java`（User-based CF）

