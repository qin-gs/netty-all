package com.example.chapter08;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;

public class BootstrapWithInitializer {

    public void bootstrap() throws InterruptedException {
        // 创建 ServerBootstrap 创建 和 绑定 Channel
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置 EventLoopGroup，用来处理 Channel 的 事件
        bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                // 指定 Channel 的实现
                .channel(NioServerSocketChannel.class)
                // 注册一个 ChannelInitializer，添加多个 ChannelHandler
                .childHandler(new ChannelInitializerImpl());

        // 绑定到地址
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(8080));
        future.sync();
    }

    static class ChannelInitializerImpl extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // 将需要的 ChannelHandler 绑定到 pipeline
            pipeline.addLast(new HttpClientCodec());
            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        }
    }
}
