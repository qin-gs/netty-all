package com.example.chapter13;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LogEventHandler extends SimpleChannelInboundHandler<LogEvent> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常发生时，关闭对应 Channel
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 将解码后的 LogEvent 简单输出一些
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent msg) throws Exception {
        System.err.println(msg);
    }
}
