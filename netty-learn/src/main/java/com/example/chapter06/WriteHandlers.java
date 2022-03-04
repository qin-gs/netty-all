package com.example.chapter06;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.nio.charset.StandardCharsets;

import static io.netty.channel.DummyChannelHandlerContext.DUMMY_INSTANCE;

public class WriteHandlers {

    private static final ChannelHandlerContext CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE = DUMMY_INSTANCE;
    public static final ByteBuf HELLO_WORLD = Unpooled.copiedBuffer("hello world", StandardCharsets.UTF_8);

    /**
     * 通过 Channel 写数据，会从头到尾经过 ChannelPipeline
     */
    public static void writeViaChannel() {
        ChannelHandlerContext ctx = CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE;
        Channel channel = ctx.channel();
        channel.write(HELLO_WORLD);
    }

    /**
     * 通过 pipeline 写数据，从头到尾经过 ChannelPipeline
     */
    public static void writeViaChannelPipeline() {
        ChannelHandlerContext ctx = CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE;
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.write(HELLO_WORLD);
    }

    /**
     * 通过 ChannelHandlerContext 写数据，之后传递 pipelines 之后的 ChannelHandler
     */
    public static void writeViaChannelHandlerContext() {
        ChannelHandlerContext ctx = CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE;
        ctx.write(HELLO_WORLD);
    }

}
