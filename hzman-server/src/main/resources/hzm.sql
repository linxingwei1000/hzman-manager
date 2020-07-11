create table hzm.hzm_item(
  id int(8) primary key auto_increment,
  asin varchar(128) not null default '' comment 'ASIN',
  ctime bigint(20) default 0 not null comment '创建时间',
  utime bigint(20) default 0 not null comment '更新时间')ENGINE=InnoDB DEFAULT CHARSET=utf8;


drop table hzm.hzm_item;
CREATE TABLE hzm.hzm_item (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `asin` varchar(128) not null default '' comment 'ASIN',
  `ctime` bigint(20) default 0 not null comment '创建时间',
  `utime` bigint(20) default 0 not null comment '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_asin` (`asin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';
