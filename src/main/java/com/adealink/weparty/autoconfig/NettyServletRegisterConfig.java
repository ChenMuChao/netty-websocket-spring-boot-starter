package com.adealink.weparty.autoconfig;

import com.adealink.weparty.servlet.fitler.ApplicationFilterConfigRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc: 配置过滤器
 * @author: caokunliang
 * creat_date: 2019/12/10 0010
 * creat_time: 15:11
 **/
@Configuration
public class NettyServletRegisterConfig {

    @Bean
    @ConditionalOnMissingBean(ApplicationFilterConfigRegistry.class)
    public ApplicationFilterConfigRegistry getApplicationFilterConfigRegistry() {
        return new ApplicationFilterConfigRegistry();
    }

}
