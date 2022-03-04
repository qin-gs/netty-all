package com.example.chapter07;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleExamples {

    private static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();

    public static void schedule() {
        // 创建一个线程池
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        // 10s 后执行代码
        executor.schedule(() -> {
            System.out.println("it's 10s later");
        }, 10, TimeUnit.SECONDS);
        // 任务完成，关闭线程池
        executor.shutdown();
    }

    public static void scheduleViaEventLoop() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        channel.eventLoop().schedule(() -> {
            System.out.println("10s later");
        }, 10, TimeUnit.SECONDS);
    }

    public static void scheduleFixedViaEventLoop() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        ScheduledFuture<?> future = channel.eventLoop().scheduleAtFixedRate(() -> {
            System.out.println("run every 10s");
        }, 10, 10, TimeUnit.SECONDS);

        // 终止操作
        future.cancel(true);
    }

    public static void main(String[] args) {
        schedule();
    }


}
