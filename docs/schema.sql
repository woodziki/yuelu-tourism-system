/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : yuelu_tourism

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 12/03/2026 13:09:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint NOT NULL,
                              `spot_id` bigint NOT NULL,
                              `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                              `star` int NOT NULL COMMENT 'INT 1-5',
                              `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_comment
-- ----------------------------
INSERT INTO `t_comment` VALUES (1, 2, 17, '必看', 5, '2026-03-06 19:23:58');
INSERT INTO `t_comment` VALUES (2, 2, 20, '很震撼', 5, '2026-03-06 19:23:58');

-- ----------------------------
-- Table structure for t_favorite
-- ----------------------------
DROP TABLE IF EXISTS `t_favorite`;
CREATE TABLE `t_favorite`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` bigint NOT NULL,
                               `spot_id` bigint NOT NULL,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_favorite
-- ----------------------------
INSERT INTO `t_favorite` VALUES (1, 1, 1);
INSERT INTO `t_favorite` VALUES (2, 1, 16);
INSERT INTO `t_favorite` VALUES (3, 2, 1);
INSERT INTO `t_favorite` VALUES (4, 2, 16);
INSERT INTO `t_favorite` VALUES (5, 2, 17);
INSERT INTO `t_favorite` VALUES (6, 2, 20);
INSERT INTO `t_favorite` VALUES (7, 3, 2);
INSERT INTO `t_favorite` VALUES (8, 3, 5);

-- ----------------------------
-- Table structure for t_route
-- ----------------------------
DROP TABLE IF EXISTS `t_route`;
CREATE TABLE `t_route`  (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                            `intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                            `map_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                            `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_route
-- ----------------------------
INSERT INTO `t_route` VALUES (1, '📚 千年学府人文线 (半日游)', '探寻湖湘文化发源地，感受千年书院的学术气息，秋季还可赏绝美红枫。适合人文历史爱好者与亲子家庭。', 'https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=800&q=80', '人文,历史,半日游');
INSERT INTO `t_route` VALUES (2, '🚠 岳麓之巅观景线 (半日游)', '乘坐特色敞篷索道直达山顶，打卡“长沙之眼”，俯瞰湘江与星城全貌，体验高空与道教福地的完美结合。', 'https://images.unsplash.com/photo-1506744626753-1fa7603a4b28?auto=format&fit=crop&w=800&q=80', '观景,打卡,高空');
INSERT INTO `t_route` VALUES (3, '🍃 深度寻幽静心线 (一日游)', '避开拥挤人群，漫步古刹名泉，探秘山林深处的清澈湖泊与精致园林，享受岳麓山最纯粹的宁静与氧吧。', 'https://images.unsplash.com/photo-1444858291040-58f756a3bdd6?auto=format&fit=crop&w=800&q=80', '自然,休闲,一日游');
INSERT INTO `t_route` VALUES (4, '心越线', '爱心！', '\\img\\route\\xinyuexian.jpg', '热门打卡');

-- ----------------------------
-- Table structure for t_route_spot
-- ----------------------------
DROP TABLE IF EXISTS `t_route_spot`;
CREATE TABLE `t_route_spot`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `route_id` bigint NOT NULL COMMENT 'FK',
                                 `spot_id` bigint NOT NULL COMMENT 'FK',
                                 `sort` int NOT NULL COMMENT '游玩顺序 (1,2,3...)',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_route_spot
-- ----------------------------
INSERT INTO `t_route_spot` VALUES (1, 1, 23, 1);
INSERT INTO `t_route_spot` VALUES (2, 1, 16, 2);
INSERT INTO `t_route_spot` VALUES (3, 1, 17, 3);
INSERT INTO `t_route_spot` VALUES (4, 1, 1, 4);
INSERT INTO `t_route_spot` VALUES (5, 1, 2, 5);
INSERT INTO `t_route_spot` VALUES (6, 2, 23, 1);
INSERT INTO `t_route_spot` VALUES (7, 2, 27, 2);
INSERT INTO `t_route_spot` VALUES (8, 2, 26, 3);
INSERT INTO `t_route_spot` VALUES (9, 2, 22, 4);
INSERT INTO `t_route_spot` VALUES (10, 2, 11, 5);
INSERT INTO `t_route_spot` VALUES (11, 2, 4, 6);
INSERT INTO `t_route_spot` VALUES (12, 3, 24, 1);
INSERT INTO `t_route_spot` VALUES (13, 3, 3, 2);
INSERT INTO `t_route_spot` VALUES (14, 3, 6, 3);
INSERT INTO `t_route_spot` VALUES (15, 3, 5, 4);
INSERT INTO `t_route_spot` VALUES (16, 3, 14, 5);
INSERT INTO `t_route_spot` VALUES (17, 4, 17, 1);
INSERT INTO `t_route_spot` VALUES (18, 4, 5, 2);
INSERT INTO `t_route_spot` VALUES (19, 4, 25, 3);
INSERT INTO `t_route_spot` VALUES (20, 4, 28, 4);
INSERT INTO `t_route_spot` VALUES (21, 4, 22, 5);
INSERT INTO `t_route_spot` VALUES (22, 4, 21, 6);
INSERT INTO `t_route_spot` VALUES (23, 4, 18, 7);
INSERT INTO `t_route_spot` VALUES (24, 4, 13, 8);
INSERT INTO `t_route_spot` VALUES (25, 4, 16, 9);

-- ----------------------------
-- Table structure for t_spot
-- ----------------------------
DROP TABLE IF EXISTS `t_spot`;
CREATE TABLE `t_spot`  (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                           `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                           `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                           `price` decimal(10, 2) NULL DEFAULT NULL,
                           `duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                           `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                           `view_count` int NULL DEFAULT 0,
                           `score` double NULL DEFAULT 0,
                           `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'https://api.dujin.org/bing/1920.php' COMMENT '景点封面图',
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_spot
-- ----------------------------
INSERT INTO `t_spot` VALUES (1, '岳麓书院', '千年学府，四大书院之一', '<p>位于岳麓山脚下，湖湘文化发源地，北宋开宝九年创建...</p>', 40.00, '2小时', '历史人文', 1250, 4.8, '/img/yuelushuyuan.jpg');
INSERT INTO `t_spot` VALUES (2, '爱晚亭', '停车坐爱枫林晚，霜叶红于二月花', '<p>中国四大名亭之一，秋季赏枫绝佳去处。始建于清乾隆五十七年...</p>', 0.00, '1小时', '自然风光', 1800, 4.9, '/img/aiwan.jpg');
INSERT INTO `t_spot` VALUES (3, '麓山寺', '汉魏最初名胜，湖湘第一道场', '<p>湖南省最古老的佛寺之一，藏于深山古木中。始建于西晋泰始四年...</p>', 0.00, '1小时', '宗教祈福', 980, 4.7, '/img/lushansi.jpg');
INSERT INTO `t_spot` VALUES (4, '云麓宫', '道教七十二福地之第二十三福地', '<p>岳麓山道教圣地，可俯瞰湘江和长沙城。位于云麓峰顶...</p>', 0.00, '0.5小时', '宗教祈福', 850, 4.6, '/img/yunlugong.jpg');
INSERT INTO `t_spot` VALUES (5, '穿石坡湖', '山林深处的静谧湖泊', '<p>湖水清澈，四周林木掩映，有木栈道可供漫步。被称为岳麓山之眼...</p>', 0.00, '1小时', '自然风光', 1100, 4.8, '/img/chuanshipohu.jpg');
INSERT INTO `t_spot` VALUES (6, '白鹤泉', '麓山第一泉，泉水清冽甘甜', '<p>因传说有白鹤栖息而得名。泉水清冽甘甜，被誉为麓山第一泉...</p>', 0.00, '0.5小时', '自然风光', 620, 4.5, '/img/baihequan.jpg');
INSERT INTO `t_spot` VALUES (7, '黄兴墓', '近代民主革命家黄兴的长眠之地', '<p>墓塔如利剑直指苍天，象征着黄兴将军不畏强权、仗剑护正义...</p>', 0.00, '0.5小时', '历史人文', 530, 4.6, '/img/huangxingmu.jpg');
INSERT INTO `t_spot` VALUES (8, '蔡锷墓', '护国名将蔡锷的纪念墓冢', '<p>掩映在古枫翠柏中，是岳麓山大型墓葬之一，彰显将军历史功绩...</p>', 0.00, '0.5小时', '历史人文', 500, 4.5, '/img/caiemu.jpg');
INSERT INTO `t_spot` VALUES (9, '禹王碑', '岳麓山现存最古老的碑刻之一', '<p>相传为大禹治水时所刻，字体奇古，被称为蝌蚪文...</p>', 0.00, '0.5小时', '历史人文', 420, 4.4, '/img/yuwangbei.jpg');
INSERT INTO `t_spot` VALUES (10, '舍利塔', '隋代佛教舍利塔，造型古朴', '<p>位于清风峡内，始建于隋代，塔身由花岗岩砌成，结构精巧...</p>', 0.00, '0.5小时', '宗教祈福', 350, 4.3, '/img/shelita.jpg');
INSERT INTO `t_spot` VALUES (11, '长沙电视塔(长沙之眼)', '登麓山之巅，览星城千年', '<p>塔高88米，海拔350米，是长沙独一无二的高空沉浸式城市文化观景平台...</p>', 48.00, '1小时', '现代游乐', 2200, 4.7, '/img/csdianshita.jpg');
INSERT INTO `t_spot` VALUES (12, '岳麓山索道', '直达山顶观景平台，俯瞰林海', '<p>敞篷式索道，可感受穿梭在林间的独特体验，上行50元，下行40元...</p>', 50.00, '0.5小时', '现代游乐', 3100, 4.8, '/img/suodao.jpg');
INSERT INTO `t_spot` VALUES (13, '岳王亭', '昭示民众投身抗日运动而建', '<p>六角攒尖重檐顶，亭内立有石碑一座，阴刻岳王遗像...</p>', 0.00, '0.5小时', '历史人文', 290, 4.2, '/img/yuewangtin.jpg');
INSERT INTO `t_spot` VALUES (14, '万景园', '集盆景生产、展示、游览为一体', '<p>内有温室、花房等，园中盆景如雨后湖山、砥柱中流等多姿多彩...</p>', 0.00, '1小时', '自然风光', 410, 4.5, '/img/wanjingyuan.jpg');
INSERT INTO `t_spot` VALUES (15, '鸟语林', '汇集全球400余种近万只鸟类', '<p>中南地区一处集鸟类观赏、科普教育、生态保护于一体的综合性景区...</p>', 50.00, '2小时', '现代游乐', 1500, 4.6, '/img/niaoyulin.jpg');
INSERT INTO `t_spot` VALUES (16, '湖南大学', '没有围墙的开放式千年学府', '<p>坐落于湘江之滨、岳麓山下，享有“千年学府，百年名校”之誉。校园环境优美，历史人文气息浓厚。</p>', 0.00, '2小时', '历史人文', 2500, 4.8, '/img/hunandaxue.jpg');
INSERT INTO `t_spot` VALUES (17, '东方红广场', '湖南省所有大学里面唯一的红色广场', '<p>湖南大学东方红广场是湖南大学于文化大革命期间修建的，当时为了热爱毛主席，于是修建了这个广场，同时湖南大学美术系设计了毛主席雕像。</p>', 0.00, '0.5小时', '历史人文', 1600, 4.7, '/img/dongfanghongguangchang.jpg');
INSERT INTO `t_spot` VALUES (18, '陆军第73军第77师抗日阵亡将士纪念碑', '抗日英烈铭记之地', '<p>位于岳麓山赫石坡上，刻有约600位抗战阵亡将士的名字，是抗战时期重要的历史见证与爱国主义教育基地。</p>', 0.00, '0.5小时', '历史人文', 850, 4.8, '/img/lujunjinianbei.jpg');
INSERT INTO `t_spot` VALUES (19, '观光长廊', '坐落在岳麓山脊中部，全长140米', '<p>集休闲、娱乐、登高揽胜为一体。登上观光长廊，岳麓山之灵秀、橘子洲之飘逸、长沙城之厚重融为一体。</p>', 0.00, '0.5小时', '自然风光', 1300, 4.6, '/img/guangunagchanglang.jpg');
INSERT INTO `t_spot` VALUES (20, '古炮台', '抗战时期主要炮兵阵地', '<p>景区观光车终点站“观光长廊”下有古炮台，是当年长沙会战的重要历史遗迹，见证了长沙城的战火与坚韧。</p>', 0.00, '0.5小时', '历史人文', 920, 4.5, '/img/gupaotai.jpg');
INSERT INTO `t_spot` VALUES (21, '园庆林', '多功能综合性公园', '<p>一处知名的旅游胜地，有七里山、古茶园、樱花树等景观，还提供了丰富的文化活动，有宫殿剧表演等。</p>', 0.00, '1小时', '自然风光', 780, 4.4, '/img/yuanqinglin.jpg');
INSERT INTO `t_spot` VALUES (22, '岳麓山观景平台', '俯瞰长沙城的绝佳位置', '<p>位于山顶区域，登高望远，可一览无余地将湘江和长沙城区的美景尽收眼底，是游客必打卡之地。</p>', 0.00, '0.5小时', '自然风光', 3200, 4.9, '/img/guanjingpingtai.jpg');
INSERT INTO `t_spot` VALUES (23, '岳麓山东门', '岳麓山主要入口之一', '<p>交通极其便利，紧邻地铁站，周边餐饮服务设施齐全，是绝大多数游客登山的首选起点。</p>', 0.00, '0小时', '现代游乐', 4500, 4.5, '/img/dongmen.jpg');
INSERT INTO `t_spot` VALUES (24, '岳麓山南门', '靠近大学城的入口', '<p>毗邻湖南大学和东方红广场，文化氛围浓厚，从这里上山可以一路感受百年学府的学术气息。</p>', 0.00, '0小时', '现代游乐', 3800, 4.6, '/img/nanmen.jpg');
INSERT INTO `t_spot` VALUES (25, '岳麓山西门游客中心', '桃花岭方向的综合服务中心', '<p>位于西侧，主要面向自驾游和桃花岭方向的游客，提供详细的导览服务、停车与休息区域。</p>', 0.00, '0.5小时', '现代游乐', 1200, 4.4, '/img/ximen.jpg');
INSERT INTO `t_spot` VALUES (26, '岳麓山索道上站', '直达山顶观景平台', '<p>敞篷索道的终点，下车即可步行至观景平台，是体力有限游客登顶的最佳选择。</p>', 0.00, '0小时', '现代游乐', 2800, 4.7, '/img/suodao.jpg');
INSERT INTO `t_spot` VALUES (27, '岳麓山索道下站', '索道起点，体验林间穿梭', '<p>位于东门附近，从这里购票上车，上行票价50元/人，下行40元/人，开启林间穿梭之旅。</p>', 0.00, '0小时', '现代游乐', 2900, 4.6, '/img/suodao.jpg');
INSERT INTO `t_spot` VALUES (28, '岳麓山心悦线补给站', '登山途中的能量补给', '<p>为徒步登山的游客精心准备的休息站点，提供饮水、小吃、医疗急救等贴心服务。</p>', 0.00, '0.5小时', '现代游乐', 950, 4.5, '/img/xinyuexianbujizhan.jpg');
INSERT INTO `t_spot` VALUES (30, 'ceshi', '666', '', 0.00, '', '', 0, 0, '');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'PK, Auto Increment',
                           `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Unique',
                           `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                           `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'test01', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '第一个测试游客', '2026-02-26 14:32:52');
INSERT INTO `t_user` VALUES (2, 'test02', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '知音用户', '2026-03-03 22:33:41');
INSERT INTO `t_user` VALUES (3, 'test03', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '路人用户', '2026-03-03 22:33:41');
INSERT INTO `t_user` VALUES (4, 'history_fan1', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '楚风汉韵', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (5, 'history_fan2', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '考古小达人', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (6, 'history_fan3', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '历史长河', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (7, 'nature_lover1', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '森系少女', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (8, 'nature_lover2', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '山水游人', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (9, 'nature_lover3', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '绿野寻踪', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (10, 'religion_seeker1', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '云水禅心', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (11, 'religion_seeker2', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '寻道岳麓', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (12, 'modern_kid1', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '都市探险家', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (13, 'modern_kid2', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '高空爱好者', '2026-03-05 17:29:37');
INSERT INTO `t_user` VALUES (14, 'zk', '$2a$10$OfkTWgAUypWphmIWIZxQhuAChLKyE/PJbF4qxxRJWCpc3xkrFZFny', 'z', '2026-03-06 15:39:04');
INSERT INTO `t_user` VALUES (15, 'admin', '$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG', '超级管理员', '2026-03-07 01:08:50');

SET FOREIGN_KEY_CHECKS = 1;
