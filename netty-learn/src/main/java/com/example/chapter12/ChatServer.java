package com.example.chapter12;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

/**
 * WebSocket 通信
 */
public class ChatServer {

    /**
     * 创建 DefaultChannelGroup，将保存所有已连接的 WebSocket Channel
     */
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;

    public ChannelFuture start(InetSocketAddress address) {
        // 引导服务器
        ServerBootstrap bootstrap = new ServerBootstrap();
        ChannelFuture future = bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(createInitializer(channelGroup))
                .bind(address);
        future.syncUninterruptibly();
        channel = future.channel();
        return future;
    }

    /**
     * 通过重写 ChannelInitializer#initChannel 方法添加多个 pipeline
     */
    protected ChannelHandler createInitializer(ChannelGroup group) {
        return new ChatServerInitializer(group);
    }

    /**
     * 关闭服务器，释放资源
     */
    public void destroy() {
        if (channel != null) {
            channel.close();
        }
        channelGroup.close();
        group.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        ChatServer endpoint = new ChatServer();
        ChannelFuture future = endpoint.start(new InetSocketAddress(port));
        Runtime.getRuntime().addShutdownHook(new Thread(endpoint::destroy));
        future.channel().closeFuture().syncUninterruptibly();
    }
}
