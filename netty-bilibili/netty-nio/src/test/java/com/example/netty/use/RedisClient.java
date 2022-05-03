package com.example.netty.use;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

/**
 * 向 redis 中发送请求，(手动根据 redis 中的 resp 协议发送信息)
 *
 * Redis在TCP端口6379上监听到来的连接，客户端连接到来时，Redis服务器为此创建一个TCP连接。在客户端与服务器端之间传输的每个Redis命令或者数据都以\r\n结尾
 *
 * *3\r\n  #消息一共有三行
 * $3\r\n #第一行有长度为3
 * set\r\n #第一行的消息
 * $4\r\n  #第二行长度为4
 * name\r\n #第二行的消息
 * $6\r\n #第三行长度为6
 * 123456\r\n #第三行的消息
 * +OK\r\n #操作成功
 */
public class RedisClient {


    public static void main(String[] args) throws InterruptedException {
        byte[] line = {13, 10};

        NioEventLoopGroup group = new NioEventLoopGroup();

        new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(group)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf buffer = ctx.alloc().buffer();
                                buffer.writeBytes("*3".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("$3".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("set".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("$4".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("name".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("$8".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);
                                buffer.writeBytes("my name".getBytes(StandardCharsets.UTF_8));
                                buffer.writeBytes(line);

                                ctx.writeAndFlush(buffer);
                            }
                        });
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                        ByteBuf buf = (ByteBuf) msg;
                        System.out.println("buf = " + buf.toString(StandardCharsets.UTF_8));
                    }
                })
                .connect("localhost", 6379)
                .sync()
                .channel()
                .closeFuture()
                .sync();
    }
}
