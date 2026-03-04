-- 岳麓山智慧旅游推荐系统 - 数据库建表脚本
-- 严格依据 PRD 第 5 章（Database Schema）编写，禁止随意增删字段/改名。

-- 1) 建库（如已存在可跳过）
CREATE DATABASE IF NOT EXISTS yuelu_tourism
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE yuelu_tourism;

-- 5.1 用户表 (t_user)
CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, Auto Increment',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique',
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  create_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.2 景点表 (t_spot)
CREATE TABLE IF NOT EXISTS t_spot (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, Auto Increment',
  name VARCHAR(100) NOT NULL,
  intro TEXT,
  content LONGTEXT,
  price DECIMAL(10,2),
  duration VARCHAR(50),
  tags VARCHAR(255),
  view_count INT DEFAULT 0,
  score DOUBLE DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.3 线路表 (t_route)
CREATE TABLE IF NOT EXISTS t_route (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, Auto Increment',
  name VARCHAR(100) NOT NULL,
  intro VARCHAR(500),
  map_img VARCHAR(255),
  tags VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.4 线路-景点关联表 (t_route_spot)
CREATE TABLE IF NOT EXISTS t_route_spot (
  id BIGINT PRIMARY KEY COMMENT 'PK, Auto Increment',
  route_id BIGINT NOT NULL COMMENT 'FK',
  spot_id BIGINT NOT NULL COMMENT 'FK',
  sort INT NOT NULL COMMENT '游玩顺序 (1,2,3...)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.5 行为表 (t_comment, t_favorite)
CREATE TABLE IF NOT EXISTS t_comment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  spot_id BIGINT NOT NULL,
  content TEXT,
  star INT NOT NULL COMMENT 'INT 1-5',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_favorite (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  spot_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

