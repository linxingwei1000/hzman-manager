package com.cn.hzm.core.exception;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum ExceptionCode {

  /**
   * amazon错误
   */
  REQUEST_LIMIT(10001, "限流"),
  REQUEST_SKU_REQUEST_ERROR(10002, "sku请求出错:"),
  USER_NO_EXIST(10003, "用户不存在"),
  USER_EXIST(10004, "用户已存在"),
  USER_PASSWORD_ERROR(10005, "账号密码错误"),
  USER_ROLE_MUST(10006, "用户角色必选"),
  USER_PASSWORD_MOD_ERROR(10007, "无法修改其他用户密码"),
  USER_ROLE_FACTORY_MUST_CHOOSE_FACTORY(10008, "厂家角色必须选择厂家"),
  SHIPMENT_ID_NOT_EXIST(10009, "amazon入库货物单号不存在"),
  SHIPMENT_ID_FAIL_RETRY(10010, "amazon入库货物单号处理失败，请重试"),

  /**
   * 业务错误
   */
  INTERNAL_SERVER_ERR(20001, "系统内部错误"),
  FTP_UPLOAD_ERR(20002, "ftp上传失败，请重试"),
  ORDER_DELETE_ILLEGAL(20003, "订单删除操作失败：只有厂家确认前的订单可以删除"),

  FACTORY_NO_EXIST(20004, "工厂不存在"),
  FACTORY_ORDER_ITEM_MUST(20005, "工厂订单商品必选"),
  FACTORY_ORDER_ITEM_NUM_MUST(20006, "工厂订单商品数量必填"),
  FACTORY_ORDER_ITEM_DUPLICATE(20007, "订单添加重复商品"),
  FACTORY_ORDER_ILLEGAL(20008, "当前订单状态不能修改"),
  FACTORY_ORDER_CONFIRM_DATE_MUST(20009, "厂家交货日期必填"),

  ;



  @Getter
  @Accessors(fluent = true)
  Integer code;

  @Getter
  @Accessors(fluent = true)
  String desc;

  ExceptionCode(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }
}
