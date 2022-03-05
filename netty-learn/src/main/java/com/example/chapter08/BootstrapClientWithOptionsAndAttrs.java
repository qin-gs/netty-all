package com.example.chapter08;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public class BootstrapClientWithOptionsAndAttrs {

    public static void main(String[] args) {
        new BootstrapClientWithOptionsAndAttrs().bootstrap();
    }

    public void bootstrap() {
        // 创建一个属性
        final AttributeKey<Integer> id = AttributeKey.newInstance("ID");
        // 创建一个 Bootstrap 用来创建并连接客户端 Channel
        Bootstrap bootstrap = new Bootstrap();
        // 设置 EventLoop 处理事件
        bootstrap.group(new NioEventLoopGroup())
                // 指定 Channel 的实现
                .channel(NioSocketChannel.class)
                // 设置处理 Channel io 和 数据 的 ChannelInboundHandler
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println("received data");
                    }

                    @Override
                    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                        // 使用 AttributeKey 获取属性的值
                        Integer idValue = ctx.channel().attr(id).get();
                    }
                })
                // 通过 ChannelOption，设置一些属性
                // 需要在 connect/bind 方法被调用前设置到 Channel 上面
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20 * 60)
                // 设置 id
                .attr(id, 124);
        // 连接到远程主机
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("www.baidu.com", 80));
        future.syncUninterruptibly();
    }
}
