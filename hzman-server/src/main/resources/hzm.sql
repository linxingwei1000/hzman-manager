drop table hzm.hzm_item;
CREATE TABLE hzm.hzm_item (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `asin` varchar(128) not null default '' comment 'ASIN',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_asin` (`asin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';
