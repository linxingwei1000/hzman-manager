package com.cn.hzm.core.exception;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum ExceptionCode {

  /**
   * amazon错误
   */
  REQUEST_LIMIT(10001, "限流"),
  REQUEST_SKU_REQUEST_ERROR(10002, "sku请求出错:"),

  /**
   * 业务错误
   */
  INTERNAL_SERVER_ERR(20001, "系统内部错误"),
  FTP_UPLOAD_ERR(20002, "ftp上传失败，请重试"),
  ORDER_DELETE_ILLEGAL(20003, "订单删除操作失败：只有厂家确认前的订单可以删除"),
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
