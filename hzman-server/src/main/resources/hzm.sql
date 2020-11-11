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


drop table hzm.hzm_factory;
CREATE TABLE hzm.hzm_factory (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `factory_name` varchar(128) NOT NULL COMMENT '厂家名',
  `address` varchar(255) default '' COMMENT '厂家地址',
  `contact_person` varchar(128) NOT NULL COMMENT '厂家联系人',
  `contact_info` varchar(128) default '' COMMENT '厂家联系方式',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='厂家管理表';

drop table hzm.hzm_factory_order;
CREATE TABLE hzm.hzm_factory_order (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `factory_id` int(11) unsigned NOT NULL COMMENT '厂家id',
  `item_id` int(11) unsigned NOT NULL COMMENT '商品id',
  `order_num` int(11) unsigned NOT NULL COMMENT '订货数量',
  `remark` varchar(255) default '' COMMENT '备注',
  `item_price` double unsigned default 0 COMMENT '商品单价，厂家填写',
  `delivery_date` varchar(64) default NULL COMMENT '厂家交货日期，厂家填写',
  `waybill_num` varchar(255) default NULL COMMENT '运单编号',
  `receive_num` int(11) unsigned default 0 COMMENT '确认收货数量',
  `payment_voucher` varchar(255) default NULL COMMENT '付款凭证，图片地址',
  `order_status` tinyint(4) unsigned NOT NULL COMMENT '订单状态',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='厂家订单表';
