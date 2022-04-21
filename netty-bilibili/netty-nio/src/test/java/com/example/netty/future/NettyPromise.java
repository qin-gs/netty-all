package com.example.netty.future;

import com.example.nio.thread.Action;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NettyPromise {

    static Logger log = LoggerFactory.getLogger(NettyPromise.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        EventLoop eventLoop = new NioEventLoopGroup().next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread( () -> {
            try {
                log.info("开始");
                TimeUnit.SECONDS.sleep(2);
                // 向 promise 中设置结果
                promise.setSuccess(2);
            } catch (InterruptedException e) {
                promise.setFailure(new Exception());
                throw new RuntimeException(e);
            }
        }).start();

        log.info("等到结果 ");
        // 同步阻塞等到结果
        log.info(String.valueOf(promise.get()));
    }
}
