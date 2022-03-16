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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogEvent msg) throws Exception {
        // 构建输出字符串
        StringBuilder builder = new StringBuilder();
        builder.append(msg.getReceivedTimestamp());
        builder.append(" [");
        builder.append(msg.getSource().toString());
        builder.append("] [");
        builder.append(msg.getLogfile());
        builder.append("] : ");
        builder.append(msg.getMsg());
        System.out.println(builder);
    }
}
