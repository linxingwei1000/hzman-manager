drop table hzm.hzm_item;
CREATE TABLE hzm.hzm_item (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `asin` varchar(128) default '' comment 'ASIN',
  `sku` varchar(128) not null default '' comment 'sku，用户侧填写',
  `fnsku` varchar(128) default '' comment '库存相关sku',
  `attrs` varchar(4096) default '' comment '属性相关',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_asin` (`asin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';


drop table hzm.hzm_inventory;
CREATE TABLE hzm.hzm_inventory (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `item_id` int(11) unsigned NOT NULL COMMENT '商品id',
  `total_quantity` int(11) unsigned NOT NULL COMMENT '一共',
  `aws_quantity` int(11) unsigned NOT NULL COMMENT '亚马逊一共',
  `aws_stock_quantity` int(11) unsigned NOT NULL COMMENT '亚马逊库存',
  `local_quantity` int(11) unsigned NOT NULL COMMENT '本地库存',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='库存表';
