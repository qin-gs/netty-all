package com.example.netty.advance.solve.短连接;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 每次创建一个 channel
 */
public class HelloClient {

    static Logger log = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            send();
        }
    }

    private static void send() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        new Bootstrap().channel(NioSocketChannel.class).group(group).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    /**
                     * channel 建立成功后，调用该方法
                     */
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ByteBuf buf = ctx.alloc().buffer(18);
                        buf.writeBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});
                        ctx.writeAndFlush(buf);
                        ctx.channel().close();
                    }
                });
            }
        }).connect("localhost", 8080).sync().channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}
