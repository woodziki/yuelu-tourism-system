# 岳麓山景点推荐系统（Yuelu Tourism System）

## 技术栈

- **后端**：Java 18、Spring Boot 2.7+、MyBatis-Plus、MySQL 8.0、Lombok、Hutool
- **前端**：Vue 2、Element UI、Axios（后续步骤实现）
- **算法**：纯 Java 手写实现（PRD 指定 User-based CF，后续步骤实现）

## 当前进度（Step 5）

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
  - ✅ Step 4：互动模块（收藏 Favorite 与评论 Comment）
    - 收藏接口：添加收藏、取消收藏、分页查询“我的收藏”（`FavoriteController`）
    - 评论接口：针对景点发表评论、分页查询景点评论列表（`CommentController`）
    - 鉴权策略：发表评论、收藏相关接口需登录，从 JWT 中解析 userId，绝不信任前端传入的 userId
  - ✅ Step 5：推荐算法（User-based 协同过滤）
    - 推荐工具类：`RecommendUtils`（User-based CF，余弦相似度，纯 Java 实现）
    - 推荐服务：`RecommendService` / `RecommendServiceImpl`
      - 从 `t_favorite`、`t_comment` 构建用户-景点兴趣度矩阵
      - 兴趣度公式：`Score = (W_fav * Is_fav) + (W_rate * Rating)`（其中 `W_fav = 3`，`W_rate = 1`）
      - 调用协同过滤算法获取推荐景点 ID，再回查 `t_spot` 返回完整信息
    - 冷启动策略：当目标用户无行为数据或无相似用户时，按 `view_count` 降序返回 Top 10 热门景点
    - 推荐接口：`GET /spot/recommend`（需登录，从 Token 中解析当前用户 ID）

## 开发日志 / 更新记录

- [x] 初始化 Vue 2 + Element UI 前端工程架构。
- [x] 封装基于 Axios 的企业级网络请求拦截器（统一处理 Token 与 401 跳转）。
- [x] 完成带毛玻璃特效和高品质风景图的登录/注册一体化页面。
- [x] 完成首页（推荐页），对接基于协同过滤的 `GET /spot/recommend` 接口，并展示精美卡片。
- [x] 前后端配合彻底解决图片资源的本地化存储与防盗链展示问题。
- [x] 完成景点详情页（`SpotDetail.vue`），实现动态路由跳转与数据渲染。
- [x] 打通核心推荐闭环：实现前端状态感知级别的“收藏/取消收藏”全栈联动逻辑。
- [x] 实现公共组件 `Navbar.vue`，打通全局导航体系（首页推荐 / 全部景点 / 我的收藏）。
- [x] 后端完成 `GET /favorite/list` 接口，支持用户收藏数据的全量回显。
- [x] 前端完成“我的收藏”页面（`MyFavorites.vue`），实现个性化收藏夹功能。
- [x] 搭建“全部景点”浏览大厅（`AllSpots.vue`），完善系统公共游览功能。
- [x] 完成“打分与评价”全栈闭环（`/comment/add` + `/comment/list` + 详情页评论区），为协同过滤算法提供显式评分数据源。
- [x] 升级“全部景点”大厅，实现按名称和标签的综合检索功能（`AllSpots.vue` 搜索 + 标签筛选）。
- [x] 打通“经典路线”模块（`/route/list` + `Routes.vue`），实现路线规划与景点串联的时间轴可视化展示。
- [x] 后台景点管理 CRUD：后端新增 `POST /spot/add`、`PUT /spot/update`、`DELETE /spot/delete/{id}`，前端 `SpotManage.vue` 表格 + 分页 + 新增/编辑弹窗 + 删除二次确认。

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

- Step 6（前端部分）：基于 Vue 2 + Element UI 实现以下页面与交互：
  - 首页“热门景点推荐”与“猜你喜欢”模块，对接 `/spot/list` 与 `/spot/recommend` 接口
  - 景点详情页，支持收藏、评分、评论交互，并展示推荐结果入口
  - “我的收藏”页面，对接 `/favorite/my` 接口，支持一键跳转到景点详情
  - 登录/注册页，对接 `/user/login` 与 `/user/register`，在 Axios 中全局携带 JWT Token

