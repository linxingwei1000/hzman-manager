-- 权限表
CREATE TABLE `hzm_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限id',
  `type` varchar(32) NOT NULL COMMENT '权限类型',
  `code` varchar(32) NOT NULL COMMENT '权限code',
  `description` varchar(32) DEFAULT NULL COMMENT '描述',
  `create_time` bigint(20) DEFAULT '0' COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT '0' COMMENT '更新时间',
  `last_opt_user_id` bigint(20) DEFAULT NULL COMMENT '最后操作人id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='hzm权限表'