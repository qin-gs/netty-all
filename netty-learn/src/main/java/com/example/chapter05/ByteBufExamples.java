package com.example.chapter05;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class ByteBufExamples {
    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    public static final ByteBuf BYTE_BUF_FROM_SOMEWHERE = Unpooled.buffer(1024);

    /**
     * 堆缓冲区
     */
    public static void headBuffer() {
        ByteBuf heapBuf = BYTE_BUF_FROM_SOMEWHERE;
        // 检查是否有支撑数组
        if (heapBuf.hasArray()) {
            byte[] array = heapBuf.array();
            // 获取偏移量
            int offset = heapBuf.arrayOffset() + heapBuf.readerIndex();
            // 获取可读字节数
            int length = heapBuf.readableBytes();
            handleArray(array, offset, length);
        }
    }

    /**
     * 直接缓冲区
     */
    public static void directBuffer() {
        ByteBuf directBuf = BYTE_BUF_FROM_SOMEWHERE;
        // 如果没有支撑数组，则为直接缓冲区
        if (!directBuf.hasArray()) {
            // 获取可读字节数
            int length = directBuf.readableBytes();
            // 分配一个新数组报错字节数据
            byte[] array = new byte[length];
            // 将字节复制到数组
            directBuf.getBytes(directBuf.readerIndex(), array);
            handleArray(array, 0, length);
        }
    }

    /**
     * 复合缓冲区模式(ByteBuffer)
     */
    public static void byteBufferComposite(ByteBuffer header, ByteBuffer body) {
        ByteBuffer[] message = {header, body};
        ByteBuffer message2 = ByteBuffer.allocate(header.remaining() + body.remaining());
        message2.put(header);
        message2.put(body);
        message2.flip();
    }

    /**
     * 复合缓冲区模式(ByteBuf)
     */
    public static void byteBufComposite() {
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        ByteBuf header = BYTE_BUF_FROM_SOMEWHERE;
        ByteBuf body = BYTE_BUF_FROM_SOMEWHERE;
        messageBuf.addComponents(header, body);
        messageBuf.removeComponent(0);
        for (ByteBuf buf : messageBuf) {
            System.out.println(buf.toString());
        }

        // 读取 CompositeByteBuf 中的数据
        // 获取可读字节数
        int length = messageBuf.readableBytes();
        byte[] array = new byte[length];
        messageBuf.getBytes(messageBuf.readerIndex(), array);
        handleArray(array, 0, array.length);
    }

    /**
     * 随机访问索引
     */
    public static void byteBufRelativeAccess() {
        ByteBuf buf = BYTE_BUF_FROM_SOMEWHERE;
        for (int i = 0; i < buf.capacity(); i++) {
            byte b = buf.getByte(i);
            System.out.println(((char) b));
        }
    }

    /**
     * 读取所有数据
     */
    public static void readWriteAll() {
        ByteBuf buf = BYTE_BUF_FROM_SOMEWHERE;
        while (buf.isReadable()) {
            System.out.println(buf.readByte());
        }

        while (buf.writableBytes() >= 4) {
            buf.writeInt(RANDOM.nextInt());
        }
    }


    private static void handleArray(byte[] array, int offset, int length) {
    }
}
