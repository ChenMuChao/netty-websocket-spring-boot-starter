package com.adealink.weparty.ws.support;

import com.adealink.weparty.ws.pojo.PojoEndpointServer;
import com.adealink.weparty.annotation.RequestParam;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.core.MethodParameter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class RequestParamMapMethodArgumentResolver implements MethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        return (requestParam != null && Map.class.isAssignableFrom(parameter.getParameterType()) &&
                !StringUtils.hasText(requestParam.name()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        String name = ann.name();
        if (name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + parameter.getNestedParameterType().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }

        if (!channel.hasAttr(PojoEndpointServer.REQUEST_PARAM)) {
            QueryStringDecoder decoder = new QueryStringDecoder(((FullHttpRequest) object).uri());
            channel.attr(PojoEndpointServer.REQUEST_PARAM).set(decoder.parameters());
        }

        Map<String, List<String>> requestParams = channel.attr(PojoEndpointServer.REQUEST_PARAM).get();
        MultiValueMap multiValueMap = new LinkedMultiValueMap(requestParams);
        if (MultiValueMap.class.isAssignableFrom(parameter.getParameterType())) {
            return multiValueMap;
        } else {
            return multiValueMap.toSingleValueMap();
        }
    }
}
