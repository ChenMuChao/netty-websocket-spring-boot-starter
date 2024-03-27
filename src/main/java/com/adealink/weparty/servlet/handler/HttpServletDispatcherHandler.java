package com.adealink.weparty.servlet.handler;

import com.adealink.weparty.servlet.pojo.RequestResponseWrapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * desc:
 * 请求交给，Spring的dispatcherServlet处理
 *
 * @author: caokunliang
 * creat_date: 2019/8/21 0021
 * creat_time: 15:46
 **/
@Slf4j
public class HttpServletDispatcherHandler extends SimpleChannelInboundHandler<RequestResponseWrapper> {

    private final DispatcherServlet dispatcherServlet;

    public HttpServletDispatcherHandler(DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestResponseWrapper requestResponseWrapper) throws Exception {
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) requestResponseWrapper.getServletRequest();
        MockHttpServletResponse servletResponse = (MockHttpServletResponse) requestResponseWrapper.getServletResponse();

        // dispatcher处理
        dispatcherServlet.service(servletRequest, servletResponse);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(servletResponse.getStatus()));
        response.headers().add("Content-Type", "application/json;charset=UTF-8");
        for (String name : servletResponse.getHeaderNames()) {
            response.headers().add(name, servletResponse.getHeader(name));
        }
        // Write the initial line and the header.
        channelHandlerContext.write(response);


        InputStream contentStream = new ByteArrayInputStream(servletResponse.getContentAsByteArray());
        ChunkedStream stream = new ChunkedStream(contentStream);
        ChannelFuture writeFuture = channelHandlerContext.writeAndFlush(stream);
        writeFuture.addListener(ChannelFutureListener.CLOSE);
    }


}
