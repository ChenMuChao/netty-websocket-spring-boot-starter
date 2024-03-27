package com.adealink.weparty.autoconfig;

import com.adealink.weparty.util.SpringContextUtils;
import com.adealink.weparty.ws.standard.ServerEndpointExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyWebSocketSelectorConfig {

    @Bean
    @ConditionalOnMissingBean(ServerEndpointExporter.class)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public SpringContextUtils springContextUtils() {
        return new SpringContextUtils();
    }
}
