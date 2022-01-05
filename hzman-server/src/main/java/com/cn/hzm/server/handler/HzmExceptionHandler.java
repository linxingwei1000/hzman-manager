package com.cn.hzm.server.handler;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.constant.ResponseCode;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmBasicRuntimeException;
import com.cn.hzm.core.exception.HzmUnauthorizedException;
import com.cn.hzm.core.exception.HzmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@ControllerAdvice
public class HzmExceptionHandler extends ResponseEntityExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(HzmExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handlerIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.OK).body(new HzmResponse(ResponseCode.BAD_REQUEST));
    }

    @ExceptionHandler({HzmUnauthorizedException.class})
    public ResponseEntity<Object> handlerHzmUnauthorizedException(HzmUnauthorizedException ex, WebRequest request) {
        logger.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.OK).body(new HzmResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({HzmBasicRuntimeException.class})
    public ResponseEntity<Object> handlerHzmBasicRuntimeException(HzmBasicRuntimeException ex, WebRequest request) {
        logger.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.OK).body(new HzmResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({HzmException.class})
    public ResponseEntity<Object> handlerHzmException(HzmException ex, WebRequest request) {
        logger.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(new HzmResponse(ex.getExceptionCode().code(), ex.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handlerException(Exception ex, WebRequest request) {
        logger.error("系统内部错误：", ex);
        return ResponseEntity.status(HttpStatus.OK).body(new HzmResponse(ExceptionCode.INTERNAL_SERVER_ERR.code(),ExceptionCode.INTERNAL_SERVER_ERR.desc()));
    }
}
