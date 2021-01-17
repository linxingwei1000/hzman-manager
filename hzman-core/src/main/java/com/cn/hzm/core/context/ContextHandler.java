package com.cn.hzm.core.context;

import com.cn.hzm.core.constant.ContextConst;
import com.cn.hzm.core.util.HttpUtil;
import com.cn.hzm.core.util.RandomUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by yuyang04 on 2021/1/9.
 */
public class ContextHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextHandler.class);

    public void initContext(ContentCachingRequestWrapper requestWrapper) {
        HzmContext context = HzmContext.init(RandomUtil.uuidWithoutSymbol());

        context.setPeer(HttpUtil.getRemoteIpAddress(requestWrapper));
        MDC.put(ContextConst.TRACE_ID, context.getTraceId());

        context.setBody(new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));

        context.setParam(requestWrapper.getQueryString());

        LOGGER.info("uri: {}, original request body: {}, header: {}, path param: {}.",
                requestWrapper.getRequestURI(), context.getBody(), getHeaderString(requestWrapper), context.getParam());
    }

    public void destroyContext() {
        HzmContext.get().unload();
        MDC.clear();
    }

    private String getHeaderString(ContentCachingRequestWrapper requestWrapper) {
        Enumeration<String> headers = requestWrapper.getHeaderNames();
        List<String> headKVs = Lists.newArrayList();

        while (headers.hasMoreElements()) {
            String key = headers.nextElement();
            String value = requestWrapper.getHeader(key);
            headKVs.add(key + "=" + value);
        }

        return StringUtils.join(headKVs, "#");
    }
}
