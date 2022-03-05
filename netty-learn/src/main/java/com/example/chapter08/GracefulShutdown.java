package com.example.chapter08;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;

public class GracefulShutdown {


    public void bootstrap() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println("received data");
                    }
                })
                .connect(new InetSocketAddress("www.baidu.com", 80))
                .syncUninterruptibly();
        // 释放所有的资源，关闭当前正在使用的 Channel
        Future<?> future = group.shutdownGracefully();
        future.syncUninterruptibly();
    }
}
