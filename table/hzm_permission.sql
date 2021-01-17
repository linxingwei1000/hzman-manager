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

-- 用户角色表
CREATE TABLE `hzm_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户角色id',
  `passport_id` bigint(20) NOT NULL COMMENT '用户账户id',
  `role_id` varchar(20) DEFAULT NULL COMMENT '角色id',
  `valid` tinyint(4) DEFAULT '1' COMMENT '是否有效，1有效，2无效',
  `create_time` bigint(20) DEFAULT '0' COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT '0' COMMENT '更新时间',
  `last_opt_user_id` bigint(20) DEFAULT NULL COMMENT '最后操作用户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_passportId_roleId` (`passport_id`,`role_id`),
  KEY `idx_passportId_valid` (`passport_id`,`valid`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='hzm用户角色表';