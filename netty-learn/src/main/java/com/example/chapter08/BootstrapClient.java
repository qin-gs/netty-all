package com.example.chapter08;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class BootstrapClient {

    public static void main(String[] args) {
        BootstrapClient client = new BootstrapClient();
        client.bootstrap();
    }

    public void bootstrap() {
        // 设置 EventLoopGroup，处理 Channel 事件
        EventLoopGroup group = new NioEventLoopGroup();
        // 创建 Bootstrap 来创建 和 连接新的客户端 Channel
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                // 指定 Channel
                .channel(NioSocketChannel.class)
                // 设置用于 Channel 事件 和 数据 的 ChannelInboundHandler
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println("received data");
                    }
                });

        // 连接到远程主机
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("www.baidu.com", 80));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("connection established");
                } else {
                    System.out.println("connection failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }
}
