package com.example.server;

import com.example.protocol.MessageCodecSharable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器
 */
@Slf4j
public class ChatServer {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler();
    public static final MessageCodecSharable MESSAGE_CODEC_SHARABLE = new MessageCodecSharable();

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup master = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap().channel(NioServerSocketChannel.class).group(master, worker).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 处理 粘包/半包 (不能共享)
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                ch.pipeline().addLast(LOGGING_HANDLER);
                // 可以共享
                ch.pipeline().addLast(MESSAGE_CODEC_SHARABLE);
            }
        }).bind(8080).sync().channel().closeFuture().sync();

        master.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
