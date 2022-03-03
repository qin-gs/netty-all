package com.example.chapter04;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class NettyOioServer {

    public void server(int port) throws InterruptedException {
        ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hi\n".getBytes(StandardCharsets.UTF_8)));
        // 创建阻塞模式的 EventLoopGroup
        OioEventLoopGroup group = new OioEventLoopGroup();

        // 服务端，创建 ServerBootstrap
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                // 允许阻塞模式
                .channel(OioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                // 对每个已接受的连接都调用它
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                // 添加一个 ChannelInBoundHandlerAdapter 拦截处理事件
                                new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        // 将消息写到客户端，添加监听器(消息写完就关闭)
                                        ctx.writeAndFlush(buf.duplicate())
                                                .addListener(ChannelFutureListener.CLOSE);
                                    }
                                }
                        );
                    }
                });
        // 绑定服务接受连接
        ChannelFuture future = bootstrap.bind().sync();
        future.channel().closeFuture().sync();

        // 释放资源
        group.shutdownGracefully().sync();
    }
}
