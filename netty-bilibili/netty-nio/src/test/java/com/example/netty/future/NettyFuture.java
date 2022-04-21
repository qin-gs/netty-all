package com.example.netty.future;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NettyFuture {

    static Logger log = LoggerFactory.getLogger(NettyFuture.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        EventLoop loop = group.next();
        // netty 包中的 future
        Future<Integer> future = loop.submit(() -> {
            TimeUnit.SECONDS.sleep(2);
            return 1;
        });
        // 主线程同步等待结果
        Integer i = future.get();
        log.info(i.toString());

        // 执行线程 addListener 异步接收结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.info("接收结果");
                Object now = future.getNow();
                log.info(now.toString());
            }
        });
    }
}
