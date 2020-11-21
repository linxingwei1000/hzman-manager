package com.cn.hzm.core.configure;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 4:33 下午
 */
@Configuration
@MapperScan(basePackages = {
        "com.cn.hzm.factory.dao",
        "com.cn.hzm.item.dao",
        "com.cn.hzm.order.dao",
        "com.cn.hzm.stock.dao",
        "com.cn.hzm.server.dao"})
@EnableTransactionManagement
@Slf4j
public class MybatisPlusConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactories() throws Exception {
        log.info("------------- 重载父类 sqlSessionFactory init-------");
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(resolver.getResources("classpath*:/mapper/*.xml"));
        return factory.getObject();
    }

}
