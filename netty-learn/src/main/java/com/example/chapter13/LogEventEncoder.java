package com.example.chapter13;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LogEventEncoder extends MessageToMessageEncoder<LogEvent> {

    private final InetSocketAddress remoteAddress;

    public LogEventEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, LogEvent logEvent, List<Object> out) throws Exception {
        byte[] file = logEvent.getLogfile().getBytes(StandardCharsets.UTF_8);
        byte[] msg = logEvent.getMsg().getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = ctx.alloc().buffer(file.length + msg.length + 1);
        // 写入文件名
        buf.writeBytes(file);
        // 写入一个分隔符
        buf.writeByte(LogEvent.SEPARATOR);
        // 写入日志
        buf.writeBytes(msg);
        out.add(new DatagramPacket(buf, remoteAddress));
    }
}
