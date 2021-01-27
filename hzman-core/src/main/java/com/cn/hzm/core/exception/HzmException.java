package com.cn.hzm.core.exception;

import lombok.Getter;

public class HzmException extends RuntimeException {

  @Getter
  private ExceptionCode exceptionCode;

  public HzmException(ExceptionCode exceptionCode) {
    super(exceptionCode.desc);
    this.exceptionCode = exceptionCode;
  }

  public HzmException(ExceptionCode exceptionCode, String message) {
    super(exceptionCode.desc + message);
    this.exceptionCode = exceptionCode;
  }

  public HzmException(ExceptionCode exceptionCode, String message, Throwable cause) {
    super(message, cause);
    this.exceptionCode = exceptionCode;
  }

}
