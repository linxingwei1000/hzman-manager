package com.cn.hzm.server.dto;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/25 3:31 下午
 */
@Data
@ApiModel(description = "分页DTO")
public class PageDTO {

    Integer pageNum;

    Integer pageSize;

    public <T> List<T> pageResult(List<T> source) {
        Integer begin = pageSize * (pageNum - 1);
        Integer end = pageSize * pageNum;

        Integer sourceSize = source.size();
        if (sourceSize <= begin) {
            return Lists.newArrayList();
        } else if (begin < sourceSize && sourceSize <= end){
            return source.subList(begin, sourceSize);
        }else {
            return source.subList(begin, end);
        }
    }
}
