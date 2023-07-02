package com.cn.hzm.server.interceptor;

import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.enums.AwsMarket;
import com.cn.hzm.core.repository.dao.AwsUserMarketDao;
import com.cn.hzm.core.repository.entity.AwsUserMarketDo;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.api.dto.ThreadLocalUserDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yuyang04 on 2020/7/25.
 */
@Component
public class HzmCommonInterceptor implements HandlerInterceptor {

    @Autowired
    private AwsUserMarketDao awsUserMarketDao;

    private static Logger logger = LoggerFactory.getLogger(HzmCommonInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ThreadLocalUserDto user = new ThreadLocalUserDto();
        String awsUserId = request.getHeader("awsUserId");
        String marketId = request.getHeader("marketId");
        user.setAwsUserId(StringUtils.isEmpty(awsUserId) ? 1 : Integer.parseInt(awsUserId));
        user.setMarketId(StringUtils.isEmpty(marketId) ? AwsMarket.America.getId() : marketId);

        AwsUserMarketDo awsUserMarketDo = awsUserMarketDao.getByUserIdAndMarketId(user.getAwsUserId(), user.getMarketId());
        user.setUserMarketId(awsUserMarketDo.getId());
        ThreadLocalCache.setUser(user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HzmContext.get().exception(ex);
        ThreadLocalCache.clean();
    }
}
