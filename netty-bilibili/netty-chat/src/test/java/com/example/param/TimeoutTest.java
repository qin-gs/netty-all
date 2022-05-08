package com.example.param;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeoutTest {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap().group(group)
                // 配置参数
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_BACKLOG, 10)
                .channel(NioSocketChannel.class).handler(new LoggingHandler());
        ChannelFuture future = bootstrap.connect("localhost", 8080);
        future.sync().channel().closeFuture().sync();

        group.shutdownGracefully();
    }
}
