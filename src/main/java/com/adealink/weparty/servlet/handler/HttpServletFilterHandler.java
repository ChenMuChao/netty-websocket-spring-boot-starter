package com.adealink.weparty.servlet.handler;

import com.adealink.weparty.servlet.fitler.ApplicationFilterChain;
import com.adealink.weparty.servlet.fitler.ApplicationFilterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * desc: 模拟servlet的filter链
 * @author: caokunliang
 * creat_date: 2019/12/10 0010
 * creat_time: 10:14
 **/
@Slf4j
public class HttpServletFilterHandler extends SimpleChannelInboundHandler<MockHttpServletRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MockHttpServletRequest httpServletRequest) throws Exception {
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain(ctx, httpServletRequest);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
