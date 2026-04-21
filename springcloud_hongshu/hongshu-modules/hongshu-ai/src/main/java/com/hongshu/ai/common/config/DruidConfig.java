package com.hongshu.ai.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * druid 配置多数据源
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Configuration
public class DruidConfig {

    @Primary
    @Bean("masterTransactionManager")
    public DataSourceTransactionManager masterTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("slaveTransactionManager")
    @ConditionalOnBean(name = "slaveDataSource")
    public PlatformTransactionManager slaveTransactionManager(@Qualifier("slaveDataSource") DataSource slaveDataSource) {
        return new DataSourceTransactionManager(slaveDataSource);
    }

}

