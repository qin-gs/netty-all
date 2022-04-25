package com.example.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * ByteBuf 创建
 */
public class ByteBufTest {

    public static void main(String[] args) {
        // 会自动扩容 (默认为 256)
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(32);
        System.out.println("buffer = " + buffer);

        buffer.writeBytes("a".repeat(300).getBytes());

        System.out.println("buffer = " + buffer);

        ByteBuf heapBuffer = ByteBufAllocator.DEFAULT.heapBuffer(32);
        ByteBuf directBuffer = ByteBufAllocator.DEFAULT.directBuffer(32);
    }
}
