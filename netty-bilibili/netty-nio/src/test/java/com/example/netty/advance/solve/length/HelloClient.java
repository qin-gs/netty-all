package com.example.netty.advance.solve.length;

import com.google.common.base.Strings;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HelloClient {

    static Logger log = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        new Bootstrap().channel(NioSocketChannel.class).group(group).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    /**
                     * channel 建立成功后，调用该方法
                     */
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ByteBuf buffer = ctx.alloc().buffer();
                        for (int i = 1; i < 9; i++) {
                            byte[] bytes = StringUtils.rightPad(Strings.repeat(String.valueOf(i), i), 8, '_').getBytes(StandardCharsets.UTF_8);
                            buffer.writeBytes(bytes);
                        }
                        ctx.writeAndFlush(buffer);
                    }
                });
            }
        }).connect("localhost", 8080).sync().channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}
