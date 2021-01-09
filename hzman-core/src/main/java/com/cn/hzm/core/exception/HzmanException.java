package com.cn.hzm.core.exception;

import lombok.Getter;

public class HzmanException extends RuntimeException {

  @Getter
  private ExceptionCode exceptionCode;

  public HzmanException(ExceptionCode exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  public HzmanException(ExceptionCode exceptionCode, String message) {
    super(message);
    this.exceptionCode = exceptionCode;
  }

  public HzmanException(Integer code, String message) {
    super(message);
    this.exceptionCode = exceptionCode;
  }

  public HzmanException(ExceptionCode exceptionCode, String message, Throwable cause) {
    super(message, cause);
    this.exceptionCode = exceptionCode;
  }

}
