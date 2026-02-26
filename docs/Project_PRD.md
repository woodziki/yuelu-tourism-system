# 产品需求文档 (PRD) - 岳麓山智慧旅游推荐系统

**版本**: 1.0 (Final Graduation Version)

**项目角色**: 计算机专业毕业设计

**状态**: 待开发

## 1\. 项目概述 (Project Overview)

### 1.1 背景

本项目旨在为岳麓山景区游客提供一个基于 Web 的个性化旅游推荐平台。解决游客面对海量景点信息时的“选择困难症”和“线路规划难”问题。

### 1.2 核心目标

**个性化推荐**: 基于协同过滤算法 (User-based CF)，根据用户行为（点击、收藏、评分）推荐景点。

**智能线路匹配**: 基于用户对景点的喜好，智能排序管理员预设的静态游玩线路。

**极简运维**: 采用“模拟数据注入”策略，规避爬虫风险，确保演示时算法效果可控。

### 1.3 技术栈 (Tech Stack)

**Backend**: Java 18 (User only has JDK 18), Spring Boot 2.7+, MyBatis-Plus, MySQL 8.0.

**前端**: Vue 2, Element UI, Axios.  

**开发工具**: Cursor (AI Assisted).

**算法实现**: 纯 Java 手写实现 (不依赖 Mahout/TensorFlow).

## 2\. 用户角色 (User Roles)

### 2.1 游客 (Visitor/Student)

**未登录**: 仅能浏览首页热门推荐、搜索景点、查看线路列表。

**已登录**: 可以收藏景点、对景点评分 (1-5星)、发布评论、获得个性化推荐 (CF算法)。

### 2.2 管理员 (Admin)

**内容管理**: 录入/修改/删除景点信息（含标签、票价等）。

**线路编排**: 手动创建线路，并关联该线路包含的景点。

**数据看板**: 查看系统基础运营数据（用户数、评分数等）。

## 3\. 功能需求详情 (Functional Requirements)

### 3.1 认证模块 (Auth)

**注册**: 用户名、密码、昵称。注册成功后跳转登录。

**登录**: 基于 Session 或简单的 Token 验证。

**鉴权**: 只有登录用户才能调用 /api/favorite/\* 和 /api/comment/\* 接口。

### 3.2 首页推荐模块 (Core Feature)

**逻辑分流**:

**冷启动 (Cold Start)**:

_条件_: 当前用户未登录 OR 当前用户无任何行为数据。

_逻辑_: 查询 t_spot 表，按 view_count 降序排列，取 Top 10。

_标题_: “热门景点推荐”。

**个性化推荐 (User-CF)**:

_条件_: 用户已登录且有行为记录。

_逻辑_: 调用后端协同过滤工具类，计算相似用户喜欢的景点。

_标题_: “猜您喜欢 (基于您的兴趣)”。

### 3.3 景点详情与交互 (Interaction)

**信息展示**: 封面图、名称、简介、标签 (tags)、价格、建议游玩时长。

**隐式反馈 (Implicit)**: 用户进入详情页时，后台记录一次“浏览”行为 (View)。

**显式反馈 (Explicit)**:

**收藏**: 点击按钮切换收藏状态。

**评分**: 1-5 星打分组件，点击即提交。

**评论**: 文本输入框，提交文字评论。

### 3.4 线路智能匹配模块 (Route Matching)

**线路展示**: 卡片式布局，展示线路地图（静态图）、名称、简介。

**智能排序算法 (Content-based)**:

获取当前用户收藏/好评的所有景点 ID 集合 $U = \{id_1, id_2, ...\}$。

遍历所有线路，获取每条线路包含的景点 ID 集合 $R = \{id_a, id_b, ...\}$。

计算交集大小 $Score = |U \cap R|$。

按 $Score$ 降序排列线路列表。

前端显示“匹配指数”或“命中您喜欢的 X 个景点”。

### 3.5 后台管理模块 (Admin Dashboard)

**数据看板**: 展示 4 个数字卡片（用户总数、景点总数、总浏览量、今日新增评分）。

_图表_: 使用 ECharts 展示“近7日访问趋势”（数据可前端写死数组模拟）。

**内容录入**: 支持上传图片（存本地静态目录或图床），支持多选关联线路的景点。

## 4\. 算法逻辑规范 (Algorithm Specification)

### 4.1 协同过滤 (User-Based CF)

**类名**: RecommendUtils.java

**输入**: 目标用户ID，全量用户行为数据 Map。

**兴趣度公式**:

$$Score = (W_{view} \times Count_{view}) + (W_{fav} \times Is_{fav}) + (W_{rate} \times Rating)$$

$W_{view} = 1$ (浏览)

$W_{fav} = 3$ (收藏)

$W_{rate} = 1$ (评分，取值 1-5)

**相似度公式**: 余弦相似度 (Cosine Similarity)。

**输出**: 推荐度最高的 Top N 景点 ID 列表。

### 4.2 模拟数据策略 (Data Injection)

**严禁爬虫**: 不开发任何动态爬虫功能。

**SQL注入**: 提供 data_mock.sql 脚本。

预置 5 个机器人账号。

预置机器人对特定类别（如“人文类”、“爬山类”）景点的密集评分和收藏数据。

目的：确保算法能计算出高相似度，从而产出推荐结果。

## 5\. 数据库设计 (Database Schema)

_请严格遵守以下表结构，禁止随意修改字段名。_

### 5.1 用户表 (t_user)

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| id  | BIGINT | PK, Auto Increment |
| username | VARCHAR(50) | Unique |
| password | VARCHAR(100) |     |
| nickname | VARCHAR(50) |     |
| create_time | DATETIME |     |

### 5.2 景点表 (t_spot)

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| id  | BIGINT | PK  |
| name | VARCHAR(100) |     |
| intro | TEXT | 简短介绍 |
| content | LONGTEXT | 富文本详情 |
| price | DECIMAL(10,2) | 门票/缆车费 |
| duration | VARCHAR(50) | 建议时长 (e.g. "2小时") |
| tags | VARCHAR(255) | 标签字符串 (e.g. "人文,古迹") |
| view_count | INT | 浏览量 (用于热度排序) |
| score | DOUBLE | 平均评分 (展示用) |

### 5.3 线路表 (t_route)

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| id  | BIGINT | PK  |
| name | VARCHAR(100) |     |
| intro | VARCHAR(500) |     |
| map_img | VARCHAR(255) | 静态地图路径 |
| tags | VARCHAR(255) | 线路标签 |

### 5.4 线路-景点关联表 (t_route_spot)

| **Field** | **Type** | **Description** |
| --- | --- | --- |
| id  | BIGINT | PK  |
| route_id | BIGINT | FK  |
| spot_id | BIGINT | FK  |
| sort | INT | 游玩顺序 (1, 2, 3...) |

### 5.5 行为表 (t_comment, t_favorite)

t_comment: id, user_id, spot_id, content, star (INT 1-5).

t_favorite: id, user_id, spot_id.

## 6\. 非功能性需求 (Constraints)

**代码规范**: 必须包含详细的中文注释（Javadoc 格式），用于解释业务逻辑，方便论文撰写。

**异常处理**: 必须有全局异常处理器 (GlobalExceptionHandler)，前后端交互统一使用 Result<T> 包装类。

**文档化**: 根目录下必须维护 README.md，记录 API 接口、部署步骤和待办事项。