package com.example.chapter04;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;

public class NettyNioServer {

    public void server(int port) throws InterruptedException {
        ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi\n".getBytes(StandardCharsets.UTF_8)));
        // 创建非阻塞模式的 EventLoopEvent
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 创建服务端 ServerBootstrap
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                // 指定 ChannelInitializer 每个已接受的连接都调用它
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加监听器，处理事件
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ctx.writeAndFlush(buf.duplicate())
                                        .addListener(ChannelFutureListener.CLOSE);
                            }
                        });
                    }
                });
        // 绑定服务器接收连接
        ChannelFuture future = bootstrap.bind().sync();
        future.channel().closeFuture().sync();

        // 释放资源
        group.shutdownGracefully().sync();
    }
}
