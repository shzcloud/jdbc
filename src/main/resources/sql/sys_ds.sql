SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_ds
-- ----------------------------
DROP TABLE IF EXISTS `sys_ds`;
CREATE TABLE `sys_ds`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '数据源名称',
  `driver_class_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '驱动类全限定名',
  `url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '连接信息',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `AK_ux_sys_ds_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统数据源' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_table_node
-- ----------------------------
DROP TABLE IF EXISTS `sys_table_node`;
CREATE TABLE `sys_table_node`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表名',
  `node` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点名',
  `ds_id` bigint UNSIGNED NOT NULL COMMENT '数据源id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `AK_ux_sys_table_node_tn`(`table_name`, `node`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统表节点' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_table_node_transfer_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_table_node_transfer_info`;
CREATE TABLE `sys_table_node_transfer_info`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '表名',
  `node` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '节点名',
  `dilatation` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否扩容',
  `finished` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否完成',
  `transfer_info` blob NULL COMMENT '转移信息',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `AK_ux_sys_table_node_transfer_info`(`table_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统表节点转移信息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
