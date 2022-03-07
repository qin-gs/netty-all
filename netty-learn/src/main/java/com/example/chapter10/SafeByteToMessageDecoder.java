package com.example.chapter10;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

public class SafeByteToMessageDecoder extends ByteToMessageDecoder {

    public static final int MAX_FRAME_SIZE = 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();
        // 检查缓冲区是否超过指定字节
        if (readableBytes > MAX_FRAME_SIZE) {
            // 跳过所有可读字节，抛出异常，通知 ChannelHandler
            in.skipBytes(readableBytes);
            throw new TooLongFrameException("frame too big");
        }
        // do something
    }
}
