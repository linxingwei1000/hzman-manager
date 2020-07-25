package com.cn.hzm.server.handler;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.constant.ResponseCode;
import com.cn.hzm.core.exception.HzmUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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

        return new ResponseEntity<>(new HzmResponse(ResponseCode.BAD_REQUEST), new HttpHeaders(), HttpStatus.OK);
    }

    @ExceptionHandler({HzmUnauthorizedException.class})
    public ResponseEntity<Object> handlerHzmUnauthorizedException(HzmUnauthorizedException ex, WebRequest request) {
        logger.error(ex.getMessage());

        return new ResponseEntity<>(new HzmResponse(ex.getCode(), ex.getMessage()), new HttpHeaders(), HttpStatus.OK);
    }
}
