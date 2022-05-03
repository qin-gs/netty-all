package com.example.netty.advance.solve.tcl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HelloServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup master = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(master, worker)
                // 设置滑动窗口大小
                // .option(ChannelOption.SO_RCVBUF, 10)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    }
                })
                .bind(8080)
                .sync()
                .channel()
                .closeFuture()
                .sync();

        master.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
