package com.example.chapter09;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 扩展编码器
 */
public class AbsIntegerEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 检查是否有足够的字节用来编码
        while (msg.readableBytes() >= 4) {
            // 取出整数，获取绝对值
            int value = Math.abs(msg.readInt());
            // 将编码结果写入 list 中
            out.add(value);
        }
    }
}
