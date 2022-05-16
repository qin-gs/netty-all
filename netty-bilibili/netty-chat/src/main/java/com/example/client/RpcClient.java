package com.example.client;

import com.example.message.RpcRequestMessage;
import com.example.protocol.MessageCodecSharable;
import com.example.protocol.ProtocolFrameDecoder;
import com.example.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    public static final MessageCodecSharable MESSAGE_CODEC_SHARABLE = new MessageCodecSharable();
    public static final RpcResponseMessageHandler RPC_RESPONSE_MESSAGE_HANDLER = new RpcResponseMessageHandler();

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            // 客户端建立连接
            Channel channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(MESSAGE_CODEC_SHARABLE);
                            ch.pipeline().addLast(RPC_RESPONSE_MESSAGE_HANDLER);
                        }
                    }).connect("localhost", 8080).sync().channel();

            // 发送一个请求消息，添加监听器
            ChannelFuture future = channel.writeAndFlush(
                    new RpcRequestMessage(
                            1,
                            "com.example.server.service.HelloService",
                            "hello",
                            String.class,
                            new Class[]{String.class},
                            new Object[]{"qqq"})
            ).addListener(promise -> {
                if (promise.isSuccess()) {
                    log.info("success");
                } else {
                    log.info("failed: ", promise.cause());
                }
            });
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
