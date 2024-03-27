package com.adealink.weparty.ws.support;

import com.adealink.weparty.annotation.ResponseHeader;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.core.MethodParameter;

import static com.adealink.weparty.ws.pojo.PojoEndpointServer.RESPONSE_HEADER_KEY;

public class ResponseHeaderMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        ResponseHeader ann = parameter.getParameterAnnotation(ResponseHeader.class);
        return (ann != null && HttpHeaders.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        HttpHeaders responseHeader = channel.attr(RESPONSE_HEADER_KEY).get();
        return responseHeader;
    }
}
