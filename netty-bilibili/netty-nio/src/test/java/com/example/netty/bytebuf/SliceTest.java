package com.example.netty.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 切片操作
 */
public class SliceTest {

    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(32);
        buf.writeBytes("hello world".getBytes());

        // 切片，并没有复制数据
        ByteBuf slice = buf.slice(0, 3);
        // 修改后，原来的 ByteBuf 也会变化
        slice.setByte(1, 'a');

        System.out.println("slice = " + slice.toString(StandardCharsets.UTF_8));
        System.out.println("buf = " + buf.toString(StandardCharsets.UTF_8));

        // 组合 ByteBuf 不进行复制
        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 自动增长写指针
        byteBufs.addComponent(true, slice);
        byteBufs.addComponents(true, slice, slice);
        System.out.println("byteBufs = " + byteBufs.toString(StandardCharsets.UTF_8));

        // 使用工具类
        ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(slice, slice);
        System.out.println("wrappedBuffer = " + wrappedBuffer.toString(StandardCharsets.UTF_8));
    }
}
