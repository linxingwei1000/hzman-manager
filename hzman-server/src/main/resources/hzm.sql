drop table hzm.hzm_item;
CREATE TABLE hzm.hzm_item (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `asin` varchar(128) not null comment 'ASIN',
  `sku` varchar(128) not null comment 'sku，用户侧填写',
  `title` varchar(255) not null comment '商品名称',
  `icon` varchar(512) default '' comment '商品图片',
  `marketplace_id` varchar(128) not null comment '商品市场id',
  `attribute_set` varchar(4096) default '' comment '商品属性相关',
  `relationship` varchar(4096) default '' comment '价格相关参数',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sku` (`sku`),
  KEY `idx_asin` (`asin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品表';


drop table hzm.hzm_inventory;
CREATE TABLE hzm.hzm_inventory (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `asin` varchar(128) not null comment 'ASIN',
  `sku` varchar(128) not null comment 'sku，用户侧填写',
  `fnsku` varchar(128) not null comment '仓库唯一标志',
  `total_quantity` int(11) unsigned NOT NULL COMMENT '一共',
  `amazon_quantity` int(11) unsigned NOT NULL COMMENT '亚马逊一共',
  `amazon_stock_quantity` int(11) unsigned NOT NULL COMMENT '亚马逊库存',
  `amazon_transfer_quantity` int(11) unsigned NOT NULL COMMENT '转移中数量',
  `aws_inbound_quantity` int(11) unsigned NOT NULL COMMENT '入库中数量',
  `local_quantity` int(11) unsigned NOT NULL COMMENT '本地库存',
  `item_condition` varchar(128) default null comment '商品状态',
  `earliest_availability` varchar(128) default null comment '库存可供应取货的最早日期',
  `supply_detail` varchar(4096) default null comment '库存细节',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sku` (`sku`),
  KEY `idx_asin` (`asin`)
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
  `sku` varchar(128) not null default '' comment 'sku，用户侧填写',
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


drop table hzm.hzm_operate_depend;
CREATE TABLE hzm.hzm_operate_depend (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `operate_key` varchar(128) not null comment '操作项',
  `operate_value` varchar(128) not null comment '依赖值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作依赖表';


drop table hzm.hzm_amazon_order;
CREATE TABLE hzm.hzm_amazon_order (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `amazon_order_id` varchar(255) NOT NULL COMMENT '亚马逊所定义的订单编码',
  `seller_order_id` varchar(255) default '' COMMENT '卖家所定义的订单编码',
  `purchase_date` datetime NOT NULL COMMENT '创建订单日期',
  `last_update_date` datetime NOT NULL COMMENT '订单最后更新日期',
  `order_status` varchar(255) NOT NULL COMMENT '订单状态',
  `fulfillment_channel` varchar(255) default '' COMMENT '订单配送方式：亚马逊配送 (AFN) 或卖家自行配送 (MFN)',
  `sales_channel` varchar(255) default '' COMMENT '订单中第一件商品销售渠道',
  `order_channel` varchar(255) default '' COMMENT '订单中第一件商品订单渠道',
  `ship_service_level` varchar(255) default '' COMMENT '货件服务水平',
  `shipping_address` varchar(1024) default '' COMMENT '订单的配送地址',
  `order_amount` double unsigned default 0 COMMENT '订单费用',
  `order_currency_code` varchar(16) default '' COMMENT '订单货币代码',
  `number_shipped` tinyint(4) unsigned NOT NULL COMMENT '已配送商品数量',
  `number_un_shipped` tinyint(4) unsigned NOT NULL COMMENT '未配送商品数量',
  `payment_detail` varchar(1024) default '' COMMENT '付款方式相关信息',
  `payment_method` varchar(255) default '' COMMENT '订单主要付款方式',
  `marketplace_id` varchar(255) default '' COMMENT '订单生成所在商城匿名编码',
  `buyer_email` varchar(255) default '' COMMENT '买家匿名电子邮件地址',
  `buyer_name` varchar(255) default '' COMMENT '买家姓名',
  `shipment_service_level_category` varchar(255) default '' COMMENT '订单的配送服务级别分类',
  `shipped_by_amazonTFM` tinyint(4) default 0 COMMENT '指明订单配送方是否是亚马逊配送 (Amazon TFM) 服务',
  `TFM_shipment_status` varchar(255) default '' COMMENT '亚马逊 TFM订单的状态',
  `cba_displayable_shipping_label` varchar(255) default '' COMMENT '卖家自定义的配送方式',
  `order_type` varchar(255) NOT NULL COMMENT '订单类型',
  `earliest_ship_date` datetime default NULL COMMENT '您承诺的订单发货时间范围的第一天',
  `latest_ship_date` datetime default NULL COMMENT '您承诺的订单发货时间范围的最后一天',
  `earliest_delivery_date` datetime default NULL COMMENT '您承诺的订单送达时间范围的第一天',
  `latest_delivery_date` datetime default NULL COMMENT '您承诺的订单送达时间范围的最后一天',
  `is_replacement_order` tinyint(4) default 0 COMMENT '',
  `is_business_order` tinyint(4) default 0 COMMENT '',
  `is_global_express_enabled` tinyint(4) default 0 COMMENT '',
  `is_sold_byAB` tinyint(4) default 0 COMMENT '',
  `is_premium_order` tinyint(4) default 0 COMMENT '',
  `is_ispu` tinyint(4) default 0 COMMENT '',
  `is_prime` tinyint(4) default 0 COMMENT '',
  `other_config` varchar(2048) default '' COMMENT '订单相关其他信息',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='amazon订单表';


drop table hzm.hzm_amazon_order_item;
CREATE TABLE hzm.hzm_amazon_order_item (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `amazon_order_id` varchar(255) NOT NULL COMMENT '亚马逊所定义的订单编码',
  `asin` varchar(128) not null comment 'ASIN',
  `sku` varchar(128) not null default '' comment 'sku，用户侧填写',
  `order_item_id` varchar(255) default '' COMMENT '亚马逊定义的订单商品识别号',
  `title` varchar(255) default '' COMMENT '商品名称',
  `quantity_ordered` tinyint(4) unsigned NOT NULL COMMENT '订单数量',
  `quantity_shipped` tinyint(4) unsigned NOT NULL COMMENT '已配送的商品数量',
  `item_price_amount` double unsigned default 0 COMMENT '订单费用',
  `item_price_currency_code` varchar(16) default '' COMMENT '订单货币代码',
  `item_tax_amount` double unsigned default 0 COMMENT '订单税费',
  `item_tax_currency_code` varchar(16) default '' COMMENT '订单税费货币代码',
  `shipping_price_amount` double unsigned default 0 COMMENT '运费',
  `shipping_price_currency_code` varchar(16) default '' COMMENT '运费货币代码',
  `shipping_tax_amount` double unsigned default 0 COMMENT '运费税费',
  `shipping_tax_currency_code` varchar(16) default '' COMMENT '运费税费货币代码',
  `gift_wrap_price_amount` double unsigned default 0 COMMENT '商品的礼品包装费',
  `gift_wrap_price_currency_code` varchar(16) default '' COMMENT '商品的礼品包装货币代码',
  `gift_wrap_tax_amount` double unsigned default 0 COMMENT '商品的礼品包装税费',
  `gift_wrap_tax_currency_code` varchar(16) default '' COMMENT '商品的礼品包装税费货币代码',
  `shipping_discount_amount` double unsigned default 0 COMMENT '运费的折扣',
  `shipping_discount_currency_code` varchar(16) default '' COMMENT '运费的折扣货币代码',
  `shipping_discount_tax_amount` double unsigned default 0 COMMENT '运费的折扣税费',
  `shipping_discount_tax_currency_code` varchar(16) default '' COMMENT '运费的折扣税费货币代码',
  `promotion_discount_amount` double unsigned default 0 COMMENT '促销折扣总计',
  `promotion_discount_currency_code` varchar(16) default '' COMMENT '促销折扣货币代码',
  `promotion_discount_tax_amount` double unsigned default 0 COMMENT '促销折扣总计税费',
  `promotion_discount_tax_currency_code` varchar(16) default '' COMMENT '促销折扣税费货币代码',
  `promotion_ids` varchar(255) default '' COMMENT 'PromotionId 元素列表',
  `cod_fee_amount` double unsigned default 0 COMMENT 'COD 服务费用',
  `cod_fee_currency_code` varchar(16) default '' COMMENT 'COD 服务费用货币代码',
  `cod_fee_discount_amount` double unsigned default 0 COMMENT 'COD 服务费用',
  `cod_fee_discount_currency_code` varchar(16) default '' COMMENT 'COD 服务费用货币代码',
  `gift_message_text` varchar(255) default '' COMMENT '买家提供的礼品消息',
  `gift_wrap_level` varchar(255) default '' COMMENT '买家指定的礼品包装等级',
  `invoice_data` varchar(512) default '' COMMENT '发票信息(仅适用于中国)',
  `condition_note` varchar(255) default '' COMMENT '卖家描述的商品状况',
  `condition_id` varchar(255) default '' COMMENT '商品的状况',
  `condition_subtypeId` varchar(255) default '' COMMENT '商品的子状况',
  `scheduled_delivery_start_date` datetime default NULL COMMENT '订单预约送货上门的开始日期（目的地时区)',
  `scheduled_delivery_end_date` datetime default NULL COMMENT '订单预约送货上门的终止日期（目的地时区）',
  `tax_collection` varchar(255) default '' COMMENT '',
  `product_info` varchar(255) default '' COMMENT '',
  `is_gift` tinyint(4) default 0 COMMENT '',
  `is_transparency` tinyint(4) default 0 COMMENT '',
  `other_config` varchar(2048) default '' COMMENT '商品订单相关其他信息',
  `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `utime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='amazon订单商品表';