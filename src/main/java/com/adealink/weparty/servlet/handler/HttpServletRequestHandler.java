package com.adealink.weparty.servlet.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import com.adealink.weparty.common.HandlerNameConstant;

import java.util.List;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * desc: 将http编解码器传过来的FullHttpRequest，转换为 MockHttpServletRequest，再传递给下一个handler
 *
 * @author: caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 15:44
 **/
@Slf4j
public class HttpServletRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String APPLICATION_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final EventExecutorGroup eventExecutorGroup;

    private DispatcherServlet dispatcherServlet;

    public HttpServletRequestHandler(EventExecutorGroup eventExecutorGroup, DispatcherServlet dispatcherServlet) {
        this.eventExecutorGroup = eventExecutorGroup;
        this.dispatcherServlet = dispatcherServlet;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 解码失败，直接返回
        if (!req.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        // websocket协议转发到HttpWebSocketServerHandler处理
        if (req.headers().contains(UPGRADE) || req.headers().contains(SEC_WEBSOCKET_KEY) || req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            ctx.fireChannelRead(req.retain());
            return;
        }
        // Allow only GET/POST methods.
        HttpMethod httpMethod = req.method();
        if (httpMethod != GET && httpMethod != POST) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            String uri = req.uri();
            log.warn("not support method,url:{}", uri);
            return;
        }
        // post方法只支持json格式
        if (httpMethod == POST) {
            String contentType = req.headers().get(HEADER_CONTENT_TYPE);
            if (!StringUtils.contains(contentType, APPLICATION_JSON)) {
                sendError(ctx, METHOD_NOT_ALLOWED);
                String uri = req.uri();
                log.warn("post method only support application/json:{}", uri);
                return;
            }
        }

        ChannelPipeline pipeline = ctx.pipeline();
        // 移出websocket handler
        pipeline.remove(HandlerNameConstant.WEB_SOCKET_REQUEST_HANDLER);
        // 响应数据处理handler
        pipeline.addLast(new ChunkedWriteHandler());
        // 过滤器handler
        pipeline.addLast(new HttpServletFilterHandler());
        // 模拟dispatcher handler
        pipeline.addLast(eventExecutorGroup, new HttpServletDispatcherHandler(dispatcherServlet));

        MockHttpServletRequest servletRequest = this.createMockServletRequest(req);
        log.info("handle http request,url:{}", servletRequest.getRequestURI());
        ctx.fireChannelRead(servletRequest);
    }


    private MockHttpServletRequest createMockServletRequest(FullHttpRequest fullHttpRequest) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.uri()).build();

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(uriComponents.getPath());
        servletRequest.setPathInfo(uriComponents.getPath());
        servletRequest.setMethod(fullHttpRequest.method().name());

        if (uriComponents.getScheme() != null) {
            servletRequest.setScheme(uriComponents.getScheme());
        }
        if (uriComponents.getHost() != null) {
            servletRequest.setServerName(uriComponents.getHost());
        }
        if (uriComponents.getPort() != -1) {
            servletRequest.setServerPort(uriComponents.getPort());
        }
        // 设置请求头
        for (String name : fullHttpRequest.headers().names()) {
            servletRequest.addHeader(name, fullHttpRequest.headers().get(name));
        }

        // 设置请求内容
        ByteBuf requestContentBuf = fullHttpRequest.content();
        String requestContent = requestContentBuf.toString(CharsetUtil.UTF_8);
        servletRequest.setContent(requestContent.getBytes(CharsetUtil.UTF_8));

        if (StringUtils.isNotBlank(uriComponents.getQuery())) {
            String query = UriUtils.decode(uriComponents.getQuery(), CharsetUtil.UTF_8);
            servletRequest.setQueryString(query);
        }

        for (Entry<String, List<String>> entry : uriComponents.getQueryParams().entrySet()) {
            for (String value : entry.getValue()) {
                servletRequest.addParameter(UriUtils.decode(entry.getKey(), CharsetUtil.UTF_8), UriUtils.decode(value, CharsetUtil.UTF_8));
            }
        }

        return servletRequest;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8);

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                status,
                content
        );
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
