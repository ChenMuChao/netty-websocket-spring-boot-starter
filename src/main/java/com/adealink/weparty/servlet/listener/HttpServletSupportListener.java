package com.adealink.weparty.servlet.listener;

import com.adealink.weparty.servlet.context.NettyServletContext;
import com.adealink.weparty.util.MyReflectionUtils;
import com.adealink.weparty.util.SpringContextUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/5/24 0024
 * creat_time: 20:07
 **/
@Data
@Slf4j
public class HttpServletSupportListener implements SpringApplicationRunListener {


    public HttpServletSupportListener(SpringApplication application, String[] args) {
        super();
    }


    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        boolean enableHttpServlet = SpringContextUtils.enableHttpServlet(context);

        if (enableHttpServlet) {
            ServletWebServerApplicationContext applicationContext = (ServletWebServerApplicationContext) context;
            ServletContext mockServletContext = new NettyServletContext();
            applicationContext.setServletContext(mockServletContext);
        }
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        boolean enableHttpServlet = SpringContextUtils.enableHttpServlet(context);

        if (enableHttpServlet) {
            DispatcherServlet dispatcherServlet = context.getBean(DispatcherServlet.class);
            MockServletConfig mockServletConfig = new MockServletConfig();
            MyReflectionUtils.setFieldValue(dispatcherServlet, "config", mockServletConfig);
            /**
             * 初始化servlet
             */
            try {
                dispatcherServlet.init();
            } catch (ServletException e) {
                log.error("e:{}", e);
            }
        }
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }

}
