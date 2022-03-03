package com.example.chapter04;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelOperationExamples {

    /**
     * Channel 是线程安全的
     */
    private final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();

    /**
     * 将数据写入 Chanel
     */
    public void writingToChannel() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        ByteBuf buf = Unpooled.copiedBuffer("data".getBytes(StandardCharsets.UTF_8));
        ChannelFuture future = channel.writeAndFlush(buf);
        // 数据写入完成后接收通知
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("write success");
                } else {
                    System.out.println("write failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }

    public void writingToChannelThreads() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        ByteBuf buf = Unpooled.copiedBuffer("data".getBytes(StandardCharsets.UTF_8));

        Runnable write = () -> channel.write(buf.duplicate());
        ExecutorService executor = Executors.newCachedThreadPool();
        // 提交写任务
        executor.execute(write);
        executor.execute(write);
    }

}
