package com.cn.hzm.core.exception;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum ExceptionCode {

  /**
   * amazon错误
   */
  REQUEST_LIMIT("10001", "限流"),

  /**
   * 业务错误
   */
  INTERNAL_SERVER_ERR("20001", "系统内部错误")
  ;



  @Getter
  @Accessors(fluent = true)
  String code;

  @Getter
  @Accessors(fluent = true)
  String desc;

  ExceptionCode(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }
}
