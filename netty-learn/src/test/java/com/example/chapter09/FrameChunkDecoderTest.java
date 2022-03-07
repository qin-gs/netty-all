package com.example.chapter09;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameChunkDecoderTest {

    @Test
    public void decode() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();

        EmbeddedChannel channel = new EmbeddedChannel(new FrameChunkDecoder(3));

        // 写入两个字节，产生一个新帧
        assertTrue(channel.writeInbound(input.readBytes(2)));

        try {
            // 写入 4 个字节大小的帧，会抛出异常
            channel.writeInbound(input.readBytes(4));
            Assertions.fail();
        } catch (TooLongFrameException e) {
            e.printStackTrace();
        }

        // 写入剩余 3 字节，产生一个新帧
        assertTrue(channel.writeInbound(input.readBytes(3)));
        // 将 Channel 标记为完成状态
        assertTrue(channel.finish());

        // 读取消息(读取第一次创建的帧)，验证值
        ByteBuf read = (ByteBuf) channel.readInbound();
        assertEquals(buf.readSlice(2), read);
        read.release();

        // 读取第二次创建的帧
        read = ((ByteBuf) channel.readInbound());
        assertEquals(buf.skipBytes(4).readSlice(3), read);
        read.release();

        buf.release();

    }
}