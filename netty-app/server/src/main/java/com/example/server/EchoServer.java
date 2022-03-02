package com.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * 引导服务器
 * 1. 绑定到服务器，监听并接受传入连接请求的端口
 * 2. 配置 Channel，将有关入站消息通知该 ServerHandler
 */
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoServer(8080).start();
    }

    public void start() throws InterruptedException {
        EchoServerHandler serverHandler = new EchoServerHandler();
        // 创建 LoopGroup 处理事件
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 引导 和 绑定服务器
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 指定 NIO 传输 Channel
            // 使用指定的接口配置套接字地址
            // 添加一个 ServerHandler
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    // 绑定端口
                    .localAddress(new InetSocketAddress(port))
                    // 一个新的连接被接受时，一个新的 Channel 会被创建
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(serverHandler);
                        }
                    });
            // 异步绑定服务器，调用 sync 阻塞直到绑定完成
            ChannelFuture future = bootstrap.bind().sync();
            // 获取 Channel 的 Future，并阻塞直至完成
            future.channel().closeFuture().sync();
        } finally {
            // 关闭，释放资源(包括被创建的线程)
            group.shutdownGracefully().sync();
        }
    }
}
