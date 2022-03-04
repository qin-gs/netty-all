package com.example.chapter05;

import io.netty.buffer.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DummyChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ByteProcessor;

import javax.accessibility.AccessibleValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class ByteBufExamples {
    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    public static final ByteBuf BYTE_BUF_FROM_SOMEWHERE = Unpooled.buffer(1024);
    public static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();
    public static final ChannelHandlerContext CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE = DummyChannelHandlerContext.DUMMY_INSTANCE;
    private static Charset UTF_8 = StandardCharsets.UTF_8;

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

    /**
     * 查找操作
     */
    public static void byteProcessor() {
        ByteBuf buf = BYTE_BUF_FROM_SOMEWHERE;
        int index = buf.forEachByte(ByteProcessor.FIND_CR);
        int i = buf.forEachByte(ByteBufProcessor.FIND_CR);
    }

    /**
     * 切片
     */
    public static void byteBufSlice() {
        ByteBuf buf = Unpooled.copiedBuffer("hello world", UTF_8);
        ByteBuf slice = buf.slice(0, 5);
        System.out.println(slice.toString(UTF_8));

        // 修改原内容，副本也会变
        buf.setByte(0, 'a');

        System.out.println(buf.getByte(0) == slice.getByte(0));

    }

    public static void byteBufCopy() {
        ByteBuf buf = Unpooled.copiedBuffer("hello world", UTF_8);
        ByteBuf copy = buf.copy(0, 5);
        System.out.println(copy.toString());

        buf.setByte(0, 'a');

        System.out.println(buf.getByte(0) == copy.getByte(0));

    }

    public static void byteBufSetGet() {
        ByteBuf buf = Unpooled.copiedBuffer("hello world", UTF_8);
        System.out.println(((char) buf.getByte(0)));

        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();

        // set 方法不会修改索引
        buf.setByte(0, 'a');

        System.out.println(((char) buf.getByte(0)));

        // 不会修改索引
        System.out.println(readerIndex == buf.readerIndex());
        System.out.println(writerIndex == buf.writerIndex());
    }

    public static void byteBufReadWrite() {
        ByteBuf buf = Unpooled.copiedBuffer("hello world", UTF_8);
        System.out.println(((char) buf.readByte()));

        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();

        // write 方法会修改索引
        buf.writeByte('!');

        System.out.println(readerIndex == buf.readerIndex());
        System.out.println(writerIndex == buf.writerIndex());
    }

    /**
     * 获取 ByteBufBufferAllocator 引用
     * 应用计数
     */
    public static void obtainByteBufferAllocator() {
        Channel channel = CHANNEL_FROM_SOMEWHERE;
        ByteBufAllocator allocator = channel.alloc();

        ChannelHandlerContext context = CHANNEL_HANDLER_CONTEXT_FROM_SOMEWHERE;
        ByteBufAllocator allocator1 = context.alloc();

        // 从 ByteBufAllocator 分配一个 ByteBuf
        ByteBuf buffer = allocator.directBuffer();
        // 获取引用计数
        int i = buffer.refCnt();

        // 释放引用计数的对象
        ByteBuf b = BYTE_BUF_FROM_SOMEWHERE;
        boolean release = b.release();
    }

    public static void main(String[] args) {
        byteBufReadWrite();
    }

    private static void handleArray(byte[] array, int offset, int length) {
    }
}
