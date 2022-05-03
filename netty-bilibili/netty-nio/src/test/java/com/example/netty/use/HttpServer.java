package com.example.netty.use;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

/**
 * HttpServerCodec 处理 http 协议
 */
public class HttpServer {


    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup master = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(master, worker)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new HttpServerCodec());
                        // 处理特定类型的信息，不需要 instanceof 判断
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                                System.out.println(msg.uri());
                                System.out.println(msg.headers());

                                // 返回响应
                                DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                                byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
                                response.content().writeBytes(bytes);
                                // 说明一下响应数据的长度
                                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);

                                // 将 response 返回
                                ctx.channel().writeAndFlush(response);
                            }
                        });
                    }
                })
                .bind("localhost", 8080)
                .sync()
                .channel()
                .closeFuture()
                .sync();
    }
}
