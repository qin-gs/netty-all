package com.example.chapter09;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbsIntegerEncoderTest {

    @Test
    void encode() {
        // 写入数据
        ByteBuf buf = Unpooled.buffer();
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }

        // 添加 ChannelHandler
        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());

        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());

        // 读取解码的消息，全部变为绝对值
        for (int i = 1; i < 10; i++) {
            assertEquals(i, (Integer) channel.readOutbound());
        }
        assertNull(channel.readOutbound());
    }
}