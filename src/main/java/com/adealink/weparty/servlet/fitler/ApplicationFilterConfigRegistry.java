package com.adealink.weparty.servlet.fitler;

import com.adealink.weparty.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * desc:
 * 过滤器注册表
 *
 * @author : caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 11:48
 **/
@Slf4j
public class ApplicationFilterConfigRegistry implements SmartInitializingSingleton {

    private List<ApplicationFilterConfig> applicationFilterConfigs = new ArrayList<>();

    public void register(ApplicationFilterConfig applicationFilterConfig) {
        applicationFilterConfigs.add(applicationFilterConfig);
    }

    public List<ApplicationFilterConfig> getApplicationFilterConfigs() {
        return applicationFilterConfigs;
    }

    @Override
    public void afterSingletonsInstantiated() {
        ApplicationContext applicationContext = SpringContextUtils.getApplicationContext();
        String[] beanNamesForType = applicationContext.getBeanNamesForType(FilterRegistrationBean.class);
        for (String filterName : beanNamesForType) {
            FilterRegistrationBean<?> filterRegistrationBean = applicationContext.getBean(filterName, FilterRegistrationBean.class);
            if (CollectionUtils.isEmpty(filterRegistrationBean.getUrlPatterns())) {
                continue;
            }
            Filter filter = filterRegistrationBean.getFilter();

            ApplicationFilterConfig filterConfig = new ApplicationFilterConfig();
            filterConfig.setFilter(filter);
            filterConfig.setFilterName(filterName);

            filterConfig.setUrlPatterns(filterRegistrationBean.getUrlPatterns().iterator().next());
            this.register(filterConfig);
            log.info("register filter success,name:{}", filterName);
        }
    }
}
