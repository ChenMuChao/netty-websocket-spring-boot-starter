package com.adealink.weparty.ws.standard;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Slf4j
public class KPIMonitoringHandler  extends ChannelInboundHandlerAdapter {
    private final ScheduledExecutorService kpiExecutorService = Executors.newScheduledThreadPool(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        scheduleTaskQueueMonitoring(ctx);
        super.channelActive(ctx);
    }
    private void scheduleTaskQueueMonitoring(ChannelHandlerContext ctx) {
        log.info("into scheduleTaskQueueMonitoring");
        kpiExecutorService.scheduleAtFixedRate(() -> {
            try {
                Iterator<EventExecutor> executorGroups = ctx.executor().parent().iterator();
                while (executorGroups.hasNext()) {
                    SingleThreadEventExecutor executor = (SingleThreadEventExecutor) executorGroups.next();
                    int size = executor.pendingTasks();

                    if (executor == ctx.executor()) {
                        log.info("ws executor channel:{}--> pending size in queue is : ", ctx.channel(), size);
//                        System.out.println(ctx.channel() + "--> " + executor + " pending size in queue is : --> " + size);
                    } else {
                        log.info("ws executor {}--> pending size in queue is : {}", executor, size);
//                        System.out.println(executor + " pending size in queue is : --> " + size);
                    }
                }
            } catch (Exception e) {
                log.error("Error during task scheduling", e);
            }

        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

}
