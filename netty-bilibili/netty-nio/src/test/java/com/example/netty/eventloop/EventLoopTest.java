package com.example.netty.eventloop;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EventLoopTest {

    Logger log = LoggerFactory.getLogger(EventLoopTest.class);

    @Test
    void test() throws IOException {
        // 事件循环组
        // io 事件，普通任务，定时任务
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
        // 普通任务，定时任务
        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();

        log.info(eventLoopGroup.next().toString());
        log.info(eventLoopGroup.next().toString());
        log.info(eventLoopGroup.next().toString());

        // 提交任务
        eventLoopGroup.execute(() -> {
            log.info("execute task");
        });
        // 提交定时任务
        eventLoopGroup.scheduleAtFixedRate(() -> {
            log.info("schedule task");
        }, 2, 2, TimeUnit.SECONDS);

        System.in.read();
    }

}
