package com.example.netty.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class EventLoopClient {
    private static final Logger log = LoggerFactory.getLogger(EventLoopClient.class);

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 连接到服务器，异步非阻塞，会创建另外一个线程去建立连接
                .connect(new InetSocketAddress("localhost", 8080));
        // 1. sync 方法阻塞当前线程(主线程)至 nio 线程建立完毕
        Channel channel = channelFuture.sync().channel();
        log.info(channel.toString());
        channel.writeAndFlush("hello ");
        channel.writeAndFlush("world!");

        // 输入指定值关闭 channel，由于 close 方法是异步的，
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    // close 方法是异步操作
                    channel.close();
                    // 这个输出不一定是在关闭之后
                    log.info("这个 close 不一定是在关闭之后执行的，由新创建的线程输出");
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 1. 采用 closeFuture 保证关闭后执行后面的操作
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.sync();
        log.info("这个 main closed 是在关闭之后执行的，由主线程输出");

        // 2. 使用 addListener 方法等待连接建立完成
        // channelFuture.addListener(new ChannelFutureListener() {
        //     /**
        //      * nio 线程建立完成后调用(建立连接的线程不是主线程)该方法
        //       */
        //     @Override
        //     public void operationComplete(ChannelFuture future) throws Exception {
        //         Channel ch = future.channel();
        //         ch.writeAndFlush("hello");
        //     }
        // });

        // 2. 使用 addListener 等待连接关闭
        // closeFuture.addListener(new ChannelFutureListener() {
        //     @Override
        //     public void operationComplete(ChannelFuture future) throws Exception {
        //         log.info("这个 closed 是在关闭之后执行的，由关闭连接的那个线程输出");
        //         // 关闭所有线程
        //         group.shutdownGracefully();
        //     }
        // });
    }
}
