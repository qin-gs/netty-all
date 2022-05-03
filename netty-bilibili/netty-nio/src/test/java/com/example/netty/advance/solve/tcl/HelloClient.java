package com.example.netty.advance.solve.tcl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HelloClient {

    static Logger log = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String[] args) throws InterruptedException {

        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 1, 4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        addStr(buffer, "hello world");
        addStr(buffer, "nice to meet you!");

        channel.writeOutbound(buffer);
    }

    private static void addStr(ByteBuf buffer, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        // 依次写入 长度，内容
        buffer.writeInt(bytes.length);
        buffer.writeInt(1);
        buffer.writeBytes(bytes);
    }
}
