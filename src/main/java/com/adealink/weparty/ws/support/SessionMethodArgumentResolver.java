package com.adealink.weparty.ws.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import com.adealink.weparty.ws.pojo.Session;

import static com.adealink.weparty.ws.pojo.PojoEndpointServer.SESSION_KEY;

public class SessionMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Session.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        Session session = channel.attr(SESSION_KEY).get();
        return session;
    }
}
