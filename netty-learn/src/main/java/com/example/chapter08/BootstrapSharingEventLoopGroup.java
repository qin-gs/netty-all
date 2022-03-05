package com.example.chapter08;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * 假设你的服务器正在处理一个客户端的请求，这个请求需要它充当第三方系统的客户端
 */
public class BootstrapSharingEventLoopGroup {

    public void bootstrap() {
        // 创建一个 ServerBootstrap 用来创建 ServerSocketChannel
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置 EventLoopGroup，用来处理 Channel 事件
        bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                // 指定要使用的 Channel 实现
                .channel(NioServerSocketChannel.class)
                // 设置用于处理已被接收的子 Channel 的 io 和 数据的 ChannelInboundHandler
                .childHandler(new SimpleChannelInboundHandler<ByteBuf>() {

                    private ChannelFuture channelFuture;

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        // 创建一个 Bootstrap 连接到远程主机
                        Bootstrap bootstrap = new Bootstrap();
                        // 指定 Channel 的实现
                        bootstrap.channel(NioSocketChannel.class)
                                // 为入站 io 设置 ChannelInboundHandler
                                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                        System.out.println("received data");
                                    }
                                })
                                // 共享 EventLoop
                                .group(ctx.channel().eventLoop());
                        channelFuture = bootstrap.connect(new InetSocketAddress("www.baidu.com", 80));
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        if (channelFuture.isDone()) {
                            // 执行一些数据操作
                        }
                    }
                });
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(8080));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("server bound");
                } else {
                    System.out.println("bind attempt failed");
                    future.cause().printStackTrace();
                }
            }
        });
    }
}
